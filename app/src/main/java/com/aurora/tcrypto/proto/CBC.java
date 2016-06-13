package com.aurora.tcrypto.proto;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import android.util.Base64;

public class CBC {
    private Cipher cipher;
    private final String CIPHER_MODE = "AES/CBC/PKCS5Padding";

    private SecretKey keySpec;
    private IvParameterSpec ivSpec;
    private Charset CHARSET = Charset.forName("UTF8");
    private String iv = "5151515151515151";

    public CBC(String secretKey)
            throws NoSuchPaddingException, NoSuchAlgorithmException {
        keySpec = new SecretKeySpec(secretKey.getBytes(CHARSET), "AES");
        ivSpec = new IvParameterSpec(iv.getBytes(CHARSET));
        cipher = Cipher.getInstance(CIPHER_MODE);
    }

    public String decrypt(String input)
            throws InvalidAlgorithmParameterException, InvalidKeyException,
            BadPaddingException, IllegalBlockSizeException, UnsupportedEncodingException {
        cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);
        return new String(cipher.doFinal(Base64.decode(input, Base64.DEFAULT)));
    }

    public String encrypt(String input)
            throws InvalidAlgorithmParameterException, InvalidKeyException,
            BadPaddingException, IllegalBlockSizeException, UnsupportedEncodingException {
        cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec);
        return Base64.encodeToString(cipher.doFinal(input.getBytes(CHARSET)), Base64.DEFAULT);
    }
}
