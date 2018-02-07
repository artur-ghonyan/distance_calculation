package com.distancecalculation;

import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.maps.android.SphericalUtil;
import com.google.maps.android.ui.IconGenerator;
import com.distancecalculation.utils.LocationUtils;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnMapClickListener {

    private static final String TAG = "DISTANCE_CALCULATION";
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private static final double YEREVAN_LATITUDE = 40.177200;
    private static final double YEREVAN_LONGITUDE = 44.503490;

    private FusedLocationProviderClient mFusedLocationClient;
    private LatLng mFirstLatLng;
    private GoogleMap mMap;

    private boolean mLocationPermissionGranted;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        updateUI();
    }

    private void getLocationPermission() {
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;

            updateUI();
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        mLocationPermissionGranted = false;
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mLocationPermissionGranted = true;

                    updateUI();
                }
            }
        }
    }


    @Override
    public void onMapClick(LatLng latLng) {
        if (mFirstLatLng == null) {
            mFirstLatLng = latLng;

            mMap.addMarker(new MarkerOptions().position(mFirstLatLng));
        } else {
            Polyline polyline = mMap.addPolyline(new PolylineOptions().add(mFirstLatLng, latLng));
            polyline.setColor(Color.RED);
            mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));

            // Count the distance between 2 points
            final double distance = SphericalUtil.computeDistanceBetween(mFirstLatLng, latLng);

            IconGenerator iconGenerator = new IconGenerator(this);
            MarkerOptions markerOptions = new MarkerOptions()
                    .icon(BitmapDescriptorFactory.fromBitmap(iconGenerator.makeIcon(String.format("%s m", (int) distance))))
                    .position(latLng)
                    .anchor(iconGenerator.getAnchorU(), iconGenerator.getAnchorV());
            mMap.addMarker(markerOptions);

            mFirstLatLng = null;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_clean:
                if (mMap != null) {
                    mMap.clear();
                    mFirstLatLng = null;
                }

                break;
            default:
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private void updateUI() {
        if (mMap == null) {
            return;
        }

        mMap.setOnMapClickListener(this);

        try {
            if (mLocationPermissionGranted) {
                Task<Location> locationTask = mFusedLocationClient.getLastLocation();
                locationTask.addOnCompleteListener(this, new OnCompleteListener<Location>() {

                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        if (task.isSuccessful() && task.getResult() != null) {
                            final Location location = task.getResult();
                            LocationUtils.updateCurrentLocation(mMap, location.getLatitude(),
                                    location.getLongitude(), true);
                        } else {
                            LocationUtils.updateCurrentLocation(mMap, YEREVAN_LATITUDE, YEREVAN_LONGITUDE, false);
                        }
                    }
                });
            } else {
                getLocationPermission();
            }
        } catch (SecurityException e) {
            Log.e(TAG, "SecurityException: %s", e.getCause());
        }
    }
}
