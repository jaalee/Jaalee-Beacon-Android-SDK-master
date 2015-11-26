package com.jaalee.sdk;

import java.util.List;

/**
 * Callback to be invoked when beacons are ranged.
 */
public abstract interface RangingListener {
    public abstract void onBeaconsDiscovered(Region paramRegion, List<Beacon> paramList);
}