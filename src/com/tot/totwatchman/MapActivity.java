package com.tot.totwatchman;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnCameraChangeListener;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

public class MapActivity extends FragmentActivity implements OnMapReadyCallback {

	private double la;
	private double lo;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_map);
		
		la = getIntent().getDoubleExtra("la", -1);
		lo = getIntent().getDoubleExtra("lo", -1);
		
		SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapFragment);
		mapFragment.getMapAsync(MapActivity.this);
	}

	@Override
    public void onMapReady(GoogleMap map) {
        LatLng lalo = new LatLng(la, lo);
        map.addMarker(new MarkerOptions().position(lalo).title("เช็คอินที่นี่"));
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(lalo, (float) 18));
    }
}
