// Copyright 2010 Tom Walsh
//
// This program is free software released under version 3
// of the GPL.  See file gpl.txt for more information.

package com.github.tommywalsh.mbta;

import android.app.ListActivity;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.os.Bundle;

import android.util.Log;

import java.util.SortedSet;

public class MBTAActivity extends ListActivity
{
    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

	MBTAParser parser = new MBTAParser();
	long now = java.lang.System.currentTimeMillis();

	ArrayAdapter<String> aa = new ArrayAdapter<String>(this, R.layout.listitem);
	try {
	    SortedSet<Departure> ds = parser.parse(MBTADataService.getPredictionStream());
	    for (Departure d : ds) {
		int secondsLeft = (int) ((d.when - now) / 1000);
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

		Log.d("mbta", mess);
		aa.add(mess);
	    }
	    setListAdapter(aa);
	} catch (java.io.IOException e) {
	}
    }

}


