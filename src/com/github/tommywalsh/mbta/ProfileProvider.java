// Copyright 2010 Tom Walsh
//
// This program is free software released under version 3
// of the GPL.  See file gpl.txt for more information.

package com.github.tommywalsh.mbta;

import java.util.Vector;
import java.io.Serializable;

public class ProfileProvider implements Serializable {

    static private Vector<Profile> s_allProfiles = null;
    
    static public synchronized Vector<Profile> getAllProfiles() {
	if (s_allProfiles == null) {
	    s_allProfiles = new Vector<Profile>();
	    s_allProfiles.add(getHomeToWorkProfile());
	    s_allProfiles.add(getWorkToHomeProfile());
	}
	return s_allProfiles;
    }


    static private Profile getHomeToWorkProfile() {
	Profile p = new Profile();	
	p.stops.add(new Stop("748","2541"));
	p.stops.add(new Stop("748","2612"));
	p.stops.add(new Stop("91","2531"));
	p.stops.add(new Stop("91","2612"));
	p.stops.add(new Stop("86","25712"));
	p.stops.add(new Stop("86","2615"));
	p.stops.add(new Stop("85","2612"));
	p.stops.add(new Stop("87","2510"));
	p.name = "Home to Work";
	return p;
    }

    static private Profile getWorkToHomeProfile() {
	Profile p = new Profile();	
	p.stops.add(new Stop("747","17863"));
	p.stops.add(new Stop("47","17863"));
	p.stops.add(new Stop("1","64"));
	p.name = "Work To Home";
	return p;
    }

}
