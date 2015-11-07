package com.xc0ffee.taxicab.app;

import android.app.Activity;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

public class DriverPositionManager {

    private DriverPositionListener mListener = null;
    private Activity mActivity;
    private Firebase mFirebaseRef;

    private ValueEventListener mValueListner = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            for (DataSnapshot snapshot: dataSnapshot.getChildren()) {
                Driver driver = snapshot.getValue(Driver.class);
                if (mListener != null)
                    mListener.onDriverPositionChanged(driver);
            }
        }

        @Override
        public void onCancelled(FirebaseError firebaseError) {

        }
    };

    public interface DriverPositionListener {
        void onDriverPositionChanged(Driver driver);
    }

    public DriverPositionManager(Activity activity) {
        mActivity = activity;
        Firebase.setAndroidContext(activity);
        mFirebaseRef = new Firebase("https://taxi-cab.firebaseio.com/");
    }

    public void registerDriverPositionListener(DriverPositionListener listener) {
        mListener = listener;
        mFirebaseRef.child("drivers").addValueEventListener(mValueListner);
    }

    public void deregisterDriverPositionListner() {
        mListener = null;
        mFirebaseRef.child("drivers").removeEventListener(mValueListner);
    }
}
