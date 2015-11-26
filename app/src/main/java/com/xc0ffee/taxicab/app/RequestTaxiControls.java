package com.xc0ffee.taxicab.app;

import android.app.Fragment;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.xc0ffee.taxicab.R;
import com.xc0ffee.taxicab.utils.TaxiCabUtils;

import java.io.IOException;
import java.util.List;

public class RequestTaxiControls extends Fragment  {

    private static final String TAG = "RequestTaxiControls";

    private TextView mLocationTextView;

    private TextView mTraveTime;

    private LinearLayout mRequestTaxiLayout;

    private View mRequestTaxi;

    private MapsActivity mActivity;

    private static RequestTaxiControls mRequestTaxiControlsRef = null;

    private String mDriverId;

    private LatLng mCameraPosition;

    public static RequestTaxiControls getMe() {
        if (mRequestTaxiControlsRef == null)
            mRequestTaxiControlsRef = new RequestTaxiControls();
        return mRequestTaxiControlsRef;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivity = (MapsActivity) getActivity();
    }

    @Override
    public void onResume() {
        super.onResume();

        getView().setFocusableInTouchMode(true);
        getView().requestFocus();
        getView().setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {

                if (event.getAction() == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_BACK) {
                    onBackPressed();
                    return true;
                }
                return false;
            }
        });
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        if (container == null)
            return null;

        View v = inflater.inflate(R.layout.request_taxi_controls, container, false);

        v.findViewById(R.id.locationMarkertext).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Log.d(TAG, "Pick me from here clicked");
                        showRequestTaxi();
                    }
                }
        );

        mLocationTextView = (TextView) v.findViewById(R.id.goto_pin);

        mRequestTaxiLayout = (LinearLayout) v.findViewById(R.id.show_confirm_taxi);

        mRequestTaxi = v.findViewById(R.id.request_taxi);

        mTraveTime = (TextView) v.findViewById(R.id.travel_time);

        mRequestTaxi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "Request Taxi Clicked");
                if (TaxiCabUtils.isTestStubEnabled()) {
                    new UpdateDriverPositionStub(getActivity(), mCameraPosition).start();
                }
                mActivity.setUserLocation(mCameraPosition);
                mActivity.setSelectedDriverId(mDriverId);
                mActivity.showEnrouteFragment();
            }
        });

        return v;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    private void showRequestTaxi() {
        mRequestTaxiLayout.animate().alpha(1.0f);
        CameraUpdate yourLocation = CameraUpdateFactory.newLatLngZoom(mCameraPosition, 17);
        mActivity.moveCamera(yourLocation);
    }

    public void onCameraChange(CameraPosition cameraPosition) {

        mCameraPosition = cameraPosition.target;

        new UpdateLocationInfo(cameraPosition.target).execute();

        new ComputeTravelTime(getActivity(), cameraPosition.target,
                new ComputeTravelTime.SelectedDriverDetails() {
                    @Override
                    public void onDriverSelected(String driveId, final int travelTime) {
                        mDriverId = driveId;
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mTraveTime.setText(Integer.toString(travelTime / 60));
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
            Geocoder geocoder = new Geocoder(getActivity());
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

    private void onBackPressed() {
        if (mRequestTaxiLayout.getAlpha() != 0) {
            mRequestTaxiLayout.animate().alpha(0f);
            CameraUpdate yourLocation = CameraUpdateFactory.newLatLngZoom(mCameraPosition, 15);
            mActivity.moveCamera(yourLocation);
        } else {
            mActivity.onBackPressed();
        }
    }
}
