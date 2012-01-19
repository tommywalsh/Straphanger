package com.github.tommywalsh.mbta;

// Copyright 2012 Tom Walsh
//
// This program is free software released under version 3
// of the GPL.  See file gpl.txt for more information.


import android.app.Activity;
import android.os.Bundle;
import android.widget.Button;
import android.view.View;
import android.view.View.OnClickListener;


public class DatabaseActivity extends Activity
{
    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.welcome_screen);


        Button buildButton = (Button)findViewById(R.id.build_button);
        buildButton.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    DatabaseBuilder b = new DatabaseBuilder(DatabaseActivity.this);
                    b.spawnRebuildTask();
                }});
    }

    @Override protected void onResume() {
        super.onResume();
        if (DatabaseMonitor.isComplete()) {
            finish();
        }
    }

}
