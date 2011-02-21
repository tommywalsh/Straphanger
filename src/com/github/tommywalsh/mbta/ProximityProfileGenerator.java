// Copyright 2011 Tom Walsh
//
// This program is free software released under version 3
// of the GPL.  See file gpl.txt for more information.

package com.github.tommywalsh.mbta;


import android.content.Context;
import java.util.Vector;

public class ProximityProfileGenerator
{
    // Given a postion (lat/lng), and a radius (in miles),
    // returns a profile that the closest DeparturePoints for all
    // routes that stop within the given radius
    public static Vector<Integer> getProximityProfile(Context context, double lat, double lng, double radius)
    {
	Database db = new Database(context);
	Database.NearbyDeparturePointCursorWrapper cursor = db.getNearbyDeparturePoints(lat, lng, radius);

        cursor.moveToFirst();
        String currentSubRoute = null;
        double currentDistance = 0.0;
	int currentDeparturePoint = -1;
	Vector<Integer> departurePoints = new Vector<Integer>();

        while (!cursor.isAfterLast()) {
            double newLat = cursor.getLatitude();
            double newLng = cursor.getLongitude();
            String newSubRoute = cursor.getSubrouteTag();
	    int newDeparturePoint = cursor.getDeparturePointId();

            double newDistance = distanceBetween(newLat, newLng, lat, lng);

            
            if (newSubRoute.equals(currentSubRoute)) {
                // This is not the first stop we've seen on this route

		// If it's closer that all the others we've seen...
                if (newDistance < currentDistance) {
                    // ... then remember it...
                    currentDistance = newDistance;
		    currentDeparturePoint = newDeparturePoint;
                }
                // ... otherwise throw it away

            } else {
                // This is the first point on a new route

		// If this is not the first new route...
                if (currentSubRoute != null) {

                    // ... then store the closest point from the previous route
		    departurePoints.addElement(currentDeparturePoint);
                }
                currentSubRoute = newSubRoute;
                currentDistance = newDistance;
		currentDeparturePoint = newDeparturePoint;
            }
            cursor.moveToNext();
        }
        cursor.close();
        db.close();

	// Don't forget to store the closest point from the final route
	departurePoints.addElement(currentDeparturePoint);

	return departurePoints;
    }


    private static double distanceBetween(double lat1, double lng1, double lat2, double lng2) 
    {
	float[] results = {0.0f};
	android.location.Location.distanceBetween(lat1, lng1,
						  lat2, lng2,
						  results);
	return (double)results[0];
    }
    
}
