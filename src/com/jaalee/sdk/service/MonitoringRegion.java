package com.jaalee.sdk.service;

import android.os.Messenger;

import com.jaalee.sdk.Region;

/**
 * @author JAALEE, Inc
 * @Support dev@jaalee.com
 * @Sales: sales@jaalee.com
 * @see http://www.jaalee.com/
 */

final class MonitoringRegion {
    private static final int NOT_SEEN = -1;
    final Region region;
    final Messenger replyTo;
    private long lastSeenTimeMillis = -1L;
    private boolean wasInside;

    public MonitoringRegion(Region region, Messenger replyTo) {
        this.region = region;
        this.replyTo = replyTo;
    }

    public boolean markAsSeen(long currentTimeMillis) {
        this.lastSeenTimeMillis = currentTimeMillis;
        if (!this.wasInside) {
            this.wasInside = true;
            return true;
        }
        return false;
    }

    public boolean isInside(long currentTimeMillis) {
        return (this.lastSeenTimeMillis != -1L) && (currentTimeMillis - this.lastSeenTimeMillis < BeaconService.EXPIRATION_MILLIS);
    }

    public boolean didJustExit(long currentTimeMillis) {
        if ((this.wasInside) && (!isInside(currentTimeMillis))) {
            this.lastSeenTimeMillis = -1L;
            this.wasInside = false;
            return true;
        }
        return false;
    }
}
