package com.codeioPRO.app;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

/**
 * Handles text/file share intents and redirects to MainActivity (chat tab).
 */
public class AskActivity extends Activity {

    private static final String TAG = "CodeioPROAsk";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        handleIntent(getIntent());
        finish();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleIntent(intent);
        finish();
    }

    private void handleIntent(Intent intent) {
        if (intent == null) return;
        String action = intent.getAction();
        String type   = intent.getType();

        // File/image share → open ChatActivity overlay
        if (Intent.ACTION_SEND.equals(action) && type != null) {
            if (type.startsWith("image/") || "application/pdf".equals(type)
                    || type.startsWith("text/") || type.equals("*/*")) {
                Uri streamUri = intent.getParcelableExtra(Intent.EXTRA_STREAM);
                if (streamUri != null) {
                    Intent chatIntent = new Intent(this, ChatActivity.class);
                    chatIntent.setAction(Intent.ACTION_SEND);
                    chatIntent.setType(type);
                    chatIntent.putExtra(Intent.EXTRA_STREAM, streamUri);
                    chatIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(chatIntent);
                    return;
                }
            }
        }

        // Text share / process text
        String sharedText = null;
        if (Intent.ACTION_SEND.equals(action) && "text/plain".equals(type)) {
            sharedText = intent.getStringExtra(Intent.EXTRA_TEXT);
        } else if (Intent.ACTION_PROCESS_TEXT.equals(action)) {
            CharSequence cs = intent.getCharSequenceExtra(Intent.EXTRA_PROCESS_TEXT);
            if (cs != null) sharedText = cs.toString();
        }

        if (sharedText != null) {
            SharedPreferences prefs = getSharedPreferences("codeio_prefs", MODE_PRIVATE);
            String suffix = prefs.getString("ask_suffix", "");
            if (suffix != null && !suffix.trim().isEmpty()) {
                sharedText = sharedText + "\n\n" + suffix;
            }

            // Launch MainActivity to chat tab with the shared text
            Intent mainIntent = new Intent(this, MainActivity.class);
            mainIntent.setAction(Intent.ACTION_SEND);
            mainIntent.setType("text/plain");
            mainIntent.putExtra(Intent.EXTRA_TEXT, sharedText);
            mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(mainIntent);
        }
    }
}
