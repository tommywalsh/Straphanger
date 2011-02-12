// Copyright 2011 Tom Walsh
//
// This program is free software released under version 3
// of the GPL.  See file gpl.txt for more information.

package com.github.tommywalsh.mbta;

import android.util.Log;

import android.sax.RootElement;
import android.sax.Element;
import android.sax.StartElementListener;
import android.util.Xml;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;

import java.util.Vector;
import java.io.InputStream;
import java.net.URL;



public class DatabaseBuilder
{
    public static void rebuild() {
	//	MBTADBOpenHelper openHelper = new MBTADBOpenHelper(getApplicationContext());
	//SQLiteDatabase db = openHelper.getWritableDatabase();
	
	// should delete crap here
	parseRouteList();
    }

    
    private static Vector<String> parseRouteList() 
    {
	Vector<String> tags = null;
	try {
	    Vector<String> routeTags = new Vector<String>();
	    Xml.parse(getStream(getRouteListURL()), Xml.Encoding.UTF_8, getRouteListHandler(routeTags));
	    tags = routeTags;

	    for (String tag : tags) {
		Log.d("mbta", tag);
	    }
	} catch (Exception e) {
	    android.util.Log.d("mbta", "Failure to parse route list");
	    android.util.Log.d("mbta", "Exception: " + e.toString());
	}
	return tags;
    }


	
    private static ContentHandler getRouteListHandler(final Vector<String> storage) {
	final String NS = "";
	
	RootElement root = new RootElement("body");
	
	Element preds = root.getChild(NS, "route");
	preds.setStartElementListener(new StartElementListener() {
		public void start(Attributes atts) {
		    storage.addElement(atts.getValue("tag"));
		}
	    });
	return root.getContentHandler();
    }
    
    private static URL getRouteListURL()
    {
	URL url;
	try {
	    url = new URL("http://webservices.nextbus.com/service/publicXMLFeed?command=routeList&a=mbta");
	} catch (java.net.MalformedURLException e) {
	    Log.d("mbta", "Malformed route list URL");
	    url = null;
	}
	return url;
    }

    private static InputStream getStream(URL url) throws java.io.IOException {
	InputStream is;
	try {
	    is = url.openStream();
	} catch (java.io.IOException e) {
	    Log.d("mbta", "IOException in getStream for URL " + url.toString());
	    Log.d("mbta", "Exception is " + e.toString());
	    is = null;
	}
	return is;
    }

}


