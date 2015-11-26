package com.jaalee.sdk;

/**
 * Interface used to notify called that Bluetooth stack has been restarted.
 */
public abstract interface RestartCompletedListener {
    public abstract void onRestartCompleted();
}