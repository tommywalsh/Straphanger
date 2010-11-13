// Copyright 2010 Tom Walsh
//
// This program is free software released under version 3
// of the GPL.  See file gpl.txt for more information.

package com.github.tommywalsh.mbta;

import android.app.ListActivity;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuInflater;
import android.util.Log;

import java.util.SortedSet;

public class MBTAActivity extends ListActivity
{
    private SortedSet<Departure> m_departures = null;
    private Handler m_handler = new Handler();
    private ArrayAdapter<String> m_aa = null;

    private Runnable m_updateDisplay = new Runnable() {
	    public void run() {
		m_aa.clear();
		if (m_departures == null) {
		    m_aa.add(new String("Downloading data..."));
		} else {
		    
		    long now = java.lang.System.currentTimeMillis();

		    for (Departure d : m_departures) {
			int secondsLeft = (int) ((d.when - now) / 1000);
			if (secondsLeft > 0) {
			    int hours = secondsLeft / 3600;
			    int minutes = (secondsLeft - hours*3600) / 60;
			    int seconds = (secondsLeft - hours*3600 - minutes*60);
			    
			    String mess = d.route + " to " + d.direction + " stops at " + d.where + " in ";
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
		m_handler.postDelayed(this, 500);
	    }
	};

    
    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
	m_aa = new ArrayAdapter<String>(this, R.layout.listitem);
    }

    public void onResume() {
	super.onResume();

	m_handler.removeCallbacks(m_updateDisplay);
	m_handler.postDelayed(m_updateDisplay, 10);

	if (m_currentProfile != null) {
	    m_handler.removeCallbacks(m_updateDepartures);
	    m_handler.postDelayed(m_updateDepartures, 500);
	}
    }

    private Runnable m_updateDepartures = new Runnable() {
	    public void run() {
		if (m_currentProfile != null) {
		    MBTAParser parser = new MBTAParser();
		    try {
			m_departures = parser.parse(MBTADataService.getPredictionStream(m_currentProfile));
		} catch (java.io.IOException e) {
		    }
		    m_handler.postDelayed(this, 30000);
		}
	    }
	};

    private Profile m_currentProfile = null;

		
    public void onPause() {
	super.onPause();
	m_handler.removeCallbacks(m_updateDisplay);
	m_handler.removeCallbacks(m_updateDepartures);
	m_handler = null;
    }

    @Override public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
	Profile p = m_profileMenu.processSelection(id);
	changeProfile(p);
	return true;
    }

    @Override public boolean onCreateOptionsMenu(Menu menu) {
        m_profileMenu = new ProfilePicker(menu);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    private ProfilePicker m_profileMenu;

    private void changeProfile(Profile p) {
	m_currentProfile = p;
	m_handler.removeCallbacks(m_updateDepartures);
	m_handler.postDelayed(m_updateDepartures, 500);	
    }
   
}



