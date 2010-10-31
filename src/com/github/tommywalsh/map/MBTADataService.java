// Copyright 2010 Tom Walsh
//
// This program is free software released under version 3
// of the GPL.  See file gpl.txt for more information.

package com.github.tommywalsh.mbta;

import java.net.URL;
import java.io.InputStream;

public class MBTADataService
{

    public static InputStream getPredictionStream() throws java.io.IOException {
	try {
	    URL url = new URL("http://webservices.nextbus.com/service/publicXMLFeed?command=predictionsForMultiStops&a=mbta&stops=748|null|2531&stops=748|null|2612&stops=91|null|2531&stops=91|null|2612&stops=86|null|25712&stops=86|null|2615&stops=85|null|2612&stops=87|null|2510");
	    return url.openStream();
	} catch (java.net.MalformedURLException e) {
	    return null;
	}
    }

}

