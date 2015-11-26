package com.jaalee.sdk;

/**
 * Callback to be invoked when any error happened while performing low energy scanning.
 */
public abstract interface ErrorListener {
    public abstract void onError(Integer paramInteger);
}