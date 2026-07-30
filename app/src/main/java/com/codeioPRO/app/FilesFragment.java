package com.codeioPRO.app;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Stack;

public class FilesFragment extends Fragment {

    private ListView listView;
    private TextView tvCurrentPath, tvEmptyHint;
    private File currentDir;
    private FileAdapter adapter;
    private final Stack<File> backStack = new Stack<>();
    private static final File ROOT_DIR = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "Code-ioPRO");

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inf, @Nullable ViewGroup c, @Nullable Bundle s) {
        return inf.inflate(R.layout.fragment_files, c, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle s) {
        super.onViewCreated(view, s);
        listView     = view.findViewById(R.id.files_list);
        tvCurrentPath = view.findViewById(R.id.tv_current_path);
        tvEmptyHint  = view.findViewById(R.id.tv_empty_hint);

        if (!ROOT_DIR.exists()) ROOT_DIR.mkdirs();
        currentDir = ROOT_DIR;

        view.findViewById(R.id.btn_new_file).setOnClickListener(v -> showNewFileDialog());
        view.findViewById(R.id.btn_new_folder).setOnClickListener(v -> showNewFolderDialog());
        view.findViewById(R.id.btn_back).setOnClickListener(v -> goUp());
        view.findViewById(R.id.btn_refresh).setOnClickListener(v -> refreshFiles());

        adapter = new FileAdapter(requireContext(), new ArrayList<>());
        listView.setAdapter(adapter);

        listView.setOnItemClickListener((parent, v2, pos, id) -> {
            File f = adapter.getItem(pos);
            if (f == null) return;
            if (f.isDirectory()) {
                backStack.push(currentDir);
                currentDir = f;
                refreshFiles();
            } else {
                openFile(f);
            }
        });

        listView.setOnItemLongClickListener((parent, v2, pos, id) -> {
            File f = adapter.getItem(pos);
            if (f != null) showFileMenu(v2, f);
            return true;
        });

        refreshFiles();
    }

    public void refreshFiles() {
        if (currentDir == null || !currentDir.exists()) currentDir = ROOT_DIR;
        tvCurrentPath.setText(getRelativePath(currentDir));
        adapter.clear();

        File[] files = currentDir.listFiles();
        if (files == null || files.length == 0) {
            tvEmptyHint.setVisibility(View.VISIBLE);
            return;
        }
        tvEmptyHint.setVisibility(View.GONE);
        Arrays.sort(files, (a, b) -> {
            if (a.isDirectory() != b.isDirectory()) return a.isDirectory() ? -1 : 1;
            return a.getName().compareToIgnoreCase(b.getName());
        });
        for (File f : files) adapter.add(f);
        adapter.notifyDataSetChanged();
    }

    public void addFile(File f) {
        if (f != null && f.exists()) {
            // If file is in our dir, refresh
            if (f.getParentFile() != null && f.getParentFile().getAbsolutePath().startsWith(ROOT_DIR.getAbsolutePath())) {
                refreshFiles();
            }
        }
    }

    private void goUp() {
        if (!backStack.isEmpty()) {
            currentDir = backStack.pop();
            refreshFiles();
        } else if (!currentDir.equals(ROOT_DIR)) {
            currentDir = ROOT_DIR;
            refreshFiles();
        } else {
            Toast.makeText(requireContext(), "Kök dizindesiniz", Toast.LENGTH_SHORT).show();
        }
    }

    private void showNewFileDialog() {
        EditText et = new EditText(requireContext());
        et.setHint("Dosya adı (örn. main.py)");
        new AlertDialog.Builder(requireContext())
            .setTitle("Yeni Dosya")
            .setView(et)
            .setPositiveButton("Oluştur", (d, w) -> {
                String name = et.getText().toString().trim();
                if (name.isEmpty()) return;
                File f = new File(currentDir, name);
                try { if (f.createNewFile()) { refreshFiles(); openEditor(f); }
                      else Toast.makeText(requireContext(), "Dosya zaten mevcut", Toast.LENGTH_SHORT).show();
                } catch (IOException e) { Toast.makeText(requireContext(), "Hata: " + e.getMessage(), Toast.LENGTH_SHORT).show(); }
            })
            .setNegativeButton("İptal", null)
            .show();
    }

    private void showNewFolderDialog() {
        EditText et = new EditText(requireContext());
        et.setHint("Klasör adı");
        new AlertDialog.Builder(requireContext())
            .setTitle("Yeni Klasör")
            .setView(et)
            .setPositiveButton("Oluştur", (d, w) -> {
                String name = et.getText().toString().trim();
                if (name.isEmpty()) return;
                File f = new File(currentDir, name);
                if (f.mkdir()) refreshFiles();
                else Toast.makeText(requireContext(), "Klasör oluşturulamadı", Toast.LENGTH_SHORT).show();
            })
            .setNegativeButton("İptal", null)
            .show();
    }

    private void showFileMenu(View anchor, File f) {
        PopupMenu menu = new PopupMenu(requireContext(), anchor);
        menu.getMenu().add(0, 1, 0, "✏️ Düzenle");
        menu.getMenu().add(0, 2, 0, "⬇️ İndir / Paylaş");
        menu.getMenu().add(0, 3, 0, "▶️ Shell'de Çalıştır");
        menu.getMenu().add(0, 4, 0, "📋 Yeniden Adlandır");
        menu.getMenu().add(0, 5, 0, "🗑️ Sil");
        menu.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                case 1: openEditor(f); break;
                case 2: shareFile(f); break;
                case 3: runInShell(f); break;
                case 4: renameFile(f); break;
                case 5: deleteFile(f); break;
            }
            return true;
        });
        menu.show();
    }

    private void openFile(File f) {
        String name = f.getName().toLowerCase();
        // Text/code files open in editor
        if (isTextFile(name)) { openEditor(f); return; }
        // Images open in viewer
        if (name.endsWith(".png") || name.endsWith(".jpg") || name.endsWith(".jpeg") || name.endsWith(".gif") || name.endsWith(".webp")) {
            Intent i = new Intent(Intent.ACTION_VIEW);
            Uri uri = FileProvider.getUriForFile(requireContext(), "com.codeioPRO.app.fileprovider", f);
            i.setDataAndType(uri, "image/*");
            i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(i);
            return;
        }
        // PDF
        if (name.endsWith(".pdf")) {
            Intent i = new Intent(Intent.ACTION_VIEW);
            Uri uri = FileProvider.getUriForFile(requireContext(), "com.codeioPRO.app.fileprovider", f);
            i.setDataAndType(uri, "application/pdf");
            i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            try { startActivity(i); } catch (Exception e) { Toast.makeText(requireContext(), "PDF görüntüleyici bulunamadı", Toast.LENGTH_SHORT).show(); }
            return;
        }
        // Default: share
        showFileMenu(listView, f);
    }

    private void openEditor(File f) {
        EditorActivity.open(requireContext(), f.getAbsolutePath());
    }

    private void shareFile(File f) {
        Uri uri = FileProvider.getUriForFile(requireContext(), "com.codeioPRO.app.fileprovider", f);
        Intent i = new Intent(Intent.ACTION_SEND);
        i.setType("*/*");
        i.putExtra(Intent.EXTRA_STREAM, uri);
        i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivity(Intent.createChooser(i, "Dosyayı Paylaş: " + f.getName()));
    }

    private void runInShell(File f) {
        if (!(getActivity() instanceof MainActivity)) return;
        String cmd = buildRunCommand(f);
        ((MainActivity) getActivity()).navigateToShell(cmd);
    }

    private String buildRunCommand(File f) {
        String path = "\"" + f.getAbsolutePath() + "\"";
        String name = f.getName().toLowerCase();
        if (name.endsWith(".py"))   return "python3 " + path;
        if (name.endsWith(".js"))   return "node " + path;
        if (name.endsWith(".sh"))   return "bash " + path;
        if (name.endsWith(".rb"))   return "ruby " + path;
        if (name.endsWith(".php"))  return "php " + path;
        if (name.endsWith(".java")) return "javac " + path + " && java " + f.getName().replace(".java","");
        return "cat " + path;
    }

    private void renameFile(File f) {
        EditText et = new EditText(requireContext());
        et.setText(f.getName());
        et.selectAll();
        new AlertDialog.Builder(requireContext())
            .setTitle("Yeniden Adlandır")
            .setView(et)
            .setPositiveButton("Kaydet", (d, w) -> {
                String newName = et.getText().toString().trim();
                if (newName.isEmpty() || newName.equals(f.getName())) return;
                File newFile = new File(f.getParentFile(), newName);
                if (f.renameTo(newFile)) { refreshFiles(); Toast.makeText(requireContext(), "✓ Yeniden adlandırıldı", Toast.LENGTH_SHORT).show(); }
                else Toast.makeText(requireContext(), "Yeniden adlandırma başarısız", Toast.LENGTH_SHORT).show();
            })
            .setNegativeButton("İptal", null)
            .show();
    }

    private void deleteFile(File f) {
        new AlertDialog.Builder(requireContext())
            .setTitle("Sil")
            .setMessage("\"" + f.getName() + "\" silinsin mi?")
            .setPositiveButton("Sil", (d, w) -> {
                if (deleteRecursive(f)) { refreshFiles(); Toast.makeText(requireContext(), "✓ Silindi", Toast.LENGTH_SHORT).show(); }
                else Toast.makeText(requireContext(), "Silme başarısız", Toast.LENGTH_SHORT).show();
            })
            .setNegativeButton("İptal", null)
            .show();
    }

    private boolean deleteRecursive(File f) {
        if (f.isDirectory()) { File[] ch = f.listFiles(); if (ch != null) for (File c : ch) deleteRecursive(c); }
        return f.delete();
    }

    private boolean isTextFile(String name) {
        String[] exts = {".txt",".py",".js",".ts",".java",".kt",".html",".css",".json",".xml",
            ".yaml",".yml",".sh",".bash",".md",".gradle",".go",".rs",".cpp",".c",".h",".php",
            ".rb",".swift",".r",".sql",".toml",".ini",".cfg",".conf",".env",".gitignore",
            ".dart",".cs",".scala",".pl",".lua",".asm"};
        for (String e : exts) if (name.endsWith(e)) return true;
        return false;
    }

    private String getRelativePath(File f) {
        String abs = f.getAbsolutePath();
        String root = ROOT_DIR.getAbsolutePath();
        if (abs.startsWith(root)) return "Code-ioPRO" + abs.substring(root.length());
        return abs;
    }

    private static String formatSize(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return (bytes / 1024) + " KB";
        return (bytes / (1024 * 1024)) + " MB";
    }

    // ── Adapter ─────────────────────────────────────────────────────────────
    private class FileAdapter extends ArrayAdapter<File> {
        FileAdapter(Context c, List<File> files) { super(c, 0, files); }

        @NonNull @Override
        public View getView(int pos, @Nullable View cv, @NonNull ViewGroup parent) {
            if (cv == null) cv = LayoutInflater.from(getContext()).inflate(R.layout.item_file, parent, false);
            File f = getItem(pos);
            if (f == null) return cv;
            TextView tvName = cv.findViewById(R.id.tv_file_name);
            TextView tvMeta = cv.findViewById(R.id.tv_file_meta);
            ImageView ivIcon = cv.findViewById(R.id.iv_file_icon);

            tvName.setText(f.getName());
            if (f.isDirectory()) {
                ivIcon.setImageResource(R.drawable.ic_folder);
                tvMeta.setText("Klasör");
            } else {
                ivIcon.setImageResource(getFileIcon(f.getName()));
                String meta = formatSize(f.length()) + "  •  " +
                    new SimpleDateFormat("dd MMM yy", Locale.getDefault()).format(new Date(f.lastModified()));
                tvMeta.setText(meta);
            }
            return cv;
        }

        private int getFileIcon(String name) {
            String n = name.toLowerCase();
            if (n.endsWith(".py"))   return R.drawable.ic_file_python;
            if (n.endsWith(".js") || n.endsWith(".ts")) return R.drawable.ic_file_js;
            if (n.endsWith(".java") || n.endsWith(".kt")) return R.drawable.ic_file_java;
            if (n.endsWith(".html") || n.endsWith(".css")) return R.drawable.ic_file_web;
            if (n.endsWith(".json") || n.endsWith(".xml")) return R.drawable.ic_file_data;
            if (n.endsWith(".md"))   return R.drawable.ic_file_md;
            if (n.endsWith(".sh") || n.endsWith(".bash")) return R.drawable.ic_file_shell;
            if (n.endsWith(".png") || n.endsWith(".jpg") || n.endsWith(".gif") || n.endsWith(".webp")) return R.drawable.ic_file_image;
            if (n.endsWith(".pdf"))  return R.drawable.ic_file_pdf;
            if (n.endsWith(".zip") || n.endsWith(".jar")) return R.drawable.ic_file_zip;
            if (n.endsWith(".apk"))  return R.drawable.ic_file_apk;
            return R.drawable.ic_file_generic;
        }
    }
}
