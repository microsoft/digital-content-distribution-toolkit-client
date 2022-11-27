package com.msr.bine_sdk.secure;

import android.content.Context;
import android.os.Build;
import android.security.KeyPairGeneratorSpec;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.Base64;
import android.util.Log;

import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.security.spec.AlgorithmParameterSpec;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.spec.GCMParameterSpec;

public class KeystoreManager {
    private static String TAG = KeystoreManager.class.getSimpleName();

    private static final String AndroidKeyStore = "AndroidKeyStore";
    private static final String KEY_ALIAS = "Bine-Keystore";
    private static final String AES_MODE = "AES/GCM/NoPadding";
    private static final int IV_LENGTH = 12;
    private static KeyStore keyStore;

    public static void init(Context context)  {
        try {
            keyStore = KeyStore.getInstance(AndroidKeyStore);
            keyStore.load(null);
            generateKey(context);
        }
        catch (Exception e) {
            Log.d(TAG, "Error creating keystore");
        }
    }

    private static void generateKey(Context context){
        try {
            if (!keyStore.containsAlias(KEY_ALIAS)) {
                KeyGenerator keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, AndroidKeyStore);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    keyGenerator.init(new KeyGenParameterSpec.Builder(KEY_ALIAS,
                                    KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                                    .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                                    .setRandomizedEncryptionRequired(false)
                                    .build());
                }
                else {
                    //TODO: Test on M
                    keyGenerator.init(
                            new KeyPairGeneratorSpec.Builder(context).setAlias(KEY_ALIAS).build());
                }
                keyGenerator.generateKey();
            }
        } catch (KeyStoreException | NoSuchAlgorithmException | NoSuchProviderException | InvalidAlgorithmParameterException e) {
            e.printStackTrace();
        }
    }

    private static java.security.Key getSecretKey() throws Exception {
        return keyStore.getKey(KEY_ALIAS, null);
    }

    public static String encrypt(String plainText) {
        try {
            Cipher c = Cipher.getInstance(AES_MODE);

            byte[] IV = new byte[IV_LENGTH];
            SecureRandom random = new SecureRandom();
            random.nextBytes(IV);
            c.init(Cipher.ENCRYPT_MODE, getSecretKey(),
                    new GCMParameterSpec(128, IV));

            byte[] encodedBytes = c.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
            return Base64.encodeToString(encodedBytes,Base64.NO_WRAP | Base64.URL_SAFE) +
                    ","+Base64.encodeToString(c.getIV(),Base64.NO_WRAP | Base64.URL_SAFE);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return plainText;
    }

    public static String decrypt(String encrypted) {
        try {

            String[] parts = encrypted.split(",");

            // Base64 decode of cipher text
            byte[] plainText = Base64.decode(parts[0], Base64.NO_WRAP | Base64.URL_SAFE);
            byte[] iv = Base64.decode(parts[1], Base64.NO_WRAP | Base64.URL_SAFE);
            Cipher c = Cipher.getInstance(AES_MODE);
            c.init(Cipher.DECRYPT_MODE, getSecretKey(),
                    new GCMParameterSpec(128, iv));

            byte[] bytes = c.doFinal(plainText);
            return new String(bytes, StandardCharsets.UTF_8);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
