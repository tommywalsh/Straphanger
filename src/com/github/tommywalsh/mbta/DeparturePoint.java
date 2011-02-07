// Copyright 2010 Tom Walsh
//
// This program is free software released under version 3
// of the GPL.  See file gpl.txt for more information.

package com.github.tommywalsh.mbta;

import java.io.Serializable;

public class DeparturePoint extends Stop implements Serializable
{
    public Route route;
    public String direction;
    
    public DeparturePoint() {
    }
    
    public DeparturePoint(String routeId, String stopId) {
        tag = stopId;
        route = Route.getRoute(routeId);
    }

    public DeparturePoint(Stop stop, Route rt, String dir) {
        super(stop);
        route = rt;
        direction = dir;
    }
}
