// Copyright 2011 Tom Walsh
//
// This program is free software released under version 3
// of the GPL.  See file gpl.txt for more information.

package com.github.tommywalsh.mbta;


import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import java.util.AbstractMap;
import java.util.Vector;

public class ProximityProfileGenerator
{

    public static void new_getProximityProfile(SQLiteDatabase db, double lat, double lng, double radius)
    {
	final double rLat = latsPerMile * radius;
	final double rLng = lngsPerMile * radius;

        Double minLat = new Double(lat - rLat);
        Double maxLat = new Double(lat + rLat);
        Double minLng = new Double(lng - rLng);
        Double maxLng = new Double(lng + rLng);

        Profile p = new Profile();
        p.name = "Nearby Busses";


        String sql = "SELECT lat,lng,title,subroute FROM departure_point,stop " +
            " WHERE stop.tag = departure_point.stop " +
            " AND lat < " + maxLat.toString() +
            " AND lat > " + minLat.toString() +
            " AND lng < " + maxLng.toString() +
            " AND lng > " + minLng.toString() + 
            " ORDER BY subroute";

        Cursor cursor = db.rawQuery(sql, null);
        cursor.moveToFirst();
        String currentSubRoute = null;
        String currentTitle = new String();
        double currentDistance = 0.0;
        while (!cursor.isAfterLast()) {
            double newLat = Double.parseDouble(cursor.getString(0));
            double newLng = Double.parseDouble(cursor.getString(1));
            double newDistance = distanceBetween(newLat, newLng, lat, lng);
            String newSubRoute = cursor.getString(3);
            
            if (newSubRoute.equals(currentSubRoute)) {
                // This is not the first stop we've seen on this route
                if (newDistance < currentDistance) {
                    // This point is closer that the other ones on this route
                    currentDistance = newDistance;
                    currentTitle = cursor.getString(2);
                }
                // otherwise throw this point away
            } else {
                // This is the first point on a new route
                if (currentSubRoute != null) {
                    // commit the old one here
                    android.util.Log.d("mbta", currentTitle + " on route " + currentSubRoute);
                }
                currentSubRoute = newSubRoute;
                currentDistance = newDistance;
                currentTitle = cursor.getString(2);
            }
                
            cursor.moveToNext();
        }
        

    }

    // Given a postion (lat/lng), and a radius (in miles),
    // returns a profile that the closest DeparturePoints for all
    // routes that stop within the given radius
    public static Profile getProximityProfile(double lat, double lng, double radius)
    {
        Profile p = new Profile();
        p.name = "Nearby Busses";

	for (Route ri: Route.getAllRoutes()) {
            for (StopHelper sih : getClosestStop(lat, lng, ri, radius)) {
                p.stops.addElement(sih.dp);
            }
        }

        return p;
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
        public DeparturePoint dp;
        public double distance;
    }

    private static Vector<StopHelper> getClosestStopHelper(double lat, double lng, Route ri)
    {
	Vector<StopHelper> stops = new Vector<StopHelper>();
	AbstractMap<String, Vector<Stop>> sm = ri.getStopMap();
	for (String dir : sm.keySet()) {
	    StopHelper sih = null;
	    
	    for (Stop si : sm.get(dir)) {
		double thisDistance = distanceBetween(lat, lng, si.lat, si.lng);
		if (sih == null || thisDistance < sih.distance) {
		    sih = new StopHelper();
                    sih.dp = new DeparturePoint(si, ri, dir);
		    sih.distance = thisDistance;
		}
	    }
	    if (sih != null) {
		stops.addElement(sih);
	    }
	}
	return stops;
    }
   
	
    private static Vector<StopHelper> getClosestStops(double lat, double lng, Route ri)
    {
	return getClosestStopHelper(lat, lng, ri);
    }

    // return null if no stop is within the maximum acceptable distance
    private static Vector<StopHelper> getClosestStop(double lat, double lng, Route ri, double maxDistance)
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
