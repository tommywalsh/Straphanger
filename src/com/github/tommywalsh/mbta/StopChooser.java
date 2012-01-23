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
import android.widget.TextView;
import android.widget.ScrollView;
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
        String route = i.getStringExtra(getString(R.string.route_in_intent));
        String srTitle = i.getStringExtra(getString(R.string.srtitle_in_intent));

        initGUI(route, srTitle);
    }


    private String m_subroute;
    private int m_stopChoice;
    private RadioGroup m_group;

    private void initGUI(String route, String srTitle)
    {
        m_group = (RadioGroup)findViewById(R.id.stop_group);        
        Button buttonToSelect = null;

        TextView routeTV = (TextView)findViewById(R.id.route_title);
        routeTV.setText(route);

        TextView subrouteTV = (TextView)findViewById(R.id.subroute_title);
        subrouteTV.setText(srTitle);


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

            m_group.addView(button);

            cursor.moveToNext();
        } 
        cursor.close();
        db.close();
            

        if (buttonToSelect != null) {
            m_group.check(buttonToSelect.getId());
            int top = buttonToSelect.getTop();
            ScrollView sv = (ScrollView)findViewById(R.id.stop_scrollview);
            sv.scrollTo(0, top);
        }
            
        Button okButton = (Button)findViewById(R.id.ok);
        okButton.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    View button = m_group.findViewById(m_group.getCheckedRadioButtonId());
                    Intent i = new Intent();
                    i.putExtra(getString(R.string.stop_choice_in_intent),(Integer)button.getTag());
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
