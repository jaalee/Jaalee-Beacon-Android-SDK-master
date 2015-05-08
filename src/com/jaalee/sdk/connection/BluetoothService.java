package com.jaalee.sdk.connection;

import android.bluetooth.BluetoothGattCharacteristic;
/**
 * @author JAALEE, Inc
 * 
 * @Support dev@jaalee.com
 * @Sales: sales@jaalee.com
 * 
 * @see http://www.jaalee.com/
 */

public abstract interface BluetoothService
{
  public abstract void update(BluetoothGattCharacteristic paramBluetoothGattCharacteristic);
  public abstract void onCharacteristicWrite(BluetoothGattCharacteristic paramBluetoothGattCharacteristic, int state);
}
