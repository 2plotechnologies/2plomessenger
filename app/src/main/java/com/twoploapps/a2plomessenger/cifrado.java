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
    private static final String AES = "AES";
    private static final String key = BuildConfig.CLAVE_CIFRADO;
    public static String encrypt(String mensajeTexto) {
        SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(), AES);
        byte[] encryptedData = new byte[0];
        try {
            Cipher cipher = Cipher.getInstance(AES);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            encryptedData = cipher.doFinal(mensajeTexto.getBytes(StandardCharsets.UTF_8));
        } catch (Exception ex) {
            Log.e("Error",ex.getMessage());
        }
        return Base64.encodeToString(encryptedData, Base64.DEFAULT);
    }
    public static String decrypt(String mensaje) {
        byte[] decryptedData = new byte[0];
        try {
            byte[] data = Base64.decode(mensaje, Base64.DEFAULT);
            SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(), AES);
            Cipher cipher = Cipher.getInstance(AES);
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            decryptedData = cipher.doFinal(data);
        } catch (Exception ex) {
            Log.e("Error", ex.getMessage());
        }
        return new String(decryptedData, StandardCharsets.UTF_8);
    }
}
