// Copyright 2012 Tom Walsh
//
// This program is free software released under version 3
// of the GPL.  See file gpl.txt for more information.

package com.github.tommywalsh.mbta;

import android.content.Context;
import java.util.Vector;
import java.io.Serializable;

// This is a quick-and-dirty way to pass information between activities.
//
// We have a number of cases where we'd like to pass potentially large
// amounts of data between intents (e.g. lists of busses and stops).
//
// One technique would be to copy them all into an array or some other 
// parcelable object and shove them into an intent.  This is cumbersome,
// and seems to have poor performance, at least on my phone.
//
// Another technique would be to set up a private ContentProvider.  This
// is a bit of overkill for what we need, though.
//
// Here's how to use this class:
//
// (in the "caller")
// int key = Scratchpad.putObject(myLargeAmountOfData);
// intent.putExtra(name, key);
//
// (in the called subactivity)
// int key = intent.getIntExtra(name);
// myData = (BigObject)Scratchpad.popObject(key);
//
import java.util.HashMap;
 
public class Scratchpad
{
    private static HashMap<Integer, Object> s_map = new HashMap<Integer, Object>();
    private static int s_maxKey = 0;

    public static final int NO_KEY = 0;

    public static int putObject(Object obj) {
        s_maxKey = s_maxKey + 1;
        s_map.put(s_maxKey, obj);
        return s_maxKey;
    }

    public static Object popObject(int key) {
        Object obj = s_map.get(key);
        s_map.remove(key);
        return obj;
    }
}
