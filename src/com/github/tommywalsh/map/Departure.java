// Copyright 2010 Tom Walsh
//
// This program is free software released under version 3
// of the GPL.  See file gpl.txt for more information.

package com.github.tommywalsh.mbta;

public class Departure implements Comparable {

    public int when;
    public String where;
    public String route;
    public String direction;

    public int compareTo(Object o) {
	Departure other = (Departure) o;
	int result = when - other.when;
	if (result == 0) {
	    result = route.compareTo(other.route);
	}
	return result;
    }

}
