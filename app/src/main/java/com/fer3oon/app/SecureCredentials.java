package com.fer3oon.app;

import android.content.Context;
import android.content.SharedPreferences;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.Base64;
import java.io.*;
import java.security.KeyStore;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;

public final class SecureCredentials {
    private static final String ALIAS = "fer3oon_credentials_v1";
    private static final String PREFS = "fer3oon_secure_store";
    private static final String DATA = "encrypted_credentials";
    private static final String IV = "encryption_iv";

    private SecureCredentials() {}

    private static SecretKey getKey() throws Exception {
        KeyStore store = KeyStore.getInstance("AndroidKeyStore");
        store.load(null);

        if (store.containsAlias(ALIAS)) {
            return ((KeyStore.SecretKeyEntry) store.getEntry(ALIAS, null)).getSecretKey();
        }

        KeyGenerator generator = KeyGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore"
        );

        generator.init(new KeyGenParameterSpec.Builder(
            ALIAS,
            KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setRandomizedEncryptionRequired(true)
            .build()
        );

        return generator.generateKey();
    }

    public static void save(Context context, String email, String apiKey,
                            String password) throws Exception {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        DataOutputStream output = new DataOutputStream(bytes);
        output.writeUTF(email);
        output.writeUTF(apiKey);
        output.writeUTF(password);
        output.close();

        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.ENCRYPT_MODE, getKey());
        byte[] encrypted = cipher.doFinal(bytes.toByteArray());

        SharedPreferences prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        boolean saved = prefs.edit()
            .putString(DATA, Base64.encodeToString(encrypted, Base64.NO_WRAP))
            .putString(IV, Base64.encodeToString(cipher.getIV(), Base64.NO_WRAP))
            .commit();

        if (!saved) throw new Exception("Encrypted save failed");
    }

    public static String[] load(Context context) throws Exception {
        SharedPreferences prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        String encryptedText = prefs.getString(DATA, null);
        String ivText = prefs.getString(IV, null);

        if (encryptedText == null || ivText == null) return null;

        byte[] encrypted = Base64.decode(encryptedText, Base64.NO_WRAP);
        byte[] iv = Base64.decode(ivText, Base64.NO_WRAP);

        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(
            Cipher.DECRYPT_MODE,
            getKey(),
            new GCMParameterSpec(128, iv)
        );

        DataInputStream input = new DataInputStream(
            new ByteArrayInputStream(cipher.doFinal(encrypted))
        );

        String[] result = {
            input.readUTF(),
            input.readUTF(),
            input.readUTF()
        };

        input.close();
        return result;
    }

    public static void clear(Context context) throws Exception {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .remove(DATA)
            .remove(IV)
            .commit();

        KeyStore store = KeyStore.getInstance("AndroidKeyStore");
        store.load(null);

        if (store.containsAlias(ALIAS)) {
            store.deleteEntry(ALIAS);
        }
    }
}
