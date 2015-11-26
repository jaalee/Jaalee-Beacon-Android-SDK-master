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

public class BeaconNameService implements BluetoothService {
    static boolean mCurrentIsJaaleeNewBeacon = false;//�Ƿ���Jaalee�°�Ĺ̼�
    private final HashMap<UUID, BluetoothGattCharacteristic> characteristics = new HashMap<UUID, BluetoothGattCharacteristic>();
    private final HashMap<UUID, WriteCallback> writeCallbacks = new HashMap();

    private static String getStringValue(byte[] bytes) {
        int indexOfFirstZeroByte = 0;
        while (bytes[indexOfFirstZeroByte] != 0) {
            indexOfFirstZeroByte++;
        }

        byte[] strBytes = new byte[indexOfFirstZeroByte];
        for (int i = 0; i != indexOfFirstZeroByte; i++) {
            strBytes[i] = bytes[i];
        }

        return new String(strBytes);
    }

    public void processGattServices(List<BluetoothGattService> services) {
        for (BluetoothGattService service : services)
            if (JaaleeUuid.BEACON_NAME.equals(service.getUuid())) {
                if (service.getCharacteristic(JaaleeUuid.BEACON_NAME_CHAR) != null) {
                    this.characteristics.put(JaaleeUuid.BEACON_NAME_CHAR, service.getCharacteristic(JaaleeUuid.BEACON_NAME_CHAR));
                }

                if (service.getCharacteristic(JaaleeUuid.BEACON_NEW_NAME_CHAR) != null) {
                    this.characteristics.put(JaaleeUuid.BEACON_NEW_NAME_CHAR, service.getCharacteristic(JaaleeUuid.BEACON_NEW_NAME_CHAR));
                }


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

    public BluetoothGattCharacteristic beforeCharacteristicWrite(UUID uuid, WriteCallback callback) {
        if (callback != null) {
            this.writeCallbacks.put(uuid, callback);
        }
        return (BluetoothGattCharacteristic) this.characteristics.get(uuid);
    }

    public Collection<BluetoothGattCharacteristic> getAvailableCharacteristics() {
        List chars = new ArrayList(this.characteristics.values());
        chars.removeAll(Collections.singleton(null));
        return chars;
    }

    public void onCharacteristicWrite(BluetoothGattCharacteristic characteristic, int status) {
        if (mCurrentIsJaaleeNewBeacon) {
            WriteCallback writeCallback = (WriteCallback) this.writeCallbacks.remove(characteristic.getUuid());
            if (status == 0)
                writeCallback.onSuccess();
            else
                writeCallback.onError();
        }
    }

    public String getBeaconName() {
        if (mCurrentIsJaaleeNewBeacon) {
            return this.characteristics.containsKey(JaaleeUuid.BEACON_NEW_NAME_CHAR) ?
                    getStringValue(((BluetoothGattCharacteristic) this.characteristics.get(JaaleeUuid.BEACON_NEW_NAME_CHAR)).getValue()) : null;
        }
        return this.characteristics.containsKey(JaaleeUuid.BEACON_NAME_CHAR) ?
                getStringValue(((BluetoothGattCharacteristic) this.characteristics.get(JaaleeUuid.BEACON_NAME_CHAR)).getValue()) : null;
    }

//	 public boolean isAuthSeedCharacteristic(BluetoothGattCharacteristic characteristic) {
//		 return characteristic.getUuid().equals(JaaleeUuid.AUTH_SEED_CHAR);
//	 }
// 
//	 public boolean isAuthVectorCharacteristic(BluetoothGattCharacteristic characteristic) {
//		 return characteristic.getUuid().equals(JaaleeUuid.AUTH_VECTOR_CHAR);
//	 }
// 
//	 public BluetoothGattCharacteristic getAuthSeedCharacteristic() {
//		 return (BluetoothGattCharacteristic)this.characteristics.get(JaaleeUuid.AUTH_SEED_CHAR);
//	 }
// 
//	 public BluetoothGattCharacteristic getAuthVectorCharacteristic() {
//		 return (BluetoothGattCharacteristic)this.characteristics.get(JaaleeUuid.AUTH_VECTOR_CHAR);
//	 }
}
