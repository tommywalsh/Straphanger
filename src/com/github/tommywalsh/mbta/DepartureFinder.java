// Copyright 2010 Tom Walsh
//
// This program is free software released under version 3
// of the GPL.  See file gpl.txt for more information.

package com.github.tommywalsh.mbta;
import java.util.SortedSet;
import java.util.TreeSet;


public class DepartureFinder
{
    // should be changed to DepartureSet?
    // Note that this method can take a long time.
    // Probably not a good idea to call it directy from GUI thread.  Use a runnable instead.
    static SortedSet<Departure> getDeparturesForProfile(Profile profile) 
    {
        MBTAParser parser = new MBTAParser();
        try {
            return parser.parse(MBTADataService.getPredictionStream(profile));
        } catch (java.io.IOException e) {
            android.util.Log.d("MBTA", e.toString());
        }
        return new TreeSet<Departure>();
    }

}
