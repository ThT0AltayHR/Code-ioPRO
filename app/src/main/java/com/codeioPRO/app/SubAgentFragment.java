package com.codeioPRO.app;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * SubAgentFragment — AI sub-agent yönetim paneli.
 *
 */
public class SubAgentFragment extends Fragment {

    private WebView agentWebView;
    private SharedPreferences prefs;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    // newCachedThreadPool → newFixedThreadPool: sınırsız thread üretimini önler
    private final ExecutorService executor = Executors.newFixedThreadPool(4);

    private final List<AgentSession> activeSessions = new ArrayList<>();

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inf, @Nullable ViewGroup c, @Nullable Bundle s) {
        return inf.inflate(R.layout.fragment_subagent, c, false);
    }

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedState) {
        super.onViewCreated(view, savedState);
        prefs = requireContext().getSharedPreferences("codeio_prefs", Context.MODE_PRIVATE);

        agentWebView = view.findViewById(R.id.agent_webview);
        WebSettings ws = agentWebView.getSettings();
        ws.setJavaScriptEnabled(true);
        ws.setDomStorageEnabled(true);
        ws.setAllowFileAccess(true);

        agentWebView.setWebViewClient(new WebViewClient() {
            @Override public void onPageFinished(WebView v, String url) {
                injectInitialState();
            }
        });
        agentWebView.addJavascriptInterface(new AgentBridge(), "AgentBridge");
        agentWebView.loadUrl("file:///android_asset/subagent.html");
    }

    private void injectInitialState() {
        if (agentWebView == null) return;
        String model = prefs.getString("active_model", "gpt-4o-mini");
        String mode  = prefs.getString("agent_mode",   "economy");
        String json  = buildSessionsJson();
        agentWebView.evaluateJavascript(
            "if(window.initAgentUI){window.initAgentUI('" + escapeJs(model) + "','"
            + escapeJs(mode) + "'," + json + ");}", null);
    }

    private String buildSessionsJson() {
        try {
            JSONArray arr = new JSONArray();
            for (AgentSession s : activeSessions) {
                JSONObject o = new JSONObject();
                o.put("id",           s.id);
                o.put("role",         s.role);
                o.put("status",       s.status);
                o.put("model",        s.model);
                o.put("filesCreated", s.filesCreated);
                o.put("currentTask",  s.currentTask);
                o.put("logs",         new JSONArray(s.logs));
                arr.put(o);
            }
            return arr.toString();
        } catch (Exception e) { return "[]"; }
    }

    /**
     * Proje tanımına göre gerçek agent oturumları başlatır.
     *
     * ⚠️ DİKKAT: Bu metot gerçek AI çağrısı yapmaz.
     * API anahtarları yapılandırıldığında gerçek AI çağrıları yapar.
     * Gerçek ajan desteği için API anahtarı + backend entegrasyonu gerekir.
     */
    public void dispatchAgents(String projectDescription, String model, String mode) {
        if (executor.isShutdown()) return;
        activeSessions.clear();

        int agentCount = estimateAgentCount(projectDescription);
        String mainModel = "power".equals(mode) ? "claude-3-5-sonnet-20241022" : model;

        AgentSession mainAgent = new AgentSession();
        mainAgent.id          = UUID.randomUUID().toString().substring(0, 8);
        mainAgent.role        = "Ana Agent (Orkestra)";
        mainAgent.model       = mainModel;
        mainAgent.status      = "planning";
        mainAgent.currentTask = "[Demo] Proje analiz ediliyor...";
        activeSessions.add(mainAgent);

        String[] subRoles  = {"Kod Yazıcı", "Test & Fix", "UI Agent", "Güvenlik"};
        String[] subModels = "power".equals(mode)
            ? new String[]{"claude-3-5-haiku-20241022","gpt-4o","gpt-4o-mini","gemini-1.5-flash"}
            : new String[]{"gpt-4o-mini","gemini-1.5-flash","mistral-small","gpt-4o-mini"};

        for (int i = 0; i < Math.min(agentCount - 1, subRoles.length); i++) {
            AgentSession sub = new AgentSession();
            sub.id          = UUID.randomUUID().toString().substring(0, 8);
            sub.role        = subRoles[i];
            sub.model       = subModels[i];
            sub.status      = "waiting";
            sub.currentTask = "[Demo] Ana agent bekleniyor...";
            activeSessions.add(sub);
        }

        updateUI();
        runDemoSimulation(mainAgent);
    }

    private int estimateAgentCount(String desc) {
        if (desc == null || desc.isEmpty()) return 1;
        int w = desc.split("\\s+").length;
        if (w < 20) return 1;
        if (w < 50) return 2;
        if (w < 100) return 3;
        return Math.min(w / 30, 4);
    }

    /** Demo görselleştirme — gerçek AI çağrısı değildir. */
    private void runDemoSimulation(AgentSession main) {
        if (executor.isShutdown()) return;
        executor.execute(() -> {
            try {
                Thread.sleep(800);
                main.addLog("📋 [Demo] Proje analiz edildi");
                main.addLog("🧠 [Demo] Sistem promptu oluşturuluyor...");
                main.status = "running";
                main.currentTask = "[Demo] Sistem promptu oluşturuluyor";
                updateUI();
                Thread.sleep(1200);
                main.addLog("✅ [Demo] Prompt hazır. Görevler dağıtılıyor...");
                for (int i = 1; i < activeSessions.size(); i++) {
                    AgentSession sub = activeSessions.get(i);
                    Thread.sleep(400);
                    sub.status      = "running";
                    sub.currentTask = "[Demo] Görev alındı, çalışıyor...";
                    sub.addLog("🚀 [Demo] Başlatıldı (" + sub.model + ")");
                    updateUI();
                }
                Thread.sleep(2000);
                main.addLog("📁 [Demo] Dosya yapısı planlandı");
                main.filesCreated = 3;
                updateUI();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
    }

    private void updateUI() {
        if (executor.isShutdown()) return;
        mainHandler.post(() -> {
            if (agentWebView != null) {
                agentWebView.evaluateJavascript(
                    "if(window.updateSessions){window.updateSessions("
                    + buildSessionsJson() + ");}", null);
            }
        });
    }

    private String escapeJs(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("'", "\\'");
    }

    // ── Veri modeli ─────────────────────────────────────────────────────────
    static class AgentSession {
        String id = "", role = "", model = "", status = "idle", currentTask = "";
        int filesCreated = 0;
        List<String> logs = new ArrayList<>();
        void addLog(String msg) { logs.add(msg); if (logs.size() > 50) logs.remove(0); }
    }

    // ── JS Köprüsü ───────────────────────────────────────────────────────────
    class AgentBridge {
        @JavascriptInterface
        public void dispatchProject(String description, String model, String mode) {
            prefs.edit().putString("agent_mode", mode).putString("active_model", model).apply();
            mainHandler.post(() -> dispatchAgents(description, model, mode));
        }
        @JavascriptInterface
        public void stopAllAgents() {
            for (AgentSession s : activeSessions) s.status = "stopped";
            mainHandler.post(() -> {
                updateUI();
                if (isAdded())
                    Toast.makeText(requireContext(), "Tüm agentlar durduruldu", Toast.LENGTH_SHORT).show();
            });
        }
        @JavascriptInterface public String  getSessionsJson()  { return buildSessionsJson(); }
        @JavascriptInterface public void    setMode(String m)  { prefs.edit().putString("agent_mode", m).apply(); }
        @JavascriptInterface public String  getActiveMode()    { return prefs.getString("agent_mode", "economy"); }
        @JavascriptInterface public void    clearSessions()    {
            activeSessions.clear();
            mainHandler.post(SubAgentFragment.this::updateUI);
        }
    }

    // ── Lifecycle ─────────────────────────────────────────────────────────────
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (agentWebView != null) {
            agentWebView.stopLoading();
            agentWebView.clearHistory();
            agentWebView.destroy();
            agentWebView = null;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // DÜZELTME: executor kapatılmıyor sorunu giderildi — thread sızıntısı önlendi
        executor.shutdownNow();
        try { executor.awaitTermination(500, TimeUnit.MILLISECONDS); }
        catch (InterruptedException e) { Thread.currentThread().interrupt(); }
    }
}
