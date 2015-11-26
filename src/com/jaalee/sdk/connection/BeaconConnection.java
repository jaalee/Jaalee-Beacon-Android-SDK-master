package com.jaalee.sdk.connection;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.os.Handler;

import com.jaalee.sdk.BLEDevice;
import com.jaalee.sdk.Beacon;
import com.jaalee.sdk.Utils;
import com.jaalee.sdk.internal.HashCode;
import com.jaalee.sdk.utils.L;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Establishes connection to the beacon and reads its characteristics.
 * Exposes methods to change beacon's UUID, major, minor, advertising interval and broadcasting power values.
 * You can only change those values only if there is established connection to beacon.
 * It is very important to close() connection when it is not needed.
 */

public class BeaconConnection {

    public static Set<Integer> ALLOWED_POWER_LEVELS = Collections.unmodifiableSet(new HashSet(Arrays.asList(new Integer[]{Integer.valueOf(-30), Integer.valueOf(-20), Integer.valueOf(-16), Integer.valueOf(-12), Integer.valueOf(-8), Integer.valueOf(-4), Integer.valueOf(0), Integer.valueOf(4)})));
    private final Context context;
    private final BluetoothDevice device;
    private final ConnectionCallback connectionCallback;
    private final Handler handler;
    private final BluetoothGattCallback bluetoothGattCallback;
//	private final Runnable timeoutHandler;
    private final BeaconStateService mBeaconStateService;
    private final BeaconAudioService mBeaconAudioStateService;
    private final BeaconTxPower mBeaconTxPowerService;
    private final BeaconNameService mNameService;
    ;
    private final BatteryLifeService mBatteryService;
    private final JaaleeService mBeaconService;
    private final AlertService mAlertService;
    private final Map<UUID, BluetoothService> uuidToService;
    public boolean mCurrentIsJaaleeNewBeacon = false;//�Ƿ���Jaalee�°�Ĺ̼�
    private boolean mStartWritePass = false;//�Ƿ���Jaalee�°�Ĺ̼�
    private String mPassword;
    private boolean mPassWordWriteSuccess = false;
    private boolean didReadCharacteristics;
    private LinkedList<BluetoothGattCharacteristic> toFetch;
    private long aAuth;
    private long bAuth;
    private BluetoothGatt bluetoothGatt;

    public BeaconConnection(Context context, Beacon beacon, ConnectionCallback connectionCallback) {
        mCurrentIsJaaleeNewBeacon = false;
        this.context = context;
        this.device = deviceFromBeacon(beacon);
        this.toFetch = new LinkedList<BluetoothGattCharacteristic>();
        this.handler = new Handler();
        this.connectionCallback = connectionCallback;
        this.bluetoothGattCallback = createBluetoothGattCallback();
//		this.timeoutHandler = createTimeoutHandler();
        this.mNameService = new BeaconNameService();
        this.mBatteryService = new BatteryLifeService();
        this.mBeaconService = new JaaleeService();
        this.mAlertService = new AlertService();
        this.mBeaconStateService = new BeaconStateService();
        this.mBeaconAudioStateService = new BeaconAudioService();
        this.mBeaconTxPowerService = new BeaconTxPower();

        this.uuidToService = new HashMap<UUID, BluetoothService>();
        this.uuidToService.put(JaaleeUuid.BEACON_BATTERY_LIFE, mBatteryService);
        this.uuidToService.put(JaaleeUuid.JAALEE_BEACON_SERVICE, mBeaconService);
        this.uuidToService.put(JaaleeUuid.BEACON_ALERT, mAlertService);
        this.uuidToService.put(JaaleeUuid.BEACON_NAME, mNameService);
        this.uuidToService.put(JaaleeUuid.BEACON_STATE_SERVICE, mBeaconStateService);
        this.uuidToService.put(JaaleeUuid.BEACON_AUDIO_STATE_SERVICE, mBeaconAudioStateService);
        this.uuidToService.put(JaaleeUuid.BEACON_TX_POWER_SERVICE, mBeaconTxPowerService);
    }

    public BeaconConnection(Context context, BLEDevice beacon, ConnectionCallback connectionCallback) {
        mCurrentIsJaaleeNewBeacon = false;
        this.context = context;
        this.device = deviceFromBeacon(beacon);
        this.toFetch = new LinkedList<BluetoothGattCharacteristic>();
        this.handler = new Handler();
        this.connectionCallback = connectionCallback;
        this.bluetoothGattCallback = createBluetoothGattCallback();
//		this.timeoutHandler = createTimeoutHandler();
        this.mNameService = new BeaconNameService();
        this.mBatteryService = new BatteryLifeService();
        this.mBeaconService = new JaaleeService();
        this.mAlertService = new AlertService();
        this.mBeaconStateService = new BeaconStateService();
        this.mBeaconAudioStateService = new BeaconAudioService();
        this.mBeaconTxPowerService = new BeaconTxPower();

        this.uuidToService = new HashMap<UUID, BluetoothService>();
        this.uuidToService.put(JaaleeUuid.BEACON_BATTERY_LIFE, mBatteryService);
        this.uuidToService.put(JaaleeUuid.JAALEE_BEACON_SERVICE, mBeaconService);
        this.uuidToService.put(JaaleeUuid.BEACON_ALERT, mAlertService);
        this.uuidToService.put(JaaleeUuid.BEACON_NAME, mNameService);
        this.uuidToService.put(JaaleeUuid.BEACON_STATE_SERVICE, mBeaconStateService);
        this.uuidToService.put(JaaleeUuid.BEACON_AUDIO_STATE_SERVICE, mBeaconAudioStateService);
        this.uuidToService.put(JaaleeUuid.BEACON_TX_POWER_SERVICE, mBeaconTxPowerService);
    }

    private BluetoothDevice deviceFromBeacon(BLEDevice beacon) {
        BluetoothManager bluetoothManager = (BluetoothManager) this.context.getSystemService("bluetooth");
        BluetoothAdapter bluetoothAdapter = bluetoothManager.getAdapter();
        return bluetoothAdapter.getRemoteDevice(beacon.getMacAddress());
    }

    private BluetoothDevice deviceFromBeacon(Beacon beacon) {
        BluetoothManager bluetoothManager = (BluetoothManager) this.context.getSystemService("bluetooth");
        BluetoothAdapter bluetoothAdapter = bluetoothManager.getAdapter();
        return bluetoothAdapter.getRemoteDevice(beacon.getMacAddress());
    }


    /**
     * Starts connection flow to device with password.
     *
     * @param password The password
     */
    public void connectBeaconWithPassword(String password) {
        mPassword = password;
        L.d("Trying to connect to GATT");
        this.didReadCharacteristics = true;
        this.mCurrentIsJaaleeNewBeacon = false;
        BeaconNameService.mCurrentIsJaaleeNewBeacon = false;
        BeaconTxPower.mCurrentIsJaaleeNewBeacon = false;
        BeaconAudioService.mCurrentIsJaaleeNewBeacon = false;
        BeaconStateService.mCurrentIsJaaleeNewBeacon = false;
        JaaleeService.mCurrentIsJaaleeNewBeacon = false;
        mPassWordWriteSuccess = false;

        this.mStartWritePass = false;
        this.bluetoothGatt = this.device.connectGatt(this.context, false, this.bluetoothGattCallback);
//		this.handler.postDelayed(this.timeoutHandler, TimeUnit.SECONDS.toMillis(10L));
    }

    /**
     * Disconnect with the beacon
     */
    public void disconnect() {
        if (this.bluetoothGatt != null) {
            this.bluetoothGatt.disconnect();
            this.bluetoothGatt.close();
        }
//		this.handler.removeCallbacks(this.timeoutHandler);
    }

    /**
     * @return Return true if the beacon is connected
     */
    public boolean isConnected() {
        BluetoothManager bluetoothManager = (BluetoothManager) this.context.getSystemService("bluetooth");
        int connectionState = bluetoothManager.getConnectionState(this.device, 7);
//		return (connectionState == 2) && (this.didReadCharacteristics);
        return (connectionState == 2);
    }

    public Integer GetBattLevel() {
        return this.mBatteryService.getBatteryPercent();
    }

    /**
     * Call current Beacon
     */
    public void CallBeacon() {
        if ((!isConnected()) || (!this.mAlertService.hasCharacteristic(JaaleeUuid.BEACON_ALERT_CHAR))) {
            L.w("Not connected to beacon. Discarding changing proximity UUID.");
//			writeCallback.onError();
            return;
        }
        BluetoothGattCharacteristic AlertChar = this.mAlertService.beforeCharacteristicWrite(JaaleeUuid.BEACON_ALERT_CHAR, null);
        byte[] arrayOfByte = new byte[1];
        arrayOfByte[0] = 1;
        AlertChar.setValue(arrayOfByte);
        this.bluetoothGatt.writeCharacteristic(AlertChar);
    }

    private void BeaconKeepConnect(WriteCallback writeCallback) {
        if ((!isConnected()) || (!this.mBeaconService.hasCharacteristic(JaaleeUuid.BEACON_KEEP_CONNECT_CHAR))) {
            L.w("Not connected to beacon. Discarding changing proximity UUID.");
            writeCallback.onError();
            return;
        }
        BluetoothGattCharacteristic uuidChar = this.mBeaconService.beforeCharacteristicWrite(JaaleeUuid.BEACON_KEEP_CONNECT_CHAR, writeCallback);

        byte[] arrayOfByte;
        if (this.mStartWritePass) {
//			Log.i("NRF  TEST121", "NRF  NRFд��");
            arrayOfByte = HashCode.fromString(mPassword.replaceAll("-", "").toLowerCase()).asBytes();
        } else {
//			Log.i("NRF  TEST121", "NRF  TIд��");
            arrayOfByte = new byte[1];
            arrayOfByte[0] = 1;
        }

        uuidChar.setValue(arrayOfByte);

        this.bluetoothGatt.writeCharacteristic(uuidChar);
    }

    //Config Password

    /**
     * Configure the password of the beacon
     *
     * @param NewPsaaword   The password want change to
     * @param writeCallback Callback to be invoked when write is completed.
     */
    public void ChangePassword(String NewPsaaword, WriteCallback writeCallback) {
        if ((!isConnected()) || (!this.mBeaconService.hasCharacteristic(JaaleeUuid.BEACON_UUID_CHAR))) {
            L.w("Not connected to beacon. Discarding changing proximity UUID.");
            writeCallback.onError();
            return;
        }
        byte[] OldPassAsBytes = HashCode.fromString(mPassword.replaceAll("-", "").toLowerCase()).asBytes();
        byte[] NewPasswordBytes = HashCode.fromString(NewPsaaword.toLowerCase()).asBytes();

        final BluetoothGattCharacteristic PasswordChar;


        if (mCurrentIsJaaleeNewBeacon) {
            PasswordChar = this.mBeaconService.beforeCharacteristicWrite(JaaleeUuid.BEACON_FFF8, writeCallback);
            PasswordChar.setValue(NewPasswordBytes);
        } else {
            PasswordChar = this.mBeaconService.beforeCharacteristicWrite(JaaleeUuid.BEACON_FFF6, writeCallback);
            PasswordChar.setValue(this.arraycat(OldPassAsBytes, NewPasswordBytes));
        }

        this.bluetoothGatt.writeCharacteristic(PasswordChar);

        if (!mCurrentIsJaaleeNewBeacon) {
            new Handler().postDelayed(new Runnable() {
                public void run() {
                    //execute the task
//			    	BeaconConnection.this.toFetch.add(BeaconConnection.this.mBeaconService.getAvailableCharacteristic(JaaleeUuid.CHANGE_PASSWORD_CHAR));
                    BeaconConnection.this.bluetoothGatt.readCharacteristic(PasswordChar);
                }
            }, 3000);
        }
    }


    /**
     * Config beacon's proximity uuid
     *
     * @param proximityUuid The UUID want to change to
     * @param writeCallback Callback to be invoked when write is completed.
     */
    public void writeProximityUuid(String proximityUuid, WriteCallback writeCallback) {
        if ((!isConnected()) || (!this.mBeaconService.hasCharacteristic(JaaleeUuid.BEACON_UUID_CHAR))) {
            L.w("Not connected to beacon. Discarding changing proximity UUID.");
            writeCallback.onError();
            return;
        }
        byte[] uuidAsBytes = HashCode.fromString(proximityUuid.replaceAll("-", "").toLowerCase()).asBytes();
        byte[] PasswordBytes = HashCode.fromString(mPassword.toLowerCase()).asBytes();

        final BluetoothGattCharacteristic uuidChar = this.mBeaconService.beforeCharacteristicWrite(JaaleeUuid.BEACON_UUID_CHAR, writeCallback);

        if (mCurrentIsJaaleeNewBeacon) {
            uuidChar.setValue(uuidAsBytes);
            this.bluetoothGatt.writeCharacteristic(uuidChar);
        } else {
            uuidChar.setValue(this.arraycat(PasswordBytes, uuidAsBytes));
            this.bluetoothGatt.writeCharacteristic(uuidChar);
            new Handler().postDelayed(new Runnable() {
                public void run() {
                    //execute the task
//			    	BeaconConnection.this.toFetch.add(BeaconConnection.this.mBeaconService.getAvailableCharacteristic(JaaleeUuid.BEACON_UUID_CHAR));
                    BeaconConnection.this.bluetoothGatt.readCharacteristic(uuidChar);
                }
            }, 3000);
        }
    }


    /**
     * Config beacon's Broadcast Interval
     *
     * @param BroadcastInterval Advertising interval in milliseconds (100ms-10000ms).
     * @param writeCallback     Callback to be invoked when write is completed.
     */
    public void writeAdvertisingInterval(int BroadcastInterval, WriteCallback writeCallback) {

        UUID temp_UUID;
        if (mCurrentIsJaaleeNewBeacon) {
            temp_UUID = UUID.fromString("0000fff6-0000-1000-8000-00805f9b34fb");
        } else {
            temp_UUID = UUID.fromString("0000fff7-0000-1000-8000-00805f9b34fb");
        }
        if ((!isConnected()) || (!this.mBeaconService.hasCharacteristic(temp_UUID))) {
            L.w("Not connected to beacon. Discarding changing advertising interval.");
            writeCallback.onError();
            return;
        }

        final BluetoothGattCharacteristic intervalChar = this.mBeaconService.beforeCharacteristicWrite(temp_UUID, writeCallback);

        byte[] arrayOfByte = new byte[1];
        arrayOfByte[0] = (byte) (BroadcastInterval / 100);
        byte[] PasswordBytes = HashCode.fromString(mPassword.toLowerCase()).asBytes();

        if (mCurrentIsJaaleeNewBeacon) {
            intervalChar.setValue(arrayOfByte);
            this.bluetoothGatt.writeCharacteristic(intervalChar);
        } else {
            intervalChar.setValue(this.arraycat(PasswordBytes, arrayOfByte));
            this.bluetoothGatt.writeCharacteristic(intervalChar);
            new Handler().postDelayed(new Runnable() {
                public void run() {
                    BeaconConnection.this.bluetoothGatt.readCharacteristic(intervalChar);
                }
            }, 3000);
        }
    }

    /**
     * Changes Beacon Power value.
     *
     * @param powerDBM      Beacon's measured power to be set.
     * @param writeCallback Callback to be invoked when write is completed.
     */
    public void writeBroadcastingPowerValue(int power, WriteCallback writeCallback) {
        if ((!isConnected()) || (!this.mBeaconService.hasCharacteristic(JaaleeUuid.POWER_CHAR))) {
            L.w("Not connected to beacon. Discarding changing broadcasting power.");
            writeCallback.onError();
            return;
        }

        byte[] PasswordBytes = HashCode.fromString(mPassword.toLowerCase()).asBytes();

        byte[] arrayOfByte = new byte[1];
        arrayOfByte[0] = (byte) (power);

        final BluetoothGattCharacteristic powerChar = this.mBeaconService.beforeCharacteristicWrite(JaaleeUuid.POWER_CHAR, writeCallback);

        if (mCurrentIsJaaleeNewBeacon) {
            powerChar.setValue(arrayOfByte);
            this.bluetoothGatt.writeCharacteristic(powerChar);
        } else {
            powerChar.setValue(this.arraycat(PasswordBytes, arrayOfByte));

            this.bluetoothGatt.writeCharacteristic(powerChar);
            new Handler().postDelayed(new Runnable() {
                public void run() {
                    BeaconConnection.this.bluetoothGatt.readCharacteristic(powerChar);
                }
            }, 3000);
        }
    }

    /**
     * Changes major value of the beacon. Major value will be normalized to fit the range (1, 65535).
     *
     * @param major         Major value to be set.
     * @param writeCallback Callback to be invoked when write is completed.
     */
    public void writeMajor(int major, WriteCallback writeCallback) {
        if (!isConnected() || major < 0 || major > 65535) {
            L.w("Not connected to beacon or value error. Discarding changing major.");
            writeCallback.onError();
            return;
        }

        major = Utils.normalize16BitUnsignedInt(major);

        final BluetoothGattCharacteristic majorChar = this.mBeaconService.beforeCharacteristicWrite(JaaleeUuid.MAJOR_CHAR, writeCallback);

        byte[] MajorAsBytes = HashCode.fromShort(major).asBytes();
        byte[] PasswordBytes = HashCode.fromString(mPassword.toLowerCase()).asBytes();

        if (mCurrentIsJaaleeNewBeacon) {
            majorChar.setValue(MajorAsBytes);
            this.bluetoothGatt.writeCharacteristic(majorChar);
        } else {
            majorChar.setValue(this.arraycat(PasswordBytes, MajorAsBytes));
            this.bluetoothGatt.writeCharacteristic(majorChar);
            new Handler().postDelayed(new Runnable() {
                public void run() {
                    BeaconConnection.this.bluetoothGatt.readCharacteristic(majorChar);
                }
            }, 3000);
        }

    }

    /**
     * Changes minor value of the beacon. Minor value will be normalized to fit the range (1, 65535).
     *
     * @param minor         Minor value to be set.
     * @param writeCallback Callback to be invoked when write is completed.
     */
    public void writeMinor(int minor, WriteCallback writeCallback) {
        if (!isConnected() || minor < 0 || minor > 65535) {
            L.w("Not connected to beacon or value error. Discarding changing major.");
            writeCallback.onError();
            return;
        }

        minor = Utils.normalize16BitUnsignedInt(minor);
        byte[] MinorAsBytes = HashCode.fromShort(minor).asBytes();
        final BluetoothGattCharacteristic minorChar = this.mBeaconService.beforeCharacteristicWrite(JaaleeUuid.MINOR_CHAR, writeCallback);


        byte[] PasswordBytes = HashCode.fromString(mPassword.toLowerCase()).asBytes();

        if (mCurrentIsJaaleeNewBeacon) {
            minorChar.setValue(MinorAsBytes);
            this.bluetoothGatt.writeCharacteristic(minorChar);
        } else {
            minorChar.setValue(this.arraycat(PasswordBytes, MinorAsBytes));
            this.bluetoothGatt.writeCharacteristic(minorChar);
            new Handler().postDelayed(new Runnable() {
                public void run() {
                    BeaconConnection.this.bluetoothGatt.readCharacteristic(minorChar);
                }
            }, 3000);

        }
    }

    //����״̬

    /**
     * Set Beacon State
     *
     * @param state         The state of beacon
     * @param writeCallback Callback to be invoked when write is completed.
     */
    public void writeBeaconState(int state, WriteCallback writeCallback) {
        if (!mCurrentIsJaaleeNewBeacon) {
            L.w("Current beacon not support config state.");
            writeCallback.onError();
            return;
        }
        if (!isConnected()) {
            L.w("Not connected to beacon. Discarding changing state.");
            writeCallback.onError();
            return;
        }

        byte[] arrayOfByte = new byte[1];
        switch (state) {
            case JaaleeDefine.JAALEE_BEACON_STATE_ENABLE:
                arrayOfByte[0] = 2;
                break;
            case JaaleeDefine.JAALEE_BEACON_STATE_DISABLE:
                arrayOfByte[0] = 1;
                break;
            default:
                arrayOfByte[0] = 1;
                break;
        }

        final BluetoothGattCharacteristic Char = this.mBeaconStateService.beforeCharacteristicWrite(JaaleeUuid.BEACON_STATE_CHAR, writeCallback);
        Char.setValue(arrayOfByte);
        this.bluetoothGatt.writeCharacteristic(Char);
    }

    //����״̬

    /**
     * Set Audio state
     *
     * @param state         The audio state of beacon
     * @param writeCallback Callback to be invoked when write is completed.
     */
    public void writeBeaconAudioState(int state, WriteCallback writeCallback) {
        if (!mCurrentIsJaaleeNewBeacon) {
            L.w("Current beacon not support config state.");
            writeCallback.onError();
            return;
        }
        if (!isConnected()) {
            L.w("Not connected to beacon. Discarding changing state.");
            writeCallback.onError();
            return;
        }

        byte[] arrayOfByte = new byte[1];
        switch (state) {
            case JaaleeDefine.BEACON_AUDIO_STATE_ENABLE:
                arrayOfByte[0] = 1;
                break;
            case JaaleeDefine.BEACON_AUDIO_STATE_ENABLE_WHEN_START:
                arrayOfByte[0] = 2;
                break;
            case JaaleeDefine.BEACON_AUDIO_STATE_ENABLE_WHEN_TAP:
                arrayOfByte[0] = 3;
                break;
            case JaaleeDefine.BEACON_AUDIO_STATE_DISABLE:
                arrayOfByte[0] = 4;
                break;
            default:
                arrayOfByte[0] = 1;
                break;
        }

        final BluetoothGattCharacteristic Char = this.mBeaconAudioStateService.beforeCharacteristicWrite(JaaleeUuid.BEACON_AUDIO_STATE_CHAR, writeCallback);
        Char.setValue(arrayOfByte);
        this.bluetoothGatt.writeCharacteristic(Char);
    }


    /**
     * Changes broadcasting power of the beacon.
     * Allowed values: see JaaleeDefine
     *
     * @param state         The Tx Power of the beacon
     * @param writeCallback Callback to be invoked when write is completed.
     */
    public void writeBeaconTxPower(int state, WriteCallback writeCallback) {
        if (!isConnected()) {
            L.w("Not connected to beacon. Discarding changing state.");
            writeCallback.onError();
            return;
        }

        byte[] arrayOfByte = new byte[1];

        if (mCurrentIsJaaleeNewBeacon) {
            switch (state) {
                case JaaleeDefine.JAALEE_TX_POWER_4_DBM:
                    arrayOfByte[0] = 1;
                    break;
                case JaaleeDefine.JAALEE_TX_POWER_0_DBM:
                    arrayOfByte[0] = 2;
                    break;
                case JaaleeDefine.JAALEE_TX_POWER_MINUS_4_DBM:
                    arrayOfByte[0] = 3;
                    break;
                case JaaleeDefine.JAALEE_TX_POWER_MINUS_8_DBM:
                    arrayOfByte[0] = 4;
                    break;
                case JaaleeDefine.JAALEE_TX_POWER_MINUS_12_DBM:
                    arrayOfByte[0] = 5;
                    break;
                case JaaleeDefine.JAALEE_TX_POWER_MINUS_16_DBM:
                    arrayOfByte[0] = 6;
                    break;
                case JaaleeDefine.JAALEE_TX_POWER_MINUS_20_DBM:
                    arrayOfByte[0] = 7;
                    break;
                case JaaleeDefine.JAALEE_TX_POWER_MINUS_30_DBM:
                    arrayOfByte[0] = 8;
                    break;
                case JaaleeDefine.JAALEE_TX_POWER_MINUS_40_DBM:
                    arrayOfByte[0] = 9;
                    break;
                default:
                    arrayOfByte[0] = 1;
                    break;
            }
        } else {
            switch (state) {
                case JaaleeDefine.JAALEE_TX_POWER_0_DBM:
                    arrayOfByte[0] = 0;
                    break;
                case JaaleeDefine.JAALEE_TX_POWER_MINUS_6_DBM:
                    arrayOfByte[0] = 1;
                    break;
                case JaaleeDefine.JAALEE_TX_POWER_MINUS_23_DBM:
                    arrayOfByte[0] = 2;
                    break;
                default:
                    arrayOfByte[0] = 0;
                    break;
            }
        }

        final BluetoothGattCharacteristic Char = this.mBeaconTxPowerService.beforeCharacteristicWrite(JaaleeUuid.BEACON_TX_POWER_CHAR, writeCallback);

        if (mCurrentIsJaaleeNewBeacon) {
            Char.setValue(arrayOfByte);
            this.bluetoothGatt.writeCharacteristic(Char);
        } else {
            byte[] PasswordBytes = HashCode.fromString(mPassword.toLowerCase()).asBytes();
            Char.setValue(this.arraycat(PasswordBytes, arrayOfByte));
            this.bluetoothGatt.writeCharacteristic(Char);
            new Handler().postDelayed(new Runnable() {
                public void run() {
                    BeaconConnection.this.bluetoothGatt.readCharacteristic(Char);
                }
            }, 3000);

        }
    }

    //�޸�����

    /**
     * Config Beacon's device name
     *
     * @param name          the name of the beacon,like "jaalee"
     * @param writeCallback Callback to be invoked when write is completed.
     */
    public void writeBeaconName(String name, WriteCallback writeCallback) {
        if (!isConnected()) {
            L.w("Not connected to beacon. Discarding changing state.");
            writeCallback.onError();
            return;
        }

        if (name.length() > 15) {
            L.w("the lenth of the name should be less than 15.");
            writeCallback.onError();
            return;
        }

        final BluetoothGattCharacteristic Char;

        if (mCurrentIsJaaleeNewBeacon) {
            Char = this.mNameService.beforeCharacteristicWrite(JaaleeUuid.BEACON_NEW_NAME_CHAR, writeCallback);
        } else {
            Char = this.mNameService.beforeCharacteristicWrite(JaaleeUuid.BEACON_NAME_CHAR, writeCallback);
        }


        byte[] value = name.getBytes();

        if (mCurrentIsJaaleeNewBeacon) {
            Char.setValue(value);
            this.bluetoothGatt.writeCharacteristic(Char);
        } else {
            byte[] PasswordBytes = HashCode.fromString(mPassword.toLowerCase()).asBytes();
            Char.setValue(this.arraycat(PasswordBytes, value));
            this.bluetoothGatt.writeCharacteristic(Char);
            new Handler().postDelayed(new Runnable() {
                public void run() {
                    BeaconConnection.this.bluetoothGatt.readCharacteristic(Char);
                }
            }, 3000);

        }
    }


    private Runnable createTimeoutHandler() {
        return new Runnable() {
            public void run() {
                L.v("Timeout while authenticating");
                if (!BeaconConnection.this.didReadCharacteristics) {
                    if (BeaconConnection.this.bluetoothGatt != null) {
                        BeaconConnection.this.bluetoothGatt.disconnect();
                        BeaconConnection.this.bluetoothGatt.close();
                        BeaconConnection.this.bluetoothGatt = null;
                    }
                    BeaconConnection.this.notifyAuthenticationError();
                }
            }
        };
    }

    private BluetoothGattCallback createBluetoothGattCallback() {
        return new BluetoothGattCallback() {
            public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
                if (newState == 2) {
                    L.d("Connected to GATT server, discovering services: " + gatt.discoverServices());
                } else if ((newState == 0) && (!BeaconConnection.this.didReadCharacteristics)) {
                    L.w("Disconnected from GATT server");
                    BeaconConnection.this.notifyAuthenticationError();
                } else if (newState == 0) {
                    L.w("Disconnected from GATT server");
                    BeaconConnection.this.notifyDisconnected();
                }
            }

            public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
                if (status == 0) {

                    if (JaaleeUuid.BEACON_KEEP_CONNECT_CHAR.equals(characteristic.getUuid())) {
                        //��ȡ����ֵ
                        //keep connect
                        if (characteristic.getValue().length == 3) {
                            BeaconConnection.this.mStartWritePass = true;
                        } else {
                            BeaconConnection.this.mStartWritePass = false;
                            ;
                        }

                        if (!mPassWordWriteSuccess) {
                            BeaconConnection.this.handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    // TODO Auto-generated method stub
                                    BeaconConnection.this.BeaconKeepConnect(new WriteCallback() {

                                        @Override
                                        public void onSuccess() {
                                            // TODO Auto-generated method stub
                                            //Log.i("BeaconConnection", "Keep Connect Successful");
                                            mPassWordWriteSuccess = true;
                                        }

                                        @Override
                                        public void onError() {
                                            // TODO Auto-generated method stub
                                            //Log.i("BeaconConnection", "Keep Connect Failed");
                                        }
                                    });

                                }
                            }, TimeUnit.SECONDS.toMillis(1L));
                        }
                    }
                    BluetoothService temp = ((BluetoothService) BeaconConnection.this.uuidToService.get(characteristic.getService().getUuid()));
                    if (temp != null) {
                        temp.update(characteristic);
                    }
                    if (mPassWordWriteSuccess) {
                        BeaconConnection.this.readCharacteristics(gatt);
                    }

                } else {
                    L.w("Failed to read characteristic");
                    BeaconConnection.this.toFetch.clear();
                    BeaconConnection.this.notifyAuthenticationError();
                }
            }

            public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
                BluetoothService temp = ((BluetoothService) BeaconConnection.this.uuidToService.get(characteristic.getService().getUuid()));
                if (temp != null) {
                    temp.onCharacteristicWrite(characteristic, status);
                }

                if (JaaleeUuid.BEACON_KEEP_CONNECT_CHAR.equals(characteristic.getUuid())) {
                    BeaconConnection.this.onAuthenticationCompleted(gatt);
                }
            }

            public void onServicesDiscovered(BluetoothGatt gatt, int status) {
                if (status == 0) {
                    L.v("Services discovered");
//					Log.i("NRF  TEST121", "NRF  TEST121");
                    BeaconConnection.this.processDiscoveredServices(gatt.getServices());
//					BeaconConnection.this.startAuthentication(gatt);


                    for (BluetoothGattService service : gatt.getServices()) {
                        if (JaaleeUuid.BEACON_STATE_SERVICE.equals(service.getUuid())) {
                            mCurrentIsJaaleeNewBeacon = true;//Nordic Beacon
                            BeaconNameService.mCurrentIsJaaleeNewBeacon = true;
                            BeaconTxPower.mCurrentIsJaaleeNewBeacon = true;
                            BeaconAudioService.mCurrentIsJaaleeNewBeacon = true;
                            BeaconStateService.mCurrentIsJaaleeNewBeacon = true;
                            JaaleeService.mCurrentIsJaaleeNewBeacon = true;

                        } else if (JaaleeUuid.JAALEE_BEACON_SERVICE.equals(service.getUuid())) {
                            //��ȡ����ֵ
                            BeaconConnection.this.handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    // TODO Auto-generated method stub
                                    BluetoothGattCharacteristic keepChar = BeaconConnection.this.mBeaconService.getKeepUUIDChar();
                                    BeaconConnection.this.bluetoothGatt.readCharacteristic(keepChar);
//										Log.i("NRF  TEST121", "NRF  TEST122");
                                }
                            }, TimeUnit.SECONDS.toMillis(1L));
                        }
                    }

                } else {
                    L.w("Could not discover services, status: " + status);
                    BeaconConnection.this.notifyAuthenticationError();
                }
            }
        };
    }

    private void notifyAuthenticationError() {
//		this.handler.removeCallbacks(this.timeoutHandler);
        this.connectionCallback.onAuthenticationError();
    }

    private void notifyDisconnected() {
        this.connectionCallback.onDisconnected();
    }

    private void processDiscoveredServices(List<BluetoothGattService> services) {

        this.mNameService.processGattServices(services);
        this.mBeaconService.processGattServices(services);
        this.mAlertService.processGattServices(services);
        this.mBatteryService.processGattServices(services);
        this.mBeaconStateService.processGattServices(services);
        this.mBeaconTxPowerService.processGattServices(services);
        this.mBeaconAudioStateService.processGattServices(services);

        this.toFetch.clear();
        this.toFetch.addAll(this.mBeaconService.getAvailableCharacteristics());
        this.toFetch.addAll(this.mNameService.getAvailableCharacteristics());
        //this.toFetch.addAll(this.mAlertService.getAvailableCharacteristics());
        this.toFetch.addAll(this.mBatteryService.getAvailableCharacteristics());
        this.toFetch.addAll(this.mBeaconStateService.getAvailableCharacteristics());
        this.toFetch.addAll(this.mBeaconTxPowerService.getAvailableCharacteristics());
        this.toFetch.addAll(this.mBeaconAudioStateService.getAvailableCharacteristics());
    }

//	private void startAuthentication(BluetoothGatt gatt)
//	{
//		if (!this.mNameService.isAvailable()) {
//			L.w("Authentication service is not available on the beacon");
//			notifyAuthenticationError();
//			return;
//		}
//		this.aAuth = AuthMath.randomUnsignedInt();
//		BluetoothGattCharacteristic seedChar = this.mNameService.getAuthSeedCharacteristic();
//		seedChar.setValue(AuthMath.firstStepSecret(this.aAuth), 20, 0);
//		gatt.writeCharacteristic(seedChar);
//	}

    private void onSeedWriteCompleted(final BluetoothGatt gatt, final BluetoothGattCharacteristic characteristic) {
        this.handler.postDelayed(new Runnable() {
            public void run() {
                gatt.readCharacteristic(characteristic);
            }
        }
                , 500L);
    }

    private void onBeaconSeedResponse(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
//		Integer intValue = characteristic.getIntValue(20, 0);
//		this.bAuth = UnsignedInteger.fromIntBits(intValue.intValue()).longValue();
//		String macAddress = this.device.getAddress().replace(":", "");
//		BluetoothGattCharacteristic vectorChar = this.mNameService.getAuthVectorCharacteristic();
//		vectorChar.setValue(AuthMath.secondStepSecret(this.aAuth, this.bAuth, macAddress));
//		gatt.writeCharacteristic(vectorChar);
    }

    private void onAuthenticationCompleted(final BluetoothGatt gatt) {
        this.handler.postDelayed(new Runnable() {
            public void run() {
                BeaconConnection.this.readCharacteristics(gatt);
            }
        }
                , 500L);
    }

    private void readCharacteristics(BluetoothGatt gatt) {
        if (this.toFetch.size() != 0) {
            BluetoothGattCharacteristic temp = (BluetoothGattCharacteristic) this.toFetch.poll();
//			Log.i("NRF  TEST121", "NRF:" + temp.getUuid());
            gatt.readCharacteristic(temp);

        } else if (this.bluetoothGatt != null) {
            onAuthenticated();
        }
    }

    private void onAuthenticated() {
        L.v("Authenticated to beacon");
//		this.handler.removeCallbacks(this.timeoutHandler);
        this.didReadCharacteristics = true;
        this.connectionCallback.onAuthenticated(new BeaconCharacteristics(this.mBeaconService, this.mNameService, this.mBeaconTxPowerService, this.mBeaconStateService, this.mBeaconAudioStateService));
    }

    byte[] arraycat(byte[] buf1, byte[] buf2) {
        byte[] bufret = null;
        int len1 = 0;
        int len2 = 0;
        if (buf1 != null)
            len1 = buf1.length;
        if (buf2 != null)
            len2 = buf2.length;
        if (len1 + len2 > 0)
            bufret = new byte[len1 + len2];
        if (len1 > 0)
            System.arraycopy(buf1, 0, bufret, 0, len1);
        if (len2 > 0)
            System.arraycopy(buf2, 0, bufret, len1, len2);
        return bufret;
    }

}
