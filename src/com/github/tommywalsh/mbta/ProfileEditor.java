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
import android.widget.TextView;
import android.view.View;
import android.view.ViewGroup;
import android.view.LayoutInflater;

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
        setContentView(R.layout.editor);
        
        Intent i = getIntent();
        // We know that -1 won't be in the database as an index.  Use it here as code for 
        // "new profile"
        int profileId = i.getIntExtra(getString(R.string.profile_in_intent), -1);


        Database db = new Database(this);
        setListAdapter(new ProfileCursorAdapter(db.getProfileInfo(profileId)));
    }



    
    @Override protected void onResume() {
        super.onResume();
    }
    
    @Override protected void onPause() {
        super.onPause();
    }




    private class ProfileCursorAdapter extends BaseAdapter 
    {
        public class Info 
        {
            String stop;
            String subroute;
            String route;
        }

        private Database.ProfileInfoCursorWrapper m_cursor;
        public ProfileCursorAdapter(Database.ProfileInfoCursorWrapper cursor) {
            m_cursor = cursor;
        }

        public int getCount() {
            return m_cursor.getCount();
        }
        
        public Object getItem(int position) {
            Info i = new Info();
            m_cursor.moveToPosition(position);
            i.stop = m_cursor.getStopTitle();
            i.route = m_cursor.getRouteTitle();
            i.subroute = m_cursor.getSubrouteTitle();
            return i;
        }
        public long getItemId(int position) {
            return position;
        }
        
        public View getView(int position, View convertView, ViewGroup viewGroup) {
            if (convertView == null) {
                LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.profile_entry, null);
            }

            m_cursor.moveToPosition(position);
            
            TextView routeWidget = (TextView) convertView.findViewById(R.id.route_title);
            routeWidget.setText(m_cursor.getRouteTitle());

            TextView subrouteWidget = (TextView) convertView.findViewById(R.id.subroute_title);
            subrouteWidget.setText(m_cursor.getSubrouteTitle());

            TextView stopWidget = (TextView) convertView.findViewById(R.id.stop_title);
            stopWidget.setText(m_cursor.getStopTitle());
                
            return convertView;
        }
    }
}

