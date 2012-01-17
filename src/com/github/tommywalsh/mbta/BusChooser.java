package com.github.tommywalsh.mbta;

// Copyright 2012 Tom Walsh
//
// This program is free software released under version 3
// of the GPL.  See file gpl.txt for more information.


import java.util.Vector;
import java.io.Serializable;
import android.widget.TextView;
import android.widget.CheckBox;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.app.ListActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.os.Bundle;
import android.content.Intent;

public class BusChooser extends ListActivity
{

    private Vector<ProfileEditHelper.Entry> m_entries = new Vector<ProfileEditHelper.Entry>();
    private Vector<Boolean> m_checked = new Vector<Boolean>();

    // When this activity is first created...
    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String instructions = prepInternals(getIntent());
        prepGUI(instructions);
    }


    private void prepGUI(String instructions)
    {
        // ... set up the look and feel...
        setContentView(R.layout.profile_picker);        

        // ... set up our array adapter...
        setListAdapter(new ItemDataAdapter());

        // ... give instructions...
        TextView header = (TextView) findViewById(R.id.picker_header);
        header.setText(instructions);

        // ... hook up our buttons ...
        Button okButton = (Button) findViewById(R.id.picker_ok);
        okButton.setOnClickListener(new OnClickListener() {
                @Override public void onClick(View v) {
                    reportResults();
                }});

        Button cancelButton = (Button) findViewById(R.id.picker_cancel);
        cancelButton.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    finish();
                }});
    }

    private String prepInternals(Intent intent)
    {
        int key = intent.getIntExtra(getString(R.string.profile_entries_in_intent), 
                                     Scratchpad.NO_KEY);
        if (key != Scratchpad.NO_KEY) {
            Object o = Scratchpad.popObject(key);
            m_entries = (Vector<ProfileEditHelper.Entry>)o;
            // All checkboxes are initially unchecked until someone tells them not to be
            for (int i=0; i<m_entries.size(); i++) {
                m_checked.add(false);
            }
        }

        return intent.getStringExtra(getString(R.string.instructions_in_intent));
    }



    private void reportResults()
    {
        // Collect checked items
        Vector<Integer> ids = new Vector<Integer>();
        for(int i=0; i<m_checked.size(); i++) {
            if (m_checked.elementAt(i)) {
                ids.add(m_entries.elementAt(i).stopId);
            }
        }

        // And package them up for whoever called us
        int key = Scratchpad.putObject(ids);
        Intent i = new Intent();
        i.putExtra(getString(R.string.departures_in_intent), key);
        setResult(RESULT_OK, i);
        finish();
    }


    private class ItemDataAdapter extends VectorAdapter<ProfileEditHelper.Entry>
    {
        public ItemDataAdapter() {
            super(getApplicationContext(), R.layout.picker_entry);
        }

        public Vector<ProfileEditHelper.Entry> getVector() {
            return m_entries;
        }

        @Override public View processView(int p, ProfileEditHelper.Entry e, View view) {

            Integer position = p;

            TextView busText = (TextView) view.findViewById(R.id.bus_text);
            busText.setText(e.route + " - " + e.subroute);

            TextView stopText = (TextView) view.findViewById(R.id.stop_text);
            stopText.setText(e.stop);

            CheckBox cb = (CheckBox) view.findViewById(R.id.bus_check);
            cb.setTag(position);
            cb.setChecked(m_checked.elementAt(position));
            cb.setOnCheckedChangeListener(new OnCheckedChangeListener() {
                    public void onCheckedChanged(CompoundButton button, boolean isChecked) {
                        m_checked.set((Integer)button.getTag(), isChecked);
                        notifyDataSetChanged();
                    }});
            
            return view;
        }
    }
}

