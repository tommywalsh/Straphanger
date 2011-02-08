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

import android.location.LocationManager;
import android.location.LocationListener;
import android.location.Location;
import android.view.View;
import android.view.MotionEvent;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.Paint;


// This activity handles the Map application.
public class LocationPicker extends MapActivity implements LocationListener
{

    Location m_currentLocation;

    public void onLocationChanged(Location loc) {
	m_currentLocation = loc;
	// enable button here, maybe
    }
    public void onProviderDisabled(String provider) {
    }
    public void onProviderEnabled(String provider) {
    }
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }


    private static GeoPoint locationToGeoPoint(Location loc) {
	return new GeoPoint((int)(loc.getLatitude() * 1E6),
			    (int)(loc.getLongitude() * 1E6));
    }


    // Draw a little circle at our current position
    public class LocationIndicator extends Overlay {
	
	public boolean draw(Canvas canvas, MapView mapView, boolean shadow, long when) {
	    super.draw(canvas, mapView, shadow, when);
	    
	    if (m_currentLocation != null) {
		Point screenCoords = new Point();

		Paint paint = new Paint();		
		mapView.getProjection().toPixels(locationToGeoPoint(m_currentLocation), 
						 screenCoords);
		paint.setStrokeWidth(3);
		if (shadow) {
		    paint.setARGB(255,0,0,255);
		    paint.setStyle(Paint.Style.FILL_AND_STROKE);
		} else {		    
		    paint.setARGB(255,0,255,0);
		    paint.setStyle(Paint.Style.STROKE);
		}
		canvas.drawCircle(screenCoords.x, screenCoords.y, 10, paint);

	    }
	    return true;
	}
    }


    View.OnTouchListener m_clickListener = new View.OnTouchListener() {
	    public boolean onTouch(View view, MotionEvent me) {
		float x = me.getX();
		float y = me.getY();
		
		MapView mv = (MapView)view;
		GeoPoint where = mv.getProjection().fromPixels((int)x, (int)y);

		
		Intent i = new Intent();
		i.putExtra("com.github.tommywalsh.mbta.Lat", where.getLatitudeE6());
		i.putExtra("com.github.tommywalsh.mbta.Lng", where.getLongitudeE6());
		setResult(RESULT_OK, i);

		finish();
		
		return true;
	    }
	};


    
    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.map);
	
	m_locManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
	
	MapView mapview = (MapView) findViewById(R.id.mapView);
	mapview.setBuiltInZoomControls(true);
	mapview.getOverlays().add(new LocationIndicator());
	mapview.setOnTouchListener(m_clickListener);

	m_controller = mapview.getController();

	m_controller.setZoom(16); // 16 seems best for biking speeds
	m_controller.setCenter(new GeoPoint(42378778, -71095667)); // Union Square

    }

    @Override protected boolean isLocationDisplayed() {
	return true;
    }

    @Override protected boolean isRouteDisplayed() {
	return false;
    }

    @Override protected void onStart() {
	super.onStart();
	m_locManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 50, this);
    }

    @Override protected void onStop() {
	// Conserve battery by not asking for location updates when we're not visible
	super.onStop();
	m_locManager.removeUpdates(this);
    }

    MapController m_controller;
    LocationManager m_locManager;

}

