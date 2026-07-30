package com.codeioPRO.app;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.webkit.JavascriptInterface;
import android.widget.Toast;
import com.codeioPRO.app.util.SecretsManager;
import org.json.JSONObject;

/**
 * Bridge between settings.html WebView and Android SharedPreferences / SecretsManager.
 * Injected as "AndroidSettings" into the settings WebView.
 */
public class SettingsBridge {

    private final Activity activity;
    private final Dialog   dialog;
    private final SecretsManager secrets;

    public SettingsBridge(Activity activity, Dialog dialog) {
        this.activity = activity;
        this.dialog   = dialog;
        this.secrets  = new SecretsManager(activity);
    }

    @JavascriptInterface
    public String getSettings() {
        try {
            android.content.SharedPreferences p =
                activity.getSharedPreferences("codeio_prefs", Context.MODE_PRIVATE);
            JSONObject o = new JSONObject();
            // Booleans
            o.put("star_bg_enabled",       p.getBoolean("star_bg_enabled",       true));
            o.put("copy_btn_enabled",      p.getBoolean("copy_btn_enabled",      true));
            o.put("download_btn_enabled",  p.getBoolean("download_btn_enabled",  true));
            o.put("run_btn_enabled",       p.getBoolean("run_btn_enabled",       true));
            o.put("haptic_feedback",       p.getBoolean("haptic_feedback",       true));
            o.put("compress_context",      p.getBoolean("compress_context",      true));
            o.put("rich_notifications",    p.getBoolean("rich_notifications",    true));
            o.put("auto_save_files",       p.getBoolean("auto_save_files",       false));
            o.put("use_drawer_assistant",  p.getBoolean("use_drawer_assistant",  true));
            o.put("use_drawer_shared",     p.getBoolean("use_drawer_shared",     true));
            o.put("trigger_voice",         p.getBoolean("trigger_voice_assistant", true));
            o.put("show_quota",            p.getBoolean("show_quota",            true));
            o.put("auto_update_check",     p.getBoolean("auto_update_check",     true));
            o.put("dark_mode",             p.getBoolean("dark_mode",             true));
            o.put("continue_last_chat",    p.getBoolean("continue_last_chat",    false));
            // Integers
            o.put("text_zoom",             p.getInt("text_zoom",             100));
            o.put("context_max_tokens",    p.getInt("context_max_tokens",    4096));
            o.put("request_timeout_sec",   p.getInt("request_timeout_sec",   60));
            o.put("max_history_msgs",      p.getInt("max_history_msgs",      50));
            // Strings
            o.put("custom_api_url",        p.getString("custom_api_url",     "https://duck.ai"));
            o.put("ask_suffix",            p.getString("ask_suffix",         ""));
            o.put("shared_doc_suffix",     p.getString("shared_doc_suffix",  ""));
            o.put("proxy_host",            p.getString("proxy_host",         ""));
            o.put("proxy_port",            p.getString("proxy_port",         ""));
            o.put("github_username",       p.getString("github_username",    ""));
            o.put("active_provider",       p.getString("active_provider",    "duckduckgo"));
            o.put("active_model",          p.getString("active_model",       ""));
            // Secrets (existence only)
            o.put("has_openai_key",        secrets.hasSecret("openai_api_key"));
            o.put("has_claude_key",        secrets.hasSecret("claude_api_key"));
            o.put("has_gemini_key",        secrets.hasSecret("gemini_api_key"));
            o.put("has_github_token",      secrets.hasSecret("github_token"));
            o.put("has_groq_key",          secrets.hasSecret("groq_api_key"));
            o.put("app_version",           "1.1.0");
            return o.toString();
        } catch (Exception e) { return "{}"; }
    }

    @JavascriptInterface
    public void saveSettings(String jsonStr) {
        try {
            JSONObject obj = new JSONObject(jsonStr);
            android.content.SharedPreferences.Editor ed =
                activity.getSharedPreferences("codeio_prefs", Context.MODE_PRIVATE).edit();

            String[] boolKeys = {
                "star_bg_enabled","copy_btn_enabled","download_btn_enabled","run_btn_enabled",
                "haptic_feedback","compress_context","rich_notifications","auto_save_files",
                "use_drawer_assistant","use_drawer_shared","trigger_voice_assistant","show_quota",
                "auto_update_check","dark_mode","continue_last_chat"
            };
            String[] strKeys  = {
                "custom_api_url","ask_suffix","shared_doc_suffix","proxy_host","proxy_port",
                "github_username","active_provider","active_model","system_prompt"
            };
            String[] intKeys  = {
                "text_zoom","context_max_tokens","request_timeout_sec","max_history_msgs"
            };
            String[] secKeys  = {
                "openai_api_key","claude_api_key","gemini_api_key","github_token",
                "groq_api_key","mistral_api_key","openrouter_api_key","custom_api_key"
            };

            for (String k : boolKeys) if (obj.has(k)) ed.putBoolean(k, obj.optBoolean(k));
            for (String k : strKeys)  if (obj.has(k)) ed.putString(k, obj.optString(k,""));
            for (String k : intKeys)  if (obj.has(k)) ed.putInt(k, obj.optInt(k));
            ed.apply();

            for (String k : secKeys) {
                if (obj.has(k)) {
                    String v = obj.optString(k, "");
                    if (!v.isEmpty()) secrets.setSecret(k, v);
                }
            }

            activity.runOnUiThread(() ->
                Toast.makeText(activity, "✓ Ayarlar kaydedildi", Toast.LENGTH_SHORT).show());
        } catch (Exception e) {
            activity.runOnUiThread(() ->
                Toast.makeText(activity, "Hata: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        }
    }

    /** Generic single-key setter — used by the expanded 150+ settings page */
    @JavascriptInterface
    public void set(String key, String value) {
        try {
            android.content.SharedPreferences.Editor ed =
                activity.getSharedPreferences("codeio_prefs", Context.MODE_PRIVATE).edit();
            // Try boolean
            if ("true".equals(value) || "false".equals(value)) {
                ed.putBoolean(key, Boolean.parseBoolean(value));
            } else {
                // Try int
                try { ed.putInt(key, Integer.parseInt(value)); }
                catch (NumberFormatException e2) { ed.putString(key, value); }
            }
            ed.apply();
        } catch (Exception e) { /* ignore */ }
    }

    /** Returns ALL prefs as JSON — for the expanded settings page */
    @JavascriptInterface
    public String getAll() {
        try {
            android.content.SharedPreferences p =
                activity.getSharedPreferences("codeio_prefs", Context.MODE_PRIVATE);
            JSONObject o = new JSONObject(p.getAll().toString().replace("{", "").replace("}", ""));
            // Parse the raw map properly
            JSONObject out = new JSONObject();
            for (java.util.Map.Entry<String, ?> entry : p.getAll().entrySet()) {
                out.put(entry.getKey(), entry.getValue());
            }
            // Add secrets presence
            out.put("has_openai_key", secrets.hasSecret("openai_api_key"));
            out.put("has_claude_key", secrets.hasSecret("claude_api_key"));
            out.put("has_gemini_key", secrets.hasSecret("gemini_api_key"));
            out.put("has_github_token", secrets.hasSecret("github_token"));
            out.put("has_groq_key", secrets.hasSecret("groq_api_key"));
            out.put("app_version", "1.1.0");
            return out.toString();
        } catch (Exception e) { return getSettings(); }
    }

    @JavascriptInterface
    public void close() {
        activity.runOnUiThread(() -> { if (dialog != null && dialog.isShowing()) dialog.dismiss(); });
    }

    @JavascriptInterface
    public void clearAllData() {
        activity.runOnUiThread(() ->
            new androidx.appcompat.app.AlertDialog.Builder(activity)
                .setTitle("Tüm Veriyi Sil")
                .setMessage("Tüm ayarlar, API anahtarları ve sohbet geçmişi silinsin mi?")
                .setPositiveButton("Sil", (d, w) -> {
                    activity.getSharedPreferences("codeio_prefs", Context.MODE_PRIVATE)
                        .edit().clear().apply();
                    secrets.clearAll();
                    Toast.makeText(activity, "✓ Tüm veri silindi", Toast.LENGTH_LONG).show();
                })
                .setNegativeButton("İptal", null)
                .show());
    }
}
