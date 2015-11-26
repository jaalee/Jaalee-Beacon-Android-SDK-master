package com.jaalee.sdk.connection;

import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

/**
 * @author JAALEE, Inc
 * @Support dev@jaalee.com
 * @Sales: sales@jaalee.com
 * @see http://www.jaalee.com/
 */

public class JaaleeService implements BluetoothService {
    static boolean mCurrentIsJaaleeNewBeacon = false;//�Ƿ���Jaalee�°�Ĺ̼�
    private final HashMap<UUID, BluetoothGattCharacteristic> characteristics = new HashMap();
    private final HashMap<UUID, WriteCallback> writeCallbacks = new HashMap();

    private static String getStringValue(byte[] bytes) {
//		int indexOfFirstZeroByte = 0;
//		while (bytes[indexOfFirstZeroByte] != 0) {
//			indexOfFirstZeroByte++;
//		}

//		byte[] strBytes = new byte[indexOfFirstZeroByte];
//		String StrTemp = "";
//		for (int i = 0; i != indexOfFirstZeroByte; i++) {
//			strBytes[i] = bytes[i];
//		}
//		Log.i("TEST", "VALUE:"+strBytes);
//
//		return new String(strBytes);

        String stmp = "";
        StringBuilder sb = new StringBuilder("");
        for (int n = 0; n < bytes.length; n++) {
            stmp = Integer.toHexString(bytes[n] & 0xFF);
            sb.append((stmp.length() == 1) ? "0" + stmp : stmp);
            sb.append(" ");
        }
        return sb.toString().toUpperCase().trim();
    }

    private static int getUnsignedByte(byte[] bytes) {
        return unsignedByteToInt(bytes[0]);
    }

    private static int unsignedByteToInt(byte value) {
        return value & 0xFF;
    }

    private static int getUnsignedInt16(byte[] bytes) {
        return unsignedByteToInt(bytes[1]) + (unsignedByteToInt(bytes[0]) << 8);
    }

    public void processGattServices(List<BluetoothGattService> services) {
        for (BluetoothGattService service : services)
            if (JaaleeUuid.JAALEE_BEACON_SERVICE.equals(service.getUuid())) {

                if (service.getCharacteristic(JaaleeUuid.BEACON_KEEP_CONNECT_CHAR) != null) {
                    this.characteristics.put(JaaleeUuid.BEACON_KEEP_CONNECT_CHAR, service.getCharacteristic(JaaleeUuid.BEACON_KEEP_CONNECT_CHAR));
                }

                if (service.getCharacteristic(JaaleeUuid.BEACON_UUID_CHAR) != null) {
                    this.characteristics.put(JaaleeUuid.BEACON_UUID_CHAR, service.getCharacteristic(JaaleeUuid.BEACON_UUID_CHAR));
                }

                if (service.getCharacteristic(JaaleeUuid.MAJOR_CHAR) != null) {
                    this.characteristics.put(JaaleeUuid.MAJOR_CHAR, service.getCharacteristic(JaaleeUuid.MAJOR_CHAR));
                }

                if (service.getCharacteristic(JaaleeUuid.MINOR_CHAR) != null) {
                    this.characteristics.put(JaaleeUuid.MINOR_CHAR, service.getCharacteristic(JaaleeUuid.MINOR_CHAR));
                }

                if (service.getCharacteristic(JaaleeUuid.POWER_CHAR) != null) {
                    this.characteristics.put(JaaleeUuid.POWER_CHAR, service.getCharacteristic(JaaleeUuid.POWER_CHAR));
                }

                if (service.getCharacteristic(JaaleeUuid.BEACON_FFF6) != null) {
                    this.characteristics.put(JaaleeUuid.BEACON_FFF6, service.getCharacteristic(JaaleeUuid.BEACON_FFF6));
                }

                if (service.getCharacteristic(JaaleeUuid.BEACON_FFF7) != null) {
                    this.characteristics.put(JaaleeUuid.BEACON_FFF7, service.getCharacteristic(JaaleeUuid.BEACON_FFF7));
                }

                if (service.getCharacteristic(JaaleeUuid.BEACON_FFF8) != null) {
                    this.characteristics.put(JaaleeUuid.BEACON_FFF8, service.getCharacteristic(JaaleeUuid.BEACON_FFF8));
                }
            }
    }

    public boolean hasCharacteristic(UUID uuid) {
        return this.characteristics.containsKey(uuid);
    }

    public String getBeaconUUID() {
        return this.characteristics.containsKey(JaaleeUuid.BEACON_UUID_CHAR) ?
                getStringValue(((BluetoothGattCharacteristic) this.characteristics.get(JaaleeUuid.BEACON_UUID_CHAR)).getValue()) : null;
    }

    public int getBeaconMajor() {
        return this.characteristics.containsKey(JaaleeUuid.MAJOR_CHAR) ?
                getUnsignedInt16(((BluetoothGattCharacteristic) this.characteristics.get(JaaleeUuid.MAJOR_CHAR)).getValue()) : null;
    }

    public int getBeaconMinor() {
        return this.characteristics.containsKey(JaaleeUuid.MINOR_CHAR) ?
                getUnsignedInt16(((BluetoothGattCharacteristic) this.characteristics.get(JaaleeUuid.MINOR_CHAR)).getValue()) : null;
    }

    public int getBeaconPower() {
        return this.characteristics.containsKey(JaaleeUuid.POWER_CHAR) ?
                getUnsignedByte(((BluetoothGattCharacteristic) this.characteristics.get(JaaleeUuid.POWER_CHAR)).getValue()) : null;
    }

    public int getBeaconMfgr() {
        if (mCurrentIsJaaleeNewBeacon) {
            return this.characteristics.containsKey(JaaleeUuid.BEACON_FFF7) ?
                    getUnsignedInt16(((BluetoothGattCharacteristic) this.characteristics.get(JaaleeUuid.BEACON_FFF7)).getValue()) : null;
        }
        return 0;
    }

    public int getBeaconBroadcastInterval() {
        if (mCurrentIsJaaleeNewBeacon) {
            return this.characteristics.containsKey(JaaleeUuid.BEACON_FFF6) ?
                    getUnsignedByte(((BluetoothGattCharacteristic) this.characteristics.get(JaaleeUuid.BEACON_FFF6)).getValue()) : null;
        } else {
            return this.characteristics.containsKey(JaaleeUuid.BEACON_FFF7) ?
                    getUnsignedByte(((BluetoothGattCharacteristic) this.characteristics.get(JaaleeUuid.BEACON_FFF7)).getValue()) : null;
        }
    }

    public void update(BluetoothGattCharacteristic characteristic) {
        this.characteristics.put(characteristic.getUuid(), characteristic);

        if (!mCurrentIsJaaleeNewBeacon) {
            byte[] Value = characteristic.getValue();

            WriteCallback writeCallback = (WriteCallback) this.writeCallbacks.remove(characteristic.getUuid());
            if (writeCallback != null) {
                if (Value[0] == 1) {
                    writeCallback.onSuccess();
                } else {
                    writeCallback.onError();
                }
            }
        }

    }

    public Collection<BluetoothGattCharacteristic> getAvailableCharacteristics() {
        List chars = new ArrayList(this.characteristics.values());
        chars.removeAll(Collections.singleton(null));
        return chars;
    }

    public BluetoothGattCharacteristic getAvailableCharacteristic(UUID uuid) {
        return (BluetoothGattCharacteristic) this.characteristics.get(uuid);
    }

    public BluetoothGattCharacteristic getKeepUUIDChar() {
        return (BluetoothGattCharacteristic) this.characteristics.get(JaaleeUuid.BEACON_KEEP_CONNECT_CHAR);
    }

    public BluetoothGattCharacteristic beforeCharacteristicWrite(UUID uuid, WriteCallback callback) {
//		if (this.writeCallbacks.containsKey(uuid))
//		{
//			this.writeCallbacks.remove(uuid);
//		}
        this.writeCallbacks.put(uuid, callback);
        return (BluetoothGattCharacteristic) this.characteristics.get(uuid);
    }

    public void onCharacteristicWrite(BluetoothGattCharacteristic characteristic, int status) {
        if (mCurrentIsJaaleeNewBeacon) {
            WriteCallback writeCallback = (WriteCallback) this.writeCallbacks.remove(characteristic.getUuid());
            if (status == 0)
                writeCallback.onSuccess();
            else
                writeCallback.onError();
        } else if (JaaleeUuid.BEACON_KEEP_CONNECT_CHAR.equals(characteristic.getUuid())) {
            WriteCallback writeCallback = (WriteCallback) this.writeCallbacks.remove(characteristic.getUuid());
            if (status == 0)
                writeCallback.onSuccess();
            else
                writeCallback.onError();
        }

    }
}
