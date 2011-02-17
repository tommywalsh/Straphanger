// Copyright 2011 Tom Walsh
//
// This program is free software released under version 3
// of the GPL.  See file gpl.txt for more information.

package com.github.tommywalsh.mbta;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.content.Intent;
import android.content.Context;
import android.content.DialogInterface;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteDatabase;

import java.util.Vector;

public class Straphanger extends Activity
{
    private static final int s_locationPickerId = 1050;
    private static final int s_profileDialogId  = 1060;
    private ProfileProvider m_profProvider;


    // What to do when we click on the "stored profiles" button
    private OnClickListener m_profileListener = new OnClickListener() {
	    public void onClick(View v) {
		showDialog(s_profileDialogId);
	    }
	};


    // Small dialog to load from a list of stored profiles
    Dialog getProfilePickerDialog() {
        Vector<Profile> profiles = m_profProvider.getProfiles();
        final int size = profiles.size();
	final String[] names = new String[size];
        final int[] ids = new int[size];
        for (int i = 0; i < size; i++) {
            Profile p = profiles.elementAt(i);
            names[i] = p.name;
            android.util.Log.d("mbta", p.name);
            ids[i] = p.id;
        }
        
	AlertDialog.Builder builder = new AlertDialog.Builder(this);
	builder.setTitle("Pick a profile");
	builder.setItems(names, new DialogInterface.OnClickListener() {                
		public void onClick(DialogInterface d, int id) {
		    viewDepartures(m_profProvider.getDeparturePointsInProfile(ids[id]));
		}
	    });

	return builder.create();
    }

    private OnClickListener m_databaseListener = new OnClickListener() {
	    public void onClick(View v) {
		m_dbBuilder.spawnRebuildTask(Straphanger.this);
	    }
	};


    private OnClickListener m_proximityListener = new OnClickListener() {
	    public void onClick(View v) {
                launchLocationPicker();
	    }
	};
    private void launchLocationPicker() {
        startActivityForResult(new Intent(this, LocationPicker.class), s_locationPickerId);
    }



    // When a sub-activity is finished, this is called, provided we registered for 
    // result when we launched the sub-activity 
    @Override public void onActivityResult(int request, int result, Intent data) {
        if (result == RESULT_OK) {
            if (request == s_locationPickerId) {
                double lat = data.getDoubleExtra("com.github.tommywalsh.mbta.Lat", 0.0);
                double lng = data.getDoubleExtra("com.github.tommywalsh.mbta.Lng", 0);
                Vector<Integer> departurePoints = ProximityProfileGenerator.getProximityProfile(m_db, lat, lng, 0.5);
		viewDepartures(departurePoints);
            }
        }
    }

    // Called when a dialog is requested
    public Dialog onCreateDialog(int index) {
	switch (index) {
	case s_profileDialogId:
	    return getProfilePickerDialog();
	default:
	    return super.onCreateDialog(index);
	}
    } 


    private void viewDepartures(Vector<Integer> dp) {
	final int size = dp.size();
	int[] transitArray = new int[size];
	for (int ix = 0; ix < size; ix++) {
	    transitArray[ix] = dp.elementAt(ix);
	    Integer i = new Integer(transitArray[ix]);
	}
	Intent i = new Intent(this, DepartureViewer.class);
	i.putExtra(getString(R.string.departures_in_intent), transitArray);
	startActivity(i);        
    }


    private DatabaseBuilder m_dbBuilder;
    private SQLiteDatabase m_db;

    @Override public void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_screen);

	m_dbBuilder = new DatabaseBuilder(getApplicationContext());

        MBTADBOpenHelper openHelper = new MBTADBOpenHelper(getApplicationContext());
	m_db = openHelper.getReadableDatabase();

	m_profProvider = new ProfileProvider(this);
	

        Button profileButton = (Button)findViewById(R.id.saved_profile_button);
        profileButton.setOnClickListener(m_profileListener);

        Button proximityButton = (Button)findViewById(R.id.proximity_button);
        proximityButton.setOnClickListener(m_proximityListener);

	Button databaseButton = (Button)findViewById(R.id.database_button);
	databaseButton.setOnClickListener(m_databaseListener);
    }   

}
