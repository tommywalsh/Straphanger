// Copyright 2010-11 Tom Walsh
//
// This program is free software released under version 3
// of the GPL.  See file gpl.txt for more information.

package com.github.tommywalsh.mbta;

import android.content.Context;
import java.util.Vector;

// This class may be eliminatable in the future.  Right now, it's sole purpose
// is to provide a list of departure points in a particular profile.  This
// data needs to be used in the same way as the data returned from 
// ProximityProfileGenerator.  Should that class be modified to insert its
// info into the database, or should we change to an interface that both
// data sources can provide, then this class can be eliminated, and callers
// can go straight to the database.
public class ProfileProvider
{
    private Context m_context;

    ProfileProvider(Context context) {
	m_context = context;
    }


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
