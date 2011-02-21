// Copyright 2011 Tom Walsh
//
// This program is free software released under version 3
// of the GPL.  See file gpl.txt for more information.

package com.github.tommywalsh.mbta;

import android.app.ListActivity;
import android.os.Bundle;
import android.content.Intent;
import android.content.Context;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;
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

    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.editor);
        
        Intent i = getIntent();
        // We know that -1 won't be in the database as an index.  Use it here as code for 
        // "new profile"
        int profileId = i.getIntExtra(getString(R.string.profile_in_intent), -1);


        Button addToProfileButton = (Button)findViewById(R.id.add_to_profile);
        addToProfileButton.setOnClickListener(m_addToProfileListener);

        Database db = new Database(this);
        Database.DeparturePointCursorWrapper cursor = db.getDeparturePointsInProfile(profileId);
        cursor.moveToFirst();
        Vector<Integer> ids = new Vector<Integer>();
        while(!(cursor.isAfterLast())) {
            ids.addElement(cursor.getDeparturePointId());
            cursor.moveToNext();
        }
    
        setListAdapter(new ProfileInfoAdapter(db.getProfileInfo(ids)));
    }



    
    @Override protected void onResume() {
        super.onResume();
    }
    
    @Override protected void onPause() {
        super.onPause();
    }



    private static final int s_locationPickerId = 2050;    
    private OnClickListener m_addToProfileListener = new OnClickListener() {
	    public void onClick(View v) {
                // Launch the location picker.  We'll infer busses when it returns
                startActivityForResult(new Intent(ProfileEditor.this, LocationPicker.class), s_locationPickerId);
	    }
	};

    @Override public void onActivityResult(int request, int result, Intent data) {
        if (result == RESULT_OK) {
            if (request == s_locationPickerId) {
                double lat = data.getDoubleExtra("com.github.tommywalsh.mbta.Lat", 0.0);
                double lng = data.getDoubleExtra("com.github.tommywalsh.mbta.Lng", 0.0);
                Vector<Integer> departurePoints = ProximityProfileGenerator.getProximityProfile(this, lat, lng, 0.5);
            }
        }
    }





    private class ProfileInfoAdapter extends BaseAdapter 
    {
        public class Info 
        {
            Integer id;
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
            while (!(cursor.isAfterLast())) {
                Info i = new Info();
                i.stop = cursor.getStopTitle();
                i.route = cursor.getRouteTitle();
                i.subroute = cursor.getSubrouteTitle();
                i.id = cursor.getDepartureId();
                m_profileInfo.addElement(i);
                cursor.moveToNext();
            }

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
                
            return convertView;
        }
    }
}

