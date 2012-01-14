package com.github.tommywalsh.mbta;

import android.content.Context;
import java.util.Vector;

// This class works on the analogy of a current working buffer,
// which can be saved to (or restored from) persistent storage.
// Simple edit operations are performed on the current working buffer
// 
// In acutality, every action is immediately executed directly in the
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
        android.util.Log.d("mbta", "Editing profile " + i);
        m_ctx = ctx;
        m_persistentId = persistentProfileId;
    }

    public void clearBuffer() 
    {
        android.util.Log.d("mbta", "Clearing buffer");
        clearProfile(BUFFER_PROFILE);
    }

    public void loadBufferFromPersistentStorage() 
    {
        android.util.Log.d("mbta", "Loading buffer");
        copyProfile(m_persistentId, BUFFER_PROFILE);
    }

    public void saveBufferToPersistentStorage() 
    {
        android.util.Log.d("mbta", "Saving buffer");
        copyProfile(BUFFER_PROFILE, m_persistentId);
    }

    public void deletePersistentStorage() 
    {
        android.util.Log.d("mbta", "Deleting persistent");
        clearProfile(m_persistentId);
    }

    public void removeItemFromBuffer(int departurePoint) 
    {
        android.util.Log.d("mbta", "Removing item");
        getDB().deleteDeparturePointFromProfile(BUFFER_PROFILE, departurePoint);
    }    

    public Vector<Entry> getItemsFromBuffer()
    {
        android.util.Log.d("mbta", "Getting all items...");
        Vector<Entry> entries = new Vector<Entry>();
        Database.ProfileInfoCursorWrapper cursor = getDB().getProfileInfo(BUFFER_PROFILE);
        cursor.moveToFirst();
        while(!(cursor.isAfterLast())) {
            android.util.Log.d("mbta", "Reading an item");
            entries.addElement(new Entry(cursor.getStopTitle(),
                                         cursor.getSubrouteTitle(),
                                         cursor.getRouteTitle(),
                                         cursor.getDepartureId()));
            
            cursor.moveToNext();
        }
        cursor.close();
        return entries;
    }

    public String getBufferName()
    {
        android.util.Log.d("mbta", "Getting name");
        String name = getDB().getProfileName(BUFFER_PROFILE);
        android.util.Log.d("mbta", "Just got name");
        return name;
    }

    public void suspend()
    {
        android.util.Log.d("mbta", "Suspending");
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
        android.util.Log.d("mbta", "Pulling from " + s);

        String name = db.getProfileName(sourceId);
        Vector<Integer> points = new Vector<Integer>();
        Database.DeparturePointCursorWrapper cursor = db.getDeparturePointsInProfile(sourceId);
        cursor.moveToFirst();
        while (!(cursor.isAfterLast())) {
            android.util.Log.d("mbta", "Adding point from " + s);
            points.add(cursor.getDeparturePointId());
            cursor.moveToNext();
        }

        // and push it into destination
        db.saveProfile(destinationId, name, points);
    }





}