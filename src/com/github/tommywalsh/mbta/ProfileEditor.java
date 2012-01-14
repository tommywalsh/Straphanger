// Copyright 2011 Tom Walsh
//
// This program is free software released under version 3
// of the GPL.  See file gpl.txt for more information.

package com.github.tommywalsh.mbta;

import android.app.ListActivity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.content.Intent;
import android.content.Context;
import android.content.DialogInterface;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;
import android.widget.EditText;
import android.widget.CompoundButton;
import android.view.View;
import android.view.ViewGroup;
import android.view.LayoutInflater;

import java.io.Serializable;
import java.util.Vector;

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

    private ProfileEditHelper m_helper;

    private Vector<ProfileEditHelper.Entry> m_items;
    private Vector<Integer> m_departures = new Vector<Integer>();
    private int m_profileId;

    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.editor);
        
        Intent i = getIntent();

        m_profileId = i.getIntExtra(getString(R.string.profile_in_intent), ProfileEditHelper.NEW_PROFILE);

        m_helper = new ProfileEditHelper(getApplicationContext(), m_profileId);
        m_helper.clearBuffer();
        if (m_profileId != ProfileEditHelper.NEW_PROFILE) {
            m_helper.loadBufferFromPersistentStorage();
        }

        /*        Button addToProfileButton = (Button)findViewById(R.id.add_to_profile);
        addToProfileButton.setOnClickListener(m_addToProfileListener);

        Button saveProfileButton = (Button)findViewById(R.id.save_profile);
        saveProfileButton.setOnClickListener(m_saveProfileListener);
        */
        Button deleteProfileButton = (Button)findViewById(R.id.delete_profile);
        deleteProfileButton.setOnClickListener(m_deleteProfileListener);
        /*
        Button cancelButton = (Button)findViewById(R.id.cancel_profile);
        cancelButton.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    finish();
                }
            });
        */
        TextView header = (TextView)findViewById(R.id.editor_header);
        header.setText(m_helper.getBufferName());

    }


    private String m_profileName;
    @Override protected void onResume() {
        super.onResume();

        m_profileName = m_helper.getBufferName();
        m_items = m_helper.getItemsFromBuffer();
        setListAdapter(new ProfileInfoAdapter());            
    }
    
    @Override protected void onPause() {
        super.onPause();
        m_helper.suspend();
    }


    private static final int s_locationPickerId = 2050;    
    private OnClickListener m_addToProfileListener = new OnClickListener() {
	    public void onClick(View v) {
                // Launch the location picker.  We'll infer busses when it returns
                startActivityForResult(new Intent(ProfileEditor.this, LocationPicker.class), 
                                       s_locationPickerId);
	    }
	};


    private OnClickListener m_saveProfileListener = new OnClickListener() {
            EditText tv;
	    public void onClick(View v) {
                tv = new EditText(ProfileEditor.this);
                AlertDialog.Builder builder = new AlertDialog.Builder(ProfileEditor.this);
                builder.setTitle("Profile Name");
                builder.setCancelable(true);

                if (m_profileName.length() == 0) {
                    tv.setText("Untitled");
                } else {
                    tv.setText(m_profileName);
                }
                builder.setView(tv);
                builder.setNegativeButton("Cancel", null);
                builder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            //                            getDB().saveProfile(m_profileId, tv.getText().toString(), m_departures);
                            finish();
                        }
                    });
                builder.show();
            }
        };


    private OnClickListener m_deleteProfileListener = new OnClickListener() {
	    public void onClick(View v) {
                TextView tv = new TextView(ProfileEditor.this);
                AlertDialog.Builder builder = new AlertDialog.Builder(ProfileEditor.this);
                builder.setCancelable(true);
                builder.setTitle("Delete Profile?");
                tv.setText("For realz???");
                builder.setView(tv);
                builder.setNegativeButton("OMFG! Noooo!", null);
                builder.setPositiveButton("Yes!  GTFO!", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            //                            getDB().deleteProfile(m_profileId);
                            m_helper.deletePersistentStorage();
                            finish();
                        }
                    });
                builder.show();
            }
        };


    @Override public void onActivityResult(int request, int result, Intent data) {
        if (result == RESULT_OK) {
            if (request == s_locationPickerId) {
                double lat = data.getDoubleExtra("com.github.tommywalsh.mbta.Lat", 0.0);
                double lng = data.getDoubleExtra("com.github.tommywalsh.mbta.Lng", 0.0);
                Vector<Integer> newDeparturePoints = ProximityProfileGenerator.getProximityProfile(this, lat, lng, 0.25);
                m_departures.addAll(newDeparturePoints);
                setListAdapter(new ProfileInfoAdapter());
            }
        }
    }



    private class ProfileInfoAdapter extends BaseAdapter 
    {
        public ProfileInfoAdapter() {
        }

        public int getCount() {
            return 0;
        }
        
        public Object getItem(int position) {
            return m_items.elementAt(position);
        }

        public long getItemId(int position) {
            return position;
        }
        
        public View getView(int position, View convertView, ViewGroup viewGroup) {
            if (convertView == null) {
                LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.profile_entry, null);
            }

            ProfileEditHelper.Entry thisInfo = m_items.elementAt(position);
            
            TextView routeWidget = (TextView) convertView.findViewById(R.id.route_title);
            routeWidget.setText(thisInfo.route);

            TextView subrouteWidget = (TextView) convertView.findViewById(R.id.subroute_title);
            subrouteWidget.setText(thisInfo.subroute);

            TextView stopWidget = (TextView) convertView.findViewById(R.id.stop_title);
            stopWidget.setText(thisInfo.stop);
                
            return convertView;
        }
    }
}

