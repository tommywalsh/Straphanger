// Copyright 2010-11 Tom Walsh
//
// This program is free software released under version 3
// of the GPL.  See file gpl.txt for more information.

package com.github.tommywalsh.mbta;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import java.util.Vector;
import java.io.Serializable;
import java.io.ObjectInputStream;
import java.io.FileInputStream;
import java.io.ObjectOutputStream;
import java.io.FileOutputStream;

public class ProfileProvider
{
    private Context m_context;

    ProfileProvider(Context context) {
	m_context = context;
    }

    public Vector<Profile> getProfiles() {
        MBTADBOpenHelper openHelper = new MBTADBOpenHelper(m_context.getApplicationContext());
	SQLiteDatabase db = openHelper.getReadableDatabase();
        String query = "SELECT id,name FROM profile"; // TODO: make this a pre-compiled query
        Cursor cursor = db.rawQuery(query,null);
        cursor.moveToFirst();
        Vector<Profile> profiles = new Vector<Profile>();
        while (!(cursor.isAfterLast())) {
            Profile profile = new Profile();
            profile.id = cursor.getInt(0);
            profile.name = cursor.getString(1);
            profiles.addElement(profile);
            cursor.moveToNext();
        }
        return profiles;
    }


    public Vector<Integer> getDeparturePointsInProfile(int profileId) {
        MBTADBOpenHelper openHelper = new MBTADBOpenHelper(m_context.getApplicationContext());
	SQLiteDatabase db = openHelper.getReadableDatabase();
        Integer pid = new Integer(profileId);
        String query = "SELECT point FROM profile_point WHERE profile = " + pid.toString();
        Vector<Integer> departurePoints = new Vector<Integer>();
        Cursor cursor = db.rawQuery(query,null);
        cursor.moveToFirst();
        while(!(cursor.isAfterLast())) {
            departurePoints.addElement(new Integer(cursor.getInt(0)));
            cursor.moveToNext();
        }
        return departurePoints;
    }

}
