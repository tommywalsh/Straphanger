// Copyright 2010 Tom Walsh
//
// This program is free software released under version 3
// of the GPL.  See file gpl.txt for more information.

package com.github.tommywalsh.mbta;

import java.util.Vector;
import java.io.Serializable;

public class Profile implements Serializable
{
    public Vector<Stop> stops = new Vector<Stop>();
    public String name;   
}