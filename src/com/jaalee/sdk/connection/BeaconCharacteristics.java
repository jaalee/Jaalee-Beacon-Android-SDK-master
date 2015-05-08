package com.jaalee.sdk.connection;

import com.jaalee.sdk.internal.Objects;

/**
 * Value object for beacon's characteristics (battery level, broadcasting power, advertising interval, software and hardware version)
 */
public class BeaconCharacteristics
{
	private final String BeaconUUID;
	private final int BeaconMajor;
	private final int BeaconMinor;
	private final int BeaconPowerValue;
	private final int BeaconBroadcastInterval;
	private final String BeaconName;
	private final int BeaconMfgr;
	private final int BeaconTxPower;
	private final int BeaconState;
	private final int BeaconAudioState;
	

	public BeaconCharacteristics(JaaleeService JLService, BeaconNameService Name, BeaconTxPower txPower, BeaconStateService state, BeaconAudioService audio)
	{
		this.BeaconUUID = JLService.getBeaconUUID().replaceAll(" ", "");
		this.BeaconMajor = JLService.getBeaconMajor();
		this.BeaconMinor = JLService.getBeaconMinor();
		this.BeaconPowerValue = JLService.getBeaconPower();
		this.BeaconBroadcastInterval = JLService.getBeaconBroadcastInterval();
		this.BeaconName = Name.getBeaconName().replaceAll(" ", "");
		this.BeaconMfgr = JLService.getBeaconMfgr();
		this.BeaconTxPower = txPower.getBeaconTxPower();
		this.BeaconState = state.getBeaconState();
		this.BeaconAudioState = audio.getBeaconAudioState();
	}
	
	public int getBeaconTxPower()
	{
		return this.BeaconTxPower;
	}
	
	public int getBeaconState()
	{
		return this.BeaconState;
	}
	
	public int getBeaconAudioState()
	{
		return this.BeaconAudioState;
	}
	
	public int getBeaconMfgr()
	{
		return this.BeaconMfgr;
	}

	public String getBeaconUUID() {
		return this.BeaconUUID;
	}
	public int getBroadcastingPower() {
		return this.BeaconPowerValue;
	}
	public int getBroadcastRate() {
		return this.BeaconBroadcastInterval;
	}
	public int getMajor() {
		return this.BeaconMajor;
	}
	public int getMinor() {
		return this.BeaconMinor;
	}
	public String getBeaconName()
	{
		return this.BeaconName;
	}

	public String toString() {
		return Objects.toStringHelper(this)
				.add("BeaconState", this.BeaconState)
				.add("BeaconUUID", this.BeaconUUID)
				.add("BeaconMajor", this.BeaconMajor)
				.add("BeaconMinor", this.BeaconMinor)
				.add("BeaconPowerValue", this.BeaconPowerValue)
				.add("BeaconBroadcast", this.BeaconBroadcastInterval)
				.add("BeaconMfgr", this.BeaconMfgr)
				.add("BeaconTxPower", this.BeaconTxPower)
				.add("BeaconAudioState", this.BeaconAudioState)
				.add("BeaconName", this.BeaconName).toString();
	}
}
