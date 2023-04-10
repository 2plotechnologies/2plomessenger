package com.twoploapps.a2plomessenger;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.security.spec.KeySpec;

import android.util.Base64;
import android.util.Log;

import javax.crypto.Cipher;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

public class cifrado {
    private static final int KEY_SIZE = 128; // Tamaño de la clave en bits.
    private static final int IV_SIZE = 16; // Tamaño del vector de inicialización en bytes.
    private static final String ALGORITHM = "AES/CBC/PKCS5Padding"; // Algoritmo de cifrado.

    // Genera una clave aleatoria para cifrar los datos.
    public static SecretKeySpec generateKey() {
        byte[] keyBytes = new byte[0];
        try {
            String password = BuildConfig.CLAVE_CIFRADO;
            byte[] salt = new byte[16];
            SecureRandom random = new SecureRandom();
            random.nextBytes(salt);
            int iterations = 10000;
            int keyLength = 128;
            KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, iterations, keyLength);
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
            keyBytes = factory.generateSecret(spec).getEncoded();
        } catch (Exception ex) {
            Log.e("error", ex.getMessage());
        }
        return new SecretKeySpec(keyBytes, "AES");
    }
    public static String encrypt(String plainText, SecretKeySpec secretKey, byte[] iv) {
        byte[] cipherText = new byte[0];
        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, new IvParameterSpec(iv));
            cipherText = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
        } catch (Exception ex) {
            Log.e("error", ex.getMessage());
        }
        return Base64.encodeToString(cipherText, Base64.DEFAULT);
    }
    // Desencripta los datos utilizando una clave y un vector de inicialización.
    public static String decrypt(String cipherText, SecretKeySpec secretKey, byte[] iv) {
        byte[] plainText = new byte[0];
        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, new IvParameterSpec(iv));
            plainText = cipher.doFinal(Base64.decode(cipherText, Base64.DEFAULT));
        } catch (Exception ex) {
            Log.e("error", ex.getMessage());
        }
        return new String(plainText, StandardCharsets.UTF_8);
    }
}
