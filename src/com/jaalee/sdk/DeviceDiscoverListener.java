package com.jaalee.sdk;

/**
 * Callback to be invoked when beacons are ranged.
 */
public abstract interface DeviceDiscoverListener {
    public abstract void onBLEDeviceDiscovered(BLEDevice device);
}