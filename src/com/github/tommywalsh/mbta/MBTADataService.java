// Copyright 2010 Tom Walsh
//
// This program is free software released under version 3
// of the GPL.  See file gpl.txt for more information.

package com.github.tommywalsh.mbta;

import java.net.URL;
import java.io.InputStream;

public class MBTADataService
{

    private static URL getURLForProfile(Profile p) 
    {
	String urlString = "http://webservices.nextbus.com/service/publicXMLFeed?command=predictionsForMultiStops&a=mbta";
	for (DeparturePoint s : p.stops) {
	    urlString += "&stops=" + s.route + "|null|" + s.where;
	}
	try {
	    return new URL(urlString);
	} catch (java.net.MalformedURLException e) {
	    return null;
	}

    }

    public static InputStream getPredictionStream(Profile p) throws java.io.IOException {
	URL url = getURLForProfile(p);
	return url.openStream();
    }

}

