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
import androidx.fragment.app.Fragment;
import com.codeioPRO.app.util.SecretsManager;

import org.json.JSONArray;
import org.json.JSONObject;

public class AiMarketFragment extends Fragment {

    private WebView marketWebView;
    private SecretsManager secretsManager;

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inf, @Nullable ViewGroup c, @Nullable Bundle s) {
        return inf.inflate(R.layout.fragment_ai_market, c, false);
    }

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle s) {
        super.onViewCreated(view, s);
        secretsManager = new SecretsManager(requireContext());
        marketWebView = view.findViewById(R.id.market_webview);

        WebSettings ws = marketWebView.getSettings();
        ws.setJavaScriptEnabled(true);
        ws.setDomStorageEnabled(true);
        ws.setAllowFileAccess(true);

        marketWebView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView v, String url) {
                loadMarketData();
            }
        });
        marketWebView.addJavascriptInterface(new MarketBridge(), "Market");
        marketWebView.loadUrl("file:///android_asset/ai_market.html");

        // Active models button
        View btnActive = view.findViewById(R.id.btn_active_models);
        if (btnActive != null) {
            btnActive.setOnClickListener(v -> showActiveModels());
        }
    }

    private void loadMarketData() {
        String json = buildMarketJson();
        marketWebView.evaluateJavascript(
            "if(window.loadMarket)window.loadMarket(" + json + ");", null);
    }

    private String buildMarketJson() {
        try {
            JSONArray providers = new JSONArray();

            // OpenAI
            JSONObject openai = new JSONObject();
            openai.put("id", "openai");
            openai.put("name", "OpenAI");
            openai.put("description", "GPT-4o, GPT-4 Turbo, o1 ve daha fazlası");
            openai.put("logo", "openai");
            openai.put("color", "#00A67E");
            openai.put("free", false);
            openai.put("apiUrl", "https://api.openai.com/v1");
            openai.put("docsUrl", "https://platform.openai.com/api-keys");
            openai.put("hasKey", secretsManager.hasSecret("openai_api_key"));
            JSONArray openaiModels = new JSONArray();
            openaiModels.put("gpt-4o"); openaiModels.put("gpt-4o-mini");
            openaiModels.put("gpt-4-turbo"); openaiModels.put("o1-preview"); openaiModels.put("o1-mini");
            openai.put("models", openaiModels);
            providers.put(openai);

            // Anthropic
            JSONObject anthropic = new JSONObject();
            anthropic.put("id", "anthropic");
            anthropic.put("name", "Anthropic Claude");
            anthropic.put("description", "Claude 3.5 Sonnet, Claude 3 Haiku ve Opus");
            anthropic.put("logo", "anthropic");
            anthropic.put("color", "#CC785C");
            anthropic.put("free", false);
            anthropic.put("apiUrl", "https://api.anthropic.com/v1");
            anthropic.put("docsUrl", "https://console.anthropic.com/settings/keys");
            anthropic.put("hasKey", secretsManager.hasSecret("claude_api_key"));
            JSONArray claudeModels = new JSONArray();
            claudeModels.put("claude-3-5-sonnet-20241022");
            claudeModels.put("claude-3-5-haiku-20241022");
            claudeModels.put("claude-3-opus-20240229");
            claudeModels.put("claude-3-haiku-20240307");
            anthropic.put("models", claudeModels);
            providers.put(anthropic);

            // Google Gemini
            JSONObject gemini = new JSONObject();
            gemini.put("id", "gemini");
            gemini.put("name", "Google Gemini");
            gemini.put("description", "Gemini 1.5 Pro, Flash ve Ultra");
            gemini.put("logo", "gemini");
            gemini.put("color", "#4285F4");
            gemini.put("free", false);
            gemini.put("apiUrl", "https://generativelanguage.googleapis.com/v1beta");
            gemini.put("docsUrl", "https://aistudio.google.com/app/apikey");
            gemini.put("hasKey", secretsManager.hasSecret("gemini_api_key"));
            JSONArray geminiModels = new JSONArray();
            geminiModels.put("gemini-1.5-pro"); geminiModels.put("gemini-1.5-flash");
            geminiModels.put("gemini-2.0-flash"); geminiModels.put("gemini-ultra");
            gemini.put("models", geminiModels);
            providers.put(gemini);

            // Groq (Free tier)
            JSONObject groq = new JSONObject();
            groq.put("id", "groq");
            groq.put("name", "Groq");
            groq.put("description", "Ultra-hızlı LPU çıkarım — Llama, Mixtral");
            groq.put("logo", "groq");
            groq.put("color", "#F55036");
            groq.put("free", true);
            groq.put("apiUrl", "https://api.groq.com/openai/v1");
            groq.put("docsUrl", "https://console.groq.com/keys");
            groq.put("hasKey", secretsManager.hasSecret("groq_api_key"));
            JSONArray groqModels = new JSONArray();
            groqModels.put("llama-3.3-70b-versatile");
            groqModels.put("llama-3.1-8b-instant");
            groqModels.put("mixtral-8x7b-32768");
            groqModels.put("gemma2-9b-it");
            groq.put("models", groqModels);
            providers.put(groq);

            // Mistral
            JSONObject mistral = new JSONObject();
            mistral.put("id", "mistral");
            mistral.put("name", "Mistral AI");
            mistral.put("description", "Mistral Large, Codestral ve daha fazlası");
            mistral.put("logo", "mistral");
            mistral.put("color", "#FF7000");
            mistral.put("free", false);
            mistral.put("apiUrl", "https://api.mistral.ai/v1");
            mistral.put("docsUrl", "https://console.mistral.ai/api-keys/");
            mistral.put("hasKey", secretsManager.hasSecret("mistral_api_key"));
            JSONArray mistralModels = new JSONArray();
            mistralModels.put("mistral-large-latest"); mistralModels.put("codestral-latest");
            mistralModels.put("mistral-small-latest"); mistralModels.put("open-mixtral-8x22b");
            mistral.put("models", mistralModels);
            providers.put(mistral);

            // Cohere
            JSONObject cohere = new JSONObject();
            cohere.put("id", "cohere");
            cohere.put("name", "Cohere");
            cohere.put("description", "Command R+, Command R ve Embed");
            cohere.put("logo", "cohere");
            cohere.put("color", "#39594D");
            cohere.put("free", true);
            cohere.put("apiUrl", "https://api.cohere.ai/v1");
            cohere.put("docsUrl", "https://dashboard.cohere.com/api-keys");
            cohere.put("hasKey", secretsManager.hasSecret("cohere_api_key"));
            JSONArray cohereModels = new JSONArray();
            cohereModels.put("command-r-plus"); cohereModels.put("command-r");
            cohereModels.put("command"); cohereModels.put("command-light");
            cohere.put("models", cohereModels);
            providers.put(cohere);

            // OpenRouter
            JSONObject openrouter = new JSONObject();
            openrouter.put("id", "openrouter");
            openrouter.put("name", "OpenRouter");
            openrouter.put("description", "200+ model — tek API ile hepsi");
            openrouter.put("logo", "openrouter");
            openrouter.put("color", "#6467F2");
            openrouter.put("free", true);
            openrouter.put("apiUrl", "https://openrouter.ai/api/v1");
            openrouter.put("docsUrl", "https://openrouter.ai/keys");
            openrouter.put("hasKey", secretsManager.hasSecret("openrouter_api_key"));
            JSONArray openrouterModels = new JSONArray();
            openrouterModels.put("openai/gpt-4o"); openrouterModels.put("anthropic/claude-3.5-sonnet");
            openrouterModels.put("google/gemini-pro-1.5"); openrouterModels.put("meta-llama/llama-3.3-70b-instruct");
            openrouter.put("models", openrouterModels);
            providers.put(openrouter);

            // Together AI
            JSONObject together = new JSONObject();
            together.put("id", "together");
            together.put("name", "Together AI");
            together.put("description", "Açık kaynak modeller — Llama, Qwen, Falcon");
            together.put("logo", "together");
            together.put("color", "#0A84FF");
            together.put("free", true);
            together.put("apiUrl", "https://api.together.xyz/v1");
            together.put("docsUrl", "https://api.together.ai/settings/api-keys");
            together.put("hasKey", secretsManager.hasSecret("together_api_key"));
            JSONArray togetherModels = new JSONArray();
            togetherModels.put("meta-llama/Llama-3.3-70B-Instruct-Turbo");
            togetherModels.put("Qwen/Qwen2.5-72B-Instruct-Turbo");
            togetherModels.put("deepseek-ai/DeepSeek-V3");
            together.put("models", togetherModels);
            providers.put(together);

            // Perplexity
            JSONObject perplexity = new JSONObject();
            perplexity.put("id", "perplexity");
            perplexity.put("name", "Perplexity");
            perplexity.put("description", "Sonar ile gerçek zamanlı web araması");
            perplexity.put("logo", "perplexity");
            perplexity.put("color", "#20B2AA");
            perplexity.put("free", false);
            perplexity.put("apiUrl", "https://api.perplexity.ai");
            perplexity.put("docsUrl", "https://www.perplexity.ai/settings/api");
            perplexity.put("hasKey", secretsManager.hasSecret("perplexity_api_key"));
            JSONArray perplexityModels = new JSONArray();
            perplexityModels.put("llama-3.1-sonar-large-128k-online");
            perplexityModels.put("llama-3.1-sonar-small-128k-online");
            perplexity.put("models", perplexityModels);
            providers.put(perplexity);

            // DeepSeek
            JSONObject deepseek = new JSONObject();
            deepseek.put("id", "deepseek");
            deepseek.put("name", "DeepSeek");
            deepseek.put("description", "DeepSeek R1, V3 — Çok güçlü kod modeli");
            deepseek.put("logo", "deepseek");
            deepseek.put("color", "#4D6BFE");
            deepseek.put("free", false);
            deepseek.put("apiUrl", "https://api.deepseek.com/v1");
            deepseek.put("docsUrl", "https://platform.deepseek.com/api_keys");
            deepseek.put("hasKey", secretsManager.hasSecret("deepseek_api_key"));
            JSONArray deepseekModels = new JSONArray();
            deepseekModels.put("deepseek-reasoner"); deepseekModels.put("deepseek-chat");
            deepseek.put("models", deepseekModels);
            providers.put(deepseek);

            // Custom API
            JSONObject custom = new JSONObject();
            custom.put("id", "custom");
            custom.put("name", "Özel API");
            custom.put("description", "Kendi API endpoint\'ini ekle");
            custom.put("logo", "custom");
            custom.put("color", "#00D4FF");
            custom.put("free", true);
            custom.put("apiUrl", "");
            custom.put("docsUrl", "");
            custom.put("hasKey", secretsManager.hasSecret("custom_api_key"));
            providers.put(custom);

            JSONObject result = new JSONObject();
            result.put("providers", providers);
            result.put("activeProvider", requireActivity()
                .getSharedPreferences("codeio_prefs", Context.MODE_PRIVATE)
                .getString("active_provider", "m-ai"));
            result.put("activeModel", requireActivity()
                .getSharedPreferences("codeio_prefs", Context.MODE_PRIVATE)
                .getString("active_model", "claude-haiku-4-2"));
            return result.toString();
        } catch (Exception e) {
            return "{}";
        }
    }

    private void showActiveModels() {
        // Show dialog with active API keys
        StringBuilder sb = new StringBuilder("Aktif API Anahtarları:\n\n");
        String[] keys = {"openai_api_key", "claude_api_key", "gemini_api_key", "groq_api_key",
            "mistral_api_key", "openrouter_api_key", "together_api_key", "deepseek_api_key"};
        String[] names = {"OpenAI", "Claude", "Gemini", "Groq",
            "Mistral", "OpenRouter", "Together AI", "DeepSeek"};
        for (int i = 0; i < keys.length; i++) {
            if (secretsManager.hasSecret(keys[i])) {
                sb.append("✓ ").append(names[i]).append("\n");
            }
        }
        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("🧠 Aktif Modeller")
            .setMessage(sb.toString())
            .setPositiveButton("Tamam", null)
            .show();
    }

    public void refresh() {
        if (marketWebView != null) loadMarketData();
    }

    private class MarketBridge {
        @JavascriptInterface
        public void saveApiKey(String providerId, String apiKey, String selectedModel) {
            if (apiKey == null || apiKey.trim().isEmpty()) return;
            String keyName = providerId + "_api_key";
            secretsManager.setSecret(keyName, apiKey.trim());
            requireActivity().getSharedPreferences("codeio_prefs", Context.MODE_PRIVATE)
                .edit()
                .putString("active_provider", providerId)
                .putString("active_model", selectedModel != null ? selectedModel : "")
                .apply();
            requireActivity().runOnUiThread(() -> {
                Toast.makeText(requireContext(), "✓ " + providerId + " API anahtarı kaydedildi!", Toast.LENGTH_SHORT).show();
                loadMarketData();
            });
        }

        @JavascriptInterface
        public void setActiveModel(String providerId, String model, String mode) {
            requireActivity().getSharedPreferences("codeio_prefs", Context.MODE_PRIVATE)
                .edit()
                .putString("active_provider", providerId)
                .putString("active_model", model)
                .putString("model_mode", mode != null ? mode : "normal")
                .apply();
            requireActivity().runOnUiThread(() ->
                Toast.makeText(requireContext(), "✓ Model: " + model + " (" + mode + ")", Toast.LENGTH_SHORT).show());
        }

        @JavascriptInterface
        public void openChat(String providerId, String model) {
            requireActivity().getSharedPreferences("codeio_prefs", Context.MODE_PRIVATE)
                .edit()
                .putString("active_provider", providerId)
                .putString("active_model", model)
                .apply();
            requireActivity().runOnUiThread(() -> {
                if (getActivity() instanceof MainActivity) {
                    ((MainActivity) getActivity()).navigateToChat();
                }
            });
        }

        @JavascriptInterface
        public void deleteApiKey(String providerId) {
            secretsManager.deleteSecret(providerId + "_api_key");
            requireActivity().runOnUiThread(() -> {
                Toast.makeText(requireContext(), "✓ " + providerId + " API anahtarı silindi", Toast.LENGTH_SHORT).show();
                loadMarketData();
            });
        }

        @JavascriptInterface
        public String getActiveProvider() {
            return requireActivity().getSharedPreferences("codeio_prefs", Context.MODE_PRIVATE)
                .getString("active_provider", "m-ai");
        }

        @JavascriptInterface
        public String getActiveModel() {
            return requireActivity().getSharedPreferences("codeio_prefs", Context.MODE_PRIVATE)
                .getString("active_model", "");
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (marketWebView != null) {
            marketWebView.stopLoading();
            marketWebView.clearHistory();
            marketWebView.destroy();
            marketWebView = null;
        }
    }
}
