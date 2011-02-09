// Copyright 2010-11 Tom Walsh
//
// This program is free software released under version 3
// of the GPL.  See file gpl.txt for more information.

package com.github.tommywalsh.mbta;

import android.app.ListActivity;
import android.widget.ArrayAdapter;
import android.os.Bundle;
import android.os.Handler;
import android.content.Intent;
import java.util.SortedSet;
import java.io.Serializable;

public class DepartureViewer extends ListActivity
{
    private SortedSet<Departure> m_departures = null;
    private Handler m_handler = new Handler();
    private ArrayAdapter<String> m_aa = null;

    private Runnable m_updateDisplay = new Runnable() {
	    public void run() {
		m_aa.clear();
		if (m_currentProfile == null) {
		    m_aa.add(getString(R.string.select_option));
		} else if (m_departures == null) {
		    m_aa.add(getString(R.string.downloading));
		} else {
		    
		    long now = java.lang.System.currentTimeMillis();

		    for (Departure d : m_departures) {

			// We're only going to refresh every 10 seconds,
			// so this data will be stale for an average of 5
			// seconds.  Deduct 5 seconds from the time left
			// so that we "average out" the error
			int secondsLeft = (int) ((d.when - now) / 1000);
			secondsLeft -= 5;

			if (secondsLeft > 0) {
			    int hours = secondsLeft / 3600;
			    int minutes = (secondsLeft - hours*3600) / 60;
			    int seconds = (secondsLeft - hours*3600 - minutes*60);
			    
			    String mess = d.route.title + " to " + d.direction + " stops at " + d.title + " in ";
			    if (hours > 0) {
				mess += (new Integer(hours)).toString() + ":";
				if (minutes < 10) {
				    mess += "0";
				}
			    } 
			    if (hours > 0 || minutes > 0) {
				mess += (new Integer(minutes)).toString();
			    }
			    mess += ":";
			    if (seconds < 10) {
				mess += "0";
			    }
			    mess += (new Integer(seconds)).toString();
			    
			    m_aa.add(mess);
			}
		    }
		}
		setListAdapter(m_aa);

		// wait 10 seconds to do this again
		m_handler.postDelayed(this, 10000);
	    }
	};

    
    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
	m_profileProvider = new ProfileProvider(this);
	m_aa = new ArrayAdapter<String>(this, R.layout.listitem);

        Intent i = getIntent();
        Serializable s = i.getSerializableExtra("com.github.tommywalsh.mbta.Profile");
        changeProfile((Profile)s);
    }

    public void onResume() {
	super.onResume();

	m_handler.removeCallbacks(m_updateDisplay);
	m_handler.post(m_updateDisplay);

	if (m_currentProfile != null) {
	    m_handler.removeCallbacks(m_updateDepartures);
	    m_handler.postDelayed(m_updateDepartures, 500);
	}
    }

    private Runnable m_updateDepartures = new Runnable() {
	    public void run() {
		if (m_currentProfile != null) {
                    m_departures = DepartureFinder.getDeparturesForProfile(m_currentProfile);

		    // Don't ask for data again until 30 seconds have passed...
		    m_handler.postDelayed(this, 30000);

		    // ... but update the display right now
		    m_handler.removeCallbacks(m_updateDisplay);
		    m_handler.post(m_updateDisplay);
		}
	    }
	};

    private Profile m_currentProfile = null;

		
    public void onPause() {
	super.onPause();
	m_handler.removeCallbacks(m_updateDisplay);
	m_handler.removeCallbacks(m_updateDepartures);
    }

    @Override public void onActivityResult(int request, int result, Intent data) {
	if (request == 1050 && result == RESULT_OK) {
	    double lat = data.getDoubleExtra("com.github.tommywalsh.mbta.Lat", 0.0);
	    double lng = data.getDoubleExtra("com.github.tommywalsh.mbta.Lng", 0);
	    Profile p = ProximityProfileGenerator.getProximityProfile(lat, lng, 0.5);
	    changeProfile(p);
	}
    }

    private ProfileProvider m_profileProvider;

    private void changeProfile(Profile p) {
	m_currentProfile = p;
	m_handler.removeCallbacks(m_updateDepartures);
	m_handler.postDelayed(m_updateDepartures, 500);	
    }
   
}
