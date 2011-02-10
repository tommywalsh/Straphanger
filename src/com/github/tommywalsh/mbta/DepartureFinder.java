// Copyright 2010-11 Tom Walsh
//
// This program is free software released under version 3
// of the GPL.  See file gpl.txt for more information.

package com.github.tommywalsh.mbta;

import android.sax.RootElement;
import android.sax.Element;
import android.sax.StartElementListener;
import android.util.Xml;

import org.xml.sax.Attributes;

import java.util.SortedSet;
import java.util.TreeSet;
import java.util.TreeSet;
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
	public void onReceived(SortedSet<Departure> departures);

	// Called if server call fails for any reason
	public void onFailed();
    }


    // Send a request for data to the server.
    // Some time later the passed-in callback will be executed on a different thread.
    static public void requestDeparturesForProfile(Profile profile, Callback callback)
    {
	final Profile p = profile;
	final Callback cb = callback;
	Thread requesterThread = new Thread() {
		@Override public void run() {
		    try {
			SortedSet<Departure> departures = parse(getPredictionStream(p));
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
    static private TreeSet<Departure> parse(InputStream is) 
    {	
	final String NS = "";
	final TreeSet<Departure> departures = new TreeSet<Departure>();
	final Departure pendingDeparture = new Departure();
	
	RootElement root = new RootElement("body");
	
	Element preds = root.getChild(NS, "predictions");
	preds.setStartElementListener(new StartElementListener() {
		public void start(Attributes atts) {
		    pendingDeparture.route = Route.getRoute(atts.getValue("routeTag"));
		    pendingDeparture.title = atts.getValue("stopTitle");
		}
	    });
	
	Element dir = preds.getChild(NS, "direction");
	dir.setStartElementListener(new StartElementListener() {
		public void start(Attributes atts) {
		    pendingDeparture.direction = atts.getValue("title");
		}
	    });
	
	Element pred = dir.getChild(NS, "prediction");
	pred.setStartElementListener(new StartElementListener() {
		public void start(Attributes atts) {
		    Departure d = new Departure();
		    d.route = pendingDeparture.route;
		    d.title = pendingDeparture.title;
		    d.direction = pendingDeparture.direction;
		    d.when = Long.decode(atts.getValue("epochTime"));
		    departures.add(d);
		}
	    });
	
	try {
	    Xml.parse(is, Xml.Encoding.UTF_8, root.getContentHandler());
	} catch (Exception e) {
	    android.util.Log.d("mbta", e.toString());
	}
	
	return departures;
	
    }
    


    // Helper functions
    private static URL getPredictionURLForProfile(Profile p) 
    {
	String urlString = "http://webservices.nextbus.com/service/publicXMLFeed?command=predictionsForMultiStops&a=mbta";
	for (DeparturePoint s : p.stops) {
	    urlString += "&stops=" + s.route.tag + "|null|" + s.tag;
	}
	try {
	    return new URL(urlString);
	} catch (java.net.MalformedURLException e) {
	    return null;
	}

    }

    private static InputStream getPredictionStream(Profile p) throws java.io.IOException {
	URL url = getPredictionURLForProfile(p);
	return url.openStream();
    }

    
}
