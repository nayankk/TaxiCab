package com.xc0ffee.taxicab.app;

import android.app.Activity;
import android.location.Location;
import android.os.Bundle;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

public class GooglePlayServicesManager implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    private static final long LOCATION_UPDATE_INTERVAL_MS = 2000;
    private static final long LOCATION_UPDATE_FASTEST_MS = 1000;

    public interface LocationUpdateListener {
        void onLocationFound(Location location);
        void onLocationUpdated(Location location);
    }

    private LocationUpdateListener mListener = null;

    private GoogleApiClient mGoogleApiClient = null;

    private Location mCurrentLoc = null;

    private static GooglePlayServicesManager mGooglePlayServicesManager = null;

    private boolean mRequestingLocationUpdates = false;

    private boolean mConnected = false;

    private LocationRequest mLocationRequest = null;
    public static GooglePlayServicesManager getMe(Activity activity) {
        if (mGooglePlayServicesManager == null)
            mGooglePlayServicesManager = new GooglePlayServicesManager(activity);
        return mGooglePlayServicesManager;
    }

    public GooglePlayServicesManager(Activity activity) {
        mGoogleApiClient = new GoogleApiClient.Builder(activity)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();

        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(LOCATION_UPDATE_INTERVAL_MS);
        mLocationRequest.setFastestInterval(LOCATION_UPDATE_FASTEST_MS);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    public void onResume() {
        requestLocationUpdates();
    }

    public void onPause() {
        if (mConnected)
            stopLocationUpdates();
    }

    public void requestLocationUpdates() {
        mRequestingLocationUpdates = true;
        startLocationUpdates();
    }

    private void startLocationUpdates() {
        if (mGoogleApiClient.isConnected())
            LocationServices.FusedLocationApi.requestLocationUpdates(
                    mGoogleApiClient, mLocationRequest, this);
    }

    private void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(
                mGoogleApiClient, this);
    }

    @Override
    public void onConnected(Bundle bundle) {
        mCurrentLoc = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (mListener != null)
            mListener.onLocationFound(mCurrentLoc);
        if (mRequestingLocationUpdates) {
            startLocationUpdates();
            mRequestingLocationUpdates = false;
        }
        mConnected = true;
    }

    @Override
    public void onConnectionSuspended(int i) {
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
    }

    public void addLocationUpdateListner(LocationUpdateListener listener) {
        mListener = listener;
        if (mCurrentLoc != null)
            mListener.onLocationFound(mCurrentLoc);
    }

    @Override
    public void onLocationChanged(Location location) {
        if (mListener != null)
            mListener.onLocationUpdated(location);
    }
}
