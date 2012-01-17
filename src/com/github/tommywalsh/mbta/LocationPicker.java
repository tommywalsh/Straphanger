package com.github.tommywalsh.mbta;

// Copyright 2011-12 Tom Walsh
//
// This program is free software released under version 3
// of the GPL.  See file gpl.txt for more information.

import com.google.android.maps.MapActivity;
import com.google.android.maps.MapView;
import com.google.android.maps.MapController;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.Overlay;
import com.google.android.maps.MyLocationOverlay;

import android.location.LocationManager;
import android.location.LocationListener;
import android.location.Location;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.MotionEvent;
import android.view.GestureDetector;
import android.widget.Button;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.Paint;

// This class presents a map and allows the user to select a location
//
// This class takes no input from the caller.
//
// If the user does pick a location, we "return" a RESULT_OK code with
// the latitude and longitude stored in the returned intent.
//
// The user selects a location by double-tap.
public class LocationPicker extends MapActivity
{

    // There is only one user operation: selecting a location
    // Here we just need to package up that location into the
    // intent, and "return" it to the caller
    private void onLocationSelected(double lat, double lng) {
        Intent i = new Intent();
        i.putExtra("com.github.tommywalsh.mbta.Lat", lat);
        i.putExtra("com.github.tommywalsh.mbta.Lng", lng);
        setResult(RESULT_OK, i);
        finish();
    }





    // This is the only class that knows about GeoPoints.
    // Here are some utility functions to convert to lat/long pairs,
    // which is what the rest of this app uses
    private static double getLatitude(GeoPoint g) {
        return ( (double)(g.getLatitudeE6()) / 1.0E6);
    }
    private static double getLongitude(GeoPoint g) {
        return ( (double)(g.getLongitudeE6()) / 1.0E6);
    }








    // We'll delegate to the standard Google MapActivity for most of the work,
    // but we need a couple of custom overlays.  One will show the user's current
    // location, and the other will process the double-tap
    private void initOverlays(MapView mapview) {

	mapview.getOverlays().add(new LocationSelectionOverlay(this));

        m_locationIndicatorOverlay = new MyLocationOverlay(this, mapview);
	mapview.getOverlays().add(m_locationIndicatorOverlay);


        // This guy will scroll to our current location as soon as we get a fix.
        m_locationIndicatorOverlay.runOnFirstFix(new Runnable() {
                public void run() {
                    m_controller.animateTo(m_locationIndicatorOverlay.getMyLocation());
                }});
    }

    // This overlay shows user's current location on the map
    private MyLocationOverlay m_locationIndicatorOverlay = null;

    // This overlay class reacts to double-taps
    public class LocationSelectionOverlay extends Overlay {

        // On the first tap (and every subsequent tap), we use the MapView's "onTap" API
        // to learn the position.  We could figure this out ourselves in onDoubleTap, but 
        // it would involve lots of calculation.  This is easier.
        private GeoPoint m_tapLoc = null;
        @Override public boolean onTap(GeoPoint where, MapView mv) {
            m_tapLoc = where;
            return false;
        }
            

        // At double-tap time, we rely on the fact that the "onTap" code above
        // has already saved our position.  Here we just need to unpackage the
        // data, and send it on to the onLocationSelected code
        public class DoubleTapListener extends GestureDetector.SimpleOnGestureListener {
            public boolean onDoubleTap(MotionEvent e) {
		GeoPoint tapLocation = m_tapLoc;
		
		// Unfortunately, if you double-tap on the position indicator, the 
		// press event is "stolen" from us, and so m_tapLoc is not valid
		// Workaround that here by assuming an invalid m_tapLoc
		// means a tap on the currentposition indicator
		if (tapLocation == null) {
		    tapLocation = m_locationIndicatorOverlay.getMyLocation();
		}

                onLocationSelected(getLatitude(tapLocation),
                                   getLongitude(tapLocation));

                return true;
            }
        }


        // This plumbing is necessary to enable gesture detection on a mapview overlay
        private GestureDetector m_gd;
        public LocationSelectionOverlay(Context cx) {
            m_gd = new GestureDetector(cx, new DoubleTapListener());
        }
        @Override public boolean onTouchEvent(MotionEvent e, MapView mv) {
            return m_gd.onTouchEvent(e);
        }

    }




    private MapController m_controller;

    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.map);
	
	
        MapView mapview = (MapView) findViewById(R.id.mapView);
	m_controller = mapview.getController();
	mapview.setBuiltInZoomControls(true);
        initOverlays(mapview);
    }


    @Override protected void onResume() {
        super.onResume();
        m_locationIndicatorOverlay.enableMyLocation();
    }
    
    @Override protected void onPause() {
        super.onPause();
        // Save battery by not caring about location when we're not active.
        m_locationIndicatorOverlay.disableMyLocation();
    }


    // MapActivity requires this override.  We never display routes.
    @Override protected boolean isRouteDisplayed() { 
        return false;
    }
}

