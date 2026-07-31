package com.codeioPRO.app;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class DashboardFragment extends Fragment {

    private WebView dashboardWebView;

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inf, @Nullable ViewGroup container, @Nullable Bundle saved) {
        return inf.inflate(R.layout.fragment_dashboard, container, false);
    }

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle saved) {
        super.onViewCreated(view, saved);
        dashboardWebView = view.findViewById(R.id.dashboardWebView);

        WebSettings ws = dashboardWebView.getSettings();
        ws.setJavaScriptEnabled(true);
        ws.setDomStorageEnabled(true);
        ws.setAllowFileAccess(true);
        ws.setAllowContentAccess(true);
        ws.setLoadWithOverviewMode(true);
        ws.setUseWideViewPort(true);

        dashboardWebView.setWebViewClient(new WebViewClient());
        dashboardWebView.addJavascriptInterface(this, "Android");
        dashboardWebView.loadUrl("file:///android_asset/dashboard.html");
    }

    @JavascriptInterface
    public void navigateToFiles() {
        requireActivity().runOnUiThread(() -> {
            if (getActivity() instanceof MainActivity)
                ((MainActivity) getActivity()).navigateToFiles();
        });
    }

    @JavascriptInterface
    public void navigateToChat() {
        requireActivity().runOnUiThread(() -> {
            if (getActivity() instanceof MainActivity)
                ((MainActivity) getActivity()).navigateToChat();
        });
    }

    @JavascriptInterface
    public void navigateToMarket() {
        requireActivity().runOnUiThread(() -> {
            if (getActivity() instanceof MainActivity)
                ((MainActivity) getActivity()).navigateToMarket();
        });
    }

    @JavascriptInterface
    public void navigateToAgents() {
        requireActivity().runOnUiThread(() -> {
            if (getActivity() instanceof MainActivity)
                ((MainActivity) getActivity()).navigateToAgents();
        });
    }

    @JavascriptInterface
    public void setModel(final String model) {
        requireActivity().runOnUiThread(() -> {
            SharedPreferences prefs = requireActivity()
                    .getSharedPreferences("codeio_prefs", Context.MODE_PRIVATE);
            prefs.edit().putString("active_model", model).apply();
        });
    }

    @JavascriptInterface
    public String getActiveModel() {
        SharedPreferences prefs = requireActivity()
                .getSharedPreferences("codeio_prefs", Context.MODE_PRIVATE);
        return prefs.getString("active_model", "gpt-4o-mini");
    }

    /** Gerçek zamanlı aktif agent sayısını döndürür. */
    @JavascriptInterface
    public int getAgentCount() {
        try {
            if (getActivity() instanceof MainActivity) {
                SubAgentFragment agentFragment = ((MainActivity) getActivity()).getAgentsFragment();
                if (agentFragment != null) {
                    return agentFragment.getActiveAgentCount();
                }
            }
        } catch (Exception e) { /* fragment henüz hazır değil */ }
        return 0;
    }
}
