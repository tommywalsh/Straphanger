package com.github.tommywalsh.mbta;

// Copyright 2011-12 Tom Walsh
//
// This program is free software released under version 3
// of the GPL.  See file gpl.txt for more information.


import android.app.ListActivity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuInflater;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.content.Intent;
import android.content.Context;
import android.content.DialogInterface;

import java.util.Vector;

public class Straphanger extends ListActivity
{
    private static final int s_locationPickerId = 1050;
    private static final int s_profileDialogId  = 1060;
    private static final int s_editorId = 1070;
    private ProfileProvider m_profProvider;



    //////////////////////////////////////////////////////////////////////////////
    ///// FUNCTIONS FOR LAUNCHING AND REACTING TO "PICK A PROFILE" DIALOGS ///////

    private static final int EDIT_PROFILE = 2;

    // Launches the dialog, for purposes of the given action
    void launchProfileSelectionDialog(int act)
    {
        final int action = act;
        ProfileChooserDialog dlg = new ProfileChooserDialog(
            Straphanger.this,
            new ProfileChooserDialog.Listener() {
                public void onProfileChoose(int profileId) {
                    onProfileSelectionDialogConfirm(action, profileId);
                }
            });

        dlg.show();
    }

    // React to user selecting a profile from the dialog
    void onProfileSelectionDialogConfirm(int action, int id)
    {
        assert(action == EDIT_PROFILE);
        launchEditor(id);
    }
    ///////////////////////////////////////////////////////////////////////////////





    private OnClickListener m_databaseListener = new OnClickListener() {
	    public void onClick(View v) {
		m_dbBuilder.spawnRebuildTask(Straphanger.this);
	    }
	};

    private OnClickListener m_proximityListener = new OnClickListener() {
	    public void onClick(View v) {
                launchLocationPicker();
	    }
	};
    private void launchLocationPicker() {
        startActivityForResult(new Intent(this, LocationPicker.class), s_locationPickerId);
    }





    private OnClickListener m_newProfileListener = new OnClickListener() {
	    public void onClick(View v) {
                android.util.Log.d("mbta", "clicked");
                launchEditor(null);
	    }
	};
    private void launchEditor(Integer profileId) {
        Intent i = new Intent(this, ProfileEditor.class);
        if (profileId != null) {
            i.putExtra(getString(R.string.profile_in_intent), profileId.intValue());
        }
        startActivityForResult(i, s_editorId);
    }



    // When a sub-activity is finished, this is called, provided we registered for 
    // result when we launched the sub-activity 
    @Override public void onActivityResult(int request, int result, Intent data) {
        if (result == RESULT_OK) {
            switch (request) {
            case s_locationPickerId:
                {
                    double lat = data.getDoubleExtra("com.github.tommywalsh.mbta.Lat", 0.0);
                    double lng = data.getDoubleExtra("com.github.tommywalsh.mbta.Lng", 0);
                    Vector<Integer> departurePoints = ProximityProfileGenerator.getProximityProfile(this, lat, lng, 0.5);
                    viewDepartures(departurePoints);
                    break;
                }
            case s_editorId:
                {
                    loadButtons();
                    ((ButtonAdapter)getListAdapter()).notifyDataSetChanged();
                    break;
                }
            }
        }
    }


    private void viewDepartures(Vector<Integer> dp) {
	final int size = dp.size();
	int[] transitArray = new int[size];
	for (int ix = 0; ix < size; ix++) {
	    transitArray[ix] = dp.elementAt(ix);
	    Integer i = new Integer(transitArray[ix]);
	}
	Intent i = new Intent(this, DepartureViewer.class);
	i.putExtra(getString(R.string.departures_in_intent), transitArray);
	startActivity(i);        
    }


    private DatabaseBuilder m_dbBuilder;

    @Override public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true; 
    }
    
    @Override public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId()) {
        case R.id.edit_profile:
            launchProfileSelectionDialog(EDIT_PROFILE);
            return true;
        case R.id.rebuild_database:
            //            rebuildDatabase();
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }
    

    @Override public void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_screen);

	m_dbBuilder = new DatabaseBuilder();
	m_profProvider = new ProfileProvider(this);

        m_buttonInfos.add(new ButtonInfo("Nearby Busses", m_proximityListener));
        m_buttonInfos = addProfileButtonInfo(m_buttonInfos);
        m_buttonInfos.add(new ButtonInfo("New Profile", m_newProfileListener));

        setListAdapter(new ButtonAdapter());
    }

    void loadButtons() {
        m_buttonInfos.clear();
        m_buttonInfos.add(new ButtonInfo("Nearby Busses", m_proximityListener));
        m_buttonInfos = addProfileButtonInfo(m_buttonInfos);
        m_buttonInfos.add(new ButtonInfo("New Profile", m_newProfileListener));
    }

    Button m_loadProfileButton;
    Button m_editProfileButton;



    private class ButtonInfo
    {
        public String text;
        public OnClickListener listener;
        ButtonInfo (String t, OnClickListener l) {
            text = t; listener = l;
        }
    }

    private Vector<ButtonInfo> m_buttonInfos = new Vector<ButtonInfo>();

    private class ButtonListener implements OnClickListener
    {
        public ButtonListener(int id) {
            profileId = id;
        }
        private int profileId;
        public void onClick(View v) {
            viewDepartures(m_profProvider.getDeparturePointsInProfile(profileId));
        }
    }

    private Vector<ButtonInfo> addProfileButtonInfo(Vector<ButtonInfo> buttonInfos)
    {
        Database db = new Database(Straphanger.this);
        Database.ProfileCursorWrapper cursor = db.getProfiles();
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            buttonInfos.add(new ButtonInfo(cursor.getProfileName(),
                                           new ButtonListener(cursor.getProfileId())));
            cursor.moveToNext();
        }
        cursor.close();
        db.close();

        return buttonInfos;
    }

    private class ButtonAdapter extends VectorAdapter<ButtonInfo>
    {
        public ButtonAdapter() {
            super(getApplicationContext(), R.layout.bus_stop_entry);
        }
        public Vector<ButtonInfo> getVector() {
            return m_buttonInfos;
        }
        public View processView(int position, ButtonInfo buttonInfo, View view) {
            Button button = (Button)view.findViewById(R.id.button);
            button.setText(buttonInfo.text);
            button.setOnClickListener(buttonInfo.listener);
            return view;
        }
    }
}
