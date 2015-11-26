package com.jaalee.sdk.utils;

import com.jaalee.sdk.internal.UnsignedInteger;
import com.jaalee.sdk.internal.UnsignedLong;
import com.jaalee.sdk.internal.UnsignedLongs;

import java.math.BigInteger;
import java.security.SecureRandom;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

/**
 * @author JAALEE, Inc
 * @Support dev@jaalee.com
 * @Sales: sales@jaalee.com
 * @see http://www.jaalee.com/
 */

public class AuthMath {
    static final byte[] FIXED_KEY = {-1, -118, -14, 7, 1, 54, 37, -62, -40, 16, 9, 127, 32, -45, 5, 15};
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final long G_BASE = 5L;
    private static final long P_MODULO = 4294967291L;

    public static long randomUnsignedInt() {
        return UnsignedInteger.fromIntBits(RANDOM.nextInt(2147483646)).longValue() + 1L;
    }

    public static int firstStepSecret(long aAuth) {
        return modExpWithBase(5L, aAuth, 4294967291L);
    }

    public static byte[] secondStepSecret(long aAuth, long bAuth, String macAddress) {
        byte[] sessionKey = sessionKey(aAuth, bAuth);
        byte[] macSecret = macAddressToMacSecret(macAddress);
        try {
            return aesDecrypt(sessionKey, aesEncrypt(FIXED_KEY, macSecret));
        } catch (Exception e) {
        }
        return null;
    }

    static int modExpWithBase(long base, long exp, long mod) {
        long result = 1L;

        for (; exp > 0L; exp /= 2L) {
            if ((exp & 1L) != 0L) {
                result = UnsignedLongs.remainder(result * base, mod);
            }
            base = UnsignedLongs.remainder(base * base, mod);
        }
        return (int) result;
    }

    static long modExpWithBaseAsLong(long base, long exp, long mod) {
        return UnsignedInteger.fromIntBits(modExpWithBase(base, exp, mod)).longValue();
    }

    static long hexStringToUnsignedLong(String hex) {
        return UnsignedLong.valueOf(new BigInteger(hex, 16)).longValue();
    }

    private static byte[] sessionKey(long aAuth, long bAuth) {
        int sessionInt = modExpWithBase(bAuth, aAuth, 4294967291L);

        byte s0 = (byte) sessionInt;
        byte s1 = (byte) (sessionInt >> 8);
        byte s2 = (byte) (sessionInt >> 16);
        byte s3 = (byte) (sessionInt >> 24);

        return new byte[]{s0, s1, s2, s3, s3, s2, s1, s0, s0, s3, s1, s2, s3, s0, s2, s1};
    }

    static byte[] macAddressToMacSecret(String macAddress) {
        long macAddressValue = hexStringToUnsignedLong(macAddress);

        byte b0 = (byte) (int) macAddressValue;
        byte b1 = (byte) (int) (macAddressValue >> 8);
        byte b2 = (byte) (int) (macAddressValue >> 16);
        byte b3 = (byte) (int) (macAddressValue >> 24);
        byte b4 = (byte) (int) (macAddressValue >> 32);
        byte b5 = (byte) (int) (macAddressValue >> 40);

        return new byte[]{b0, b1, b2, b3, b4, b5, b2, b1, b0, b5, b4, b3, b4, b2, b3, b1};
    }

    static byte[] aesEncrypt(byte[] key, byte[] text) throws Exception {
        SecretKeySpec secretKeySpec = new SecretKeySpec(key, "AES");
        Cipher cipher = Cipher.getInstance("AES/ECB/NoPadding");
        cipher.init(1, secretKeySpec);
        return cipher.doFinal(text);
    }

    static byte[] aesDecrypt(byte[] key, byte[] text) throws Exception {
        SecretKeySpec secretKeySpec = new SecretKeySpec(key, "AES");
        Cipher cipher = Cipher.getInstance("AES/ECB/NoPadding");
        cipher.init(2, secretKeySpec);
        return cipher.doFinal(text);
    }
}
