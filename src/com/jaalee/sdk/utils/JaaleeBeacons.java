package com.jaalee.sdk.utils;

import com.jaalee.sdk.Beacon;

/**
 * @author JAALEE, Inc
 * @Support dev@jaalee.com
 * @Sales: sales@jaalee.com
 * @see http://www.jaalee.com/
 */

public class JaaleeBeacons {
    public static boolean isJaaleeBeacon(Beacon beacon) {
        return true;
//		return ("jaalee".equalsIgnoreCase(beacon.getName()) || "WWW.JAALEE.COM".equalsIgnoreCase(beacon.getName()) 
//				|| "EBEFD083-70A2-47C8-9837-E7B5634DF524".equalsIgnoreCase(beacon.getProximityUUID()));
    }
}
