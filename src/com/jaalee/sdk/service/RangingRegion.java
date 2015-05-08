 package com.jaalee.sdk.service;
 
 import android.os.Messenger;

import com.jaalee.sdk.Beacon;
import com.jaalee.sdk.Region;
import com.jaalee.sdk.Utils;

 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Comparator;
import java.util.concurrent.ConcurrentHashMap;
/**
 * @author JAALEE, Inc
 * 
 * @Support dev@jaalee.com
 * @Sales: sales@jaalee.com
 * 
 * @see http://www.jaalee.com/
 */

 class RangingRegion
 {
	 private static final Comparator<Beacon> BEACON_ACCURACY_COMPARATOR = new Comparator<Beacon>()
	{
		 public int compare(Beacon lhs, Beacon rhs) 
		 {
			 return Double.compare(Utils.computeAccuracy(lhs), Utils.computeAccuracy(rhs));
		 }
	};

   final Region region;
   final Messenger replyTo;
   final ConcurrentHashMap<Beacon, Long> beacons;
 
   RangingRegion(Region region, Messenger replyTo)
   {
	   this.region = region;
	   this.replyTo = replyTo;
	   this.beacons = new ConcurrentHashMap<Beacon, Long>();
   }
 
   public Collection<Beacon> getSortedBeacons()
   {
	   ArrayList<Beacon> sortedBeacons = new ArrayList<Beacon>(this.beacons.keySet());
	   Collections.sort(sortedBeacons, BEACON_ACCURACY_COMPARATOR);
	   return sortedBeacons;
   }
 }
