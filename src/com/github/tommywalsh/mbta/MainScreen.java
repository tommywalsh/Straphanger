// Copyright 2011 Tom Walsh
//
// This program is free software released under version 3
// of the GPL.  See file gpl.txt for more information.

package com.github.tommywalsh.mbta;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.content.Intent;
import android.content.Context;

public class MainScreen extends Activity
{
    private static final int s_locationPickerId = 1050;

    private OnClickListener m_profileListener = new OnClickListener() {
	    public void onClick(View v) {
                android.util.Log.d("mbta", "Choose profile");
	    }
	};



    private OnClickListener m_proximityListener = new OnClickListener() {
	    public void onClick(View v) {
                android.util.Log.d("mbta", "Show nearby busses");
                launchLocationPicker();
	    }
	};
    private void launchLocationPicker() {
        startActivityForResult(new Intent(this, LocationPicker.class), s_locationPickerId);
    }




    @Override public void onActivityResult(int request, int result, Intent data) {
        if (result == RESULT_OK) {
            if (request == s_locationPickerId) {
                double lat = data.getDoubleExtra("com.github.tommywalsh.mbta.Lat", 0.0);
                double lng = data.getDoubleExtra("com.github.tommywalsh.mbta.Lng", 0);
                Profile p = ProximityProfileGenerator.getProximityProfile(lat, lng, 0.5);
                viewDeparturesForProfile(p);
            }
        }
    }



    private void viewDeparturesForProfile(Profile p) {
        Intent i = new Intent(this, DepartureViewer.class);
        i.putExtra("com.github.tommywalsh.mbta.Profile", p);
        startActivity(i);        
    }


    @Override public void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_screen);
	
        Button profileButton = (Button)findViewById(R.id.saved_profile_button);
        profileButton.setOnClickListener(m_profileListener);

        Button proximityButton = (Button)findViewById(R.id.proximity_button);
        proximityButton.setOnClickListener(m_proximityListener);
    }   
}
