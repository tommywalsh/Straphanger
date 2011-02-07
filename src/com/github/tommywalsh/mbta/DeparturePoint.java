// Copyright 2010 Tom Walsh
//
// This program is free software released under version 3
// of the GPL.  See file gpl.txt for more information.

package com.github.tommywalsh.mbta;

import java.io.Serializable;

public class DeparturePoint implements Serializable
{
    public String route;
    public String direction;
    public String where;
    
    
    public DeparturePoint() {
    }
    
    public DeparturePoint(String r, String w) {
	where = w;
	route = r;
    }
}
