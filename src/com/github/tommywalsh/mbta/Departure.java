// Copyright 2010 Tom Walsh
//
// This program is free software released under version 3
// of the GPL.  See file gpl.txt for more information.

package com.github.tommywalsh.mbta;

public class Departure extends DeparturePoint implements Comparable {

    public long when;
    public String direction;  // direction belongs in DeparturePoint!

    public int compareTo(Object o) {
	Departure other = (Departure) o;
	int result = (int) (when - other.when);
	if (result == 0) {
	    result = route.compareTo(other.route);
	}
	return result;
    }

}
