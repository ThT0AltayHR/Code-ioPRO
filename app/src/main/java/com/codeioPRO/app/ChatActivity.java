package com.codeioPRO.app;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import androidx.appcompat.app.AppCompatActivity;

/**
 * Bottom drawer overlay for the assistant / voice chat.
 * Shows a sliding WebView panel over the current app.
 */
public class ChatActivity extends AppCompatActivity {

    private boolean isDismissing = false;
    private int cardHeight = 0;
    private int collapsedHeight = 0;
    private WebView drawerWebView;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        overridePendingTransition(0, 0);

        final View bottomDrawerCard = findViewById(R.id.bottomDrawerCard);
        final View drawerHeader     = findViewById(R.id.drawerHeader);
        final View dismissArea      = findViewById(R.id.dismissArea);
        drawerWebView = findViewById(R.id.drawerWebView);

        if (drawerWebView != null) {
            WebSettings ws = drawerWebView.getSettings();
            ws.setJavaScriptEnabled(true);
            ws.setDomStorageEnabled(true);
            ws.setMediaPlaybackRequiresUserGesture(false);
            String pref = getSharedPreferences("codeio_prefs", MODE_PRIVATE)
                .getString("custom_api_url", "https://duck.ai");
            drawerWebView.loadUrl(pref != null && !pref.isEmpty() ? pref : "https://duck.ai");
        }

        if (dismissArea != null) {
            dismissArea.setOnClickListener(v -> dismissDrawer());
        }

        if (bottomDrawerCard != null) {
            bottomDrawerCard.getViewTreeObserver().addOnGlobalLayoutListener(
                new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override public void onGlobalLayout() {
                        bottomDrawerCard.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                        int screen = getResources().getDisplayMetrics().heightPixels;
                        cardHeight      = (int) (screen * 0.85f);
                        collapsedHeight = (int) (screen * 0.55f);
                        android.view.ViewGroup.LayoutParams params = bottomDrawerCard.getLayoutParams();
                        if (params != null) { params.height = cardHeight; bottomDrawerCard.setLayoutParams(params); }
                        bottomDrawerCard.setTranslationY(cardHeight);
                        bottomDrawerCard.animate().translationY(cardHeight - collapsedHeight).setDuration(300).start();
                    }
                });
        }

        if (drawerHeader != null && bottomDrawerCard != null) {
            final float[] ity  = {0f};
            final float[] itry = {0f};
            drawerHeader.setOnTouchListener((v, ev) -> {
                if (cardHeight <= 0) return false;
                switch (ev.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        ity[0]  = ev.getRawY();
                        itry[0] = bottomDrawerCard.getTranslationY();
                        return true;
                    case MotionEvent.ACTION_MOVE:
                        float ny = Math.max(0, Math.min(itry[0] + ev.getRawY() - ity[0], cardHeight));
                        bottomDrawerCard.setTranslationY(ny);
                        return true;
                    case MotionEvent.ACTION_UP:
                        float cy  = bottomDrawerCard.getTranslationY();
                        float col = cardHeight - collapsedHeight;
                        if (cy < col / 2f)                    bottomDrawerCard.animate().translationY(0).setDuration(250).start();
                        else if (cy > (col + cardHeight) / 2f) dismissDrawer();
                        else                                   bottomDrawerCard.animate().translationY(col).setDuration(250).start();
                        v.performClick();
                        return true;
                }
                return false;
            });
        }

        // Handle incoming intent (e.g. shared text)
        handleIncomingIntent(getIntent());
    }

    private void handleIncomingIntent(Intent intent) {
        if (intent == null || drawerWebView == null) return;
        String action = intent.getAction();
        String type   = intent.getType();
        if (Intent.ACTION_SEND.equals(action) && "text/plain".equals(type)) {
            String text = intent.getStringExtra(Intent.EXTRA_TEXT);
            if (text != null) {
                String esc = text.replace("\\", "\\\\").replace("'", "\\'").replace("\n", "\\n");
                drawerWebView.post(() -> drawerWebView.evaluateJavascript(
                    "(function(){var t=document.querySelector('textarea,[contenteditable]');" +
                    "if(t){t.focus();document.execCommand('insertText',false,'" + esc + "');}})();", null));
            }
        }
    }

    private void dismissDrawer() {
        if (isDismissing) return;
        isDismissing = true;
        View card = findViewById(R.id.bottomDrawerCard);
        if (card != null) {
            card.animate().translationY(cardHeight > 0 ? cardHeight : card.getHeight())
                .setDuration(250)
                .withEndAction(() -> { ChatActivity.super.finish(); overridePendingTransition(0, 0); })
                .start();
        } else { super.finish(); overridePendingTransition(0, 0); }
    }

    @Override public void finish() { dismissDrawer(); }
}
