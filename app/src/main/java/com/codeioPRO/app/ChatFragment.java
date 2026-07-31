package com.codeioPRO.app;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.CookieManager;
import android.webkit.JavascriptInterface;
import android.webkit.PermissionRequest;
import android.webkit.URLUtil;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.app.DownloadManager;
import android.media.MediaScannerConnection;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.codeioPRO.app.util.UpdateChecker;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

public class ChatFragment extends Fragment {

    private WebView chatWebView;
    private float currentZoomLevel = 100f;
    private ProgressBar progressBar;
    private ValueCallback<Uri[]> mUploadMessage;
    private static final int FILE_CHOOSER_REQUEST_CODE = 1;
    private Uri pendingSharedFileUri = null;
    private boolean pendingVoiceChat = false;
    private final String TAG = "CodeioPRO";

    private String pendingDownloadUrl, pendingDownloadUserAgent, pendingDownloadContentDisposition, pendingDownloadMimetype;
    private long pendingDownloadContentLength;
    private boolean isPendingBlob;
    private String pendingBlobData, pendingBlobMimetype, pendingBlobContentDisposition, pendingBlobCurrentUrl;


    // Chat mode tabs
    private TextView tabVoice, tabChat, tabCode, tabAI;
    private String currentChatMode = "chat";

    // System prompts per mode
    private static final String PROMPT_CHAT = "Günlük sohbet asistanısın. Sıcak, samimi ve doğal konuş. Seni Muhammed geliştirdi.";
    private static final String PROMPT_CODE = "Kod yazma ve geliştirme uzmanısın. Anlaşılır, çalışan kod yaz ve açıkla. Seni Muhammed geliştirdi.";
    private static final String PROMPT_AI   = "Gelişmiş yapay zeka asistanısın. Derin analiz, akıl yürütme ve problem çözme konusunda yardım et. Seni Muhammed geliştirdi.";

    // Model & agent mode
    private String currentModel = "gpt-4o-mini";
    private String agentMode    = "economy";

    // ── JS patches ──────────────────────────────────────────────────────────
    private static final String BLOB_JS =
        "(function(){if(window.blobHandlerInjected)return;window.blobHandlerInjected=true;" +
        "window.blobMap=window.blobMap||new Map();var oC=URL.createObjectURL;" +
        "URL.createObjectURL=function(b){var u=oC.call(URL,b);" +
        "if(b instanceof Blob)window.blobMap.set(u,b);return u};})();";

    private static final String CLIPBOARD_JS =
        "(function(){if(window.clipboardPatchInjected)return;window.clipboardPatchInjected=true;" +
        "if(navigator.clipboard){navigator.clipboard.writeText=function(t){" +
        "return new Promise(function(res,rej){try{Android.copyToClipboard(t);res();}catch(e){rej(e);}});}" +
        "}})();";


    // ── Marka gizleme: duck.ai sayfasındaki DuckDuckGo logolarını maskele ──────
    private static final String BRAND_HIDE_JS =
        "(function(){" +
        "if(window.brandHideInjected)return;window.brandHideInjected=true;" +
        "var style=document.createElement('style');" +
        "style.textContent=" +
        "'[class*=\"logo\"],[class*=\"Logo\"],[class*=\"wordmark\"],' +" +
        "'[class*=\"Wordmark\"],[class*=\"dax\"],[class*=\"Dax\"],' +" +
        "'[class*=\"duck\"]:not(input):not(textarea),' +" +
        "'[aria-label*=\"DuckDuckGo\"],' +" +
        "'a[href*=\"duckduckgo.com/about\"],' +" +
        "'a[href*=\"spread.duckduckgo\"]' +" +
        "'{display:none!important}';" +
        "document.head.appendChild(style);" +
        // MutationObserver to handle dynamic content
        "var obs=new MutationObserver(function(m){" +
        "document.querySelectorAll('[class*=\"logo\"],[class*=\"Logo\"]').forEach(function(el){" +
        "if(el.textContent&&el.textContent.includes('DuckDuckGo')){" +
        "el.style.display='none';}" +
        "});});" +
        "obs.observe(document.body,{childList:true,subtree:true});" +
        "})()";

    private static final String STAR_BG_JS =
        "(function(){if(window.starBgInjected)return;window.starBgInjected=true;" +
        "var c=document.createElement('canvas');" +
        "c.style.cssText='position:fixed;top:0;left:0;width:100%;height:100%;pointer-events:none;z-index:0;opacity:0.25;';" +
        "document.body.insertBefore(c,document.body.firstChild);" +
        "var ctx=c.getContext('2d'),stars=[];" +
        "function init(){c.width=innerWidth;c.height=innerHeight;stars=[];" +
        "for(var i=0;i<80;i++)stars.push({x:Math.random()*c.width,y:Math.random()*c.height," +
        "r:Math.random()*1.5+0.4,s:Math.random()*0.25+0.1,a:Math.random()});}" +
        "function draw(){ctx.clearRect(0,0,c.width,c.height);" +
        "stars.forEach(function(s){s.a+=0.008;if(s.a>1)s.a=0;s.y-=s.s;" +
        "if(s.y<0){s.y=c.height;s.x=Math.random()*c.width;}" +
        "ctx.beginPath();ctx.arc(s.x,s.y,s.r,0,6.28);" +
        "ctx.fillStyle='rgba(255,255,255,'+s.a+')';ctx.fill();});" +
        "requestAnimationFrame(draw);}" +
        "init();draw();window.addEventListener('resize',init);})();";

    private static final String COPY_BTN_JS =
        "(function(){if(window.copyBtnInjected)return;window.copyBtnInjected=true;" +
        "function inject(){document.querySelectorAll('pre:not([data-cb])').forEach(function(p){" +
        "p.setAttribute('data-cb','1');p.style.position='relative';" +
        "var b=document.createElement('button');" +
        "b.textContent='⎘ Kopyala';" +
        "b.style='position:absolute;top:8px;right:8px;background:#00d4ff;color:#0e1525;border:none;" +
        "border-radius:6px;padding:4px 10px;font-size:11px;cursor:pointer;font-weight:700;z-index:10;';" +
        "b.onclick=function(){var code=p.querySelector('code');var t=code?code.innerText:p.innerText;" +
        "if(typeof Android!='undefined')Android.copyToClipboard(t);" +
        "b.textContent='✓ Kopyalandı';b.style.background='#00ff88';" +
        "setTimeout(function(){b.textContent='⎘ Kopyala';b.style.background='#00d4ff';},1500);};" +
        "p.appendChild(b);})}" +
        "inject();new MutationObserver(inject).observe(document.body,{childList:true,subtree:true});})();";

    private static final String DOWNLOAD_BTN_JS =
        "(function(){if(window.dlBtnInjected)return;window.dlBtnInjected=true;" +
        "var extMap={javascript:'js',typescript:'ts',python:'py',java:'java',kotlin:'kt'," +
        "bash:'sh',shell:'sh',html:'html',css:'css',json:'json',xml:'xml',yaml:'yaml'," +
        "go:'go',rust:'rs',cpp:'cpp',c:'c',php:'php',ruby:'rb',swift:'swift'};" +
        "function inject(){document.querySelectorAll('pre:not([data-dl])').forEach(function(p){" +
        "p.setAttribute('data-dl','1');p.style.position='relative';" +
        "var lang=p.querySelector('code');var lc=lang?(lang.className||''):'';" +
        "var m=lc.match(/language-(\\w+)/);var ext=m?(extMap[m[1]]||m[1]):'txt';" +
        "var b=document.createElement('button');" +
        "b.textContent='⬇ İndir';" +
        "b.style='position:absolute;top:8px;right:110px;background:#1a1a2e;color:#00d4ff;" +
        "border:1px solid #00d4ff;border-radius:6px;padding:4px 10px;font-size:11px;cursor:pointer;font-weight:700;z-index:10;';" +
        "b.onclick=function(){var code=p.querySelector('code');var t=code?code.innerText:p.innerText;" +
        "var fn='code_'+Date.now()+'.'+ext;" +
        "if(typeof Android!='undefined')Android.saveCodeToFile(t,fn);};" +
        "p.appendChild(b);})}" +
        "inject();new MutationObserver(inject).observe(document.body,{childList:true,subtree:true});})();";

    private static final String RUN_BTN_JS =
        "(function(){if(window.runBtnInjected)return;window.runBtnInjected=true;" +
        "var runnable=['python','python3','javascript','js','bash','shell','sh','ruby','php'];" +
        "function inject(){document.querySelectorAll('pre:not([data-run])').forEach(function(p){" +
        "p.setAttribute('data-run','1');var lang=p.querySelector('code');" +
        "var lc=lang?(lang.className||''):'';" +
        "var m=lc.match(/language-(\\w+)/);" +
        "if(!m||runnable.indexOf(m[1])===-1)return;" +
        "p.style.position='relative';" +
        "var b=document.createElement('button');" +
        "b.textContent='▶ Çalıştır';" +
        "b.style='position:absolute;top:8px;right:215px;background:#00d4ff;color:#0e1525;" +
        "border:none;border-radius:6px;padding:4px 10px;font-size:11px;cursor:pointer;font-weight:700;z-index:10;';" +
        "b.onclick=function(){var code=p.querySelector('code');var t=code?code.innerText:p.innerText;" +
        "if(typeof Android!='undefined')Android.runCodeInShell(t,m[1]);};" +
        "p.appendChild(b);})}" +
        "inject();new MutationObserver(inject).observe(document.body,{childList:true,subtree:true});})();";

    private static final String SETTINGS_BTN_JS =
        "(function(){if(window.codeioSettBtnInjected)return;" +
        "function inject(){var p=document.querySelector('path[d^=\"M5.647 14.153\"]');" +
        "var wb=p?p.closest('button,[role=\"button\"]'):null;" +
        "if(wb&&!document.querySelector('.codeio-sett')){" +
        "var b=wb.cloneNode(true);b.className='codeio-sett';" +
        "var s=b.querySelector('svg');" +
        "if(s)s.innerHTML='<path fill=\"currentColor\" d=\"M12 2a10 10 0 1 0 10 10A10 10 0 0 0 12 2zm0 18a8 8 0 1 1 8-8 8 8 0 0 1-8 8zm0-14a1 1 0 0 0-1 1v4a1 1 0 0 0 .55.89l3 1.5a1 1 0 0 0 .9-1.79L13 10.38V7a1 1 0 0 0-1-1z\"/>';" +
        "b.style.marginLeft='8px';" +
        "b.addEventListener('click',function(e){e.stopPropagation();e.preventDefault();" +
        "if(typeof Android!='undefined')Android.showSettingsDialog();});" +
        "wb.parentNode.insertBefore(b,wb.nextSibling);" +
        "window.codeioSettBtnInjected=true;return true;}return false;}" +
        "if(!inject()){var obs=new MutationObserver(function(){if(inject())obs.disconnect();});" +
        "obs.observe(document.body,{childList:true,subtree:true});}})();";

    // Model switching — compact floating pill (NO full-screen overlay)
    private static final String MODEL_SWITCHER_JS =
        "(function(){if(window.codeioModelInjected)return;window.codeioModelInjected=true;" +
        "var MODELS=[" +
        "{id:'gpt-4o-mini',label:'GPT-4o mini',icon:'🟢'}," +
        "{id:'gpt-4o',label:'GPT-4o',icon:'🟢'}," +
        "{id:'claude-3-5-haiku-20241022',label:'Claude Haiku',icon:'🟠'}," +
        "{id:'mistral-small',label:'Mistral Small',icon:'🔴'}," +
        "{id:'meta-llama/llama-3.1-405b',label:'Llama 3.1 405B',icon:'🔵'}," +
        "{id:'gemma-2-27b-it',label:'Gemma 2 27B',icon:'🔵'}" +
        "];" +
        "var curModel=typeof Android!='undefined'?Android.getCurrentModel():'gpt-4o-mini';" +
        "var bar=document.createElement('div');" +
        "bar.id='codeio-bar';" +
        "bar.style='position:fixed;bottom:14px;right:14px;z-index:9999;display:flex;align-items:center;gap:6px;" +
        "background:rgba(20,24,36,0.96);border:1px solid #252E42;border-radius:20px;padding:7px 14px;" +
        "box-shadow:0 4px 20px rgba(0,0,0,.7);backdrop-filter:blur(8px);max-width:220px;';" +
        "var dot=document.createElement('div');" +
        "dot.style='width:8px;height:8px;border-radius:50%;background:#F26207;flex-shrink:0;';" +
        "var lbl=document.createElement('span');" +
        "lbl.style='color:#F0F2F5;font-size:12px;font-weight:600;white-space:nowrap;overflow:hidden;text-overflow:ellipsis;max-width:160px;';" +
        "function refreshLabel(){var m=MODELS.find(function(x){return x.id===curModel;})||MODELS[0];lbl.textContent=m.label;}" +
        "refreshLabel();" +
        "bar.appendChild(dot);bar.appendChild(lbl);" +
        "document.body.appendChild(bar);" +
        // Compact dropdown picker
        "var picker=document.createElement('div');" +
        "picker.style='display:none;position:fixed;bottom:60px;right:14px;z-index:10000;" +
        "background:#141824;border:1px solid #252E42;border-radius:16px;padding:10px;" +
        "width:220px;box-shadow:0 8px 32px rgba(0,0,0,.8);';" +
        "MODELS.forEach(function(m){" +
        "var row=document.createElement('div');" +
        "row.style='display:flex;align-items:center;gap:10px;padding:10px 12px;border-radius:10px;cursor:pointer;" +
        "background:'+(curModel===m.id?'rgba(242,98,7,.1)':'transparent')+';" +
        "border:1px solid '+(curModel===m.id?'#F26207':'transparent')+';margin-bottom:4px;transition:.15s;';" +
        "row.innerHTML='<span>'+m.icon+'</span><span style="color:#F0F2F5;font-size:13px;font-weight:600">'+m.label+'</span>';" +
        "row.onclick=function(){curModel=m.id;refreshLabel();" +
        "if(typeof Android!='undefined')Android.setModel(curModel);" +
        "picker.style.display='none';" +
        "picker.querySelectorAll('div').forEach(function(r,i){if(r.style){r.style.background=MODELS[i]&&MODELS[i].id===curModel?'rgba(242,98,7,.1)':'transparent';r.style.borderColor=MODELS[i]&&MODELS[i].id===curModel?'#F26207':'transparent';}});};" +
        "picker.appendChild(row);});" +
        "document.body.appendChild(picker);" +
        "bar.onclick=function(){picker.style.display=picker.style.display==='none'?'block':'none';};" +
        "document.addEventListener('click',function(e){if(!bar.contains(e.target)&&!picker.contains(e.target))picker.style.display='none';});" +
        "})()";


    private static final String VOICE_JS =
        "(function(){function tryClick(){" +
        "var sp=document.querySelector('path[d*=\"M9.41 10.125\"]');" +
        "var sb=sp?sp.closest('button,[role=\"button\"]'):null;" +
        "if(!sb)sb=document.querySelector('button[aria-label*=\"sidebar\"],button[aria-label*=\"Sidebar\"]');" +
        "if(sb&&sb.offsetParent!==null)sb.click();" +
        "var ap=document.querySelectorAll('path[d*=\"M5.625 0c.345\"]');" +
        "for(var i=0;i<ap.length;i++){var b=ap[i].closest('button,[role=\"button\"]');if(b){b.click();return true;}}" +
        "var els=document.querySelectorAll('button,[role=\"button\"]');" +
        "for(var i=0;i<els.length;i++){if((els[i].innerText||'').toLowerCase().includes('voice chat')){els[i].click();return true;}}" +
        "return false;}" +
        "if(!tryClick()){var obs=new MutationObserver(function(m,o){if(tryClick())o.disconnect();});" +
        "obs.observe(document.body,{childList:true,subtree:true});setTimeout(function(){obs.disconnect();},15000);}})();";

    // ── Fragment lifecycle ──────────────────────────────────────────────────
    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inf, @Nullable ViewGroup container, @Nullable Bundle saved) {
        return inf.inflate(R.layout.fragment_chat, container, false);
    }

    @SuppressLint({"SetJavaScriptEnabled","ClickableViewAccessibility"})
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle saved) {
        super.onViewCreated(view, saved);
        StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder().build());

        progressBar = view.findViewById(R.id.progressBar);
        // Setup chat mode tabs
        tabVoice = view.findViewById(R.id.tab_voice);
        tabChat  = view.findViewById(R.id.tab_chat);
        tabCode  = view.findViewById(R.id.tab_code);
        tabAI    = view.findViewById(R.id.tab_ai);
        if (tabVoice != null) tabVoice.setOnClickListener(v2 -> selectChatMode("voice", tabVoice));
        if (tabChat  != null) tabChat.setOnClickListener(v2  -> selectChatMode("chat",  tabChat));
        if (tabCode  != null) tabCode.setOnClickListener(v2  -> selectChatMode("code",  tabCode));
        if (tabAI    != null) tabAI.setOnClickListener(v2    -> selectChatMode("ai",    tabAI));

        // Settings button in header
        View btnSettings = view.findViewById(R.id.btn_chat_settings);
        if (btnSettings != null) btnSettings.setOnClickListener(v2 -> {
            if (chatWebView != null) chatWebView.evaluateJavascript(SETTINGS_BTN_JS, null);
        });

        chatWebView = view.findViewById(R.id.chatWebView);

        WebSettings ws = chatWebView.getSettings();
        ws.setJavaScriptEnabled(true);
        ws.setDomStorageEnabled(true);
        ws.setLoadWithOverviewMode(true);
        ws.setUseWideViewPort(true);
        ws.setSupportZoom(false);
        ws.setBuiltInZoomControls(false);
        ws.setDisplayZoomControls(false);
        ws.setAllowFileAccess(false);
        ws.setAllowContentAccess(false);
        ws.setMediaPlaybackRequiresUserGesture(false);
        ws.setCacheMode(WebSettings.LOAD_DEFAULT);
        ws.setDatabaseEnabled(true);
        ws.setSaveFormData(false);
        ws.setGeolocationEnabled(false);
        ws.setUserAgentString(ws.getUserAgentString() + " Code-ioPRO/1.0");

        SharedPreferences prefs = requireActivity().getSharedPreferences("codeio_prefs", Context.MODE_PRIVATE);
        int savedZoom = prefs.getInt("text_zoom", 100);
        currentZoomLevel = savedZoom;
        ws.setTextZoom(savedZoom);

        CookieManager.getInstance().setAcceptCookie(true);
        CookieManager.getInstance().setAcceptThirdPartyCookies(chatWebView, false);

        chatWebView.setWebViewClient(new MyWebViewClient());
        chatWebView.setWebChromeClient(new MyWebChromeClient());
        chatWebView.addJavascriptInterface(this, "Android");

        setupDownloadListener();
        setupScaleGesture();

        if (requireActivity().checkSelfPermission(android.Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{android.Manifest.permission.RECORD_AUDIO}, 123);
        }

        UpdateChecker.checkForUpdates(requireContext(), "ThT0AltayHR", "Code-ioPRO");
        loadChat();
    }

    private void loadChat() {
        SharedPreferences p = requireActivity().getSharedPreferences("codeio_prefs", Context.MODE_PRIVATE);
        String url = p.getString("custom_api_url", "https://chat.openai.com");
        if (url == null || url.trim().isEmpty()) url = "https://chat.openai.com";
        chatWebView.loadUrl(url);
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setupScaleGesture() {
        ScaleGestureDetector sgd = new ScaleGestureDetector(requireContext(),
            new ScaleGestureDetector.SimpleOnScaleGestureListener() {
                @Override public boolean onScale(ScaleGestureDetector d) {
                    currentZoomLevel = Math.max(50f, Math.min(currentZoomLevel * d.getScaleFactor(), 300f));
                    int nz = Math.round(currentZoomLevel);
                    chatWebView.getSettings().setTextZoom(nz);
                    requireActivity().getSharedPreferences("codeio_prefs", Context.MODE_PRIVATE)
                        .edit().putInt("text_zoom", nz).apply();
                    return true;
                }
            });
        sgd.setQuickScaleEnabled(false);
        chatWebView.setOnTouchListener((v, e) -> {
            sgd.onTouchEvent(e);
            return e.getPointerCount() > 1 || sgd.isInProgress();
        });
    }

    private void setupDownloadListener() {
        chatWebView.setDownloadListener((url, ua, cd, mime, len) -> {
            if (url.startsWith("blob:")) {
                String ecd = cd != null ? cd.replace("'", "\\'") : "";
                chatWebView.evaluateJavascript(
                    "(function(){var u='" + url + "';" +
                    "var b=window.blobMap?window.blobMap.get(u):null;" +
                    "if(b){var r=new FileReader();r.onloadend=function(){Android.processBlob(r.result,b.type,'" + ecd + "',window.location.href);};r.readAsDataURL(b);}" +
                    "else{var x=new XMLHttpRequest();x.open('GET',u,true);x.responseType='blob';" +
                    "x.onload=function(){if(this.status==200){var r2=new FileReader();r2.readAsDataURL(this.response);" +
                    "r2.onloadend=function(){Android.processBlob(r2.result,'" + mime + "','" + ecd + "',window.location.href);};}};x.send();}})();", null);
            } else if (url.startsWith("data:")) {
                processBlob(url, mime, cd, url);
            } else {
                if (checkDownloadPermissions()) startStandardDownload(url, ua, cd, mime, len);
                else { pendingDownloadUrl=url; pendingDownloadUserAgent=ua; pendingDownloadContentDisposition=cd; pendingDownloadMimetype=mime; pendingDownloadContentLength=len; isPendingBlob=false; }
            }
        });
    }

    public void handleExternalIntent(Intent intent) {
        if (intent == null || chatWebView == null) return;
        String action = intent.getAction();
        String type   = intent.getType();
        if (Intent.ACTION_SEND.equals(action) && "text/plain".equals(type)) {
            String text = intent.getStringExtra(Intent.EXTRA_TEXT);
            if (text != null) {
                String esc = text.replace("\\", "\\\\").replace("'", "\\'").replace("\n", "\\n");
                chatWebView.post(() -> chatWebView.evaluateJavascript(
                    "(function(){var ta=document.querySelector('textarea,[contenteditable]');" +
                    "if(ta){ta.focus();document.execCommand('insertText',false,'" + esc + "');}})();", null));
            }
        } else if (Intent.ACTION_SEND.equals(action) && type != null && (type.startsWith("image/") || "application/pdf".equals(type))) {
            Uri uri = intent.getParcelableExtra(Intent.EXTRA_STREAM);
            if (uri != null) pendingSharedFileUri = uri;
        }
    }


    // ── Chat mode switching ──────────────────────────────────────────────────
    private void selectChatMode(String mode, TextView selectedTab) {
        currentChatMode = mode;
        // Update tab visuals
        for (TextView t : new TextView[]{tabVoice, tabChat, tabCode, tabAI}) {
            if (t == null) continue;
            if (t == selectedTab) {
                t.setBackgroundResource(R.drawable.tab_selected);
                t.setTextColor(getResources().getColor(R.color.accent, null));
            } else {
                t.setBackgroundResource(R.drawable.tab_unselected);
                t.setTextColor(getResources().getColor(R.color.text_secondary, null));
            }
        }
        // Inject mode-specific system prompt
        String prompt;
        switch (mode) {
            case "voice":
                if (chatWebView != null) chatWebView.evaluateJavascript(VOICE_JS, null);
                return;
            case "code":  prompt = PROMPT_CODE; break;
            case "ai":    prompt = PROMPT_AI;   break;
            default:       prompt = PROMPT_CHAT; break;
        }
        final String p = prompt.replace("'", "\\'").replace("\"", "\\\"");
        if (chatWebView != null) {
            chatWebView.evaluateJavascript(
                "(function(){" +
                "var ta=document.querySelector('textarea,[contenteditable]');" +
                "if(ta){ta.placeholder='" + p.substring(0, Math.min(p.length(), 80)) + "...';}"+
                "})()", null);
        }
    }

    public boolean canGoBack() { return chatWebView != null && chatWebView.canGoBack(); }
    public void goBack() { if (chatWebView != null) chatWebView.goBack(); }

    // ── WebViewClient ───────────────────────────────────────────────────────
    private class MyWebViewClient extends WebViewClient {
        @Override public void onPageStarted(WebView v, String url, Bitmap fav) {
            if (progressBar != null) progressBar.setVisibility(View.VISIBLE);
        }
        @Override public void onPageFinished(WebView v, String url) {
            if (progressBar != null) progressBar.setVisibility(View.GONE);
            v.evaluateJavascript(BLOB_JS, null);
            v.evaluateJavascript(BRAND_HIDE_JS, null);
            v.evaluateJavascript(CLIPBOARD_JS, null);
            v.evaluateJavascript(SETTINGS_BTN_JS, null);
            v.evaluateJavascript(COPY_BTN_JS, null);
            v.evaluateJavascript(DOWNLOAD_BTN_JS, null);
            v.evaluateJavascript(RUN_BTN_JS, null);
            v.evaluateJavascript(STAR_BG_JS, null);
            v.evaluateJavascript(MODEL_SWITCHER_JS, null);
            if (pendingVoiceChat) { pendingVoiceChat = false; v.evaluateJavascript(VOICE_JS, null); }
            if (pendingSharedFileUri != null) {
                Uri fu = pendingSharedFileUri; pendingSharedFileUri = null;
                if (mUploadMessage != null) { mUploadMessage.onReceiveValue(new Uri[]{fu}); mUploadMessage = null; }
            }
        }
        @Override public boolean shouldOverrideUrlLoading(WebView v, WebResourceRequest r) { return false; }
    }

    // ── WebChromeClient ─────────────────────────────────────────────────────
    private class MyWebChromeClient extends WebChromeClient {
        @Override public void onProgressChanged(WebView v, int p) {
            if (progressBar == null) return;
            if (p < 100) { progressBar.setVisibility(View.VISIBLE); progressBar.setProgress(p); }
            else          progressBar.setVisibility(View.GONE);
        }
        @Override public boolean onShowFileChooser(WebView v, ValueCallback<Uri[]> cb, FileChooserParams p) {
            mUploadMessage = cb;
            Intent i = new Intent(Intent.ACTION_GET_CONTENT);
            i.addCategory(Intent.CATEGORY_OPENABLE);
            i.setType("*/*");
            i.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, false);
            startActivityForResult(Intent.createChooser(i, "Dosya Seç"), FILE_CHOOSER_REQUEST_CODE);
            return true;
        }
        @Override public void onPermissionRequest(PermissionRequest req) { req.grant(req.getResources()); }
    }

    // ── JavaScript interface ────────────────────────────────────────────────
    @JavascriptInterface
    public void copyToClipboard(final String text) {
        requireActivity().runOnUiThread(() -> {
            ClipboardManager cm = (ClipboardManager) requireActivity().getSystemService(Context.CLIPBOARD_SERVICE);
            if (cm != null) { cm.setPrimaryClip(ClipData.newPlainText("Code-ioPRO", text));
                Toast.makeText(requireContext(), "✓ Kopyalandı", Toast.LENGTH_SHORT).show(); }
        });
    }

    @JavascriptInterface
    public void processBlob(String base64Data, String mime, String cd, String currentUrl) {
        if (checkDownloadPermissions()) saveBlobToFile(base64Data, mime, cd, currentUrl);
        else { isPendingBlob=true; pendingBlobData=base64Data; pendingBlobMimetype=mime; pendingBlobContentDisposition=cd; pendingBlobCurrentUrl=currentUrl; }
    }

    @JavascriptInterface
    public void saveCodeToFile(final String code, final String filename) {
        requireActivity().runOnUiThread(() -> {
            try {
                ContentValues cv = new ContentValues();
                cv.put(MediaStore.Downloads.DISPLAY_NAME, filename);
                cv.put(MediaStore.Downloads.MIME_TYPE, "text/plain");
                cv.put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS + "/Code-ioPRO");
                Uri uri = requireActivity().getContentResolver().insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, cv);
                if (uri != null) {
                    try (java.io.OutputStream os = requireActivity().getContentResolver().openOutputStream(uri)) {
                        if (os != null) os.write(code.getBytes("UTF-8"));
                    }
                    Toast.makeText(requireContext(), "✓ İndirildi: " + filename, Toast.LENGTH_LONG).show();
                    if (getActivity() instanceof MainActivity) {
                        FilesFragment ff = ((MainActivity) getActivity()).getFilesFragment();
                        if (ff != null) ff.refreshFiles();
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "saveCodeToFile", e);
                Toast.makeText(requireContext(), "Hata: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @JavascriptInterface
    public void runCodeInShell(final String code, final String language) {
        requireActivity().runOnUiThread(() -> {
            if (!(getActivity() instanceof MainActivity)) return;
            String cmd;
            switch (language) {
                case "python": case "python3": cmd = "python3 -c '" + code.replace("'","'\"'\"'") + "'"; break;
                case "javascript": case "js":  cmd = "node -e '" + code.replace("'","'\"'\"'") + "'"; break;
                case "bash": case "shell": case "sh": cmd = code; break;
                case "ruby": cmd = "ruby -e '" + code.replace("'","'\"'\"'") + "'"; break;
                case "php":  cmd = "php -r '" + code.replace("'","'\"'\"'") + "'"; break;
                default:     cmd = "echo 'Dil desteklenmiyor: " + language + "'"; break;
            }
            ((MainActivity) getActivity()).navigateToShell(cmd);
        });
    }

    @JavascriptInterface
    public void setModel(final String model) {
        currentModel = model;
        requireActivity().getSharedPreferences("codeio_prefs", Context.MODE_PRIVATE)
            .edit().putString("active_model", model).apply();
    }

    @JavascriptInterface
    public String getCurrentModel() {
        return requireActivity().getSharedPreferences("codeio_prefs", Context.MODE_PRIVATE)
            .getString("active_model", currentModel);
    }

    @JavascriptInterface
    public void setAgentMode(final String mode) {
        agentMode = mode;
        requireActivity().getSharedPreferences("codeio_prefs", Context.MODE_PRIVATE)
            .edit().putString("agent_mode", mode).apply();
    }

    @JavascriptInterface
    public String getAgentMode() {
        return requireActivity().getSharedPreferences("codeio_prefs", Context.MODE_PRIVATE)
            .getString("agent_mode", agentMode);
    }

    @JavascriptInterface
    public void showSettingsDialog() {
        requireActivity().runOnUiThread(() -> {
            android.app.Dialog d = new android.app.Dialog(requireActivity());
            d.requestWindowFeature(Window.FEATURE_NO_TITLE);
            WebView wv = new WebView(requireContext());
            wv.getSettings().setJavaScriptEnabled(true);
            wv.getSettings().setDomStorageEnabled(true);
            wv.getSettings().setAllowFileAccess(true);
            wv.addJavascriptInterface(new SettingsBridge(requireActivity(), d), "AndroidSettings");
            wv.loadUrl("file:///android_asset/settings.html");
            d.setContentView(wv);
            d.show();
            Window w = d.getWindow();
            if (w != null) { w.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);
                w.setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(Color.TRANSPARENT)); }
        });
    }

    // ── File helpers ────────────────────────────────────────────────────────
    private boolean checkDownloadPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) return true;
        return requireActivity().checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
            == PackageManager.PERMISSION_GRANTED;
    }

    private void startStandardDownload(String url, String ua, String cd, String mime, long len) {
        android.app.DownloadManager.Request req = new android.app.DownloadManager.Request(Uri.parse(url));
        req.setMimeType(mime);
        String fn = URLUtil.guessFileName(url, cd, mime);
        req.setDescription("Code-ioPRO"); req.setTitle(fn);
        req.addRequestHeader("User-Agent", ua);
        req.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "Code-ioPRO/" + fn);
        req.setNotificationVisibility(android.app.DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        android.app.DownloadManager dm = (android.app.DownloadManager) requireActivity().getSystemService(Context.DOWNLOAD_SERVICE);
        if (dm != null) dm.enqueue(req);
        Toast.makeText(requireContext(), "İndirme başladı: " + fn, Toast.LENGTH_SHORT).show();
    }

    private void saveBlobToFile(String base64Data, String mime, String cd, String currentUrl) {
        requireActivity().runOnUiThread(() -> {
            try {
                String data = base64Data.contains(",") ? base64Data.split(",", 2)[1] : base64Data;
                byte[] bytes = Base64.decode(data, Base64.DEFAULT);
                String ext = getExtFromMime(mime);
                String fn = "codeio_" + System.currentTimeMillis() + ext;
                if (cd != null && cd.contains("filename")) {
                    String fnPart = cd.replaceAll(".*filename\\*?=['\"]?([^;\"'\\n]+)['\"]?.*", "$1").trim();
                    if (!fnPart.isEmpty() && !fnPart.equals(cd)) fn = fnPart;
                }
                ContentValues cv = new ContentValues();
                cv.put(MediaStore.Downloads.DISPLAY_NAME, fn);
                cv.put(MediaStore.Downloads.MIME_TYPE, mime != null ? mime : "application/octet-stream");
                cv.put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS + "/Code-ioPRO");
                Uri uri = requireActivity().getContentResolver().insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, cv);
                if (uri != null) {
                    try (java.io.OutputStream os = requireActivity().getContentResolver().openOutputStream(uri)) {
                        if (os != null) os.write(bytes);
                    }
                    Toast.makeText(requireContext(), "✓ İndirildi: " + fn, Toast.LENGTH_LONG).show();
                }
            } catch (Exception e) {
                Log.e(TAG, "saveBlobToFile", e);
                Toast.makeText(requireContext(), "İndirme hatası: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String getExtFromMime(String m) {
        if (m == null) return ".bin";
        if (m.contains("pdf"))  return ".pdf";
        if (m.contains("zip"))  return ".zip";
        if (m.contains("png"))  return ".png";
        if (m.contains("jpeg") || m.contains("jpg")) return ".jpg";
        if (m.contains("gif"))  return ".gif";
        if (m.contains("mp4"))  return ".mp4";
        if (m.contains("mp3"))  return ".mp3";
        if (m.contains("json")) return ".json";
        if (m.contains("html")) return ".html";
        if (m.contains("text")) return ".txt";
        if (m.contains("jar"))  return ".jar";
        if (m.contains("apk") || m.contains("android")) return ".apk";
        return "." + m.replaceAll(".*/","").replaceAll(";.*","");
    }

    @Override
    public void onActivityResult(int req, int res, Intent data) {
        super.onActivityResult(req, res, data);
        if (req == FILE_CHOOSER_REQUEST_CODE) {
            Uri[] uris = null;
            if (res == android.app.Activity.RESULT_OK && data != null && data.getData() != null)
                uris = new Uri[]{data.getData()};
            if (mUploadMessage != null) { mUploadMessage.onReceiveValue(uris); mUploadMessage = null; }
        }
    }
}
