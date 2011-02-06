// Copyright 2011 Tom Walsh
//
// This program is free software released under version 3
// of the GPL.  See file gpl.txt for more information.

package com.github.tommywalsh.mbta;

import java.io.FileInputStream;
import android.sax.RootElement;
import android.sax.Element;
import android.sax.StartElementListener;
import android.util.Xml;

import android.content.Context;

import org.xml.sax.Attributes;

import java.io.InputStream;
import java.util.Vector;

public class ProximityTest
{

    // These are approximations that only make sense near Boston
    public static final double latsPerMile = 0.0144578;
    public static final double lngsPerMile = 0.019566791;
    public static final double milesPerMeter = 0.000621;

    public class StopInfo {
	public String tag;
	public double lat;
	public double lng;
	public String title;
    }

    public class RouteInfo {
	public String tag;
	public String title;
	public double minLat;
	public double minLng;
	public double maxLat;
	public double maxLng;
	public Vector<StopInfo> stops;
    }

    private static RouteInfo getRouteFromFile(String filename) 
    {
	try {
	    java.io.File file = new java.io.File(filename);
	    FileInputStream fis = new FileInputStream(file);
	    ProximityTest pt = new ProximityTest();
	    return pt.parse(fis);
	} catch (java.io.FileNotFoundException e) {
	    android.util.Log.d("MBTA", e.toString());
	} catch (java.io.IOException e) {
	    android.util.Log.d("MBTA", e.toString());
	}
	return null;
    }

    public RouteInfo parse(InputStream is) {
		
	final String NS = "";

	final RouteInfo ri = new RouteInfo();

	RootElement root = new RootElement("body");

	Element route = root.getChild(NS, "route");
	route.setStartElementListener(new StartElementListener() {
		public void start(Attributes atts) {
		    ri.tag = atts.getValue("tag");
		    ri.title = atts.getValue("title");
		    ri.minLat = Double.parseDouble(atts.getValue("latMin"));
		    ri.maxLat = Double.parseDouble(atts.getValue("latMax"));
		    ri.minLng = Double.parseDouble(atts.getValue("lonMin"));
		    ri.maxLng = Double.parseDouble(atts.getValue("lonMax"));
		    ri.stops = new Vector<StopInfo>();
		}
	    });

	Element stop = route.getChild(NS, "stop");
	stop.setStartElementListener(new StartElementListener() {
		public void start(Attributes atts) {
		    StopInfo si = new StopInfo();
		    si.tag = atts.getValue("tag");
		    si.title = atts.getValue("title");
		    si.lat = Double.parseDouble(atts.getValue("lat"));
		    si.lng = Double.parseDouble(atts.getValue("lon"));
		    ri.stops.addElement(si);
		}
	    });

	try {
	    Xml.parse(is, Xml.Encoding.UTF_8, root.getContentHandler());
	} catch (Exception e) {
	    android.util.Log.d("mbta", e.toString());
	}
	
	return ri;
	
    }

    static double distanceBetween(double lat1, double lng1, double lat2, double lng2) 
    {
	float[] results = {0.0f};
	android.location.Location.distanceBetween(lat1, lng1,
						  lat2, lng2,
						  results);
	return (double)results[0];
    }

    static private class StopInfoHelper {
	public StopInfo si;
	public double distance;
    }

    static StopInfoHelper getClosestStopHelper(double lat, double lng, RouteInfo ri)
    {
	StopInfoHelper sih = null;
	
	for (StopInfo si : ri.stops) {
	    double thisDistance = distanceBetween(lat, lng, si.lat, si.lng);
	    if (sih == null || thisDistance < sih.distance) {
		sih = new StopInfoHelper();
		sih.si = si;
		sih.distance = thisDistance;
	    }
	}
	return sih;
    }
   
	
    static StopInfo getClosestStop(double lat, double lng, RouteInfo ri)
    {
	StopInfoHelper sih = getClosestStopHelper(lat, lng, ri);
	return sih.si;
    }

    // return null if no stop is within the maximum acceptable distance
    static StopInfo getClosestStop(double lat, double lng, RouteInfo ri, double maxDistance)
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

	StopInfo si = null;
	if (latOk && lngOk) {
	    StopInfoHelper sih = getClosestStopHelper(lat, lng, ri);
	    if (sih != null && sih.distance*0.000621 < maxDistance) {
		si = sih.si;
	    }
	}
	return si;
    }



    static Vector<RouteInfo> allRoutes() {
	Vector<RouteInfo> routes = new Vector<RouteInfo>();
	routes.addElement(getRouteFromFile("/sdcard/route91.xml"));
	routes.addElement(getRouteFromFile("/sdcard/route86.xml"));
	routes.addElement(getRouteFromFile("/sdcard/route87.xml"));
	return routes;
    }
	    

    static void doit(Context context)
    {
	Vector<RouteInfo> rc = allRoutes();
	for (RouteInfo ri: rc) {

	    // my house
	    double lat = 42.379159;
	    double lng = -71.099908;
	    
	    // Kenmore
	    double lat2 = 42.348622;
	    double lng2 = -71.093769;

	    StopInfo si2 = getClosestStop(lat2, lng2, ri);
	    android.util.Log.d("MBTA", "Closest stop to Kenmore is " + si2.title);

	    StopInfo si3 = getClosestStop(lat2, lng2, ri, 0.5);
	    if (si3 != null) {
		android.util.Log.d("MBTA", "Closest stop to Kenmore is " + si3.title);
	    } else {
		android.util.Log.d("MBTA", "No stops within a half mile of Kenmore");
	    }			    

	    StopInfo si4 = getClosestStop(lat, lng, ri, 0.5);
	    if (si4 != null) {
		android.util.Log.d("MBTA", "Closest stop to home is " + si4.title);
	    } else {
		android.util.Log.d("MBTA", "No stops close to home");
	    }			    

	}

    }
    
}