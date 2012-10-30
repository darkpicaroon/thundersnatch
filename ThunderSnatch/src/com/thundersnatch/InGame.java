package com.thundersnatch;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;

import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

public class InGame extends MapActivity{

	MapView mapV;
	GeoPoint geoP;
	MapController mapC;
	
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    
	    requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
        		WindowManager.LayoutParams.FLAG_FULLSCREEN);
	    
	    setContentView(R.layout.activity_map);
	    
	    mapV = (MapView) findViewById(R.id.mapView);
	    mapV.displayZoomControls(true);
	    mapV.setBuiltInZoomControls(true);
	}	
	
	protected boolean isRouteDisplayed() {
		return false;
	}
	
}
