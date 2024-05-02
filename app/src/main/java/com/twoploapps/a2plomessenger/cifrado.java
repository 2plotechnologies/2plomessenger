package com.twoploapps.a2plomessenger;

import android.util.Base64;

import java.nio.charset.StandardCharsets;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import timber.log.Timber;

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
            Timber.tag("Error").e(ex);
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
            Timber.tag("Error").e(ex);
        }
        return new String(decryptedData, StandardCharsets.UTF_8);
    }
}
