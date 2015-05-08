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

public class AlertService
  implements BluetoothService
{
	private final HashMap<UUID, BluetoothGattCharacteristic> characteristics = new HashMap();
	private final HashMap<UUID, WriteCallback> writeCallbacks = new HashMap();
	
	public void processGattServices(List<BluetoothGattService> services)
	{
		for (BluetoothGattService service : services)
			if (JaaleeUuid.BEACON_ALERT.equals(service.getUuid())) {
				
				if (service.getCharacteristic(JaaleeUuid.BEACON_ALERT_CHAR) != null)
				{
					this.characteristics.put(JaaleeUuid.BEACON_ALERT_CHAR, service.getCharacteristic(JaaleeUuid.BEACON_ALERT_CHAR));	
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
	}
	
	public BluetoothGattCharacteristic beforeCharacteristicWrite(UUID uuid, WriteCallback callback) {
		if (callback != null)
		{
			this.writeCallbacks.put(uuid, callback);
		}
		return (BluetoothGattCharacteristic)this.characteristics.get(uuid);
	}
 
	public void onCharacteristicWrite(BluetoothGattCharacteristic characteristic, int status) {
		WriteCallback writeCallback = (WriteCallback)this.writeCallbacks.remove(characteristic.getUuid());
		if (writeCallback != null)
		{
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
}
