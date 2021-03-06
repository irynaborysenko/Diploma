package com.aurora.tcrypto.proto;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Random;

public class KeyShare {

    /**
     * Secret key value
     */
    private BigInteger secret;

    private BigInteger verifier;

    private BigInteger groupVerifier;

    private BigInteger n;

    private final BigInteger delta;

    /**
     * The secret key value used to sign messages.
     */
    private BigInteger signVal;

    private int id;

    private static SecureRandom random;

    private MessageDigest md;

    static {
        final byte[] randSeed = new byte[20];
        (new Random()).nextBytes(randSeed);
        random = new SecureRandom(randSeed);
    }

    /**
     * Create a new share
     *
     * @param id     -
     *               the identifier of this share
     * @param secret -
     *               a secret value generated by a Dealer
     * @param n      -
     *               the modulo of the group public key
     * @param delta  -
     *               N! (group size factorial)
     */
    public KeyShare(final int id, final BigInteger secret, final BigInteger n,
                    final BigInteger delta) {
        this.id = id;
        this.secret = secret;
        this.verifier = null;
        this.n = n;
        this.delta = delta;
        this.signVal = BigConst.FOUR.multiply(delta).multiply(secret);
    }

    public int getId() {
        return id;
    }

    public BigInteger getSecret() {
        return secret;
    }

    public void setVerifiers(final BigInteger verifier, final BigInteger groupVerifier) {
        this.verifier = verifier;
        this.groupVerifier = groupVerifier;
    }

    public BigInteger getVerifier() {
        return verifier;
    }

    public BigInteger getSignVal() {
        return signVal;
    }

    @Override
    public String toString() {
        return "KeyShare[" + id + "]";
    }

    /**
     * Create a SigShare and a Verifier for byte[] b
     */
    public SigShare sign(final byte[] b) {
        final BigInteger x = (new BigInteger(b)).mod(n);

        final int randbits = n.bitLength() + 3 * BigConst.L1;

        // r \elt (0, 2^L(n)+3*l1)
        final BigInteger r = (new BigInteger(randbits, random));
        final BigInteger vprime = groupVerifier.modPow(r, n);
        final BigInteger xtilde = x.modPow(BigConst.FOUR.multiply(delta), n);
        final BigInteger xprime = xtilde.modPow(r, n);

        BigInteger c = null;
        BigInteger z = null;
        // Try to generate C and Z
        try {
            md = MessageDigest.getInstance("SHA");
            md.reset();

            // debug("v: " + groupVerifier.mod(n));
            md.update(groupVerifier.mod(n).toByteArray());

            // debug("xtilde: " + xtilde);
            md.update(xtilde.toByteArray());

            // debug("vi: " + verifier.mod(n));
            md.update(verifier.mod(n).toByteArray());

            // debug("xi^2: " + x.modPow(signVal,n).modPow(TWO,n));
            md.update(x.modPow(signVal, n).modPow(BigConst.TWO, n).toByteArray());

            // debug("v': "+ vprime);
            md.update(vprime.toByteArray());

            // debug("x': " + xprime);
            md.update(xprime.toByteArray());
            c = new BigInteger(md.digest()).mod(n);
            z = (c.multiply(secret)).add(r);
        } catch (final java.security.NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        final Verifier ver = new Verifier(z, c, verifier, groupVerifier);

        return new SigShare(this.id, x.modPow(signVal, n), ver);
    }

    private static void debug(final String s) {
        System.err.println("KeyShare: " + s);
    }
}
