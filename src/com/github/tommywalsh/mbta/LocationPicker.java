// Copyright 2011 Tom Walsh
//
// This program is free software released under version 3
// of the GPL.  See file gpl.txt for more information.

package com.github.tommywalsh.mbta;

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
import android.view.MotionEvent;
import android.view.GestureDetector;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.Paint;


// This activity allows the user to pick a location on a map.
public class LocationPicker extends MapActivity
{

    // This is the only class that knows about GeoPoints.
    // Here are some utility functions to convert to/from lat/long pairs
    private static GeoPoint locationToGeoPoint(Location loc) {
	return new GeoPoint((int)(loc.getLatitude() * 1E6),
			    (int)(loc.getLongitude() * 1E6));
    }

    private static double getLatitude(GeoPoint g) {
        return ( (double)(g.getLatitudeE6()) / 1.0E6);
    }
    private static double getLongitude(GeoPoint g) {
        return ( (double)(g.getLongitudeE6()) / 1.0E6);
    }





    // This overlay sits on top of the map, and waits for the user to double-tap the desired location
    public class LocationSelectionOverlay extends Overlay {

        // On the first tap (and every subsequent tap), we use the MapView's "onTap" API
        // to learn the position.  We could figure this out ourselves in onDoubleTap, but 
        // it would involve lots of calculation.  This is easier
        private GeoPoint m_tapLoc = null;
        @Override public boolean onTap(GeoPoint where, MapView mv) {
            m_tapLoc = where;
            return false;
        }
            

        // At double-tap time, we rely on the fact that the "onTap" code above has already saved
        // our position.  Here we just need to send this position back to our caller, and finish the activity
        public class DoubleTapListener extends GestureDetector.SimpleOnGestureListener {
            public boolean onDoubleTap(MotionEvent e) {
                Intent i = new Intent();
                i.putExtra("com.github.tommywalsh.mbta.Lat", getLatitude(m_tapLoc));
                i.putExtra("com.github.tommywalsh.mbta.Lng", getLongitude(m_tapLoc));
                setResult(RESULT_OK, i);
                finish();
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


    // This overlay shows user's current location on the map
    private MyLocationOverlay m_locationIndicatorOverlay = null;

    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.map);
	
	
	MapView mapview = (MapView) findViewById(R.id.mapView);
	mapview.setBuiltInZoomControls(true);
	mapview.getOverlays().add(new LocationSelectionOverlay(this));

        m_locationIndicatorOverlay = new MyLocationOverlay(this, mapview);
	mapview.getOverlays().add(m_locationIndicatorOverlay);

	MapController controller = mapview.getController();

	controller.setZoom(15);
	controller.setCenter(new GeoPoint(42355500,-71060500)); // Downtown Crossing
    }

    @Override protected void onResume() {
        super.onResume();
        m_locationIndicatorOverlay.enableMyLocation();
    }
    
    @Override protected void onPause() {
        super.onPause();
        m_locationIndicatorOverlay.disableMyLocation();
    }    

    @Override protected boolean isRouteDisplayed() { 
        return false;
    }
}
