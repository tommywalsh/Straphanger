// Copyright 2011 Tom Walsh
//
// This program is free software released under version 3
// of the GPL.  See file gpl.txt for more information.

package com.github.tommywalsh.mbta;

import android.app.ListActivity;
import android.os.Bundle;
import android.content.Intent;

import java.io.Serializable;


// This is an on-screen editor that lets users edit profiles
//   You may pass a profile in the Intent you use to spawn the activity
//     If you do, that profile is edited
//     If not, a new profile is created
// 
//   This activity can be spawned to request a return value or not
//     If so, and if the user doesn't cancel, then
//       the changed profile will be returned.
public class ProfileEditor extends ListActivity
{

    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent i = getIntent();
        int profileId = i.getIntExtra(getString(R.string.profile_in_intent), -1);
        if (profileId == -1) {
            android.util.Log.d("mbta", "no profile, edit new");
        } else {
            android.util.Log.d("mbta", "got a profile, edit existing");
        }
    }

    
    @Override protected void onResume() {
        super.onResume();
    }
    
    @Override protected void onPause() {
        super.onPause();
    }
}

