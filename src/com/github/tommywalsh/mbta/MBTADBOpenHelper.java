// Copyright 2011 Tom Walsh
//
// This program is free software released under version 3
// of the GPL.  See file gpl.txt for more information.

package com.github.tommywalsh.mbta;

import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteDatabase;
import android.content.Context;

public class MBTADBOpenHelper extends SQLiteOpenHelper
{
    MBTADBOpenHelper(Context context) {
	super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    private String createTable(String name, String schema) {
	return "CREATE TABLE " + name + " " + schema + ";";
    }
    @Override public void onCreate(SQLiteDatabase db) {
	db.execSQL(createTable(ROUTE_NAME, ROUTE_SCHEMA));
	db.execSQL(createTable(STOP_NAME,  STOP_SCHEMA));
	db.execSQL(createTable(ROUTE_DIRECTION_NAME, ROUTE_DIRECTION_SCHEMA));
	db.execSQL(createTable(DEPARTURE_POINT_NAME, DEPARTURE_POINT_SCHEMA));
	db.execSQL(createTable(PROFILE_NAME, PROFILE_SCHEMA));
	db.execSQL(createTable(PROFILE_POINTS_NAME, PROFILE_POINTS_SCHEMA));
    }

    @Override public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }

    private static final String DATABASE_NAME = "straphanger";
    private static final int DATABASE_VERSION = 1;

    private static final String ROUTE_NAME = "route";
    private static final String ROUTE_SCHEMA = 
	"(id INTEGER PRIMARY KEY, tag TEXT, title TEXT, minLat REAL, maxLat REAL, minLng REAL, maxLng REAL)";	

    private static final String STOP_NAME = "stop";
    private static final String STOP_SCHEMA = 
	"(id INTEGER PRIMARY KEY, tag TEXT, lat REAL, lng REAL, title TEXT)";

    private static final String ROUTE_DIRECTION_NAME = "route_direction";
    private static final String ROUTE_DIRECTION_SCHEMA = 
	"(id INTEGER PRIMARY KEY, route INTEGER NOT NULL, direction TEXT, FOREIGN KEY(route) REFERENCES route(id) )";

    private static final String DEPARTURE_POINT_NAME = "departure_point";
    private static final String DEPARTURE_POINT_SCHEMA = 
	"(id INTEGER PRIMARY KEY, routedir INTEGER NOT NULL, stop INTEGER NOT NULL," +
	" FOREIGN KEY(routedir) REFERENCES route_direction(id)," +
	" FOREIGN KEY(stop) REFERENCES stop(id) )";

    private static final String PROFILE_NAME = "profile";
    private static final String PROFILE_SCHEMA =
	"(id INTEGER PRIMARY KEY, name TEXT)";

    private static final String PROFILE_POINTS_NAME = "profile_point";
    private static final String PROFILE_POINTS_SCHEMA =
	"(profile INTEGER NOT NULL, point INTEGER NOT NULL," +
	" FOREIGN KEY(profile) REFERENCES profile(id)," +
	" FOREIGN KEY(point) REFERENCES departure_point(id) )";
	

}