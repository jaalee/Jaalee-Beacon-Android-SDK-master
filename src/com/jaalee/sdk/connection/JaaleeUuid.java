package com.jaalee.sdk.connection;

import java.util.UUID;

/**
 * @author JAALEE, Inc
 * @Support dev@jaalee.com
 * @Sales: sales@jaalee.com
 * @see http://www.jaalee.com/
 */

public class JaaleeUuid {
    public static final UUID JAALEE_BEACON_SERVICE = UUID.fromString("0000fff0-0000-1000-8000-00805f9b34fb");
    public static final UUID BEACON_KEEP_CONNECT_CHAR = UUID.fromString("0000fff1-0000-1000-8000-00805f9b34fb");
    public static final UUID BEACON_UUID_CHAR = UUID.fromString("0000fff2-0000-1000-8000-00805f9b34fb");
    public static final UUID MAJOR_CHAR = UUID.fromString("0000fff3-0000-1000-8000-00805f9b34fb");
    public static final UUID MINOR_CHAR = UUID.fromString("0000fff4-0000-1000-8000-00805f9b34fb");
    public static final UUID POWER_CHAR = UUID.fromString("0000fff5-0000-1000-8000-00805f9b34fb");
    public static final UUID BEACON_FFF6 = UUID.fromString("0000fff6-0000-1000-8000-00805f9b34fb");
    public static final UUID BEACON_FFF7 = UUID.fromString("0000fff7-0000-1000-8000-00805f9b34fb");
    public static final UUID BEACON_FFF8 = UUID.fromString("0000fff8-0000-1000-8000-00805f9b34fb");

    public static final UUID BEACON_ALERT = UUID.fromString("00001802-0000-1000-8000-00805f9b34fb");
    public static final UUID BEACON_ALERT_CHAR = UUID.fromString("00002a06-0000-1000-8000-00805f9b34fb");

    public static final UUID BEACON_NAME = UUID.fromString("0000ff80-0000-1000-8000-00805f9b34fb");
    public static final UUID BEACON_NAME_CHAR = UUID.fromString("00002a00-0000-1000-8000-00805f9b34fb");
    public static final UUID BEACON_NEW_NAME_CHAR = UUID.fromString("00002a90-0000-1000-8000-00805f9b34fb");


    public static final UUID BEACON_BATTERY_LIFE = UUID.fromString("0000180f-0000-1000-8000-00805f9b34fb");
    public static final UUID BEACON_BATTERY_LIFE_CHAR = UUID.fromString("00002a19-0000-1000-8000-00805f9b34fb");

    //�°����е� BEACON״̬����
    public static final UUID BEACON_STATE_SERVICE = UUID.fromString("0000ff70-0000-1000-8000-00805f9b34fb");
    public static final UUID BEACON_STATE_CHAR = UUID.fromString("00002a80-0000-1000-8000-00805f9b34fb");

    //	Beacon��������
    public static final UUID BEACON_AUDIO_STATE_SERVICE = UUID.fromString("0000FF60-0000-1000-8000-00805f9b34fb");
    public static final UUID BEACON_AUDIO_STATE_CHAR = UUID.fromString("00002a70-0000-1000-8000-00805f9b34fb");

    //���书��
    public static final UUID BEACON_TX_POWER_SERVICE = UUID.fromString("00001804-0000-1000-8000-00805f9b34fb");
    public static final UUID BEACON_TX_POWER_CHAR = UUID.fromString("00002a07-0000-1000-8000-00805f9b34fb");

    public static final UUID BEACON_DEVICE_INFO = UUID.fromString("0000180a-0000-1000-8000-00805f9b34fb");
    public static final UUID BEACON_DEVICE_MODEL = UUID.fromString("00002a24-0000-1000-8000-00805f9b34fb");
    public static final UUID BEACON_DEVICE_VERSION = UUID.fromString("00002a26-0000-1000-8000-00805f9b34fb");
}
