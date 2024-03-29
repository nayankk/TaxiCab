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

    private OnBackPressedListener mBackPressedListener;

    private String mUsername;
    private String mPassword;
    private String mName;
    private String mPhoneNumber;

    public interface OnBackPressedListener {
        void doBack();
    }

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
        mBackPressedListener.doBack();
    }

    public int getVerificationCode() {
        return mVerifCode;
    }

    public void setVerificationCode(int code) {
        mVerifCode = code;
    }

    public void setOnBackPressedListener(OnBackPressedListener listener) {
        mBackPressedListener = listener;
    }

    public String getUsername() {
        return mUsername;
    }

    public void setUsername(String username) {
        mUsername = username;
    }

    public String getPassword() {
        return mPassword;
    }

    public void setPassword(String password) {
        mPassword = password;
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        mName = name;
    }

    public String getPhoneNumber() {
        return mPhoneNumber;
    }

    public void setPhoneNumber(String phNumber) {
        mPhoneNumber = phNumber;
    }
}
