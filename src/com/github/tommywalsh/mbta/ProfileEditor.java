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
import android.widget.CheckBox;
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

    private Vector<Integer> m_departures = new Vector<Integer>();
    private boolean[] m_checkMap;
    private Database m_db;
    private int m_profileId;

    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.editor);
        
        Intent i = getIntent();
        // We know that -1 won't be in the database as an index.  Use it here as code for 
        // "new profile"
        m_profileId = i.getIntExtra(getString(R.string.profile_in_intent), -1);


        Button addToProfileButton = (Button)findViewById(R.id.add_to_profile);
        addToProfileButton.setOnClickListener(m_addToProfileListener);

        Button deleteFromProfileButton = (Button)findViewById(R.id.delete_from_profile);
        deleteFromProfileButton.setOnClickListener(m_deleteFromProfileListener);

        Button saveProfileButton = (Button)findViewById(R.id.save_profile);
        saveProfileButton.setOnClickListener(m_saveProfileListener);

        Button deleteProfileButton = (Button)findViewById(R.id.delete_profile);
        deleteProfileButton.setOnClickListener(m_deleteProfileListener);

        Button cancelButton = (Button)findViewById(R.id.cancel_profile);
        cancelButton.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    finish();
                }
            });

        TextView header = (TextView)findViewById(R.id.editor_header);
        if (m_profileId >= 0) {
            m_profileName = getDB().getProfileName(m_profileId);
            header.setText(m_profileName);
        }

    }


    private Database getDB() {
        if (m_db == null) {
            m_db = new Database(this);
        }
        return m_db;
    }
    private void closeDB() {
        if (m_db != null) {
            m_db.close();
            m_db = null;
        }
    }

    private String m_profileName;
    @Override protected void onResume() {
        super.onResume();

        Database db = getDB();
        m_profileName = db.getProfileName(m_profileId);
        Database.DeparturePointCursorWrapper cursor = db.getDeparturePointsInProfile(m_profileId);
        cursor.moveToFirst();
        while(!(cursor.isAfterLast())) {
            m_departures.addElement(cursor.getDeparturePointId());
            cursor.moveToNext();
        }
        cursor.close();

        m_checkMap = new boolean[m_departures.size()];
        setListAdapter(new ProfileInfoAdapter(m_db.getProfileInfo(m_departures)));
    }
    
    @Override protected void onPause() {
        super.onPause();
        closeDB();
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
                            getDB().saveProfile(m_profileId, tv.getText().toString(), m_departures);
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
                            getDB().deleteProfile(m_profileId);
                            finish();
                        }
                    });
                builder.show();
            }
        };


    private OnClickListener m_deleteFromProfileListener = new OnClickListener() {
	    public void onClick(View v) {
                Vector<Integer> newDepartures = new Vector<Integer>();
                for (int i = 0; i < m_departures.size(); i++) {
                    if (!m_checkMap[i]) {
                        newDepartures.addElement(m_departures.elementAt(i));
                    }                    
                }
                m_departures = newDepartures;
                m_checkMap = new boolean[m_departures.size()];
                setListAdapter(new ProfileInfoAdapter(getDB().getProfileInfo(m_departures)));
	    }
	};


    @Override public void onActivityResult(int request, int result, Intent data) {
        if (result == RESULT_OK) {
            if (request == s_locationPickerId) {
                double lat = data.getDoubleExtra("com.github.tommywalsh.mbta.Lat", 0.0);
                double lng = data.getDoubleExtra("com.github.tommywalsh.mbta.Lng", 0.0);
                Vector<Integer> newDeparturePoints = ProximityProfileGenerator.getProximityProfile(this, lat, lng, 0.25);
                m_departures.addAll(newDeparturePoints);
                m_checkMap = new boolean[m_departures.size()];
                setListAdapter(new ProfileInfoAdapter(getDB().getProfileInfo(m_departures)));
            }
        }
    }





    private class ProfileInfoAdapter extends BaseAdapter 
    {
        public class Info 
        {
            String stop;
            String subroute;
            String route;
        }

        private Vector<Info> m_profileInfo = new Vector<Info>();
        public ProfileInfoAdapter(Database.ProfileInfoCursorWrapper cursor) {
            // We could just keep a handle to the cursor, and only extract data when we need to.
            // BUT!  We know we're going to need to access it all anyhow, and converting to a 
            // vector here allows us to add and remove stuff during the editing session quickly,
            // and without worrying about overlapping cursors, and pending DB transactions, etc.
            cursor.moveToFirst();
            m_departures.clear();
            while (!(cursor.isAfterLast())) {
                Info i = new Info();
                i.stop = cursor.getStopTitle();
                i.route = cursor.getRouteTitle();
                i.subroute = cursor.getSubrouteTitle();
                m_profileInfo.addElement(i);
                m_departures.addElement(cursor.getDepartureId());
                cursor.moveToNext();
            }
            cursor.close();
            m_checkMap = new boolean[m_departures.size()];
        }

        public int getCount() {
            return m_profileInfo.size();
        }
        
        public Object getItem(int position) {
            return m_profileInfo.elementAt(position);
        }

        public long getItemId(int position) {
            return position;
        }
        
        public View getView(int position, View convertView, ViewGroup viewGroup) {
            if (convertView == null) {
                LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.profile_entry, null);
            }

            Info thisInfo = m_profileInfo.elementAt(position);
            
            TextView routeWidget = (TextView) convertView.findViewById(R.id.route_title);
            routeWidget.setText(thisInfo.route);

            TextView subrouteWidget = (TextView) convertView.findViewById(R.id.subroute_title);
            subrouteWidget.setText(thisInfo.subroute);

            TextView stopWidget = (TextView) convertView.findViewById(R.id.stop_title);
            stopWidget.setText(thisInfo.stop);
                
            CheckBox check = (CheckBox) convertView.findViewById(R.id.check);
            check.setTag(new Integer(position));
            check.setChecked(false);
            check.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        Integer i = (Integer) buttonView.getTag();
                        m_checkMap[i] = isChecked;
                    }
                });

            return convertView;
        }
    }
}

