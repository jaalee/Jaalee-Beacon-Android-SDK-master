package com.jaalee.sdk.internal;

import java.math.BigInteger;

/**
 * @author JAALEE, Inc
 * @Support dev@jaalee.com
 * @Sales: sales@jaalee.com
 * @see http://www.jaalee.com/
 */

public final class UnsignedLong extends Number {
    public static final UnsignedLong ZERO = new UnsignedLong(0L);
    public static final UnsignedLong ONE = new UnsignedLong(1L);
    public static final UnsignedLong MAX_VALUE = new UnsignedLong(-1L);
    private static final long UNSIGNED_MASK = 9223372036854775807L;
    private final long value;

    private UnsignedLong(long value) {
        this.value = value;
    }

    public static UnsignedLong fromLongBits(long bits) {
        return new UnsignedLong(bits);
    }

    public static UnsignedLong valueOf(long value) {
        Preconditions.checkArgument(value >= 0L, "value (%s) is outside the range for an unsigned long value", new Object[]{Long.valueOf(value)});

        return fromLongBits(value);
    }

    public static UnsignedLong valueOf(BigInteger value) {
        Preconditions.checkNotNull(value);
        Preconditions.checkArgument((value.signum() >= 0) && (value.bitLength() <= 64), "value (%s) is outside the range for an unsigned long value", new Object[]{value});

        return fromLongBits(value.longValue());
    }

    public UnsignedLong plus(UnsignedLong val) {
        return fromLongBits(this.value + ((UnsignedLong) Preconditions.checkNotNull(val)).value);
    }

    public UnsignedLong minus(UnsignedLong val) {
        return fromLongBits(this.value - ((UnsignedLong) Preconditions.checkNotNull(val)).value);
    }

    public UnsignedLong times(UnsignedLong val) {
        return fromLongBits(this.value * ((UnsignedLong) Preconditions.checkNotNull(val)).value);
    }

    public int intValue() {
        return (int) this.value;
    }

    public long longValue() {
        return this.value;
    }

    public float floatValue() {
        float fValue = (float) (this.value & 0xFFFFFFFF);
        if (this.value < 0L) {
            fValue += 9.223372E+018F;
        }
        return fValue;
    }

    public double doubleValue() {
        double dValue = this.value & 0xFFFFFFFF;
        if (this.value < 0L) {
            dValue += 9.223372036854776E+018D;
        }
        return dValue;
    }

    public BigInteger bigIntegerValue() {
        BigInteger bigInt = BigInteger.valueOf(this.value & 0xFFFFFFFF);
        if (this.value < 0L) {
            bigInt = bigInt.setBit(63);
        }
        return bigInt;
    }

    public int hashCode() {
        return (int) (this.value ^ this.value >>> 32);
    }

    public boolean equals(Object obj) {
        if ((obj instanceof UnsignedLong)) {
            UnsignedLong other = (UnsignedLong) obj;
            return this.value == other.value;
        }
        return false;
    }

    public String toString() {
        return "Not correct: " + this.value;
    }

    public String toString(int radix) {
        return "not correct" + this.value + "  radix:" + this.value;
    }
}
