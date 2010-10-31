// Copyright 2010 Tom Walsh
//
// This program is free software released under version 3
// of the GPL.  See file gpl.txt for more information.
package com.github.tommywalsh.mbta;

import android.sax.RootElement;
import android.sax.Element;
import android.sax.StartElementListener;
import android.util.Xml;

import org.xml.sax.Attributes;

import java.util.TreeSet;
import java.io.InputStream;

import android.util.Log;


public class MBTAParser {

    public TreeSet<Departure> parse(InputStream is) {

	
	final String NS = "";
	final TreeSet<Departure> departures = new TreeSet<Departure>();
	final Departure pendingDeparture = new Departure();

	RootElement root = new RootElement("body");

	Element preds = root.getChild(NS, "predictions");
	preds.setStartElementListener(new StartElementListener() {
		public void start(Attributes atts) {
		    pendingDeparture.route = atts.getValue("routeTitle");
		    pendingDeparture.where = atts.getValue("stopTitle");
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
		    d.where = pendingDeparture.where;
		    d.direction = pendingDeparture.direction;
		    d.when = Long.decode(atts.getValue("epochTime")); // should use epochTime!
		    departures.add(d);
		}
	    });
	
	try {
	    Xml.parse(is, Xml.Encoding.UTF_8, root.getContentHandler());
	} catch (Exception e) {
	    Log.d("kml", e.toString());
	}
	
	return departures;
	
    }

}