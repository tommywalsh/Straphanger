// Copyright 2011 Tom Walsh
//
// This program is free software released under version 3
// of the GPL.  See file gpl.txt for more information.

package com.github.tommywalsh.mbta;


import android.content.Context;
import java.util.AbstractMap;
import java.util.Vector;

public class ProximityProfileGenerator
{

    // Given a postion (lat/lng), and a radius (in miles),
    // returns a profile that the closest DeparturePoints for all
    // routes that stop within the given radius
    public static Profile getProximityProfile(double lat, double lng, double radius)
    {
        Profile p = new Profile();
        p.name = "Nearby Busses";

	for (RouteInfo ri: RouteInfo.getAllRoutes()) {
            for (StopHelper sih : getClosestStop(lat, lng, ri, radius)) {
                // We should cut this conversion stuff out!
                // Stop/Departure types should be melded with Stop/RouteInfo types
                DeparturePoint s = new DeparturePoint();
                s.route = ri.tag;
                s.tag = sih.si.tag;
                p.stops.addElement(s);
            }
        }

        return p;
    }

    public static void doit(Context context)
    {
	
	for (RouteInfo ri: RouteInfo.getAllRoutes()) {

	    // my house
	    double lat = 42.379159;
	    double lng = -71.099908;
	    
	    for (StopHelper sih : getClosestStop(lat, lng, ri, 0.5)) {
		android.util.Log.d("MBTA", ri.title + ": " + sih.direction + " stops at " + sih.si.title);
	    }
	}

    }


    // These are approximations that only make sense near Boston
    private static final double latsPerMile = 0.0144578;
    private static final double lngsPerMile = 0.019566791;
    private static final double milesPerMeter = 0.000621;

    private static double distanceBetween(double lat1, double lng1, double lat2, double lng2) 
    {
	float[] results = {0.0f};
	android.location.Location.distanceBetween(lat1, lng1,
						  lat2, lng2,
						  results);
	return (double)results[0];
    }

    static private class StopHelper {
	public Stop si;
	public double distance;
	public String direction;
    }

    private static Vector<StopHelper> getClosestStopHelper(double lat, double lng, RouteInfo ri)
    {
	Vector<StopHelper> stops = new Vector<StopHelper>();
	AbstractMap<String, Vector<Stop>> sm = ri.getStopMap();
	for (String dir : sm.keySet()) {
	    StopHelper sih = null;
	    
	    for (Stop si : sm.get(dir)) {
		double thisDistance = distanceBetween(lat, lng, si.lat, si.lng);
		if (sih == null || thisDistance < sih.distance) {
		    sih = new StopHelper();
		    sih.si = si;
		    sih.direction = dir;
		    sih.distance = thisDistance;
		}
	    }
	    if (sih != null) {
		stops.addElement(sih);
	    }
	}
	return stops;
    }
   
	
    private static Vector<StopHelper> getClosestStops(double lat, double lng, RouteInfo ri)
    {
	return getClosestStopHelper(lat, lng, ri);
    }

    // return null if no stop is within the maximum acceptable distance
    private static Vector<StopHelper> getClosestStop(double lat, double lng, RouteInfo ri, double maxDistance)
    {
	double rLat = latsPerMile * maxDistance;
	double minLat = lat - rLat;
	double maxLat = lat + rLat;
	double rLng = lngsPerMile * maxDistance;
	double minLng = lng - rLng;
	double maxLng = lng + rLng;
	   
	// if our acceptable range is entirely higher or lower than the available range,
	// the lat/lng is no good
	boolean latOk = !( (ri.maxLat < minLat) || (maxLat < ri.minLat) );
	boolean lngOk = !( (ri.maxLng < minLng) || (maxLng < ri.minLng) );

	Vector<StopHelper> retval = new Vector<StopHelper>();	
	if (latOk && lngOk) {
	    Vector<StopHelper> candidates = getClosestStopHelper(lat, lng, ri);
	    for (StopHelper sih : candidates) {
		if (sih.distance*0.000621 < maxDistance) {
		    retval.addElement(sih);
		}
	    }
	}
	return retval;
    }
    
}
