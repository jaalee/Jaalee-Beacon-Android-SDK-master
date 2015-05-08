 package com.jaalee.sdk;
 
 import android.os.Parcel;
 import android.os.Parcelable;

import com.jaalee.sdk.internal.Objects;
import com.jaalee.sdk.internal.Preconditions;

/**
 * Defines a region based on device's proximity to a beacon. Region is defined by proximity UUID, major and minor numbers of targeting beacons. All of those can be nulls which means wildcard on that field.

Region's identifier is introduced similarly to CLBeaconRegion's identifier to uniquely identify region. You can use this identifier in the app to differentiate regions.

This mimics CLBeaconRegion from iOS.
 *
 */
 public class Region
   implements Parcelable
 {
   private final String identifier;
   private final String proximityUUID;
   private final Integer major;
   private final Integer minor;
   public static final Parcelable.Creator<Region> CREATOR = new Parcelable.Creator<Region>()
   {
	   public Region createFromParcel(Parcel source) {
		   return new Region(source);
	   }
 
	   public Region[] newArray(int size)
	   {
		   return new Region[size];
	   }
   };
 
   /**
    * 
    * @param identifier A unique identifier for a region. Cannot be null.
    * @param proximityUUID Proximity UUID of beacons. Can be null. Null indicates all proximity UUIDs.
    * @param major Major version of the beacons. Can be null. Null indicates all major versions.
    * @param minor Minor version of the beacons. Can be null. Null indicates all minor versions.
    */
   public Region(String identifier, String proximityUUID, Integer major, Integer minor)
   {
	   this.identifier = ((String)Preconditions.checkNotNull(identifier));
	   this.proximityUUID = (proximityUUID != null ? Utils.normalizeProximityUUID(proximityUUID) : proximityUUID);
	   this.major = major;
	   this.minor = minor;
   }
   
   /**
    * 
    * @return Region's unique identifier. Cannot be null.
    */ 
   public String getIdentifier()
   {
	   return this.identifier;
   }
 
   /**
    * 
    * @return Proximity UUID of targeting beacons.
    */
   public String getProximityUUID()
   {
	   return this.proximityUUID;
   }
 
  /**
   * 
   * @return Major version of targeting beacons.
   */
   public Integer getMajor()
   {
	   return this.major;
   }
 
   /**
    * 
    * @return Minor version of targeting beacons.
    */
   public Integer getMinor()
   {
	   return this.minor;
   }
 
   public String toString()
   {
	   return Objects.toStringHelper(this).add("identifier", this.identifier).add("proximityUUID", this.proximityUUID).add("major", this.major).add("minor", this.minor).toString();
   }
 
   public boolean equals(Object o)
   {
	   if (this == o) return true;
	   if ((o == null) || (getClass() != o.getClass())) return false;
 
	   Region region = (Region)o;
 
	   if (this.major != null ? !this.major.equals(region.major) : region.major != null) return false;
	   if (this.minor != null ? !this.minor.equals(region.minor) : region.minor != null) return false;
	   if (this.proximityUUID != null ? !this.proximityUUID.equals(region.proximityUUID) : region.proximityUUID != null) {
		   return false;
	   }
	   return true;
   }
 
   public int hashCode()
   {
	   int result = this.proximityUUID != null ? this.proximityUUID.hashCode() : 0;
	   result = 31 * result + (this.major != null ? this.major.hashCode() : 0);
	   result = 31 * result + (this.minor != null ? this.minor.hashCode() : 0);
	   return result;
   }
 
   private Region(Parcel parcel)
   {
	   this.identifier = parcel.readString();
	   this.proximityUUID = parcel.readString();
	   Integer majorTemp = Integer.valueOf(parcel.readInt());
	   if (majorTemp.intValue() == -1) {
		   majorTemp = null;
	   }
	   this.major = majorTemp;
	   Integer minorTemp = Integer.valueOf(parcel.readInt());
	   if (minorTemp.intValue() == -1) {
		   minorTemp = null;
	   }
	   this.minor = minorTemp;
   }
 
   public int describeContents()
   {
	   return 0;
   }
 
   public void writeToParcel(Parcel dest, int flags)
   {
	   dest.writeString(this.identifier);
	   dest.writeString(this.proximityUUID);
	   dest.writeInt(this.major == null ? -1 : this.major.intValue());
	   dest.writeInt(this.minor == null ? -1 : this.minor.intValue());
   }
 
   public static enum State
   {
	   INSIDE, 
	   OUTSIDE;
   }
 }
