package com.codeioPRO.app;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class EditorActivity extends AppCompatActivity {

    private static final String EXTRA_FILE_PATH = "file_path";
    private WebView editorWebView;
    private String filePath;
    private String currentContent = "";
    private boolean isModified = false;

    public static void open(Context ctx, String path) {
        Intent i = new Intent(ctx, EditorActivity.class);
        i.putExtra(EXTRA_FILE_PATH, path);
        ctx.startActivity(i);
    }

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        filePath = getIntent().getStringExtra(EXTRA_FILE_PATH);
        if (filePath == null) { finish(); return; }

        File f = new File(filePath);
        TextView tvFilename = findViewById(R.id.tv_editor_filename);
        if (tvFilename != null) tvFilename.setText(f.getName());

        editorWebView = findViewById(R.id.editor_webview);
        WebSettings ws = editorWebView.getSettings();
        ws.setJavaScriptEnabled(true);
        ws.setDomStorageEnabled(true);
        ws.setAllowFileAccess(true);
        ws.setAllowContentAccess(true);

        editorWebView.addJavascriptInterface(new EditorBridge(), "Editor");
        editorWebView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView v, String url) {
                loadFileContent();
            }
        });
        editorWebView.loadUrl("file:///android_asset/editor.html");

        // Toolbar buttons
        View back = findViewById(R.id.btn_editor_back);
        View save = findViewById(R.id.btn_editor_save);
        View run  = findViewById(R.id.btn_editor_run);
        View share = findViewById(R.id.btn_editor_share);

        if (back != null)  back.setOnClickListener(v -> onBackPressed());
        if (save != null)  save.setOnClickListener(v -> saveFile());
        if (run  != null)  run.setOnClickListener(v  -> runFile());
        if (share != null) share.setOnClickListener(v -> shareFile());
    }

    private void loadFileContent() {
        try {
            File f = new File(filePath);
            byte[] bytes = new byte[(int) f.length()];
            try (FileInputStream fis = new FileInputStream(f)) {
                fis.read(bytes);
            }
            currentContent = new String(bytes, StandardCharsets.UTF_8);
            String escaped = escapeForJs(currentContent);
            String lang    = detectLanguage(f.getName());
            editorWebView.evaluateJavascript(
                "if(window.editorLoad)window.editorLoad('" + escaped + "','" + lang + "');", null);
        } catch (IOException e) {
            Toast.makeText(this, "Dosya okunamadı: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void saveFile() {
        editorWebView.evaluateJavascript("if(window.editorGetContent)window.editorGetContent();", null);
    }

    private void runFile() {
        File f = new File(filePath);
        String name = f.getName().toLowerCase();
        String cmd;
        if (name.endsWith(".py"))  cmd = "python3 \"" + filePath + "\"";
        else if (name.endsWith(".js")) cmd = "node \"" + filePath + "\"";
        else if (name.endsWith(".sh")) cmd = "bash \"" + filePath + "\"";
        else if (name.endsWith(".rb")) cmd = "ruby \"" + filePath + "\"";
        else { Toast.makeText(this, "Bu dosya türü çalıştırılamaz", Toast.LENGTH_SHORT).show(); return; }
        Intent i = new Intent(this, MainActivity.class);
        i.putExtra("run_command", cmd);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        // Just finish and let shell run it
        if (getApplicationContext() instanceof MainActivity) {
            ((MainActivity) getApplicationContext()).navigateToShell(cmd);
        }
        finish();
    }

    private void shareFile() {
        File f = new File(filePath);
        Uri uri = FileProvider.getUriForFile(this, "com.codeioPRO.app.fileprovider", f);
        Intent i = new Intent(Intent.ACTION_SEND);
        i.setType("text/plain");
        i.putExtra(Intent.EXTRA_STREAM, uri);
        i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivity(Intent.createChooser(i, "Dosyayı Paylaş: " + f.getName()));
    }

    private String detectLanguage(String name) {
        String n = name.toLowerCase();
        if (n.endsWith(".py"))   return "python";
        if (n.endsWith(".js"))   return "javascript";
        if (n.endsWith(".ts"))   return "typescript";
        if (n.endsWith(".java")) return "java";
        if (n.endsWith(".kt"))   return "kotlin";
        if (n.endsWith(".html")) return "html";
        if (n.endsWith(".css"))  return "css";
        if (n.endsWith(".json")) return "json";
        if (n.endsWith(".xml"))  return "xml";
        if (n.endsWith(".yaml") || n.endsWith(".yml")) return "yaml";
        if (n.endsWith(".sh") || n.endsWith(".bash")) return "shell";
        if (n.endsWith(".md"))   return "markdown";
        if (n.endsWith(".go"))   return "go";
        if (n.endsWith(".rs"))   return "rust";
        if (n.endsWith(".cpp") || n.endsWith(".c") || n.endsWith(".h")) return "cpp";
        if (n.endsWith(".php"))  return "php";
        if (n.endsWith(".rb"))   return "ruby";
        return "plaintext";
    }

    private String escapeForJs(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\")
                .replace("'", "\\'")
                .replace("\n", "\\n")
                .replace("\r", "")
                .replace("\"", "\\\"");
    }

    private class EditorBridge {
        @JavascriptInterface
        public void onContentChange(String content) {
            currentContent = content;
            isModified = true;
        }

        @JavascriptInterface
        public void saveContent(String content) {
            currentContent = content;
            try {
                try (FileOutputStream fos = new FileOutputStream(filePath)) {
                    fos.write(content.getBytes(StandardCharsets.UTF_8));
                }
                isModified = false;
                runOnUiThread(() -> Toast.makeText(EditorActivity.this, "✓ Kaydedildi", Toast.LENGTH_SHORT).show());
                // Notify FilesFragment to refresh
                runOnUiThread(() -> {
                    if (EditorActivity.this.getParent() instanceof MainActivity) {
                        FilesFragment ff = ((MainActivity) EditorActivity.this.getParent()).getFilesFragment();
                        if (ff != null) ff.refreshFiles();
                    }
                });
            } catch (IOException e) {
                runOnUiThread(() -> Toast.makeText(EditorActivity.this, "Kaydetme hatası: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        }

        @JavascriptInterface
        public String getFilePath() { return filePath; }

        @JavascriptInterface
        public String getFileName() { return new File(filePath).getName(); }
    }

    @Override
    public void onBackPressed() {
        if (isModified) {
            new AlertDialog.Builder(this)
                .setTitle("Kaydedilmemiş Değişiklikler")
                .setMessage("Değişiklikler kaydedilsin mi?")
                .setPositiveButton("Kaydet", (d, w) -> { saveFile(); finish(); })
                .setNegativeButton("Kaydetme", (d, w) -> finish())
                .setNeutralButton("İptal", null)
                .show();
        } else {
            super.onBackPressed();
        }
    }
}
