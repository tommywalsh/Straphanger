// Copyright 2011 Tom Walsh
//
// This program is free software released under version 3
// of the GPL.  See file gpl.txt for more information.

package com.github.tommywalsh.mbta;

public class Prediction implements Comparable {
    public long when;
    public String stopTitle;
    public String routeTitle;
    public String routeDirection;


    public int compareTo(Object o) {
	Prediction other = (Prediction) o;
	int result = (int) (when - other.when);
	if (result == 0) {
	    result = routeTitle.compareTo(other.routeTitle);
	}
	return result;
    }
}
