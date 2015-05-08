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
 * 
 * @Support dev@jaalee.com
 * @Sales: sales@jaalee.com
 * 
 * @see http://www.jaalee.com/
 */

public class BeaconTxPower
  implements BluetoothService
{
	private final HashMap<UUID, BluetoothGattCharacteristic> characteristics = new HashMap();
	private final HashMap<UUID, WriteCallback> writeCallbacks = new HashMap();
	
	static boolean mCurrentIsJaaleeNewBeacon = false;//是否是Jaalee新版的固件
	
	public void processGattServices(List<BluetoothGattService> services)
	{
		for (BluetoothGattService service : services)
			if (JaaleeUuid.BEACON_TX_POWER_SERVICE.equals(service.getUuid())) {
				if (service.getCharacteristic(JaaleeUuid.BEACON_TX_POWER_CHAR) != null)
				{
					this.characteristics.put(JaaleeUuid.BEACON_TX_POWER_CHAR, service.getCharacteristic(JaaleeUuid.BEACON_TX_POWER_CHAR));	
				}
			}
	}

	public boolean hasCharacteristic(UUID uuid)
	{
		return this.characteristics.containsKey(uuid);
	}
	
	public void update(BluetoothGattCharacteristic characteristic)
	{
		this.characteristics.put(characteristic.getUuid(), characteristic);
		if (!mCurrentIsJaaleeNewBeacon)
		{
			byte [] Value = characteristic.getValue();
			
			WriteCallback writeCallback = (WriteCallback)this.writeCallbacks.remove(characteristic.getUuid());
			if (writeCallback != null)
			{
				if (Value[0] == 1)
				{
					writeCallback.onSuccess();
				}
				else
				{
					writeCallback.onError();
				}
			}
		}
	}
	
	public BluetoothGattCharacteristic beforeCharacteristicWrite(UUID uuid, WriteCallback callback) {
		if (callback != null)
		{
			this.writeCallbacks.put(uuid, callback);
		}
		return (BluetoothGattCharacteristic)this.characteristics.get(uuid);
	}
 
	public void onCharacteristicWrite(BluetoothGattCharacteristic characteristic, int status) {
		if (mCurrentIsJaaleeNewBeacon)
		{
			WriteCallback writeCallback = (WriteCallback)this.writeCallbacks.remove(characteristic.getUuid());
			if (status == 0)
				writeCallback.onSuccess();
			else
				writeCallback.onError();			
		}
	}

	public Collection<BluetoothGattCharacteristic> getAvailableCharacteristics() {
		List chars = new ArrayList(this.characteristics.values());
		chars.removeAll(Collections.singleton(null));
		return chars;
	}

	public Integer getBeaconTxPower() {
		int temp = this.characteristics.containsKey(JaaleeUuid.BEACON_TX_POWER_CHAR) ? Integer.valueOf(getUnsignedByte(((BluetoothGattCharacteristic)this.characteristics.get(JaaleeUuid.BEACON_TX_POWER_CHAR)).getValue())) : null;
		int txPower;
		if (mCurrentIsJaaleeNewBeacon)
		{
			switch(temp)
			{
				case 1:
					txPower = 4;
					break;
				case 2:
					txPower = 0;
					break;
				case 3:
					txPower = -4;
					break;
				case 4:
					txPower = -8;
					break;
				case 5:
					txPower = -12;
					break;
				case 6:
					txPower = -16;
					break;
				case 7:
					txPower = -20;
					break;
				case 8:
					txPower = -30;
					break;
				case 9:
					txPower = -40;
					break;
				default:
					txPower = 4;
					break;				
			}
		}
		else
		{
			switch(temp)
			{
				case 0:
					txPower = 0;
					break;			
				case 1:
					txPower = -6;
					break;
				case 2:
					txPower = -23;
					break;
				default:
					txPower = 0;
					break;				
			}			
		}
		return txPower;
	}
	
	private static int getUnsignedByte(byte[] bytes) {
		return unsignedByteToInt(bytes[0]);
	}
	private static int unsignedByteToInt(byte value)
	{
		return value & 0xFF;
	}
}
