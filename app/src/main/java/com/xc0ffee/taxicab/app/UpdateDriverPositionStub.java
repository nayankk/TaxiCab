package com.xc0ffee.taxicab.app;

import android.content.Context;

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

public class UpdateDriverPositionStub extends Thread {

    private final Context mContext;
    private final LatLng mCurrentLatLng;
    private final Firebase mFirebaseRef;

    UpdateDriverPositionStub(Context context, LatLng currentPos) {
        mContext = context;
        mCurrentLatLng = currentPos;

        Firebase.setAndroidContext(context);
        mFirebaseRef = new Firebase("https://taxi-cab.firebaseio.com/");
    }

    @Override
    public void run() {
        super.run();

        new ComputeTravelTime(mContext, mCurrentLatLng,
                new ComputeTravelTime.SelectedDriverDetails() {
            @Override
            public void onDriverSelected(String driveId, int travelTime) {
                lockDriver(driveId, travelTime);
            }
        });
    }

    private void lockDriver(final String driverId, int travelTime) {
        mFirebaseRef.child("drivers").child(driverId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Driver driver = dataSnapshot.getValue(Driver.class);
                        Double latitude = Double.parseDouble(driver.getLongitude());
                        Double longitude = Double.parseDouble(driver.getLatitude());
                        LatLng drivePos = new LatLng(latitude, longitude);
                        move(driverId, drivePos, mCurrentLatLng);
                    }

                    @Override
                    public void onCancelled(FirebaseError firebaseError) {

            }
        });
    }

    private void move(final String driverId, LatLng orig, LatLng dest) {
        String url = "https://maps.googleapis.com/maps/api/directions/json?origin=" +
                orig.latitude + "," + orig.longitude + "&" +
                "destination=" + dest.latitude + "," + dest.longitude;
        AsyncHttpClient client = new AsyncHttpClient();
        client.get(mContext, url, new JsonHttpResponseHandler() {

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                moveStep(driverId, response);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers,
                                  Throwable throwable, JSONArray errorResponse) {
                super.onFailure(statusCode, headers, throwable, errorResponse);
            }
        });
    }

    private void moveStep(final String driverId, final JSONObject response) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Firebase driverRef = mFirebaseRef.child("drivers").child(driverId);

                    JSONArray routes = response.getJSONArray("routes");
                    JSONArray legs = routes.getJSONObject(0).getJSONArray("legs");
                    JSONArray steps = legs.getJSONObject(0).getJSONArray("steps");
                    int totalSteps = steps.length();
                    for (int i = 0; i < totalSteps; i++) {
                        Double lat = Double.parseDouble(
                                steps.getJSONObject(i).getJSONObject("end_location").getString("lat"));
                        Double lng = Double.parseDouble(
                                steps.getJSONObject(i).getJSONObject("end_location").getString("lng"));
                        driverRef.child("latitude").setValue(lng);
                        driverRef.child("longitude").setValue(lat);
                        Thread.sleep(10000);
                    }

                } catch (JSONException e) {
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
}
