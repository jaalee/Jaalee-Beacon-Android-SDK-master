 package com.jaalee.sdk;
 
 import android.bluetooth.BluetoothAdapter;
 import android.bluetooth.BluetoothManager;
 import android.content.ComponentName;
 import android.content.Context;
 import android.content.Intent;
 import android.content.ServiceConnection;
 import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
 import android.os.Handler;
 import android.os.IBinder;
 import android.os.Message;
 import android.os.Messenger;
 import android.os.RemoteException;
import com.jaalee.sdk.internal.Preconditions;
import com.jaalee.sdk.service.BeaconService;
import com.jaalee.sdk.service.MonitoringResult;
import com.jaalee.sdk.service.RangingResult;
import com.jaalee.sdk.service.ScanPeriodData;
import com.jaalee.sdk.utils.L;

 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author JAALEE, Inc
 * 
 * @Support dev@jaalee.com
 * @Sales: sales@jaalee.com
 * 
 * @see http://www.jaalee.com/
 */

 public class BeaconManager
 {
//   private static final String ANDROID_MANIFEST_CONDITIONS_MSG = "AndroidManifest.xml does not contain android.permission.BLUETOOTH or android.permission.BLUETOOTH_ADMIN permissions. BeaconService may be also not declared in AndroidManifest.xml.";
   private final Context context;
   private final InternalServiceConnection serviceConnection;
   private final Messenger incomingMessenger;
   private final Set<String> rangedRegionIds;
   private final Set<String> monitoredRegionIds;
   private Messenger serviceMessenger;
   private RangingListener rangingListener;
   private MonitoringListener monitoringListener;
   private DeviceDiscoverListener DiscoverBLEListener;
   private ErrorListener errorListener;
   private ServiceReadyCallback callback;
   private ScanPeriodData foregroundScanPeriod;
   private ScanPeriodData backgroundScanPeriod;
 
   public BeaconManager(Context context)
   {
      this.context = ((Context)Preconditions.checkNotNull(context));
      this.serviceConnection = new InternalServiceConnection();
      this.incomingMessenger = new Messenger(new IncomingHandler());
      this.rangedRegionIds = new HashSet<String>();
      this.monitoredRegionIds = new HashSet<String>();
   }
 
   /**
    * @return Returns true if device supports Bluetooth Low Energy.
    */
   public boolean hasBluetooth()
   {
     return this.context.getPackageManager().hasSystemFeature("android.hardware.bluetooth_le");
   }
   
   
 /**
  * 
  * @return Returns true if device supports Bluetooth Low Energy.
  */
   public boolean isBluetoothEnabled()
   {
	   if (!checkPermissionsAndService()) {
		   L.e("AndroidManifest.xml does not contain android.permission.BLUETOOTH or android.permission.BLUETOOTH_ADMIN permissions. BeaconService may be also not declared in AndroidManifest.xml.");
	       return false;
     }
	   BluetoothManager bluetoothManager = (BluetoothManager)this.context.getSystemService("bluetooth");
	   BluetoothAdapter adapter = bluetoothManager.getAdapter();
	   return (adapter != null) && (adapter.isEnabled());
   }
 
   /**
    * 
    * @return Checks if required Bluetooth permissions are set (android.permission.BLUETOOTH and android.permission.BLUETOOTH_ADMIN) along with BeaconService in AndroidManifest.xml.
    */
   public boolean checkPermissionsAndService()
   {
	   PackageManager pm = this.context.getPackageManager();
	   int bluetoothPermission = pm.checkPermission("android.permission.BLUETOOTH", this.context.getPackageName());
	   int bluetoothAdminPermission = pm.checkPermission("android.permission.BLUETOOTH_ADMIN", this.context.getPackageName());
 
	    Intent intent = new Intent(this.context, BeaconService.class);
	     List<ResolveInfo> resolveInfo = pm.queryIntentServices(intent, 65536);
 
	     return (bluetoothPermission == 0) && (bluetoothAdminPermission == 0) && (resolveInfo.size() > 0);
   }
 
   /**
    * Connects to BeaconService.
    * @param callback Callback to be invoked when connection is made to service.
    */
   public void connect(ServiceReadyCallback callback)
   {
	   if (!checkPermissionsAndService()) 
	   {
		   L.e("AndroidManifest.xml does not contain android.permission.BLUETOOTH or android.permission.BLUETOOTH_ADMIN permissions. BeaconService may be also not declared in AndroidManifest.xml.");
	   }
	   this.callback = ((ServiceReadyCallback)Preconditions.checkNotNull(callback, "callback cannot be null"));
	   if (isConnectedToService()) {
		   callback.onServiceReady();
	   }
	   boolean bound = this.context.bindService(new Intent(this.context, BeaconService.class), this.serviceConnection, 1);
 
	   if (!bound)
		   L.wtf("Could not bind service");
   }
 
   /**
    * Disconnects from BeaconService. If there were any ranging or monitoring in progress, they will be stopped. This should be typically called in onDestroy method.
    */
   public void disconnect()
   {
	   if (!isConnectedToService()) 
	   {
		   L.i("Not disconnecting because was not connected to service");
		   return;
	   }
	   
	   //hrb спринй
	   CopyOnWriteArrayList<String> tempRangedRegionIds = new CopyOnWriteArrayList<String>(this.rangedRegionIds);
	   for (String regionId : tempRangedRegionIds)
	   {
		   try 
		   {
			   internalStopRanging(regionId);
		   } 
		   catch (RemoteException e) 
		   {
			   L.e("Swallowing error while disconnect/stopRanging", e);
		   }
	   }
	  
	   CopyOnWriteArrayList<String> tempMonitoredRegionIds = new CopyOnWriteArrayList<String>(this.monitoredRegionIds);
	   for (String regionId : tempMonitoredRegionIds) 
	   {
		   try 
		   {
			   internalStopMonitoring(regionId);
		   }
		   catch (RemoteException e) 
		   {
			   L.e("Swallowing error while disconnect/stopMonitoring", e);
		   }
	   }
	   this.context.unbindService(this.serviceConnection);
	   this.serviceMessenger = null;
   }

   /**
    * Sets new ranging listener. Old one will be discarded.
    * @param listener The listener
    */
   public void setRangingListener(RangingListener listener)
   {
	   this.rangingListener = ((RangingListener)Preconditions.checkNotNull(listener, "listener cannot be null"));
   }
 
   /**
    * Sets new monitoring listener. Old one will be discarded.
    * @param listener the new listener
    */
   public void setMonitoringListener(MonitoringListener listener)
   {
	   this.monitoringListener = ((MonitoringListener)Preconditions.checkNotNull(listener, "listener cannot be null"));
   }
   
   
   /**
    * Sets new ble device discover listener
    * @param listener the new listener
    */
   public void setDeviceDiscoverListener(DeviceDiscoverListener listener)
   {
	   this.DiscoverBLEListener = ((DeviceDiscoverListener)Preconditions.checkNotNull(listener, "listener cannot be null")); 
   }
 
   /**
    * Sets new error listener. Old one will be discarded.
    * @param listener The new listener
    */
   public void setErrorListener(ErrorListener listener)
   {
	   this.errorListener = listener;
	   if ((isConnectedToService()) && (listener != null))
		   registerErrorListenerInService();
   }
 
   /**
    * Changes defaults scanning periods when ranging is performed. Default values: scanPeriod=1s, waitTime=0s
    * @param scanPeriodMillis  How long to perform Bluetooth Low Energy scanning?
    * @param waitTimeMillis  How long to wait until performing next scanning?
    */
   public void setForegroundScanPeriod(long scanPeriodMillis, long waitTimeMillis)
   {
	   if (isConnectedToService())
			setScanPeriod(new ScanPeriodData(scanPeriodMillis, waitTimeMillis), 10);
     else
    	 this.foregroundScanPeriod = new ScanPeriodData(scanPeriodMillis, waitTimeMillis);
   }
   
   
 /**
  * Changes defaults scanning periods when monitoring is performed. Default values: scanPeriod=5s, waitTime=25s
  * @param scanPeriodMillis How long to perform Bluetooth Low Energy scanning?
  * @param waitTimeMillis How long to wait until performing next scanning?
  */
   public void setBackgroundScanPeriod(long scanPeriodMillis, long waitTimeMillis)
   {
	   if (isConnectedToService())
		   setScanPeriod(new ScanPeriodData(scanPeriodMillis, waitTimeMillis), 9);
	   else
    	 this.backgroundScanPeriod = new ScanPeriodData(scanPeriodMillis, waitTimeMillis);
   }
 
   
   private void setScanPeriod(ScanPeriodData scanPeriodData, int msgId)
   {
	   Message scanPeriodMsg = Message.obtain(null, msgId);
	   scanPeriodMsg.obj = scanPeriodData;
	   try {
	       this.serviceMessenger.send(scanPeriodMsg);
	   } catch (RemoteException e) {
    	 L.e("Error while setting scan periods: " + msgId);
	   }
   }
 
   private void registerErrorListenerInService() {
	   Message registerMsg = Message.obtain(null, 7);
	   registerMsg.replyTo = this.incomingMessenger;
	   try {
		   this.serviceMessenger.send(registerMsg);
	   } catch (RemoteException e) {
		   L.e("Error while registering error listener");
	   }
   }
   
//   /**
//    * Starts ranging given range. Ranging results will be delivered to listener registered via setRangingListener(RangingListener). If given region is already ranged, this is no-op.
//    * @param region Region to range.
//    * @throws RemoteException  If communication with service failed.
//    */
//   public void startDiscoverBLEDevice()
//     throws RemoteException
//   {
//	   	if (!isConnectedToService()) {
//	   		L.i("Not starting discover, not connected to service");
//	   		return;
//	   	}	   	
// 
//	   	Message startRangingMsg = Message.obtain(null, 100);	   	
//	   	startRangingMsg.replyTo = this.incomingMessenger;
//	   	try {
//	   		this.serviceMessenger.send(startRangingMsg);
//	   	} catch (RemoteException e) {
//	   		L.e("Error while starting discover", e);
//	   		throw e;
//	   	}
//   }   
 
   
   /**
    * Starts ranging given range. Ranging results will be delivered to listener registered via setRangingListener(RangingListener). If given region is already ranged, this is no-op.
    * It will also disvocer all the ble device around the phone. 
    * @param region Region to range.
    * @throws RemoteException  If communication with service failed.
    */
   public void startRangingAndDiscoverDevice(Region region)
     throws RemoteException
   {
	   	if (!isConnectedToService()) {
	   		L.i("Not starting ranging and discover ble device, not connected to service");
	   		return;
	   	}
	   	Preconditions.checkNotNull(region, "region cannot be null");
 
	   	if (this.rangedRegionIds.contains(region.getIdentifier())) {
	   		L.i("Region already ranged but that's OK: " + region);
	   	}
	   	this.rangedRegionIds.add(region.getIdentifier());
 
	   	Message startRangingMsg = Message.obtain(null, 1);
	   	startRangingMsg.obj = region;
	   	startRangingMsg.replyTo = this.incomingMessenger;
	   	try {
	   		this.serviceMessenger.send(startRangingMsg);
	   	} catch (RemoteException e) {
	   		L.e("Error while starting ranging", e);
	   		throw e;
	   	}
	   	
	   	startRangingMsg = Message.obtain(null, 100);	   	
	   	startRangingMsg.replyTo = this.incomingMessenger;
	   	try {
	   		this.serviceMessenger.send(startRangingMsg);
	   	} catch (RemoteException e) {
	   		L.e("Error while starting discover", e);
	   		throw e;
	   	}
	   	
   }
 
   public void stopRanging(Region region) throws RemoteException {
	   if (!isConnectedToService()) {
		   L.i("Not stopping ranging, not connected to service");
		   return;
	   }
	   Preconditions.checkNotNull(region, "region cannot be null");
	   internalStopRanging(region.getIdentifier());
   }
 
   private void internalStopRanging(String regionId) throws RemoteException {
	   this.rangedRegionIds.remove(regionId);
	   Message stopRangingMsg = Message.obtain(null, 2);
	   stopRangingMsg.obj = regionId;
	   try {
		   this.serviceMessenger.send(stopRangingMsg);
		   } catch (RemoteException e) {
			   L.e("Error while stopping ranging", e);
			   throw e;
			   }
	   }
 
   public void startMonitoring(Region region) throws RemoteException {
	   if (!isConnectedToService()) {
		   L.i("Not starting monitoring, not connected to service");
		   return;
	   }
	   
	   Preconditions.checkNotNull(region, "region cannot be null");
	   
	   if (this.monitoredRegionIds.contains(region.getIdentifier())) {
		   L.i("Region already monitored but that's OK: " + region);
	   }
	   
	   this.monitoredRegionIds.add(region.getIdentifier());
 
	   Message startMonitoringMsg = Message.obtain(null, 4);	   
	   startMonitoringMsg.obj = region;
	   startMonitoringMsg.replyTo = this.incomingMessenger;
	   
	   try {
		   this.serviceMessenger.send(startMonitoringMsg);
	   } catch (RemoteException e) 
	   {
		   L.e("Error while starting monitoring", e);
		   throw e;
	   }
   }
 
   public void stopMonitoring(Region region) throws RemoteException {
	   if (!isConnectedToService()) {
		   L.i("Not stopping monitoring, not connected to service");
		   return;
	   }
	   Preconditions.checkNotNull(region, "region cannot be null");
	   internalStopMonitoring(region.getIdentifier());
   }
 
   private void internalStopMonitoring(String regionId) throws RemoteException {
	   this.monitoredRegionIds.remove(regionId);
	   Message stopMonitoringMsg = Message.obtain(null, 5);
	   stopMonitoringMsg.obj = regionId;
	   try {
		   this.serviceMessenger.send(stopMonitoringMsg);
	   } catch (RemoteException e) {
		   L.e("Error while stopping ranging");
		   throw e;
	   }
   }
 
   private boolean isConnectedToService() {
	   return this.serviceMessenger != null;
   }
 
   private class IncomingHandler extends Handler
   {
     private IncomingHandler()
     {
     }
 
     public void handleMessage(Message msg)
     {
    	 switch (msg.what) {
    	 case 3:
    		 if (BeaconManager.this.rangingListener != null) {
	         RangingResult rangingResult = (RangingResult)msg.obj;
	         BeaconManager.this.rangingListener.onBeaconsDiscovered(rangingResult.region, rangingResult.beacons);
    		 }break;
    	 case 6:
    		 if (BeaconManager.this.monitoringListener != null) {
    			 MonitoringResult monitoringResult = (MonitoringResult)msg.obj;
    			 if (monitoringResult.state == Region.State.INSIDE)
    				 BeaconManager.this.monitoringListener.onEnteredRegion(monitoringResult.region);
    			 else
    				 BeaconManager.this.monitoringListener.onExitedRegion(monitoringResult.region);
    		 }
    		 break;
    	 case 8:
    		 if (BeaconManager.this.errorListener != null) {
    			 Integer errorId = (Integer)msg.obj;
    			 BeaconManager.this.errorListener.onError(errorId);
    		 }
    		 break;
    	 case 100:
    		 if (BeaconManager.this.DiscoverBLEListener != null) {
    			 BLEDevice DiscoveringResult = (BLEDevice)msg.obj;
    	         BeaconManager.this.DiscoverBLEListener.onBLEDeviceDiscovered(DiscoveringResult);
    		 } 		 
    		 break;
    	 default:
    		 L.d("Unknown message: " + msg);
    		 break;
    	 }
     }
   }
 
   private class InternalServiceConnection
     implements ServiceConnection
   {
     private InternalServiceConnection()
     {
     }
 
     public void onServiceConnected(ComponentName name, IBinder service)
     {
    	 BeaconManager.this.serviceMessenger = new Messenger(service);
    	 if (BeaconManager.this.errorListener != null) {
    		 BeaconManager.this.registerErrorListenerInService();
    	 }
    	 if (BeaconManager.this.foregroundScanPeriod != null) {
    		 BeaconManager.this.setScanPeriod(BeaconManager.this.foregroundScanPeriod, 9);
    		 BeaconManager.this.foregroundScanPeriod = null;
    	 }
    	 if (BeaconManager.this.backgroundScanPeriod != null) {
    		 BeaconManager.this.setScanPeriod(BeaconManager.this.backgroundScanPeriod, 10);
    		 BeaconManager.this.backgroundScanPeriod = null;
    	 }
    	 if (BeaconManager.this.callback != null) {
    		 BeaconManager.this.callback.onServiceReady();
    		 BeaconManager.this.callback = null;
    	 }
     }
 
     public void onServiceDisconnected(ComponentName name)
     {
    	 L.e("Service disconnected, crashed? " + name);
    	 BeaconManager.this.serviceMessenger = null;
     }
   }
 
 }

