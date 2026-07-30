package com.codeioPRO.app.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import androidx.appcompat.app.AlertDialog;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.Executors;

import org.json.JSONObject;

public class UpdateChecker {
    private static final String TAG = "UpdateChecker";
    private static final String API_URL = "https://api.github.com/repos/%s/%s/releases/latest";
    private static final String CURRENT_VERSION = "1.1.0";

    public static void checkForUpdates(Context ctx, String owner, String repo) {
        SharedPreferences prefs = ctx.getSharedPreferences("codeio_prefs", Context.MODE_PRIVATE);
        if (!prefs.getBoolean("auto_update_check", true)) return;

        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                URL url = new URL(String.format(API_URL, owner, repo));
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(10000);
                conn.setReadTimeout(10000);
                conn.setRequestProperty("User-Agent", "Code-ioPRO/1.0");

                int code = conn.getResponseCode();
                if (code != 200) return;

                StringBuilder sb = new StringBuilder();
                try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
                    String line;
                    while ((line = br.readLine()) != null) sb.append(line);
                }

                JSONObject release = new JSONObject(sb.toString());
                String tagName   = release.optString("tag_name", "").replaceAll("[vV]", "");
                String releaseName = release.optString("name", tagName);
                String releaseBody = release.optString("body", "");
                String htmlUrl   = release.optString("html_url", "");

                if (tagName.isEmpty() || tagName.equals(CURRENT_VERSION)) return;

                // Check if already dismissed
                String dismissed = prefs.getString("dismissed_version", "");
                if (tagName.equals(dismissed)) return;

                // Show dialog on main thread
                new Handler(Looper.getMainLooper()).post(() ->
                    showUpdateDialog(ctx, tagName, releaseName, releaseBody, htmlUrl, prefs));

            } catch (Exception e) {
                Log.d(TAG, "Update check failed: " + e.getMessage());
            }
        });
    }

    private static void showUpdateDialog(Context ctx, String version, String name, String body, String url, SharedPreferences prefs) {
        String msg = "Sürüm " + version + " yayınlandı!\n\n";
        if (!name.isEmpty() && !name.equals(version)) msg += "📌 " + name + "\n\n";
        if (!body.isEmpty()) msg += body.length() > 300 ? body.substring(0, 300) + "…" : body;

        final String versionFinal = version;
        new AlertDialog.Builder(ctx)
            .setTitle("🔄 Güncelleme Mevcut")
            .setMessage(msg)
            .setPositiveButton("GitHub'da İndir", (d, w) -> {
                android.content.Intent i = new android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(url));
                ctx.startActivity(i);
            })
            .setNeutralButton("Bu Sürümü Atla", (d, w) ->
                prefs.edit().putString("dismissed_version", versionFinal).apply())
            .setNegativeButton("Sonra", null)
            .setCancelable(true)
            .show();
    }
}
