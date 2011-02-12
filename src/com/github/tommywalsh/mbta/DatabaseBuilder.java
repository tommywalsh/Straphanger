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
import android.app.ProgressDialog;
import android.content.Context;
import android.content.ContentValues;
import android.os.AsyncTask;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;

import java.util.Vector;
import java.util.TreeMap;
import java.io.InputStream;
import java.net.URL;

import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteDatabase;



public class DatabaseBuilder
{
    // Must pass in the application context here, so that 
    // database may be shared with entire application
    public DatabaseBuilder(Context appContext) 
    {
	MBTADBOpenHelper openHelper = new MBTADBOpenHelper(appContext);
	m_db = openHelper.getWritableDatabase();
    }

    // Returns immediately, but opens progress dialog on the given context
    public void spawnRebuildTask(Context ctx) 
    {
	new RebuilderTask(ctx).execute();
    }



    // This task handles the progress dialog and calls into code that really rebuilds
    private class RebuilderTask extends AsyncTask<Void,Integer,Void>
    {
	public RebuilderTask(Context ctx) {
	    m_ctx = ctx;
	    m_dialogs = 0;
	}
	private Context m_ctx;
	private ProgressDialog m_dlg;
	private int m_dialogs; // how many dialogs have we created?

	@Override protected void onPreExecute() {
	    m_dlg = ProgressDialog.show(m_ctx, "", "Loading Route List", true);
	    m_dialogs = 1;
	}

	@Override protected void onPostExecute(java.lang.Void v) {
	    m_dlg.cancel();
	}

	@Override protected void onProgressUpdate(Integer... val) {
	    if (m_dialogs == 1) {
		// Make a new dialog now that we know the number of routes
		m_dlg.cancel();
		m_dlg = new ProgressDialog(m_ctx);
		m_dlg.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		m_dlg.setMessage("Loading Route Files");
		m_dlg.setCancelable(false);
		m_dlg.setMax(val[0]);
		m_dlg.show();
		m_dialogs = 2;
	    } else {
		assert (m_dialogs == 2);
		m_dlg.setProgress(val[0]);
	    }
	}

	@Override protected Void doInBackground(java.lang.Void... v) {
	    m_db.delete("route", null, null);

	    Vector<String> routeList = parseRouteList();
	    publishProgress(routeList.size());
	    int numProcessed = 0;
	    for (String tag : routeList) {
		try {
		    Thread.sleep(2000);
		} catch (Exception e) {
		}
		
		// add the route data
		RouteInfoHelper rih = parseRoute(tag);
		ContentValues routeData = rih.routeData;
		m_db.insert("route", null, routeData);

		// and the stop data
		for (ContentValues stopData : rih.stopData) {
		    m_db.insert("stop", null, stopData);
		}

		numProcessed++;
		publishProgress(numProcessed);
	    }
	    return null;
	}
    }

    private SQLiteDatabase m_db;


    private static class RouteInfoHelper
    {
	public ContentValues routeData = new ContentValues();
	public Vector<ContentValues> stopData = new Vector<ContentValues>();
	public class SubRouteHelper {
	    String direction = new String();
	    Vector<String> orderedStopTags = new Vector<String>();
	};
	public Vector<SubRouteHelper> subRouteData = new Vector<SubRouteHelper>();
    }

    private static RouteInfoHelper parseRoute(String routeTag)
    {
	final RouteInfoHelper rh = new RouteInfoHelper();
	try {
	    Xml.parse(getStream(getRouteURL(routeTag)), Xml.Encoding.UTF_8, getRouteHandler(rh));
	} catch (Exception e) {
	    android.util.Log.d("mbta", "Failure to parse route " + routeTag);
	    android.util.Log.d("mbta", "Exception: " + e.toString());
	}
	return rh;
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
    
    private static ContentHandler getRouteHandler(final RouteInfoHelper routeInfo)
    {
	final String NS = "";
	
	RootElement root = new RootElement("body");
	
	Element route = root.getChild(NS, "route");
	route.setStartElementListener(new StartElementListener() {
		public void start(Attributes atts) {
		    routeInfo.routeData.put("tag", atts.getValue("tag"));
		    routeInfo.routeData.put("title", atts.getValue("title"));
		    routeInfo.routeData.put("minLat", Double.parseDouble(atts.getValue("latMin")));
		    routeInfo.routeData.put("maxLat", Double.parseDouble(atts.getValue("latMax")));
		    routeInfo.routeData.put("minLng", Double.parseDouble(atts.getValue("lonMin")));
		    routeInfo.routeData.put("maxLng", Double.parseDouble(atts.getValue("lonMax")));
		}
	    });
	
	Element allStop = route.getChild(NS, "stop");
	allStop.setStartElementListener(new StartElementListener() {
		public void start(Attributes atts) {
		    ContentValues cv = new ContentValues();
		    String tag = atts.getValue("tag");
		    cv.put("tag", tag);
		    cv.put("title", atts.getValue("title"));
		    cv.put("lat", atts.getValue("lat"));
		    cv.put("lng", atts.getValue("lon"));
		    routeInfo.stopData.addElement(cv);
		}
	    });

	return root.getContentHandler();
    }



    private static URL urlFromString(String str)
    {
	Log.d("mbta", "String is " + str);
	try {
	    return new URL(str);
	} catch (java.net.MalformedURLException e) {
	    Log.d("mbta", "Malformed route list URL");
	    return null;
	}
    }
    
    private static URL getRouteListURL()
    {
	return urlFromString("http://webservices.nextbus.com/service/publicXMLFeed?command=routeList&a=mbta");
    }

    private static URL getRouteURL(String routeTag) {
	return urlFromString("http://webservices.nextbus.com/service/publicXMLFeed?command=routeConfig&a=mbta&r=" + routeTag);
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


