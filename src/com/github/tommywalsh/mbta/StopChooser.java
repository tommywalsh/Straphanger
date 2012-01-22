package com.github.tommywalsh.mbta;

// Copyright 2012 Tom Walsh
//
// This program is free software released under version 3
// of the GPL.  See file gpl.txt for more information.

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.view.View;
import android.view.View.OnClickListener;

public class StopChooser extends Activity
{
    @Override public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.stop_picker);

        Intent i = getIntent();
        m_subroute = i.getStringExtra(getString(R.string.subroute_in_intent));
        m_stopChoice = i.getIntExtra(getString(R.string.stop_choice_in_intent),
                                     -1);

        initGUI();
    }


    private String m_subroute;
    private int m_stopChoice;

    private void initGUI()
    {
        RadioGroup group = (RadioGroup)findViewById(R.id.stop_group);        
        Button buttonToSelect = null;
        
        Database db = new Database(this);
        Database.SubrouteStopCursorWrapper cursor = db.getStopsForSubroute(m_subroute);
        cursor.moveToFirst();
        while(!(cursor.isAfterLast())) {
            RadioButton button = new RadioButton(this);

            int stopId = cursor.getStopId();
            button.setTag(stopId);
            button.setText(cursor.getStopName());

            if (stopId == m_stopChoice) {
                buttonToSelect = button;
            }

            android.util.Log.d("mbta", "Adding " + 
                               Integer.toString(stopId));
            
            group.addView(button);

            cursor.moveToNext();
        } 
        cursor.close();
        db.close();
            

        if (buttonToSelect != null) {
            group.check(buttonToSelect.getId());
        }
            

        android.util.Log.d("mbta", "User is editing route " + 
                           m_subroute +
                           " with stop " +
                           Integer.toString(m_stopChoice));


        Button okButton = (Button)findViewById(R.id.ok);
        okButton.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    Intent i = new Intent();
                    i.putExtra(getString(R.string.stop_choice_in_intent),7);
                    setResult(RESULT_OK, i);
                    finish();
                }});

        Button cancelButton = (Button)findViewById(R.id.cancel);
        cancelButton.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    finish();
                }});

    }
}
