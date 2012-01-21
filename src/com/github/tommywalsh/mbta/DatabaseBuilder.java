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


// This class's job is to rebuild the database from scratch
// It throws away whatever DB exists already, and downloads
// anew from the server.  Takes a long time.
public class DatabaseBuilder
{

    private Context m_context;
    static private String PREFS_NAME = "db_status";
    public DatabaseBuilder(Context ctx)
    {
        m_context = ctx;
    }

    // Returns immediately, but opens progress dialog on the given context
    public void spawnRebuildTask()
    {
	new RebuilderTask(m_context).execute();
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
            DatabaseMonitor.setComplete(false);
	    m_dlg = ProgressDialog.show(m_ctx, "", "Loading Route List", true);
	    m_dialogs = 1;
	}

	@Override protected void onPostExecute(java.lang.Void v) {
	    m_dlg.cancel();
            DatabaseMonitor.setComplete(true);
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
            
            MBTADBOpenHelper openHelper = new MBTADBOpenHelper(m_ctx);
            SQLiteDatabase m_db = openHelper.getWritableDatabase();

            m_db.beginTransaction();

	    m_db.delete("route", null, null);
            m_db.delete("stop", null, null);
	    m_db.delete("subroute", null, null);
	    m_db.delete("departure_point", null, null);
	    
	    
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
                    // Some stops are shared between routes.  If we just
                    // "insert" here, we'll get constraint violations.
                    // But, we also don't want to bother searching.
                    // Just call "replace" which might as well be called 
                    // "insertOrReplace"
		    m_db.replace("stop", null, stopData);
		}

		// and the subroute data...
		for(int i = 0; i < rih.subRouteData.size(); i++) {
		    ContentValues subRouteData = rih.subRouteData.elementAt(i);
		    m_db.insert("subroute", null, subRouteData);

		    // ...including the stops
		    for (ContentValues subRouteStops : rih.subRouteStops.elementAt(i)) {
			m_db.insert("departure_point", null, subRouteStops);
		    }
		}

		numProcessed++;
		publishProgress(numProcessed);
            }

            m_db.setTransactionSuccessful();
            m_db.endTransaction();
            m_db.close();

	    return null;
	}
    }

    private static class RouteInfoHelper
    {
	public ContentValues routeData = new ContentValues();
	public Vector<ContentValues> stopData = new Vector<ContentValues>();
	
	// these next two are associate by index
	public Vector<ContentValues> subRouteData = new Vector<ContentValues>();
	public Vector<Vector<ContentValues>> subRouteStops = new Vector<Vector<ContentValues>>();
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

	Element direction = route.getChild(NS, "direction");
	direction.setStartElementListener(new StartElementListener() {
		public void start(Attributes atts) {
		    ContentValues cv = new ContentValues();
		    cv.put("tag", atts.getValue("tag"));
		    cv.put("title", atts.getValue("title"));
		    cv.put("route", routeInfo.routeData.getAsString("tag"));
		    routeInfo.subRouteData.addElement(cv);

		    // elements will be filled in by the next listener
		    routeInfo.subRouteStops.addElement(new Vector<ContentValues>());
		}
	    });

	
	Element directionStop = direction.getChild(NS, "stop");
	directionStop.setStartElementListener(new StartElementListener() {
		int index;
		public void start(Attributes atts) {
		    ContentValues cv = new ContentValues();
		    
		    // Are we at the start of a new route?
		    if (routeInfo.subRouteStops.lastElement().isEmpty()) {
			index = 1;
		    } else {
			index++;
		    }

		    cv.putNull("id");
		    cv.put("stopNum", index);
		    cv.put("subroute", routeInfo.subRouteData.lastElement().getAsString("tag"));
		    cv.put("stop", atts.getValue("tag"));
		    routeInfo.subRouteStops.lastElement().addElement(cv);
		}
	    });

	return root.getContentHandler();
    }



    private static URL urlFromString(String str)
    {
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


