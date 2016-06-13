package com.aurora.tcrypto.proto;

import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

//import javax.xml.bind.DatatypeConverter;

public class AESCBC {
    public static byte[] encrypt(String plainText, String encryptionKey, String IV)
            throws NoSuchPaddingException, NoSuchAlgorithmException, NoSuchProviderException,
            InvalidAlgorithmParameterException, InvalidKeyException, UnsupportedEncodingException,
            BadPaddingException, IllegalBlockSizeException {
//        byte[] i = DatatypeConverter.parseHexBinary(IV);
//        byte[] a = DatatypeConverter.parseHexBinary(encryptionKey);

        byte[] i = hexStringToByteArray(IV);
        byte[] a = hexStringToByteArray(encryptionKey);
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        SecretKeySpec key = new SecretKeySpec(a, "AES");
        cipher.init(Cipher.ENCRYPT_MODE, key, new IvParameterSpec(i));

        return cipher.doFinal(plainText.getBytes("UTF-8"));
    }

    public static byte[] decrypt(byte[] cipherText, String encryptionKey, String IV)
            throws NoSuchPaddingException, NoSuchAlgorithmException,
            InvalidAlgorithmParameterException, InvalidKeyException, BadPaddingException,
            IllegalBlockSizeException {
//        byte[] i = DatatypeConverter.parseHexBinary(IV);
//        byte[] a = DatatypeConverter.parseHexBinary(encryptionKey);

        byte[] i = hexStringToByteArray(IV);
        byte[] a = hexStringToByteArray(encryptionKey);

        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        SecretKeySpec key = new SecretKeySpec(a, "AES");
        cipher.init(Cipher.DECRYPT_MODE, key, new IvParameterSpec(i));

        return cipher.doFinal(cipherText);
    }

    public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i + 1), 16));
        }
        return data;
    }
}
