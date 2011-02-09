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
//
// The activity REQUIRES a profile in order to work.  So, when you activate it, you MUST
// store the desired profile in the Intent you use to start this activity.  Like this:
//
//      Intent i = new Intent(this, DepartureViewer.class);
//      i.putExtra("com.github.tommywalsh.mbta.Profile", p);
//      startActivity(i);


public class DepartureViewer extends ListActivity
{
    // Here's the data this class holds on to
    private SortedSet<Departure> m_departures = null;   // when are the busses leaving?
    private Profile m_profile = null;                   // which busses do we care about?

    // Scheduler that allows us to update the screen, and send periodic requests for new data
    private Handler m_handler = new Handler();

    // UI elements
    private ArrayAdapter<String> m_aa = null;   // Adapter to convert plain list of strings to ListView items
    private ProgressDialog m_downloadingDialog = null;  // Dialog to tell user whassup when we're waiting for data

    // "Magic numbers"
    private static final int s_refreshInterval     = 10000;  // How long (in ms) between screen refreshes?
    private static final int s_dataRequestInterval = 30000;  // How long (in ms) between server requests?


    // When this activity is first created...
    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // ... set up our array adapter...
	m_aa = new ArrayAdapter<String>(this, R.layout.listitem);
        m_departures = null;

        // ... unpack the profile of interesting busses that has been sent to us...
        Intent i = getIntent();
        Serializable s = i.getSerializableExtra(getString(R.string.profile_in_intent));
        m_profile = (Profile)s;
        assert (m_profile != null);

        m_downloadingDialog = ProgressDialog.show(this, "", "Downloading.  Please wait...", true);

    }


    // When this activity comes to the foreground...
    public void onResume() {
	super.onResume();

        // ... schedule a request for some new data...
        assert (m_profile != null);
        m_handler.removeCallbacks(m_departureUpdater);
        m_handler.post(m_departureUpdater);

        // ... but update the screen immediately.
	m_handler.removeCallbacks(m_displayUpdater);
        updateDisplay();
    }


    // When this activity leaves the foreground...
    public void onPause() {
	super.onPause();

        // ... there's no need to waste time refreshing the screen or asking for new data
	m_handler.removeCallbacks(m_displayUpdater);
	m_handler.removeCallbacks(m_departureUpdater);
    }




    // This Runnable is in charge of updating the display.  This might be called because
    // new data has come in, or it might be just because enough time has passed since our
    // last update that we want to update the times on the screen.
    private Runnable m_displayUpdater = new Runnable() {
	    public void run() {
                updateDisplay();
            }
        };


    // Actually update the display.  This can be called from any thread
    private synchronized void updateDisplay() {
        if (m_departures == null) {
            if (m_downloadingDialog == null) {
                m_downloadingDialog = ProgressDialog.show(this, "", "Downloading.  Please wait...", true);
            }
        } else {
            if (m_downloadingDialog != null) {
                m_downloadingDialog.cancel();
                m_downloadingDialog = null;
            }

            m_aa.clear();
            long now = java.lang.System.currentTimeMillis();
            
            for (Departure d : m_departures) {
                
                // We're only going to refresh every so often -- say X seconds,
                // so this data will be stale for an average of X/2
                // seconds.  Deduct X/2 seconds from the time left
                // so that we "average out" the error
                int secondsLeft = (int) ((d.when - now) / 1000);
                secondsLeft -= s_refreshInterval;
                
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
                
        // do this again in a bit 
        m_handler.postDelayed(m_displayUpdater, s_refreshInterval);
    }



    // This Runnable is in charge of requesting data from the server.  This might be called
    // because we don't have any data yet, or maybe just because it's been a while since
    // we got data and we want to have fresher estimates on arrival times.
    private Runnable m_departureUpdater = new Runnable() {
	    public void run() {
                requestDepartureData();
            }
        };

    // Actually request data from the server.  Note that this can take a long time,
    // so it's not advisable to call this directly from the GUI thread.  Use the
    // associated runnable unless you've got a good reason
    private void requestDepartureData() {
        assert(m_profile != null);
        
        // Get the data. (can take a while)
        m_departures = DepartureFinder.getDeparturesForProfile(m_profile);        

        // Don't ask for data again for a while...
        m_handler.postDelayed(m_departureUpdater, s_dataRequestInterval);

        // ... but update the screen right away
        updateDisplay();
    }
		


    private void changeProfile(Profile p) {
    }
   
}
