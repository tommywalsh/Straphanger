// Copyright 2011 Tom Walsh
//
// This program is free software released under version 3
// of the GPL.  See file gpl.txt for more information.

package com.github.tommywalsh.mbta;

import java.util.AbstractMap;
import java.util.TreeMap;
import java.util.Vector;
import java.util.Collection;
import java.io.FileInputStream;

import android.sax.RootElement;
import android.sax.Element;
import android.sax.StartElementListener;
import android.sax.EndElementListener;
import android.sax.ElementListener;
import android.util.Xml;

import org.xml.sax.Attributes;


public class RouteInfo {

    static public RouteInfo getRoute(String tag) 
    {
	initIfNecessary();
	return s_allRoutes.get(tag);
    }
    
    static public Collection<RouteInfo> getAllRoutes()
    {
	initIfNecessary();
	return s_allRoutes.values();
    }

    public String tag;
    public String title;
    public double minLat;
    public double minLng;
    public double maxLat;
    public double maxLng;

    public AbstractMap<String, Vector<StopInfo>> getStopMap() {
	if (m_stopMap == null) {
	    heavyParse();
	}
	return m_stopMap;
    }




    static private TreeMap<String, RouteInfo> s_allRoutes = null;
    private String m_filename;
    private Vector<StopInfo> m_stops = null;
    private TreeMap<String,Vector<StopInfo>> m_stopMap = null;
    
    private RouteInfo(String cTag, String cTitle, double cMinLat, double cMaxLat, double cMinLng, double cMaxLng) {
	tag = cTag;
	title = cTitle;
	minLat = cMinLat;
	maxLat = cMaxLat;
	minLng = cMinLng;
	maxLng = cMaxLng;
	m_filename = "/sdcard/mbta/route" + tag + ".xml";
    }

    static private void	initIfNecessary()
    {
	if (s_allRoutes == null) {	    
	    s_allRoutes = new TreeMap<String, RouteInfo>();
	    parseOverview();
	}
    }


    private FileInputStream getStream() throws java.io.FileNotFoundException
    {
	java.io.File file = new java.io.File(m_filename);
	return new FileInputStream(file);
    }

    private static void parseOverview()
    {
	final String NS = "";
	
	RootElement root = new RootElement("body");

	s_allRoutes = new TreeMap<String, RouteInfo>();
	
	Element route = root.getChild(NS, "route");
	route.setStartElementListener(new StartElementListener() {
		public void start(Attributes atts) {
		    String atag = atts.getValue("tag");
		    String atitle = atts.getValue("title");
		    double aminLat = Double.parseDouble(atts.getValue("latMin"));
		    double amaxLat = Double.parseDouble(atts.getValue("latMax"));
		    double aminLng = Double.parseDouble(atts.getValue("lonMin"));
		    double amaxLng = Double.parseDouble(atts.getValue("lonMax"));
		    s_allRoutes.put(atag, new RouteInfo(atag, atitle, aminLat, amaxLat, aminLng, amaxLng));
		}
	    });
	
	try {
	    java.io.File file = new java.io.File("/sdcard/mbta/overview.xml");
	    FileInputStream is = new FileInputStream(file);
	    Xml.parse(is, Xml.Encoding.UTF_8, root.getContentHandler());
	} catch (Exception e) {
	    android.util.Log.d("mbta", e.toString());
	}	

    }
    
    // actually parse the route file
    private void heavyParse() {

	// Note: this is WRONG!  We need to parse stops for each direction!
	m_stops = new Vector<StopInfo>();
	m_stopMap = new TreeMap<String, Vector<StopInfo>>();

	final String NS = "";
	final Vector<StopInfo> currDirStops = new Vector<StopInfo>();
	
	RootElement root = new RootElement("body");
	Element route = root.getChild(NS, "route");
	Element stop = route.getChild(NS, "stop");
	stop.setStartElementListener(new StartElementListener() {
		public void start(Attributes atts) {
		    StopInfo si = new StopInfo();
		    si.tag = atts.getValue("tag");
		    si.title = atts.getValue("title");
		    si.lat = Double.parseDouble(atts.getValue("lat"));
		    si.lng = Double.parseDouble(atts.getValue("lon"));
		    m_stops.addElement(si);
		}
	    });
	Element direction = route.getChild(NS, "direction");
	stop.setElementListener(new ElementListener() {
		private String dir;
		public void start(Attributes atts) {
		    dir = atts.getValue("title");
		}
		public void end() {
		    m_stopMap.put(dir, currDirStops);
		    currDirStops.clear();
		}
	    });
	Element dirstop = direction.getChild(NS, "stop");
	stop.setStartElementListener(new StartElementListener() {
		public void start(Attributes atts) {
		    StopInfo si = new StopInfo();
		    si.tag = atts.getValue("tag");
		    si.title = atts.getValue("title");
		    si.lat = Double.parseDouble(atts.getValue("lat"));
		    si.lng = Double.parseDouble(atts.getValue("lon"));
		    currDirStops.addElement(si);
		}
	    });
	
	try {
	    FileInputStream is = getStream();
	    Xml.parse(is, Xml.Encoding.UTF_8, root.getContentHandler());
	} catch (Exception e) {
	    android.util.Log.d("mbta", e.toString());
	}
    }



}
