package com.xc0ffee.taxicab.app;

import android.app.Activity;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.google.android.gms.maps.model.LatLng;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;

public class ComputeTravelTime {

    private final Activity mActivity;
    private final LatLng mDest;

    private Firebase mFirebaseRef;

    private int mMinTime = Integer.MAX_VALUE;
    private String mSelectDriver;
    private int mAvailableDrivers = 0;
    private int mCounter = 0;
    private final SelectedDriverDetails mListener;

    public interface SelectedDriverDetails {
        void onDriverSelected(String driveId, int travelTime);
    }

    public ComputeTravelTime(final Activity activity, LatLng dest, SelectedDriverDetails listener) {
        mActivity = activity;
        mDest = dest;
        mListener = listener;

        Firebase.setAndroidContext(activity);
        mFirebaseRef = new Firebase("https://taxi-cab.firebaseio.com/");
        mFirebaseRef.child("drivers").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                computeNearestTime(dataSnapshot);
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });
    }

    private void computeNearestTime(DataSnapshot dataSnapshot) {
        for (DataSnapshot snapshot: dataSnapshot.getChildren()) {
            DriverPositionManager.Driver driver = snapshot.getValue(
                    DriverPositionManager.Driver.class);
            if (driver.getAvailable().equals("1")) {
                String longitude = driver.getLongitude();
                String latitude = driver.getLatitude();
                compute(longitude, latitude, driver.getDriverid());
                mAvailableDrivers += 1;
            }
        }
    }

    public void compute(final String longitude, final String latitude, final String driverId) {
        String url = compuetUrl(longitude, latitude);
        AsyncHttpClient client = new AsyncHttpClient();
        client.get(mActivity, url, new JsonHttpResponseHandler() {

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    JSONArray routes = response.getJSONArray("routes");
                    JSONArray legs = routes.getJSONObject(0).getJSONArray("legs");
                    int secs = legs.getJSONObject(0).getJSONObject("duration").getInt("value");
                    if (secs < mMinTime) {
                        mMinTime = secs;
                        mSelectDriver = driverId;
                    }
                    mCounter += 1;
                    if (mCounter >= mAvailableDrivers)
                        mListener.onDriverSelected(mSelectDriver, mMinTime);

                } catch (JSONException e) {
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers,
                                  Throwable throwable, JSONArray errorResponse) {
                super.onFailure(statusCode, headers, throwable, errorResponse);
            }
        });
    }

    private String compuetUrl(String longitude, String latitude) {
        String url = "https://maps.googleapis.com/maps/api/directions/json?origin=" +
                mDest.latitude + "," + mDest.longitude + "&" +
                "destination=" + longitude + "," + latitude;
        return url;
    }
}