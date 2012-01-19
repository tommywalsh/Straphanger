package com.github.tommywalsh.mbta;

// Copyright 2012 Tom Walsh
//
// This program is free software released under version 3
// of the GPL.  See file gpl.txt for more information.

import android.content.Context;
import android.content.SharedPreferences;


// This is a simple singleton class that can be used to 
// check whether or not we have a complete database
public class DatabaseMonitor
{

    public static boolean isComplete() {
        return s_isComplete;
    }

    public static void setComplete (boolean complete) {
        s_isComplete = complete;
        savePersistent();        
    }

    public static void init(Context applicationContext) {
        s_appContext = applicationContext;
        loadPersistent();
    }




    private static boolean s_isComplete = false;
    private static Context s_appContext;

    private static final String PREFS_NAME = "db_status";
    private static final String LOADED_PREF = "is_loaded";

    private static void loadPersistent() {
        SharedPreferences settings = s_appContext.getSharedPreferences(PREFS_NAME, 0);
        s_isComplete = settings.getBoolean(LOADED_PREF, false);        
    }

    private static void savePersistent() {
        SharedPreferences settings = s_appContext.getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean(LOADED_PREF, s_isComplete);
        editor.commit();
    }
}

