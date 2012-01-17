package com.github.tommywalsh.mbta;

// Copyright 2012 Tom Walsh
//
// This program is free software released under version 3
// of the GPL.  See file gpl.txt for more information.

import android.content.Context;
import android.content.DialogInterface;
import android.app.AlertDialog;





// This class presents a simple dialog that allows the user to 
// pick from the list of all profiles currently in the database
public class ProfileChooserDialog
{

    // Implement this listener, which will be called when the user chooses
    // a profile
    public interface Listener {
        public void onProfileChoose(int profileId);
    }

    public ProfileChooserDialog(Context context, Listener chooseListener) {
        m_listener = chooseListener;
        loadProfileInfo(context);
        initDialog(context);
    }

    public void show() {
        m_dialog.show();
    }





    // Private data storage
    private class ProfileInfo
    {
        String[] profileNames;
        int[] profileIds;
    };
    private Listener m_listener;
    private AlertDialog m_dialog;
    private ProfileInfo m_info;


    // This function queries the database for all profiles, and stores the 
    // information in a form that's easy for the dialog to use.
    void loadProfileInfo(Context context) {
        Database db = new Database(context);
        Database.ProfileCursorWrapper cursor = db.getProfiles();
        
        final int size = cursor.getCount();
        
        m_info = new ProfileInfo();
        m_info.profileNames = new String[size];
        m_info.profileIds = new int[size];
        
        // Loop over all the profiles and collect their relevant information
        cursor.moveToFirst();
        for (int i = 0; i < size & !(cursor.isAfterLast()); i++) {
            m_info.profileNames[i] = cursor.getProfileName();
            m_info.profileIds[i] = cursor.getProfileId();
            cursor.moveToNext();                    
        }
        cursor.close();
        db.close();
    }



    // Use a builder to construct a dialog for us, using the 
    // profile data collected above
    private void initDialog(Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Pick a profile");
        
        builder.setItems(m_info.profileNames, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface d, int id) {
                    // When the user clicks a button, translate that into
                    // a profile choice, and pass it on to the listener
                    m_listener.onProfileChoose(m_info.profileIds[id]);
                }});
        
        m_dialog = builder.create();
    }
}

