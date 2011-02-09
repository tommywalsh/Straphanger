// Copyright 2010-11 Tom Walsh
//
// This program is free software released under version 3
// of the GPL.  See file gpl.txt for more information.

package com.github.tommywalsh.mbta;

import java.util.SortedSet;
import java.util.TreeSet;
import java.lang.Thread;


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
    static void requestDeparturesForProfile(Profile profile, Callback callback)
    {
	final Profile p = profile;
	final Callback cb = callback;
	Thread requesterThread = new Thread() {
		@Override public void run() {
		    MBTAParser parser = new MBTAParser();
		    try {
			SortedSet<Departure> departures = parser.parse(MBTADataService.getPredictionStream(p));
			cb.onReceived(departures);
		    } catch (java.io.IOException e) {
			android.util.Log.d("MBTA", e.toString());
			cb.onFailed();
		    }
		}
	    };
	requesterThread.start();
    }

}
