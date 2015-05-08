package com.jaalee.sdk.service;

import android.os.Parcel;
import android.os.Parcelable;

import com.jaalee.sdk.internal.Objects;
/**
 * @author JAALEE, Inc
 * 
 * @Support dev@jaalee.com
 * @Sales: sales@jaalee.com
 * 
 * @see http://www.jaalee.com/
 */

public final class ScanPeriodData
  implements Parcelable
{
	public final long scanPeriodMillis;
	public final long waitTimeMillis;
	public static final Parcelable.Creator<ScanPeriodData> CREATOR = new Parcelable.Creator<ScanPeriodData>()
	{
		public ScanPeriodData createFromParcel(Parcel source) {
			long scanPeriodMillis = source.readLong();
			long waitTimeMillis = source.readLong();
			return new ScanPeriodData(scanPeriodMillis, waitTimeMillis);
		}

		public ScanPeriodData[] newArray(int size)
		{
			return new ScanPeriodData[size];
		}
	};

	public ScanPeriodData(long scanPeriodMillis, long waitTimeMillis)
	{
		this.scanPeriodMillis = scanPeriodMillis;
		this.waitTimeMillis = waitTimeMillis;
	}

	public boolean equals(Object o)
	{
		if (this == o) return true;
		if ((o == null) || (getClass() != o.getClass())) return false;

		ScanPeriodData that = (ScanPeriodData)o;

		if (this.scanPeriodMillis != that.scanPeriodMillis) return false;
		if (this.waitTimeMillis != that.waitTimeMillis) return false;

		return true;
	}

	public int hashCode()
	{
		int result = (int)(this.scanPeriodMillis ^ this.scanPeriodMillis >>> 32);
		result = 31 * result + (int)(this.waitTimeMillis ^ this.waitTimeMillis >>> 32);
		return result;
	}

	public String toString()
	{
		return Objects.toStringHelper(this).add("scanPeriodMillis", this.scanPeriodMillis).add("waitTimeMillis", this.waitTimeMillis).toString();
	}

	public int describeContents()
	{
		return 0;
	}

	public void writeToParcel(Parcel dest, int flags)
	{
		dest.writeLong(this.scanPeriodMillis);
		dest.writeLong(this.waitTimeMillis);
	}
}
