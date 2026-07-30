package com.codeioPRO.app.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.Base64;
import android.util.Log;

import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;

/**
 * Encrypted key-value store using Android Keystore + AES-GCM.
 * Falls back to plain SharedPreferences on older devices.
 */
public class SecretsManager {

    private static final String TAG         = "SecretsManager";
    private static final String PREFS_NAME  = "codeio_secrets";
    private static final String KEY_ALIAS   = "CodeioPROSecretsKey";
    private static final String TRANSFORM   = "AES/GCM/NoPadding";
    private static final int    GCM_IV_LEN  = 12;
    private static final int    GCM_TAG_LEN = 128;

    private final SharedPreferences prefs;

    public SecretsManager(Context ctx) {
        prefs = ctx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        ensureKeyExists();
    }

    /** Store an encrypted value. */
    public void setSecret(String key, String value) {
        if (key == null || value == null) return;
        try {
            String encrypted = encrypt(value);
            prefs.edit().putString(sanitize(key), encrypted).apply();
        } catch (Exception e) {
            Log.w(TAG, "Encrypt failed, storing plain: " + e.getMessage());
            prefs.edit().putString(sanitize(key), value).apply();
        }
    }

    /** Retrieve and decrypt a value. Returns null if not found. */
    public String getSecret(String key) {
        String raw = prefs.getString(sanitize(key), null);
        if (raw == null) return null;
        try {
            return decrypt(raw);
        } catch (Exception e) {
            return raw; // might already be plain
        }
    }

    public boolean hasSecret(String key) {
        return prefs.contains(sanitize(key));
    }

    public void deleteSecret(String key) {
        prefs.edit().remove(sanitize(key)).apply();
    }

    public void clearAll() {
        prefs.edit().clear().apply();
    }

    /** Returns JSON object with key → masked value for each stored secret. */
    public String getAllSecretsJson() {
        try {
            JSONObject obj = new JSONObject();
            Map<String, ?> all = prefs.getAll();
            for (Map.Entry<String, ?> e : all.entrySet()) {
                obj.put(e.getKey(), "****");
            }
            return obj.toString();
        } catch (Exception e) {
            return "{}";
        }
    }

    // ── Crypto helpers ────────────────────────────────────────────────────────

    private void ensureKeyExists() {
        try {
            KeyStore ks = KeyStore.getInstance("AndroidKeyStore");
            ks.load(null);
            if (!ks.containsAlias(KEY_ALIAS)) {
                KeyGenerator kg = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore");
                kg.init(new KeyGenParameterSpec.Builder(KEY_ALIAS,
                    KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                    .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                    .setKeySize(256)
                    .build());
                kg.generateKey();
            }
        } catch (Exception e) {
            Log.w(TAG, "KeyStore init failed: " + e.getMessage());
        }
    }

    private String encrypt(String plaintext) throws Exception {
        SecretKey key = getKey();
        if (key == null) return plaintext;
        Cipher cipher = Cipher.getInstance(TRANSFORM);
        cipher.init(Cipher.ENCRYPT_MODE, key);
        byte[] iv         = cipher.getIV();
        byte[] cipherText = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));
        byte[] combined   = new byte[iv.length + cipherText.length];
        System.arraycopy(iv, 0, combined, 0, iv.length);
        System.arraycopy(cipherText, 0, combined, iv.length, cipherText.length);
        return Base64.encodeToString(combined, Base64.NO_WRAP);
    }

    private String decrypt(String b64) throws Exception {
        SecretKey key = getKey();
        if (key == null) return b64;
        byte[] combined  = Base64.decode(b64, Base64.NO_WRAP);
        byte[] iv         = new byte[GCM_IV_LEN];
        byte[] cipherText = new byte[combined.length - GCM_IV_LEN];
        System.arraycopy(combined, 0, iv, 0, GCM_IV_LEN);
        System.arraycopy(combined, GCM_IV_LEN, cipherText, 0, cipherText.length);
        Cipher cipher = Cipher.getInstance(TRANSFORM);
        cipher.init(Cipher.DECRYPT_MODE, key, new GCMParameterSpec(GCM_TAG_LEN, iv));
        return new String(cipher.doFinal(cipherText), StandardCharsets.UTF_8);
    }

    private SecretKey getKey() {
        try {
            KeyStore ks = KeyStore.getInstance("AndroidKeyStore");
            ks.load(null);
            return (SecretKey) ks.getKey(KEY_ALIAS, null);
        } catch (Exception e) {
            return null;
        }
    }

    private static String sanitize(String key) {
        return key.replaceAll("[^a-zA-Z0-9_\\-]", "_");
    }
}
