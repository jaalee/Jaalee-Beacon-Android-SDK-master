package com.jaalee.sdk;

import android.os.Parcel;
import android.os.Parcelable;

import com.jaalee.sdk.internal.Objects;

/**
 * Immutable representations of single beacon.
 * Two beacons are considered equal if their proximity UUID, major and minor are equal.
 * This mimics CLBeacon from iOS.
 */
public class Beacon
        implements Parcelable {
    public static final Parcelable.Creator<Beacon> CREATOR = new Parcelable.Creator<Beacon>() {
        public Beacon createFromParcel(Parcel source) {
            return new Beacon(source);
        }

        public Beacon[] newArray(int size) {
            return new Beacon[size];
        }
    };
    private final String proximityUUID;
    private final String name;
    private final String macAddress;
    private final int major;
    private final int minor;
    private final int measuredPower;
    private final int rssi;
    private final int battLevel;

    public Beacon(String proximityUUID, String name, String macAddress, int major, int minor, int measuredPower, int rssi, int batt) {
        this.proximityUUID = Utils.normalizeProximityUUID(proximityUUID);
        this.name = name;
        this.macAddress = macAddress;
        this.major = major;
        this.minor = minor;
        this.measuredPower = measuredPower;
        this.rssi = rssi;
        this.battLevel = batt;
    }

    private Beacon(Parcel parcel) {
        this.proximityUUID = parcel.readString();
        this.name = parcel.readString();
        this.macAddress = parcel.readString();
        this.major = parcel.readInt();
        this.minor = parcel.readInt();
        this.measuredPower = parcel.readInt();
        this.rssi = parcel.readInt();
        this.battLevel = parcel.readInt();
    }

    public String getProximityUUID() {
        return this.proximityUUID;
    }

    public String getName() {
        return this.name;
    }

    public String getMacAddress() {
        return this.macAddress;
    }

    public int getMajor() {
        return this.major;
    }

    public int getMinor() {
        return this.minor;
    }

    public int getMeasuredPower() {
        return this.measuredPower;
    }

    public int getRssi() {
        return this.rssi;
    }

    public int getBattLevel() {
        return this.battLevel;
    }

    public String toString() {
        return Objects.toStringHelper(this).add("macAddress", this.macAddress).add("proximityUUID", this.proximityUUID).add("major", this.major).add("minor", this.minor).add("measuredPower", this.measuredPower)
                .add("rssi", this.rssi)
                .add("battLevel", this.battLevel)
                .toString();
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if ((o == null) || (getClass() != o.getClass())) return false;

        Beacon beacon = (Beacon) o;

        if (this.major != beacon.major) return false;
        if (this.minor != beacon.minor) return false;
        return this.proximityUUID.equals(beacon.proximityUUID);
    }

    public int hashCode() {
        int result = this.proximityUUID.hashCode();
        result = 31 * result + this.major;
        result = 31 * result + this.minor;
        return result;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.proximityUUID);
        dest.writeString(this.name);
        dest.writeString(this.macAddress);
        dest.writeInt(this.major);
        dest.writeInt(this.minor);
        dest.writeInt(this.measuredPower);
        dest.writeInt(this.rssi);
        dest.writeInt(this.battLevel);
    }
}
