// Copyright 2012 Tom Walsh
//
// This program is free software released under version 3
// of the GPL.  See file gpl.txt for more information.

package com.github.tommywalsh.mbta;

import android.content.Context;
import java.util.Vector;

// This class works on the analogy of a current working buffer,
// backed with a persistent storage area.
//
// All edits happen in the working buffer.  The working buffer can
// be saved to persistent storage.  The buffer can also be thrown
// out and replaced with what's in persistent storage.
// 
// In actuality, every action is immediately executed directly in the
// database.  This is to enable applications to resume where they left
// off, regardless of the state of the Android system.
public class ProfileEditHelper
{

    public static final int NEW_PROFILE = -1;

    public class Entry 
    {
        String stop;
        String subroute;
        String route;
        Integer stopId;
        
        public Entry(String s, String sr, String r, Integer id) {
            stop = s; subroute = sr; route = r; stopId = id;
        }
    }


    // Pass in the application context, and the ID of the profile we'll be editing
    // If this is to be a new profile, pass in NEW_PROFILE
    public ProfileEditHelper(Context ctx, int persistentProfileId) {
        Integer i = new Integer(persistentProfileId);
        m_ctx = ctx;
        m_persistentId = persistentProfileId;
    }

    public void clearBuffer() 
    {
        clearProfile(BUFFER_PROFILE);
    }

    public void loadBufferFromPersistentStorage() 
    {
        if (m_persistentId == NEW_PROFILE) {
            clearBuffer();
        } else {
            copyProfile(m_persistentId, BUFFER_PROFILE);
        }
    }

    public void saveBufferToPersistentStorage() 
    {
        if (m_persistentId == NEW_PROFILE) {
            m_persistentId = getNewId();
        }
        copyProfile(BUFFER_PROFILE, m_persistentId);
    }

    public void deletePersistentStorage() 
    {
        clearProfile(m_persistentId);
    }

    public void removeItemFromBuffer(int departurePoint) 
    {
        getDB().deleteDeparturePointFromProfile(BUFFER_PROFILE, departurePoint);
    }    

    public void addItemsToBuffer(Vector<Integer> departurePoints)
    {
        getDB().addDeparturePointsToProfile(BUFFER_PROFILE, departurePoints);
    }

    public Vector<Entry> getItemsFromBuffer()
    {
        Vector<Entry> entries = new Vector<Entry>();
        Database.ProfileInfoCursorWrapper cursor = getDB().getProfileInfo(BUFFER_PROFILE);
        cursor.moveToFirst();
        while(!(cursor.isAfterLast())) {
            Entry e = new Entry(cursor.getStopTitle(),
                                cursor.getSubrouteTitle(),
                                cursor.getRouteTitle(),
                                cursor.getDepartureId());
            entries.addElement(e);
            cursor.moveToNext();
        }
        cursor.close();
        return entries;
    }

    public String getBufferName()
    {
        String name = getDB().getProfileName(BUFFER_PROFILE);
        return name;
    }

    public void setBufferName(String name)
    {
        m_db.setProfileName(BUFFER_PROFILE, name);
    }

    public void suspend()
    {
        if (m_db != null) {
            m_db.close();
            m_db = null;
        }
    }


    private static final int BUFFER_PROFILE = 1;

    private Context m_ctx;
    private int m_persistentId;
    private Database m_db;

    private Database getDB()
    {
        if (m_db == null) {
            m_db = new Database(m_ctx);
        }
        return m_db;
    }


    private void clearProfile(int profileId)
    {
        getDB().deleteProfile(profileId);
    }


    private void copyProfile(int sourceId, int destinationId)
    {
        clearProfile(destinationId);

        Database db = getDB();
        Integer s = new Integer(sourceId);
        Integer d = new Integer(destinationId);

        // Pull information out of source
        String name = db.getProfileName(sourceId);
        Vector<Integer> points = new Vector<Integer>();
        Database.DeparturePointCursorWrapper cursor = db.getDeparturePointsInProfile(sourceId);
        cursor.moveToFirst();
        while (!(cursor.isAfterLast())) {
            points.add(cursor.getDeparturePointId());
            cursor.moveToNext();
        }

        // and push it into destination
        db.saveProfile(destinationId, name, points);
    }


    private int getNewId() {
        int max = getDB().getLargestProfileId();
        if (max < 2) {
            return 2;
        } else {
            return max+1;
        }
    }



}