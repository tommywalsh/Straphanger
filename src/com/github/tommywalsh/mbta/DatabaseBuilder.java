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
import android.os.AsyncTask;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;

import java.util.Vector;
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
	SQLiteDatabase db = openHelper.getWritableDatabase();
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
	}
	private Context m_ctx;
	private ProgressDialog m_dlg;

	@Override protected void onPreExecute() {
	    m_dlg = ProgressDialog.show(m_ctx, "", "Loading Route List", true);
	}

	@Override protected void onPostExecute(java.lang.Void v) {
	    m_dlg.cancel();
	}

	@Override protected Void doInBackground(java.lang.Void... v) {
	    Vector<String> routeList = parseRouteList();
	    return null;
	}
    }

    private SQLiteDatabase m_db;



    public static void rebuild() {
	
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


