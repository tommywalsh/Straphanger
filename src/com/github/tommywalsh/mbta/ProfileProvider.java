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

    // TODO: why not just pass cursor out?
    public Vector<Profile> getProfiles() {
	Database db = new Database(m_context);
	Database.ProfileCursorWrapper cursor = db.getProfiles();
	cursor.moveToFirst();
        Vector<Profile> profiles = new Vector<Profile>();
        while (!(cursor.isAfterLast())) {
            Profile profile = new Profile();
            profile.id = cursor.getProfileId();
            profile.name = cursor.getProfileName();
            profiles.addElement(profile);
            cursor.moveToNext();
        }
        return profiles;
    }


    // TODO: why not just pass cursor out?
    public Vector<Integer> getDeparturePointsInProfile(int profileId) {
	Database db = new Database(m_context);
	Database.DeparturePointCursorWrapper cursor = db.getDeparturePointsInProfile(profileId);
        Vector<Integer> departurePoints = new Vector<Integer>();
        cursor.moveToFirst();
        while(!(cursor.isAfterLast())) {
            departurePoints.addElement(new Integer(cursor.getInt(0)));
            cursor.moveToNext();
        }
        return departurePoints;
    }

}
