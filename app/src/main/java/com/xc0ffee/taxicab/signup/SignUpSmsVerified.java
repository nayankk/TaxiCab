package com.xc0ffee.taxicab.signup;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.xc0ffee.taxicab.R;
import com.xc0ffee.taxicab.app.TaxiCabMainActivity;

public class SignUpSmsVerified extends Fragment implements SignUpActivity.OnBackPressedListener {

    private SignUpActivity mActivity;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mActivity = ((SignUpActivity) getActivity());

        mActivity.setOnBackPressedListener(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.signup_sms_verified, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getActivity().findViewById(R.id.id_cnt).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doExit();
            }
        });
    }

    @Override
    public void doBack() {
        doExit();
    }

    private void doExit() {
        Intent data = new Intent();
        data.putExtra(TaxiCabMainActivity.KEY_USERNAME, mActivity.getUsername());
        data.putExtra(TaxiCabMainActivity.KEY_PASSWORD, mActivity.getPassword());
        getActivity().setResult(Activity.RESULT_OK);
        getActivity().finish();
    }
}
