// Copyright 2011 Tom Walsh
//
// This program is free software released under version 3
// of the GPL.  See file gpl.txt for more information.

package com.github.tommywalsh.mbta;

import java.io.Serializable;

// A simple class that describes were one can catch a bus
// If you want to know WHICH busses you can catch at a stop, see the
// class DeparturePoint
public class Stop implements Serializable {
    public String tag;
    public double lat;
    public double lng;
    public String title;
    public Stop() {}
    public Stop (Stop other) {
        tag = other.tag;
        lat = other.lat;
        lng = other.lng;
        title = other.title;
    }
}

