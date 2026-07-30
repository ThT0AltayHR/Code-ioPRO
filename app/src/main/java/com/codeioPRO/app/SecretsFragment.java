package com.codeioPRO.app;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
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
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.codeioPRO.app.util.SecretsManager;

import org.json.JSONObject;
import java.util.concurrent.Executor;

public class SecretsFragment extends Fragment {

    private WebView secretsWebView;
    private boolean authenticated = false;
    private SecretsManager secretsManager;

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inf, @Nullable ViewGroup c, @Nullable Bundle s) {
        return inf.inflate(R.layout.fragment_secrets, c, false);
    }

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle s) {
        super.onViewCreated(view, s);
        secretsManager = new SecretsManager(requireContext());
        secretsWebView = view.findViewById(R.id.secrets_webview);

        WebSettings ws = secretsWebView.getSettings();
        ws.setJavaScriptEnabled(true);
        ws.setDomStorageEnabled(true);
        ws.setAllowFileAccess(true);

        secretsWebView.setWebViewClient(new WebViewClient() {
            @Override public void onPageFinished(WebView v, String url) {
                if (!authenticated) showBiometricPrompt();
                else loadSecretsData();
            }
        });
        secretsWebView.addJavascriptInterface(new SecretsBridge(), "AndroidSecrets");
        secretsWebView.loadUrl("file:///android_asset/secrets.html");
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!authenticated && isVisible()) showBiometricPrompt();
    }

    private void showBiometricPrompt() {
        BiometricManager bm = BiometricManager.from(requireContext());
        int canAuth = bm.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_WEAK | BiometricManager.Authenticators.DEVICE_CREDENTIAL);
        if (canAuth == BiometricManager.BIOMETRIC_SUCCESS) {
            Executor executor = ContextCompat.getMainExecutor(requireContext());
            BiometricPrompt prompt = new BiometricPrompt(this, executor, new BiometricPrompt.AuthenticationCallback() {
                @Override public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult res) {
                    authenticated = true;
                    loadSecretsData();
                }
                @Override public void onAuthenticationError(int code, @NonNull CharSequence err) {
                    Toast.makeText(requireContext(), "Kimlik doğrulama gerekli: " + err, Toast.LENGTH_SHORT).show();
                    if (getActivity() instanceof MainActivity) ((MainActivity) getActivity()).navigateToChat();
                }
                @Override public void onAuthenticationFailed() {
                    Toast.makeText(requireContext(), "Kimlik doğrulama başarısız", Toast.LENGTH_SHORT).show();
                }
            });
            BiometricPrompt.PromptInfo info = new BiometricPrompt.PromptInfo.Builder()
                .setTitle("SECRETS'a Erişim")
                .setSubtitle("Code-ioPRO güvenli depo")
                .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_WEAK | BiometricManager.Authenticators.DEVICE_CREDENTIAL)
                .build();
            prompt.authenticate(info);
        } else {
            // No biometric available, grant access directly
            authenticated = true;
            loadSecretsData();
        }
    }

    private void loadSecretsData() {
        if (secretsWebView == null) return;
        String json = secretsManager.getAllSecretsJson();
        secretsWebView.evaluateJavascript("if(window.loadSecrets)window.loadSecrets(" + json + ");", null);
    }

    // ── Bridge ────────────────────────────────────────────────────────────────
    private class SecretsBridge {
        @JavascriptInterface
        public String getSecrets() {
            if (!authenticated) return "{}";
            return secretsManager.getAllSecretsJson();
        }

        @JavascriptInterface
        public void saveSecret(String key, String value) {
            if (!authenticated) return;
            secretsManager.setSecret(key, value);
            requireActivity().runOnUiThread(() ->
                Toast.makeText(requireContext(), "✓ Kaydedildi: " + key, Toast.LENGTH_SHORT).show());
        }

        @JavascriptInterface
        public void deleteSecret(String key) {
            if (!authenticated) return;
            secretsManager.deleteSecret(key);
            requireActivity().runOnUiThread(() ->
                Toast.makeText(requireContext(), "✓ Silindi: " + key, Toast.LENGTH_SHORT).show());
        }

        @JavascriptInterface
        public void saveAllSettings(String jsonStr) {
            if (!authenticated) return;
            try {
                JSONObject obj = new JSONObject(jsonStr);
                android.content.SharedPreferences.Editor editor = requireActivity()
                    .getSharedPreferences("codeio_prefs", Context.MODE_PRIVATE).edit();
                // Known settings keys
                String[] boolKeys = {"use_drawer_assistant","use_drawer_shared","trigger_voice_assistant",
                    "continue_last_chat","compress_context","star_bg_enabled","copy_btn_enabled",
                    "download_btn_enabled","run_btn_enabled","rich_notifications","haptic_feedback",
                    "auto_save_files","dark_mode","show_quota","auto_update_check"};
                String[] strKeys  = {"custom_api_url","system_prompt","ask_suffix","shared_doc_suffix",
                    "github_token","github_username","custom_headers","proxy_host","proxy_port",
                    "openai_api_key","claude_api_key","gemini_api_key","openrouter_api_key"};
                String[] intKeys  = {"context_max_tokens","text_zoom","request_timeout_sec","max_history_msgs"};

                for (String k : boolKeys) if (obj.has(k)) editor.putBoolean(k, obj.optBoolean(k));
                for (String k : strKeys)  if (obj.has(k)) {
                    String val = obj.optString(k,"");
                    if (!val.isEmpty()) secretsManager.setSecret(k, val);
                    else editor.putString(k, val);
                }
                for (String k : intKeys)  if (obj.has(k)) editor.putInt(k, obj.optInt(k));
                editor.apply();
                requireActivity().runOnUiThread(() ->
                    Toast.makeText(requireContext(), "✓ Tüm ayarlar kaydedildi", Toast.LENGTH_SHORT).show());
            } catch (Exception e) {
                requireActivity().runOnUiThread(() ->
                    Toast.makeText(requireContext(), "Ayar kaydetme hatası: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        }

        @JavascriptInterface
        public String getAllSettings() {
            try {
                android.content.SharedPreferences prefs = requireActivity()
                    .getSharedPreferences("codeio_prefs", Context.MODE_PRIVATE);
                JSONObject obj = new JSONObject();
                // Add all prefs
                obj.put("use_drawer_assistant", prefs.getBoolean("use_drawer_assistant", true));
                obj.put("use_drawer_shared", prefs.getBoolean("use_drawer_shared", true));
                obj.put("trigger_voice_assistant", prefs.getBoolean("trigger_voice_assistant", true));
                obj.put("continue_last_chat", prefs.getBoolean("continue_last_chat", false));
                obj.put("compress_context", prefs.getBoolean("compress_context", true));
                obj.put("star_bg_enabled", prefs.getBoolean("star_bg_enabled", true));
                obj.put("copy_btn_enabled", prefs.getBoolean("copy_btn_enabled", true));
                obj.put("download_btn_enabled", prefs.getBoolean("download_btn_enabled", true));
                obj.put("run_btn_enabled", prefs.getBoolean("run_btn_enabled", true));
                obj.put("rich_notifications", prefs.getBoolean("rich_notifications", true));
                obj.put("haptic_feedback", prefs.getBoolean("haptic_feedback", true));
                obj.put("auto_save_files", prefs.getBoolean("auto_save_files", false));
                obj.put("dark_mode", prefs.getBoolean("dark_mode", true));
                obj.put("show_quota", prefs.getBoolean("show_quota", true));
                obj.put("auto_update_check", prefs.getBoolean("auto_update_check", true));
                obj.put("context_max_tokens", prefs.getInt("context_max_tokens", 4096));
                obj.put("text_zoom", prefs.getInt("text_zoom", 100));
                obj.put("request_timeout_sec", prefs.getInt("request_timeout_sec", 60));
                obj.put("max_history_msgs", prefs.getInt("max_history_msgs", 50));
                obj.put("custom_api_url", prefs.getString("custom_api_url", "https://duck.ai"));
                obj.put("ask_suffix", prefs.getString("ask_suffix", ""));
                obj.put("shared_doc_suffix", prefs.getString("shared_doc_suffix", ""));
                obj.put("proxy_host", prefs.getString("proxy_host", ""));
                obj.put("proxy_port", prefs.getString("proxy_port", ""));
                obj.put("github_username", prefs.getString("github_username", ""));
                // Secrets (only existence, not value)
                obj.put("has_github_token", secretsManager.hasSecret("github_token"));
                obj.put("has_openai_key", secretsManager.hasSecret("openai_api_key"));
                obj.put("has_claude_key", secretsManager.hasSecret("claude_api_key"));
                obj.put("has_gemini_key", secretsManager.hasSecret("gemini_api_key"));
                obj.put("app_version", "1.1.0");
                return obj.toString();
            } catch (Exception e) { return "{}"; }
        }

        @JavascriptInterface
        public void testApiKey(String keyType) {
            requireActivity().runOnUiThread(() ->
                Toast.makeText(requireContext(), keyType + " testi — yakında!", Toast.LENGTH_SHORT).show());
        }

        @JavascriptInterface
        public void clearAllData() {
            requireActivity().runOnUiThread(() ->
                new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                    .setTitle("Tüm Veriyi Sil")
                    .setMessage("Tüm ayarlar, API anahtarları ve sohbet geçmişi silinsin mi?")
                    .setPositiveButton("Sil", (d, w) -> {
                        requireActivity().getSharedPreferences("codeio_prefs", Context.MODE_PRIVATE).edit().clear().apply();
                        secretsManager.clearAll();
                        Toast.makeText(requireContext(), "✓ Tüm veri silindi", Toast.LENGTH_LONG).show();
                        loadSecretsData();
                    })
                    .setNegativeButton("İptal", null)
                    .show());
        }

        @JavascriptInterface
        public void lock() {
            authenticated = false;
            requireActivity().runOnUiThread(() -> {
                secretsWebView.evaluateJavascript("if(window.showLockScreen)window.showLockScreen();", null);
                Toast.makeText(requireContext(), "SECRETS kilitlendi", Toast.LENGTH_SHORT).show();
            });
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (secretsWebView != null) {
            secretsWebView.stopLoading();
            secretsWebView.clearHistory();
            secretsWebView.destroy();
            secretsWebView = null;
        }
    }
}
