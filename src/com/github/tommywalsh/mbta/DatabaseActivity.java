package com.github.tommywalsh.mbta;

// Copyright 2012 Tom Walsh
//
// This program is free software released under version 3
// of the GPL.  See file gpl.txt for more information.


import android.app.Activity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.view.View;
import android.view.View.OnClickListener;
import android.content.Intent;


public class DatabaseActivity extends Activity
{
    // Pass one of these behavior styles in the intent when you 
    // start this activity.
    public static final int WELCOME_BEHAVIOR = 1;
    public static final int REBUILD_BEHAVIOR = 2;

    private int m_behavior;
    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent i = getIntent();
        m_behavior = i.getIntExtra(getString(R.string.db_screen_style_in_intent),
                                   REBUILD_BEHAVIOR);

        setContentView(R.layout.welcome_screen);
        
        Button buildButton = (Button)findViewById(R.id.build_button);
        buildButton.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    DatabaseMonitor.addCompletionListener(m_listener);
                    DatabaseBuilder b = new DatabaseBuilder(DatabaseActivity.this);
                    b.spawnRebuildTask();
                }});

        Button cancelButton = (Button)findViewById(R.id.cancel_build_button);
        cancelButton.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    android.util.Log.d("mbta", "CANCEL REQUEST");
                    DatabaseActivity.this.finish();
                }});


        // By default, this activity acts as a welcome screen, but it can
        // also act as a "scare" screen before rebuilding an existing database
        // If the latter, we need to jigger the UI a bit
        if (m_behavior == REBUILD_BEHAVIOR) {
            TextView tv = (TextView)findViewById(R.id.welcome_text);
            tv.setVisibility(View.INVISIBLE);
            
            tv = (TextView)findViewById(R.id.welcome_title);
            tv.setText(R.string.download_banner);

            cancelButton.setText(R.string.cancel);
        }
    }



    private DatabaseMonitor.CompletionListener m_listener = 
        new DatabaseMonitor.CompletionListener() {
            public void onCompleted() {
                android.util.Log.d("mbta", "Notified of completion");
                if (m_behavior == WELCOME_BEHAVIOR) {
                    // When we're the welcome screen, we need to spawn the 
                    // main screen now, else the app will just exit.
                    // (otherwise, the main screen will already be running)
                    android.util.Log.d("mbta", "Spawning Straphanger because DB is done");
                    startActivity(new Intent(DatabaseActivity.this,
                                             Straphanger.class));
                }
                android.util.Log.d("mbta", "Finishing dbactivity");
                finish();
            }
        };
}
