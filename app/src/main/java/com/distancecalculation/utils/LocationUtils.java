package com.distancecalculation.utils;

import android.annotation.SuppressLint;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;

public class LocationUtils {

    private static final int DEFAULT_ZOOM = 15;

    @SuppressLint("MissingPermission")
    public static void updateCurrentLocation(GoogleMap googleMap, double latitude, double longitude, boolean isLocationEnabled) {
        googleMap.setMyLocationEnabled(isLocationEnabled);
        googleMap.getUiSettings().setMyLocationButtonEnabled(isLocationEnabled);
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude, longitude), DEFAULT_ZOOM));
    }

}
