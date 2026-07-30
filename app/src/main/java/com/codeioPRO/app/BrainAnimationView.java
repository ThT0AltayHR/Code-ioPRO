package com.codeioPRO.app;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

/**
 * BrainAnimationView — Compact 3D brain WebView.
 * Shows agent activity through electric-blue pulse animations.
 * Place in XML layout, call setActive(bool) and setQuota(int 0-100).
 */
public class BrainAnimationView extends WebView {

    private boolean isActive = false;
    private int quota = 100;
    private boolean isLoaded = false;

    public BrainAnimationView(Context ctx) { super(ctx); init(); }
    public BrainAnimationView(Context ctx, AttributeSet a) { super(ctx, a); init(); }
    public BrainAnimationView(Context ctx, AttributeSet a, int d) { super(ctx, a, d); init(); }

    @SuppressLint("SetJavaScriptEnabled")
    private void init() {
        WebSettings ws = getSettings();
        ws.setJavaScriptEnabled(true);
        ws.setDomStorageEnabled(true);
        ws.setAllowFileAccess(true);
        setBackgroundColor(0x00000000);
        addJavascriptInterface(new BrainBridge(), "BrainNative");
        setWebViewClient(new WebViewClient() {
            @Override public void onPageFinished(WebView v, String url) {
                isLoaded = true; applyState(); applyQuota();
            }
        });
        loadUrl("file:///android_asset/brain_animation.html");
    }

    public void setActive(boolean active) {
        isActive = active;
        if (isLoaded) applyState();
    }

    public void setQuota(int q) {
        quota = Math.max(0, Math.min(100, q));
        if (isLoaded) applyQuota();
    }

    private void applyState() {
        post(() -> evaluateJavascript("if(window.setBrainActive){window.setBrainActive(" + isActive + ");}", null));
    }

    private void applyQuota() {
        post(() -> evaluateJavascript("if(window.setBrainQuota){window.setBrainQuota(" + quota + ");}", null));
    }

    class BrainBridge {
        @JavascriptInterface
        public void onBrainReady() { post(() -> { applyState(); applyQuota(); }); }
    }
}
