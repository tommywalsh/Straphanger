// Copyright 2010-11 Tom Walsh
//
// This program is free software released under version 3
// of the GPL.  See file gpl.txt for more information.

package com.github.tommywalsh.mbta;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.widget.ArrayAdapter;
import android.os.Bundle;
import android.os.Handler;
import android.content.Intent;
import java.util.SortedSet;
import java.io.Serializable;


// This activity is a viewer that displays all the upcoming departures for a particular profile
// It's sorted on screen based on how soon the departure will happen
//
// This activity does not produce anything, save for pixels on a screen.  So, if you
// want to activate it, only use startActivity() and not startActivityForResult().

public class DepartureViewer extends ListActivity
{

    private enum State { UNINITIALIZED, WAITING_FOR_DATA, READY};
    private State m_state = State.UNINITIALIZED;


    private SortedSet<Departure> m_departures = null;
    private Handler m_handler = new Handler();
    private ArrayAdapter<String> m_aa = null;

    private Runnable m_updateDisplay = new Runnable() {
	    public void run() {
                if (m_state == State.READY) {
                    m_aa.clear();
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
                    setListAdapter(m_aa);
		}

		// wait 10 seconds to do this again
		m_handler.postDelayed(this, 10000);
	    }
	};

    
    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
                    
                    m_state = State.READY;
                    
                    if (m_downloadingDialog != null) {
                        m_downloadingDialog.cancel();
                        m_downloadingDialog = null;
                    }

		    // Don't ask for data again until 30 seconds have passed...
		    m_handler.postDelayed(this, 30000);

		    // ... but update the display right now
		    m_handler.removeCallbacks(m_updateDisplay);
		    m_handler.post(m_updateDisplay);
		}
	    }
	};

    private Profile m_currentProfile = null;
    private ProgressDialog m_downloadingDialog = null;
		
    public void onPause() {
	super.onPause();
	m_handler.removeCallbacks(m_updateDisplay);
	m_handler.removeCallbacks(m_updateDepartures);
    }




    private void changeProfile(Profile p) {
        m_currentProfile = p;
        m_state = State.WAITING_FOR_DATA;
        m_downloadingDialog = ProgressDialog.show(this, "", "Downloading.  Please wait...", true);
	m_handler.removeCallbacks(m_updateDepartures);
	m_handler.postDelayed(m_updateDepartures, 500);	
    }
   
}
