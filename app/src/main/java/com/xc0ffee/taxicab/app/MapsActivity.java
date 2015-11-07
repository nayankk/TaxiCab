package com.xc0ffee.taxicab.app;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

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

import java.io.IOException;
import java.util.List;

public class MapsActivity extends AppCompatActivity implements
        DriverPositionManager.DriverPositionListener,
        GooglePlayServicesManager.LocationUpdateListener,
        GoogleMap.OnCameraChangeListener {

    private static final String STRING_PROFILE = "Your profile";
    private static final String STRING_LOGOUT = "Logout";

    private static final int LOCATION_PERMISSION_REQUEST_ID = 1 << 2;

    private GoogleMap mMap = null;

    private DriverPositionManager mDriversPosition = null;
    private static String TAG = "MapsFragment";

    private TextView mLocationTextView;

    private LinearLayout mRequestTaxiLayout;

    private LatLng mCameraPosition;

    private View mRequestTaxi;

    private String mDriverId;

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

        findViewById(R.id.locationMarkertext).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Log.d(TAG, "Pick me from here clicked");
                        showRequestTaxi();
                    }
                }
        );

        mLocationTextView = (TextView) findViewById(R.id.goto_pin);

        mRequestTaxiLayout = (LinearLayout) findViewById(R.id.show_confirm_taxi);

        mRequestTaxi = findViewById(R.id.request_taxi);

        mRequestTaxi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "Request Taxi Clicked");
                if (TaxiCabUtils.isTestStubEnabled()) {
                    new UpdateDriverPositionStub(MapsActivity.this, mCameraPosition).start();
                }
                setUserLocation(mCameraPosition);
                setSelectedDriverId(mDriverId);
                showEnrouteFragment();
            }
        });

        GooglePlayServicesManager.getMe(this);

        setupMapIfNeeded();
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

    @Override
    public void onBackPressed() {
        if (mRequestTaxiLayout.getAlpha() == 0f)
            super.onBackPressed();
        else {
            mRequestTaxiLayout.animate().alpha(0f);
            CameraUpdate yourLocation = CameraUpdateFactory.newLatLngZoom(mCameraPosition, 15);
            mMap.moveCamera(yourLocation);
        }
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

    private void showRequestTaxi() {
        mRequestTaxiLayout.animate().alpha(1.0f);
        CameraUpdate yourLocation = CameraUpdateFactory.newLatLngZoom(mCameraPosition, 17);
        mMap.moveCamera(yourLocation);
    }

    @TargetApi(23)
    private void setupMapIfNeeded() {
        if (mMap == null) {
            MapFragment fragment = ((MapFragment) getFragmentManager().findFragmentById(R.id.location_map));
            mMap = fragment.getMap();
            if (mMap != null) {
                if (TaxiCabUtils.isM()) {
                    if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {
                        setUpMap();
                    } else {
                        requestPermissions(
                                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                LOCATION_PERMISSION_REQUEST_ID);
                    }
                } else
                    setUpMap();
            }
        }
    }

    private void setUpMap() {
        mMap.setMyLocationEnabled(true);
        GooglePlayServicesManager.getMe(this).addLocationUpdateListner(this);
        mMap.setOnCameraChangeListener(this);
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
    public void onRequestPermissionsResult(
            int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == MapsActivity.LOCATION_PERMISSION_REQUEST_ID) {
            setUpMap();
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
        if (location != null) {
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

        new UpdateLocationInfo(cameraPosition.target).execute();

        new ComputeTravelTime(this, cameraPosition.target,
                new ComputeTravelTime.SelectedDriverDetails() {
                    @Override
                    public void onDriverSelected(String driveId, final int travelTime) {
                        mDriverId = driveId;
                        MapsActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                ((TextView)MapsActivity.this.findViewById(R.id.travel_time))
                                        .setText(Integer.toString(travelTime / 60));
                            }
                        });
                    }
                });
    }

    private class UpdateLocationInfo extends AsyncTask<Void, Void, Address> {

        private final LatLng mLocation;

        public UpdateLocationInfo(LatLng location) {
            mLocation = location;
        }

        @Override
        protected Address doInBackground(Void... voids) {
            Geocoder geocoder = new Geocoder(MapsActivity.this);
            try {
                List<Address> addresses = geocoder.getFromLocation(mLocation.latitude, mLocation.longitude, 1);
                if (addresses.size() > 0)
                    return addresses.get(0);
                return null;
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Address address) {
            super.onPostExecute(address);
            if (address != null) {
                String addressLine = "";
                if (address.getSubThoroughfare() != null)
                    addressLine += address.getSubThoroughfare();
                if (address.getThoroughfare() != null && !address.getThoroughfare().equals("null")) {
                    if (!addressLine.equals(""))
                        addressLine += " ";
                    addressLine += address.getThoroughfare();
                }
                if (address.getLocality() != null && !address.getLocality().equals("null")) {
                    if (!addressLine.equals(""))
                        addressLine += ", ";
                    addressLine += address.getLocality();
                }
                mLocationTextView.setText(addressLine);
            }
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

    private void showEnrouteFragment() {
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.replace(R.id.maps_fragment, TaxiEnrouteFragment.getInstance());
        transaction.commitAllowingStateLoss();
    }
}
