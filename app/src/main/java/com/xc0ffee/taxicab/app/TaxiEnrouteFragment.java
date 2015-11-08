package com.xc0ffee.taxicab.app;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.xc0ffee.taxicab.R;

public class TaxiEnrouteFragment extends Fragment {

    private static TaxiEnrouteFragment mTaxiEnroute;

    private MapsActivity mActivity;
    private Firebase mFirebaseRef;

    private ValueEventListener mValueListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            Driver driver = dataSnapshot.getValue(Driver.class);/*
            if (mMap != null) {
                LatLng position = new LatLng(Double.valueOf(driver.getLongitude()),
                        Double.valueOf(driver.getLatitude()));
                mMap.addMarker(new MarkerOptions().position(position).title(driver.getDriverid()));
            }*/
        }

        @Override
        public void onCancelled(FirebaseError firebaseError) {

        }
    };;

    public static TaxiEnrouteFragment getInstance() {
        if (mTaxiEnroute == null) {
            mTaxiEnroute = new TaxiEnrouteFragment();
        }
        return mTaxiEnroute;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        if (container == null)
            return null;

        View v = inflater.inflate(R.layout.taxi_enroute, container, false);
        return v;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivity = (MapsActivity) getActivity();
    }

    @Override
    public void onResume() {
        super.onResume();

        GooglePlayServicesManager.getMe(mActivity).onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        GooglePlayServicesManager.getMe(mActivity).onPause();
        stopDriverPosUpdate();
    }

    private void startDriverPosUpdate() {
        mFirebaseRef = new Firebase("https://taxi-cab.firebaseio.com/");
        mFirebaseRef.child("drivers").child(mActivity.getSelectedDriverId()).addValueEventListener(mValueListener);

    }

    private void stopDriverPosUpdate() {
        mFirebaseRef.child("drivers").child(mActivity.getSelectedDriverId()).removeEventListener(mValueListener);
    }
}
