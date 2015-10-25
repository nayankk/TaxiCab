package com.xc0ffee.taxicab.app;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Fragment;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.xc0ffee.taxicab.R;
import com.xc0ffee.taxicab.utils.TaxiCabUtils;

public class MapsFragment extends Fragment implements DriverPositionManager.DriverPositionListener{

    private GoogleMap mMap = null;

    private static MapsFragment mMapsFragment = null;
    private DriverPositionManager mDriversPosition = null;
    private static String TAG = "MapsFragment";

    public static MapsFragment getInstance() {
        if (mMapsFragment == null) {
            mMapsFragment = new MapsFragment();
        }
        return mMapsFragment;
    }

    public static void clear() {
        if (mMapsFragment != null)
            mMapsFragment = null;
    }

    public MapsFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDriversPosition = new DriverPositionManager(getActivity());
        mDriversPosition.registerDriverPositionListener(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        if (container == null)
            return null;

        View v = inflater.inflate(R.layout.maps_fragment, container, false);
        return v;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupMapIfNeeded();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mDriversPosition.deregisterDriverPositionListner();
    }

    @TargetApi(23)
    private void setupMapIfNeeded() {
        if (mMap == null) {
            mMap = ((MapFragment) getChildFragmentManager().findFragmentById(R.id.location_map)).getMap();
            if (mMap != null) {
                if (TaxiCabUtils.isM()) {
                    if (getActivity().checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {
                        setUpMap();
                    } else {
                        getActivity().requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                TaxiCabMainActivity.LOCATION_PERMISSION_REQUEST_ID);
                    }
                } else
                    setUpMap();
            }
        }
    }

    private void setUpMap() {
        mMap.setMyLocationEnabled(true);
        LocationManager locationManager =
                (LocationManager) getActivity().getSystemService(Activity.LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        String provider = locationManager.getBestProvider(criteria, true);
        try {
            Location location = locationManager.getLastKnownLocation(provider);
            if (location != null) {
                double latitude = location.getLatitude();
                double longitude = location.getLongitude();
                LatLng latLng = new LatLng(latitude, longitude);
                CameraUpdate yourLocation = CameraUpdateFactory.newLatLngZoom(latLng, 14);
                mMap.moveCamera(yourLocation);
            }
        } catch (SecurityException e) {
            Log.d(TAG, "Unable to access localation permission");
        }
    }

    @Override
    public void onDriverPositionChanged(DriverPositionManager.Driver driver) {
        if (mMap == null)
            return;
        if (driver.getAvailable().equalsIgnoreCase("1")) {
            LatLng position = new LatLng(Double.valueOf(driver.getLongitude()),
                    Double.valueOf(driver.getLatitude()));
            mMap.addMarker(new MarkerOptions().position(position).title(driver.getDriverid()));
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == TaxiCabMainActivity.LOCATION_PERMISSION_REQUEST_ID) {
            setUpMap();
        }
    }
}
