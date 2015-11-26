package com.xc0ffee.taxicab.app;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.xc0ffee.taxicab.R;
import com.xc0ffee.taxicab.utils.ObscuredSharedPreferences;
import com.xc0ffee.taxicab.utils.TaxiCabUtils;

import java.util.List;

public class MapsActivity extends AppCompatActivity implements
        DriverPositionManager.DriverPositionListener,
        GooglePlayServicesManager.LocationUpdateListener,
        GoogleMap.OnCameraChangeListener {

    private static final String STRING_PROFILE = "Your profile";
    private static final String STRING_LOGOUT = "Logout";

    private GoogleMap mMap = null;

    private DriverPositionManager mDriversPosition = null;
    private static String TAG = "MapsFragment";

    private LatLng mCameraPosition;

    private DrawerLayout mDrawerLayout;

    private ListView mDrawerList;

    private String[] mDrawerListItems = {STRING_PROFILE, STRING_LOGOUT};

    private String mDriverSelected;

    private LatLng mUserLocation;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.maps_activity);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.left_drawer);
        mDrawerList.setAdapter(new ArrayAdapter<>(this, R.layout.drawer_list_item, mDrawerListItems));
        mDrawerList.setOnItemClickListener(new DrawerItemClickListener());

        mDriversPosition = new DriverPositionManager(this);
        mDriversPosition.registerDriverPositionListener(this);

        setupMapIfNeeded();

        showRequestTaxiControls();

        GooglePlayServicesManager.getMe(this);
    }

    private class DrawerItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            selectItem(position);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        GooglePlayServicesManager.getMe(this).onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        GooglePlayServicesManager.getMe(this).onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mDriversPosition.deregisterDriverPositionListner();
    }

    private void selectItem(int position) {
        switch (mDrawerListItems[position]) {
            case STRING_PROFILE:
                Log.d("NAYAN", "Your profile clicked");
                break;
            case STRING_LOGOUT:
                discardCredentials();
                TaxiCabUtils.launchSignInActivity(this);
                break;
        }
        mDrawerLayout.closeDrawers();
    }

    private void discardCredentials() {
        try {
            ObscuredSharedPreferences obscuredPrefs = new ObscuredSharedPreferences(
                    this, getSharedPreferences(TaxiCabUtils.PREF_FILE_NAME, Context.MODE_PRIVATE));
            obscuredPrefs.edit().putString(TaxiCabUtils.PREF_USERNAME, null).apply();
            obscuredPrefs.edit().putString(TaxiCabUtils.PREF_PASSWORD, null).apply();
        } catch (Exception e) {}
    }

    @TargetApi(23)
    private void setupMapIfNeeded() {
        if (mMap == null) {
            MapFragment fragment = ((MapFragment) getFragmentManager().findFragmentById(R.id.location_map));
            mMap = fragment.getMap();
            if (mMap != null) {
                mMap.setMyLocationEnabled(true);
                GooglePlayServicesManager.getMe(this).addLocationUpdateListner(this);
                mMap.setOnCameraChangeListener(this);
            }
        }
    }

    @Override
    public void onDriverPositionChanged(Driver driver) {
        if (mMap == null)
            return;
        if (driver.getAvailable().equalsIgnoreCase("1")) {
            LatLng position = new LatLng(Double.valueOf(driver.getLongitude()),
                    Double.valueOf(driver.getLatitude()));
            mMap.addMarker(new MarkerOptions().position(position).title(driver.getDriverid()));
        }
    }

    @Override
    public void onLocationFound(Location location) {
        adjustCameraPosition(location);
    }

    @Override
    public void onLocationUpdated(Location location) {
    }

    private void adjustCameraPosition(Location location) {
        mCameraPosition = new LatLng(location.getLatitude(), location.getLongitude());
        if (mCameraPosition != null) {
            double latitude = location.getLatitude();
            double longitude = location.getLongitude();
            LatLng latLng = new LatLng(latitude, longitude);
            CameraUpdate yourLocation = CameraUpdateFactory.newLatLngZoom(latLng, 15);
            mMap.moveCamera(yourLocation);
        }
    }

    @Override
    public void onCameraChange(CameraPosition cameraPosition) {

        mCameraPosition = cameraPosition.target;

        if (RequestTaxiControls.getMe().isAdded()) {
            RequestTaxiControls.getMe().onCameraChange(cameraPosition);
        }
    }

    public void setUserLocation(LatLng userLocation) {
        mUserLocation = userLocation;
    }

    public LatLng getUserLocation() {
        return mUserLocation;
    }

    public void setSelectedDriverId(String driverId) {
        mDriverSelected = driverId;
    }

    public String getSelectedDriverId() {
        return mDriverSelected;
    }

    public void showRequestTaxiControls() {
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.replace(R.id.controls_fragment, RequestTaxiControls.getMe());
        transaction.addToBackStack("taxi_controls");
        transaction.commit();
    }

    public void showEnrouteFragment() {
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.replace(R.id.controls_fragment, TaxiEnrouteFragment.getInstance());
        transaction.commitAllowingStateLoss();
    }

    public void moveCamera(CameraUpdate moveCamera) {
        mMap.animateCamera(moveCamera, 100, null);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}
