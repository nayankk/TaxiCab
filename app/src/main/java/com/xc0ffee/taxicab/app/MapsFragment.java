package com.xc0ffee.taxicab.app;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.xc0ffee.taxicab.R;

public class MapsFragment extends Fragment {

    private static MapsFragment mMapsFragment = null;

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
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        if (container == null)
            return null;

        View v = inflater.inflate(R.layout.maps_fragment, container, false);
        return v;
    }
}
