package com.codeioPRO.app;

import android.annotation.SuppressLint;
import android.content.Context;
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

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class ShellFragment extends Fragment {

    private WebView terminalWebView;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private File workingDir;
    private Process currentProcess;
    private static final String TAG = "CodeioPRO.Shell";

    private final List<String> commandHistory = new ArrayList<>();

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inf, @Nullable ViewGroup c, @Nullable Bundle s) {
        return inf.inflate(R.layout.fragment_shell, c, false);
    }

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle s) {
        super.onViewCreated(view, s);
        terminalWebView = view.findViewById(R.id.terminal_webview);
        workingDir      = requireContext().getFilesDir();

        WebSettings ws = terminalWebView.getSettings();
        ws.setJavaScriptEnabled(true);
        ws.setDomStorageEnabled(true);
        ws.setAllowFileAccess(true);
        ws.setAllowContentAccess(true);

        terminalWebView.setWebViewClient(new WebViewClient() {
            @Override public void onPageFinished(WebView v, String url) { printWelcome(); }
        });
        terminalWebView.addJavascriptInterface(new TerminalBridge(), "Terminal");
        terminalWebView.loadUrl("file:///android_asset/terminal.html");
    }

    private void printWelcome() {
        String banner =
            "  ██████╗ ██████╗ ██████╗ ███████╗      ██╗ ██████╗     ██████╗ ██████╗  ██████╗\\n" +
            " ██╔════╝██╔═══██╗██╔══██╗██╔════╝     ██║██╔═══██╗   ██╔══██╗██╔══██╗██╔═══██╗\\n" +
            " ██║     ██║   ██║██║  ██║█████╗       ██║██║   ██║   ██████╔╝██████╔╝██║   ██║\\n" +
            " ██║     ██║   ██║██║  ██║██╔══╝  ██   ██║██║   ██║   ██╔═══╝ ██╔══██╗██║   ██║\\n" +
            " ╚██████╗╚██████╔╝██████╔╝███████╗╚█████╔╝╚██████╔╝   ██║     ██║  ██║╚██████╔╝\\n" +
            "  ╚═════╝ ╚═════╝ ╚═════╝ ╚══════╝ ╚════╝  ╚═════╝    ╚═╝     ╚═╝  ╚═╝ ╚═════╝";
        printToTerminal("\\033[1;36m" + banner + "\\033[0m", false);
        printToTerminal("\\033[1;32m Code-ioPRO Shell v1.1 — Geliştirici: Muhammed\\033[0m", false);
        printToTerminal("\\033[90m Tüm yazılım dillerini destekler • pip, npm, apt, git ve daha fazlası\\033[0m", false);
        printToTerminal("\\033[90m Yardım: 'help' yazın\\033[0m", false);
        printToTerminal("", false);
        printPrompt();
    }

    public void runCommand(String command) {
        if (command == null || command.trim().isEmpty()) return;
        mainHandler.post(() -> {
            printToTerminal("\\033[1;32m$ \\033[0m" + escapeForJs(command), false);
            executeCommand(command.trim());
        });
    }

    private void printToTerminal(String text, boolean isError) {
        if (terminalWebView == null) return;
        String escaped = escapeForJs(text);
        terminalWebView.evaluateJavascript(
            "if(window.termWrite)window.termWrite('" + escaped + "');", null);
    }

    private void printPrompt() {
        if (terminalWebView == null) return;
        String path = workingDir != null ? workingDir.getName() : "~";
        terminalWebView.evaluateJavascript(
            "if(window.termPrompt)window.termPrompt('" + escapeForJs(path) + "');", null);
    }

    private void executeCommand(String input) {
        if (input.isEmpty()) { printPrompt(); return; }
        commandHistory.add(0, input);
        if (commandHistory.size() > 200) commandHistory.remove(commandHistory.size() - 1);

        if (input.equals("clear") || input.equals("cls")) {
            if (terminalWebView != null)
                terminalWebView.evaluateJavascript("if(window.termClear)window.termClear();", null);
            printPrompt(); return;
        }
        if (input.equals("help"))   { printHelp(); printPrompt(); return; }
        if (input.startsWith("cd ")) { changeDirectory(input.substring(3).trim()); printPrompt(); return; }
        if (input.equals("pwd"))    { printToTerminal(workingDir != null ? workingDir.getAbsolutePath() : "/", false); printPrompt(); return; }
        if (input.equals("history")) {
            for (int i = Math.min(commandHistory.size()-1, 49); i >= 0; i--)
                printToTerminal("  " + (i+1) + "  " + commandHistory.get(commandHistory.size()-1-i), false);
            printPrompt(); return;
        }

        if (executor.isShutdown()) return;
        executor.execute(() -> {
            try {
                if (currentProcess != null) {
                    try { currentProcess.destroy(); } catch (Exception ignored) {}
                }

                List<String> args = buildCommand(input);
                ProcessBuilder pb = new ProcessBuilder(args);
                pb.directory(workingDir != null ? workingDir : requireContext().getFilesDir());
                pb.redirectErrorStream(true);

                // DÜZELTME: PATH sabit kodlanmıştı — artık Context'ten türetiliyoruz
                String appFiles = requireContext().getFilesDir().getAbsolutePath();
                pb.environment().put("TERM", "xterm-256color");
                pb.environment().put("HOME", appFiles);
                pb.environment().put("PATH", appFiles + "/usr/bin:/system/bin:/system/xbin");

                currentProcess = pb.start();
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(currentProcess.getInputStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        final String l = line;
                        mainHandler.post(() -> printToTerminal(l, false));
                    }
                }
                int exitCode = currentProcess.waitFor();
                if (exitCode != 0)
                    mainHandler.post(() -> printToTerminal("Çıkış kodu: " + exitCode, true));
                currentProcess = null;
            } catch (IOException | InterruptedException e) {
                final String err = e.getMessage();
                mainHandler.post(() -> printToTerminal("Hata: " + (err != null ? err : "Bilinmeyen"), true));
            } finally {
                mainHandler.post(this::printPrompt);
            }
        });
    }

    private List<String> buildCommand(String input) {
        return Arrays.asList("sh", "-c", input);
    }

    private void changeDirectory(String path) {
        File newDir;
        if (path.equals("~") || path.isEmpty()) {
            newDir = requireContext().getFilesDir();
        } else if (path.equals("..")) {
            newDir = workingDir != null && workingDir.getParentFile() != null
                ? workingDir.getParentFile() : workingDir;
        } else if (path.startsWith("/")) {
            newDir = new File(path);
        } else {
            newDir = new File(workingDir, path);
        }
        if (newDir != null && newDir.exists() && newDir.isDirectory()) {
            workingDir = newDir;
        } else {
            printToTerminal("cd: " + path + ": Böyle bir dizin yok", true);
        }
    }

    private void printHelp() {
        String[] lines = {
            "\\033[1;36m═══ Code-ioPRO Shell Yardım ═══\\033[0m",
            "",
            "\\033[1;33mTemel Komutlar:\\033[0m",
            "  ls, ll          — Dosyaları listele",
            "  cd <dizin>      — Dizin değiştir",
            "  pwd             — Geçerli dizini göster",
            "  cat <dosya>     — Dosya içeriğini göster",
            "  mkdir <ad>      — Klasör oluştur",
            "  rm <dosya>      — Dosya sil",
            "  cp, mv          — Kopyala / Taşı",
            "  grep, find      — Arama",
            "  clear           — Ekranı temizle",
            "",
            "\\033[1;33mProgramlama:\\033[0m",
            "  python3 <dosya> — Python çalıştır",
            "  node <dosya>    — JavaScript çalıştır",
            "  pip install     — Python paketi kur",
            "  npm install     — Node paketi kur",
            "  git <komut>     — Git işlemleri",
            "",
            "\\033[1;33mDiğer:\\033[0m",
            "  history         — Komut geçmişi",
            "  help            — Bu yardım",
        };
        for (String l : lines) printToTerminal(l, false);
    }

    private String escapeForJs(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("'", "\\'")
                .replace("\n", "\\n").replace("\r", "").replace("\"", "\\\"");
    }

    public void stopCurrentProcess() {
        if (currentProcess != null) {
            currentProcess.destroy();
            currentProcess = null;
            printToTerminal("^C", false);
            printPrompt();
        }
    }

    // ── JS Köprüsü ────────────────────────────────────────────────────────────
    private class TerminalBridge {
        @JavascriptInterface public void   onCommand(String cmd)    { mainHandler.post(() -> executeCommand(cmd.trim())); }
        @JavascriptInterface public void   onCtrlC()                { mainHandler.post(() -> stopCurrentProcess()); }
        @JavascriptInterface public String getHistory(int index)    { return (index >= 0 && index < commandHistory.size()) ? commandHistory.get(index) : ""; }
        @JavascriptInterface public int    getHistorySize()         { return commandHistory.size(); }
        @JavascriptInterface public void   onReady()                { mainHandler.post(() -> printWelcome()); }
    }

    // ── Lifecycle ─────────────────────────────────────────────────────────────
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (terminalWebView != null) {
            terminalWebView.stopLoading();
            terminalWebView.clearHistory();
            terminalWebView.destroy();
            terminalWebView = null;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        executor.shutdownNow();
        try { executor.awaitTermination(500, TimeUnit.MILLISECONDS); }
        catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        if (currentProcess != null) currentProcess.destroy();
    }
}
