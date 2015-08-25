package com.xc0ffee.taxicab.signup;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.xc0ffee.taxicab.R;

public class SignUpActivity extends AppCompatActivity {

    private static final String TAG = "SignUpActivity";

    public static final String KEY_NAME = "user-name";
    public static final String KEY_PASSWORD = "user-passwd";
    public static final String KEY_EMAIL = "user-email";
    public static final String KEY_PHNUMBER = "user-phone";
    public static final String KEY_RANDOM_NUMBER = "random-number";
    public static final int INVALID_CODE = Integer.MIN_VALUE;

    private int mVerifCode = INVALID_CODE;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.signup_activity);

        FragmentManager manager = getFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        Fragment verificationFragment = new SignUpGetDetailsFragment();
        transaction.add(R.id.fragment, verificationFragment);
        transaction.addToBackStack("top_level");
        transaction.commit();
    }

    @Override
    public void onBackPressed() {
        FragmentManager fm = getFragmentManager();
        int count = fm.getBackStackEntryCount();
        if (count <= 1) {
            super.onBackPressed();
        } else {
            fm.popBackStackImmediate("top_level", 0);
        }
    }

    public int getVerificationCode() {
        return mVerifCode;
    }

    public void setVerificationCode(int code) {
        mVerifCode = code;
    }
}
