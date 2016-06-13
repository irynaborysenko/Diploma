package com.aurora.tcrypto.proto;

import android.util.Log;

import java.math.BigInteger;
import java.security.SecureRandom;

public class GenerateParam {

    public static void generateInitParameters(int N) {
        int keysize = 1024;
        Log.e("comehere","comehere");
        final Dealer d = new Dealer(keysize);
        d.generateKeys(N);
    }

    public static String generateSymKey() {
        SecureRandom random = new SecureRandom();
        int keySize = 104;
        BigInteger key = BigInteger.probablePrime(keySize, random);
        return key.toString();
    }



}
