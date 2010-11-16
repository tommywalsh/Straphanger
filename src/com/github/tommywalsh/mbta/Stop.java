// Copyright 2010 Tom Walsh
//
// This program is free software released under version 3
// of the GPL.  See file gpl.txt for more information.

package com.github.tommywalsh.mbta;

public class Stop {
    public String route;
    public String where;
    
    public Stop() {
    }

    public Stop(String r, String w) {
	where = w;
	route = r;
    }
}
