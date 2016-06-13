package com.aurora.tcrypto.proto;

import java.math.BigInteger;

public class GroupKey {
    private int N, k;

    /**
     * exponent of the groupKeyPair
     */
    private BigInteger e;

    /**
     * modulus of the groupKeyPair
     */
    private BigInteger n;

    public GroupKey(final int k, final int N, final int keySize,
                    final BigInteger v, final BigInteger e, final BigInteger n) {
        this.k = k;
        this.N = N;
        this.e = e;
        this.n = n;
    }

    public BigInteger getModulus() {
        return this.n;
    }

    public BigInteger getExponent() {
        return this.e;
    }
}
