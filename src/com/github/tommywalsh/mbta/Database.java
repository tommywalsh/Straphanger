// Copyright 2011 Tom Walsh
//
// This program is free software released under version 3
// of the GPL.  See file gpl.txt for more information.

package com.github.tommywalsh.mbta;

import android.content.Context;
import android.database.Cursor;
import android.database.CursorWrapper;
import android.database.sqlite.SQLiteDatabase;

import java.util.Vector;
public class Database
{
    private SQLiteDatabase m_db;
    public Database(Context context) {
        MBTADBOpenHelper openHelper = new MBTADBOpenHelper(context.getApplicationContext());
	m_db = openHelper.getReadableDatabase();
    }





    public class RouteStopCursorWrapper extends CursorWrapper
    {
	public RouteStopCursorWrapper(Cursor cursor) {
	    super(cursor);
	}
	public String getStopTag() {
	    return getString(0);
	}
	public String getRouteTag() {
	    return getString(1);
	}
    }

    public RouteStopCursorWrapper getRoutesAndStopsForDeparturePoints(Vector<Integer> departurePoints)
    {
	String query = "SELECT stop.tag,subroute.route,departure_point.id " +
	    " FROM stop,subroute,departure_point " +
	    " WHERE stop.tag = departure_point.stop " +
	    " AND subroute.tag = departure_point.subroute " +
	    " AND departure_point.id IN ( ";
	
	boolean comma = false;
	for (Integer pt : departurePoints) {
	    if (comma) {
		query += ",";
	    } else {
		comma = true;
	    }
	    query += pt.toString();
	}
	query += ")";
	
        Cursor cursor = m_db.rawQuery(query, null);
	return new RouteStopCursorWrapper(cursor);
    }








    public class NearbyDeparturePointCursorWrapper extends CursorWrapper
    {
	public NearbyDeparturePointCursorWrapper(Cursor cursor) {
	    super(cursor);
	}
	public double getLatitude() {
	    return getDouble(0);
	}
	public double getLongitude() {
	    return getDouble(1);
	}
	public String getSubrouteTag() {
	    return getString(2);
	}
	public int getDeparturePointId() {
	    return getInt(3);
	}
    }


    // These are approximations that only make sense near Boston
    private static final double latsPerMile = 0.0144578;
    private static final double lngsPerMile = 0.019566791;

    public NearbyDeparturePointCursorWrapper getNearbyDeparturePoints(double lat, double lng, double radius)
    {
	final double rLat = latsPerMile * radius;
	final double rLng = lngsPerMile * radius;

        Double minLat = new Double(lat - rLat);
        Double maxLat = new Double(lat + rLat);
        Double minLng = new Double(lng - rLng);
        Double maxLng = new Double(lng + rLng);

        String sql = "SELECT lat,lng,subroute,departure_point.id FROM departure_point,stop " +
            " WHERE stop.tag = departure_point.stop " +
            " AND lat < " + maxLat.toString() +
            " AND lat > " + minLat.toString() +
            " AND lng < " + maxLng.toString() +
            " AND lng > " + minLng.toString() + 
            " ORDER BY subroute";
	
        Cursor cursor = m_db.rawQuery(sql, null);
	return new NearbyDeparturePointCursorWrapper(cursor);
    }






    public class ProfileCursorWrapper extends CursorWrapper
    {
	public ProfileCursorWrapper(Cursor cursor) {
	    super(cursor);
	}
	public String getProfileName() {
	    return getString(1);
	}
	public int getProfileId() {
	    return getInt(0);
	}
    }



    public ProfileCursorWrapper getProfiles()
    {
	// TODO: Make this a precompiled sql statement
	String sql = "SELECT id,name FROM profile"; // TODO: make this a pre-c
        Cursor cursor = m_db.rawQuery(sql, null);
	return new ProfileCursorWrapper(cursor);
    }




    public class DeparturePointCursorWrapper extends CursorWrapper
    {
	public DeparturePointCursorWrapper(Cursor cursor) {
	    super(cursor);
	}
	public int getDeparturePointId() {
	    return getInt(0);
	}
    }


    public DeparturePointCursorWrapper getDeparturePointsInProfile(int profileId)
    {
	String query = "SELECT point FROM profile_point WHERE profile = " + Integer.toString(profileId);
        Cursor cursor = m_db.rawQuery(query, null);
	return new DeparturePointCursorWrapper(cursor);
    }

}

