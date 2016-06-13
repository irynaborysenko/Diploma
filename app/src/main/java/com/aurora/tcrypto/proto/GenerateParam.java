package com.aurora.tcrypto.proto;

import android.util.Log;

public class GenerateParam {

    public static void generateInitParameters(int N) {
        int keysize = 1024;
        Log.e("comehere","comehere");
        final Dealer d = new Dealer(keysize);
        d.generateKeys(N);
    }

}
