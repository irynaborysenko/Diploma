package com.aurora.tcrypto.proto;

import android.util.Log;

import java.math.BigInteger;

public class Dealer {

    private int keySize;

    private KeyShare[] shares = null;

    /**
     * Group Key
     */
    private GroupKey gk;
    /**
     * Group Verifier
     */

    private BigInteger vk = null;

    /**
     * Previously established key indicator
     */
    private boolean keyInit;

    /**
     * Randomly generated polynomial
     */
    private Poly poly;

    public Dealer(final int keySize) {
        this.keySize = keySize;
        this.keyInit = false;
    }


    private final static boolean DEBUG = true;

    private static void debug(final String s) {
        System.err.println("Dealer: " + s);
    }

    /**
     * Generates keys for a (k,N) threshold signatures scheme
     */

    public void generateKeys(final int N) {

        final int k = (int) (N / 2) + 1;
        BigInteger pr, qr, p, q, d, e, m, n;
        BigInteger groupSize;
        n = m = pr = qr = null;

        p = BigInteger.probablePrime(keySize, BigConst.getRandom());
        q = BigInteger.probablePrime(keySize, BigConst.getRandom());

        pr = (p.subtract(BigConst.ONE)).divide(BigConst.TWO);
        qr = (q.subtract(BigConst.ONE)).divide(BigConst.TWO);

        m = pr.multiply(qr);
        n = p.multiply(q);

        groupSize = BigInteger.valueOf(N);
        e = new BigInteger(groupSize.bitLength() + 1, 80, BigConst.getRandom());


//        BigInteger phi = (p.subtract(BigInteger.ONE)).multiply(q.subtract(BigInteger.ONE));
//        d = e.modInverse(phi);
        d = e.modInverse(m);

//        Log.e("RSA keys","private key");
//        debug("Key: " + e);
        Log.e("RSA keys","public key");
        debug("Key: " + d);
        shares = this.generateKeyShares(d, m, k, N, n);

        vk = this.generateVerifiers(n, shares);

        this.gk = new GroupKey(k, N, keySize, vk, e, n);
        this.keyInit = true;
    }

    public GroupKey getGroupKey() throws ThresholdSigException {
        checkKeyInit();
        return this.gk;
    }

    public KeyShare[] getShares() throws ThresholdSigException {
        checkKeyInit();
        return shares;
    }

    private void checkKeyInit() throws ThresholdSigException {
        if (keyInit == false) {
            if (DEBUG)
                debug("Key pair has not been initialized by generateKeys()");
            throw new ThresholdSigException(
                    "Key pair has not been initialized by generateKeys()");
        }
    }

    /**
     * Generates secret shares for a (k,N) threshold signatures scheme
     */

    private KeyShare[] generateKeyShares(final BigInteger d, final BigInteger m,
                                         final int k, final int N, final BigInteger n) {
        BigInteger[] secrets;
        BigInteger rand;
        int randBits;

        this.poly = new Poly(d, k - 1, m);
        secrets = new BigInteger[N];
        randBits = n.bitLength() + BigConst.L1 - m.bitLength();
        Log.e("secretShares ", "are: ");
        // Generates the valies f(i) for 1<=i<=l and add some large multiple of m to each value
        for (int i = 0; i < N; i++) {
            secrets[i] = poly.eval(i + 1);
            rand = (new BigInteger(randBits, BigConst.getRandom())).multiply(m);
            secrets[i] = secrets[i].add(rand);

            debug("secrets: " + secrets[i]);
        }

        final BigInteger delta = Dealer.factorial(N);

        final KeyShare[] s = new KeyShare[N];
        for (int i = 0; i < N; i++)
            s[i] = new KeyShare(i + 1, secrets[i], n, delta);

        return s;
    }

    /**
     * Creates verifiers for secret shares to prevent corrupting shares
     * Computes v[i] = v^^s[i] mod n
     */

    private BigInteger generateVerifiers(final BigInteger n, final KeyShare[] secrets) {

        BigInteger rand = null;

        Log.e("verifications keys", "are: ");
        for (final KeyShare element : secrets) {
            // rand is an element of Q*n (squares of relative primes mod n)
            while (true) {
                rand = new BigInteger(n.bitLength(), BigConst.getRandom());
                final BigInteger d = rand.gcd(n);
                if (d.compareTo(BigConst.ONE) == 0)
                    break;
                // if d was not relatively prime(hope not)
                debug("Verifier was not relatively prime");
            }

            rand = rand.multiply(rand).mod(n);
            debug("key: " + rand);
            element.setVerifiers(rand.modPow(element.getSecret(), n), rand);
        }

        return rand;
    }

    private static BigInteger factorial(final int l) {
        BigInteger x = BigInteger.valueOf(1l);
        for (int i = 1; i <= l; i++)
            x = x.multiply(BigInteger.valueOf(i));

        return x;
    }

}
