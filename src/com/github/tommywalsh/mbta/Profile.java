// Copyright 2010 Tom Walsh
//
// This program is free software released under version 3
// of the GPL.  See file gpl.txt for more information.

package com.github.tommywalsh.mbta;

import java.util.Vector;

public class Profile
{
    public Vector<Stop> stops = new Vector<Stop>();
    public String name;
    

    static Profile getHomeToWorkProfile() {
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
}