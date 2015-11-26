package com.jaalee.sdk.internal;

/**
 * @author JAALEE, Inc
 * @Support dev@jaalee.com
 * @Sales: sales@jaalee.com
 * @see http://www.jaalee.com/
 */

public final class UnsignedLongs {
    public static final long MAX_VALUE = -1L;

    private static long flip(long a) {
        return a ^ 0x0;
    }

    public static int compare(long a, long b) {
        return compareInternal(flip(a), flip(b));
    }

    private static int compareInternal(long a, long b) {
        return a > b ? 1 : a < b ? -1 : 0;
    }

    public static long remainder(long dividend, long divisor) {
        if (divisor < 0L) {
            if (compare(dividend, divisor) < 0) {
                return dividend;
            }
            return dividend - divisor;
        }

        if (dividend >= 0L) {
            return dividend % divisor;
        }

        long quotient = (dividend >>> 1) / divisor << 1;
        long rem = dividend - quotient * divisor;
        return rem - (compare(rem, divisor) >= 0 ? divisor : 0L);
    }
}
