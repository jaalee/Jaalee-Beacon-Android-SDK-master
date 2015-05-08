package com.jaalee.sdk.connection;

import java.util.UUID;
/**
 * @author JAALEE, Inc
 * 
 * @Support dev@jaalee.com
 * @Sales: sales@jaalee.com
 * 
 * @see http://www.jaalee.com/
 */

public class JaaleeDefine
{
	public static final int JAALEE_BEACON_PROXIMITY_STATE_UNKNOWN = 0;
	public static final int JAALEE_BEACON_PROXIMITY_STATE_IMMEDIATE = 1;
	public static final int JAALEE_BEACON_PROXIMITY_STATE_NEAR = 2;
	public static final int JAALEE_BEACON_PROXIMITY_STATE_FAR = 3;
	
	public static final int JAALEE_BEACON_STATE_ENABLE = 0;
	public static final int JAALEE_BEACON_STATE_DISABLE = 1;
	
	public static final int BEACON_AUDIO_STATE_ENABLE = 0;
	public static final int BEACON_AUDIO_STATE_ENABLE_WHEN_START = 1;
	public static final int BEACON_AUDIO_STATE_ENABLE_WHEN_TAP = 2;
	public static final int BEACON_AUDIO_STATE_DISABLE = 3;
	   
	public static final int JAALEE_TX_POWER_4_DBM = 0;
	public static final int JAALEE_TX_POWER_0_DBM = 1;
	public static final int JAALEE_TX_POWER_MINUS_4_DBM = 2;
	public static final int JAALEE_TX_POWER_MINUS_6_DBM = 3;
	public static final int JAALEE_TX_POWER_MINUS_8_DBM = 4;
	public static final int JAALEE_TX_POWER_MINUS_12_DBM = 5;
	public static final int JAALEE_TX_POWER_MINUS_16_DBM = 6;
	public static final int JAALEE_TX_POWER_MINUS_20_DBM = 7;
	public static final int JAALEE_TX_POWER_MINUS_23_DBM = 8;
	public static final int JAALEE_TX_POWER_MINUS_30_DBM = 9;
	public static final int JAALEE_TX_POWER_MINUS_40_DBM = 10;
}
