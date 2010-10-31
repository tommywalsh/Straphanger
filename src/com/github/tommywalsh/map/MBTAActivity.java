// Copyright 2010 Tom Walsh
//
// This program is free software released under version 3
// of the GPL.  See file gpl.txt for more information.

package com.github.tommywalsh.mbta;

import android.app.ListActivity;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.os.Bundle;

import java.util.SortedSet;

public class MBTAActivity extends ListActivity
{
    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
	MBTAParser parser = new MBTAParser();

	ArrayAdapter<String> aa = new ArrayAdapter<String>(this, R.layout.listitem);
	try {
	    SortedSet<Departure> ds = parser.parse(MBTADataService.getPredictionStream());
	    for (Departure d : ds) {
		String mess = d.route + " to " + d.direction + " stops at " + d.where + " in " + new Integer(d.when).toString() + " minutes";
		Log.d("mbta", mess);
		aa.add(mess);
	    }
	    setListAdapter(aa);
	} catch (java.io.IOException e) {
	}
    }

}


