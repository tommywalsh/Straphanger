// Copyright 2010-11 Tom Walsh
//
// This program is free software released under version 3
// of the GPL.  See file gpl.txt for more information.

package com.github.tommywalsh.mbta;

import android.sax.RootElement;
import android.sax.Element;
import android.sax.StartElementListener;
import android.util.Xml;
import android.content.Context;
import org.xml.sax.Attributes;

import java.util.SortedSet;
import java.util.TreeSet;
import java.util.Vector;
import java.lang.Thread;
import java.io.InputStream;
import java.net.URL;


// This class handles the querying and collection of data from the server
// 
// It does its work on a separte thread, and provides results to a callback object
// which must be specified by the caller
public class DepartureFinder
{

    // Override this class to receive departure info
    // Note that the methods will be called on a different thread
    public interface Callback {

	// Called if and when server responds with departures
	// Note that the set may be empty if there are no upcoming departures
	public void onReceived(SortedSet<Prediction> departures);

	// Called if server call fails for any reason
	public void onFailed();
    }


    // Send a request for data to the server.
    // Some time later the passed-in callback will be executed on a different thread.
    static public void requestPredictionsForDeparturePoints(Context appContext, Vector<Integer> departurePoints, Callback callback)
    {
	final Context c = appContext;
	final Vector<Integer> dp = departurePoints;
	final Callback cb = callback;
	Thread requesterThread = new Thread() {
		@Override public void run() {
		    try {
			SortedSet<Prediction> departures = parse(getPredictionStream(c,dp));
			cb.onReceived(departures);
		    } catch (java.io.IOException e) {
			android.util.Log.d("MBTA", e.toString());
			cb.onFailed();
		    }
		}
	    };
	requesterThread.start();	
    }

    // This function should be run on its own thread.  It will establish a connection with the server,
    // and parse the data into usable objects "on the fly".  It may take a long time to complete.
    static private TreeSet<Prediction> parse(InputStream is) 
    {	
	final String NS = "";
	final TreeSet<Prediction> predictions = new TreeSet<Prediction>();
	final Prediction pendingPrediction = new Prediction();
	
	RootElement root = new RootElement("body");
	
	Element preds = root.getChild(NS, "predictions");
	preds.setStartElementListener(new StartElementListener() {
		public void start(Attributes atts) {
		    pendingPrediction.routeTitle = atts.getValue("routeTitle");
		    pendingPrediction.stopTitle = atts.getValue("stopTitle");
		}
	    });
	
	Element dir = preds.getChild(NS, "direction");
	dir.setStartElementListener(new StartElementListener() {
		public void start(Attributes atts) {
		    pendingPrediction.routeDirection = atts.getValue("title");
		}
	    });
	
	Element pred = dir.getChild(NS, "prediction");
	pred.setStartElementListener(new StartElementListener() {
		public void start(Attributes atts) {
		    Prediction p = new Prediction();
		    p.routeTitle = pendingPrediction.routeTitle;
		    p.stopTitle = pendingPrediction.stopTitle;
		    p.routeDirection = pendingPrediction.routeDirection;
		    p.when = Long.decode(atts.getValue("epochTime"));
		    predictions.add(p);
		}
	    });
	
	try {
	    Xml.parse(is, Xml.Encoding.UTF_8, root.getContentHandler());
	} catch (Exception e) {
	    android.util.Log.d("mbta", e.toString());
	}
	
	return predictions;
	
    }
    
    private static URL getPredictionURLForDeparturePoints(Context appContext, Vector<Integer> departurePoints) 
    {
	Database db = new Database(appContext);

	Database.RouteStopCursorWrapper cursor = db.getRoutesAndStopsForDeparturePoints(departurePoints);
        cursor.moveToFirst();
	String urlString = "http://webservices.nextbus.com/service/publicXMLFeed?command=predictionsForMultiStops&a=mbta";
        while (!cursor.isAfterLast()) {
	    urlString += "&stops=" + cursor.getRouteTag() + "|null|" + cursor.getStopTag();
	    cursor.moveToNext();
	}
	
	try {
	    return new URL(urlString);
	} catch (java.net.MalformedURLException e) {
	    return null;
	}

    }

    private static InputStream getPredictionStream(Context appContext, Vector<Integer> departurePoints) throws java.io.IOException {
	URL url = getPredictionURLForDeparturePoints(appContext, departurePoints);
	return url.openStream();
    }

    
}
