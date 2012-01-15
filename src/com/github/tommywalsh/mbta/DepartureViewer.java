// Copyright 2010-12 Tom Walsh
//
// This program is free software released under version 3
// of the GPL.  See file gpl.txt for more information.

package com.github.tommywalsh.mbta;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.widget.ArrayAdapter;
import android.widget.Toast;
import android.widget.TextView;
import android.view.View;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.content.Intent;
import java.io.Serializable;
import java.util.SortedSet;
import java.util.Vector;
import java.util.Iterator;

// This activity is a viewer that displays all the upcoming departures for a particular profile
// It's sorted on screen based on how soon the departure will happen
//
// This activity does not produce anything, save for pixels on a screen.  So, if you
// want to activate it, only use startActivity() and not startActivityForResult().
//
// The activity REQUIRES an array of departure points in order to work.  So, when you activate it, you MUST
// store them in the Intent you use to start this activity.  Like this:
//
//      Intent i = new Intent(this, DepartureViewer.class);
//	i.putExtra(getString(R.string.departures_in_intent), myDeparturePointArray);
//      startActivity(i);


public class DepartureViewer extends ListActivity
{
    // Here's the data this class holds on to
     private Vector<Prediction> m_predictions = new Vector<Prediction>();   // when are the busses leaving?
    private Vector<Integer> m_departurePoints = null;    // which busses at which stops do we care about?

    // Scheduler that allows us to update the screen, and send periodic requests for new data
    private Handler m_handler = new Handler();

    // UI elements
    private ProgressDialog m_downloadingDialog = null;  // Dialog to tell user whassup when we're waiting for data

    // "Magic numbers"
    private static final int s_refreshInterval     = 10000;  // How long (in ms) between screen refreshes?
    private static final int s_dataRequestInterval = 30000;  // How long (in ms) between server requests?






    // When this activity is first created...
    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // ... set up the look and feel...
        setContentView(R.layout.departures);

        // ... set up our array adapter...
        setListAdapter(new PredictionAdapter());
        

        // ... unpack the profile of interesting busses that has been sent to us...
        Intent i = getIntent();

        Serializable s = i.getSerializableExtra(getString(R.string.departures_in_intent));
        assert(s != null);
        int[] dp = (int[])s;

        // TODO: can we just keep the array instead of making a vector?
        m_departurePoints = new Vector<Integer>(dp.length);
        for (int ix = 0; ix < dp.length; ix++) {
            m_departurePoints.addElement(dp[ix]);
        }
    }


    // When this activity comes to the foreground...
    public void onResume() {
	super.onResume();

	// update the screen and send a request for more data to be fulfilled ASAP
        updateDisplay();
	requestDepartureData();
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


    // Actually update the display.  This must be called from the GUI thread only.
    private void updateDisplay() {
        if (m_predictions.isEmpty()) {
            if (m_downloadingDialog == null) {
                m_downloadingDialog = ProgressDialog.show(this, "", getString(R.string.download_message), true);
            }
        } else {
            
            if (m_downloadingDialog != null) {
                m_downloadingDialog.cancel();
                m_downloadingDialog = null;
            }
            ((PredictionAdapter)getListAdapter()).notifyDataSetChanged();
        }
                
        // do this again in a bit 
	m_handler.removeCallbacks(m_displayUpdater);
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

    // Actually request data from the server.  
    private void requestDepartureData() {
        
        // Send a request for the data which will be fulfilled later
	if (m_departurePoints != null) {
	    DepartureFinder.requestPredictionsForDeparturePoints(getApplicationContext(), m_departurePoints, new DepartureCallback());
	}
    }
		
    // Handle departure information from the server
    // These callbacks will be run on another thread
    private class DepartureCallback implements DepartureFinder.Callback {
	@Override public void onReceived(SortedSet<Prediction> predictions) {

            m_predictions.clear();
            Iterator<Prediction> i = predictions.iterator();
            while (i.hasNext()) {
                m_predictions.add(i.next());
            }
	    
	    // refresh the display ASAP on the GUI thread
	    m_handler.post(m_displayUpdater);

	    // ... and ask for more data later (also on the GUI thread)
	    m_handler.removeCallbacks(m_departureUpdater);
	    m_handler.postDelayed(m_departureUpdater, s_dataRequestInterval);
	}

	// Called if server call fails for any reason
	@Override public void onFailed() {
	    Toast.makeText(DepartureViewer.this, "Download failed.  Please try again later", Toast.LENGTH_LONG);
	}
    }


    private class PredictionAdapter extends VectorAdapter<Prediction>
    {
        public PredictionAdapter() {
            super(getApplicationContext(), R.layout.departure_entry);
        }

        public Vector<Prediction> getVector() {
            return m_predictions;
        }

        public View processView(int position, Prediction p, View view) {
            long now = java.lang.System.currentTimeMillis();

            // We're only going to refresh every so often -- say X seconds,
            // so this data will be stale for an average of X/2
            // seconds.  Deduct X/2 seconds from the time left
            // so that we "average out" the error
            int secondsLeft = (int) ((p.when - now) / 1000);
            secondsLeft -= s_refreshInterval/2000;
            
            String timeStr;
            String busStr = p.routeTitle + " - " + p.routeDirection;
            String stopStr = p.stopTitle;

            if (secondsLeft > 0) {
                timeStr = Util.timeString(secondsLeft);
            } else {
                timeStr = "XXX";
            }
           
            TextView timeText = (TextView)view.findViewById(R.id.departure_time);
            timeText.setText(timeStr);
            if (secondsLeft < 60*5) {
                timeText.setTextColor(getResources().getColor(R.color.accent));
            } else {
                timeText.setTextColor(getResources().getColor(R.color.foreground));
            }

            TextView busText = (TextView)view.findViewById(R.id.departure_bus);
            busText.setText(busStr);

            TextView stopText = (TextView)view.findViewById(R.id.departure_stop);
            stopText.setText(stopStr);
           
            return view;
        }
    }
}
