package com.xc0ffee.taxicab.app;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Fragment;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.xc0ffee.taxicab.R;
import com.xc0ffee.taxicab.utils.TaxiCabUtils;

public class MapsFragment extends Fragment implements
        DriverPositionManager.DriverPositionListener,
        GooglePlayServicesManager.LocationUpdateListener,
        GoogleMap.OnCameraChangeListener {

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
        v.findViewById(R.id.locationMarkertext).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Log.d(TAG, "Pick me from here clicked");
                    }
                }
        );
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
        GooglePlayServicesManager.getMe(getActivity()).onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        GooglePlayServicesManager.getMe(getActivity()).onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mDriversPosition.deregisterDriverPositionListner();
    }

    @TargetApi(23)
    private void setupMapIfNeeded() {
        if (mMap == null) {
            mMap = ((MapFragment) getChildFragmentManager().
                    findFragmentById(R.id.location_map)).getMap();
            if (mMap != null) {
                if (TaxiCabUtils.isM()) {
                    if (getActivity().checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {
                        setUpMap();
                    } else {
                        getActivity().requestPermissions(
                                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                TaxiCabMainActivity.LOCATION_PERMISSION_REQUEST_ID);
                    }
                } else
                    setUpMap();
            }
        }
    }

    private void setUpMap() {
        mMap.setMyLocationEnabled(true);
        GooglePlayServicesManager.getMe(getActivity()).addLocationUpdateListner(this);
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
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == TaxiCabMainActivity.LOCATION_PERMISSION_REQUEST_ID) {
            setUpMap();
        }
    }

    @Override
    public void onLocationFound(Location location) {
        adjustCameraPosition(location);
    }

    @Override
    public void onLocationUpdated(Location location) {
        adjustCameraPosition(location);
    }

    private void adjustCameraPosition(Location location) {
        if (location != null) {
            double latitude = location.getLatitude();
            double longitude = location.getLongitude();
            LatLng latLng = new LatLng(latitude, longitude);
            CameraUpdate yourLocation = CameraUpdateFactory.newLatLngZoom(latLng, 15);
            mMap.moveCamera(yourLocation);
            mMap.setOnCameraChangeListener(this);
        }
    }

    @Override
    public void onCameraChange(CameraPosition cameraPosition) {
        new ComputeTravelTime(getActivity(), cameraPosition.target,
                new ComputeTravelTime.SelectedDriverDetails() {
                    @Override
                    public void onDriverSelected(String driveId,
                                                 final int travelTime) {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                ((TextView) getView().findViewById(R.id.travel_time))
                                        .setText(Integer.toString(travelTime / 60));
                            }
                        });
                    }
                });
    }
}
