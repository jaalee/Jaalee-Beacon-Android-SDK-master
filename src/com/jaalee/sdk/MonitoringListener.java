package com.jaalee.sdk;

/**
 * Callback to be invoked when beacons are discovered in a region.
 */
public abstract interface MonitoringListener {
    public abstract void onEnteredRegion(Region paramRegion);

    public abstract void onExitedRegion(Region paramRegion);
}