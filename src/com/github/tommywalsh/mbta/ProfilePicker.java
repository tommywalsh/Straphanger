package com.github.tommywalsh.mbta;

// Copyright 2012 Tom Walsh
//
// This program is free software released under version 3
// of the GPL.  See file gpl.txt for more information.


import java.util.Vector;
import android.widget.TextView;
import android.widget.CheckBox;
import android.widget.Button;
import android.app.ListActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.os.Bundle;
import android.content.Intent;

public class ProfilePicker extends ListActivity
{

    private class ItemData
    {
        public int id;
        public boolean checked;
    }
    private Vector<ItemData> m_data = new Vector<ItemData> ();


    // When this activity is first created...
    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prepInternals(getIntent());
        prepGUI();
    }


    private void prepGUI()
    {
        // ... set up the look and feel...
        setContentView(R.layout.profile_picker);        

        // ... set up our array adapter...
        setListAdapter(new ItemDataAdapter());

        // ... hook up our buttons ...
        //        Button okButton = (Button) findViewById(R.id.picker_ok);

        Button cancelButton = (Button) findViewById(R.id.picker_cancel);
        cancelButton.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    finish();
                }});
    }

    private void prepInternals(Intent intent)
    {
        int ids[] = intent.getIntArrayExtra(getString(R.string.departures_in_intent));
        String instructions = intent.getStringExtra(getString(R.string.instructions_in_intent));
        
        if (ids != null) {
            for (int i=0; i<ids.length; i++) {
                ItemData d = new ItemData();
                d.id = ids[i];
                d.checked = false;
                m_data.add(d);
            }
        }
    }

    private class ItemDataAdapter extends VectorAdapter<ItemData>
    {
        public ItemDataAdapter() {
            super(getApplicationContext(), R.layout.picker_entry);
        }

        public Vector<ItemData> getVector() {
            return m_data;
        }

        public View processView(ItemData d, View view) {
            TextView busText = (TextView) view.findViewById(R.id.bus_text);
            busText.setText(Integer.toString(d.id));

            CheckBox cb = (CheckBox) view.findViewById(R.id.bus_check);
            cb.setChecked(d.checked);
            
            return view;
        }
    }
}

/*


import android.app.AlertDialog;

import android.content.Context;
import android.content.DialogInterface;

import android.widget.Button;
import android.widget.EditText;
import android.widget.AdapterView.AdapterContextMenuInfo;

import android.view.View.OnClickListener;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuItem;
import android.view.Menu;
*/

