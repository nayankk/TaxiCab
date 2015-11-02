package com.xc0ffee.taxicab.app;

import android.content.Intent;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Toast;

import com.firebase.client.AuthData;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.xc0ffee.taxicab.R;
import com.xc0ffee.taxicab.signup.DriverSignUpActivity;
import com.xc0ffee.taxicab.signup.SignUpActivity;
import com.xc0ffee.taxicab.utils.TaxiCabUtils;

public class SignInActivity extends AppCompatActivity {

    private static final String TAG = "SignInActivity";

    private EditText mEmailText;
    private EditText mPassword;
    private Firebase mFirebaseRef;
    private ProgressDialog mProgressDialog;
    private boolean isDriver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.signin);

        //Checks if the app user has checked "user" or "driver"
        RadioButton rider = (RadioButton) findViewById(R.id.rider);
        RadioButton driver = (RadioButton) findViewById(R.id.driver);
        if(radioClick(rider)){
            Toast.makeText(SignInActivity.this, "Rider selected", Toast.LENGTH_LONG);
            isDriver = false;
        }
        if(radioClick(driver)){
            Toast.makeText(SignInActivity.this, "Driver selected", Toast.LENGTH_LONG);
            isDriver = true;
        }

        findViewById(R.id.btn_signup).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!isDriver){
                    Intent intent = new Intent(SignInActivity.this, SignUpActivity.class);
                    intent.putExtra(SignUpActivity.KEY_EMAIL, mEmailText.getText().toString());
                    intent.putExtra(SignUpActivity.KEY_PASSWORD, mPassword.getText().toString());
                    startActivityForResult(intent, 0);
                }
                else{
                    Intent intent = new Intent(SignInActivity.this, DriverSignUpActivity.class);
                    intent.putExtra(SignUpActivity.KEY_EMAIL, mEmailText.getText().toString());
                    intent.putExtra(SignUpActivity.KEY_PASSWORD, mPassword.getText().toString());
                    startActivityForResult(intent, 0);
                }
            }
        });

        findViewById(R.id.btn_signin).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "Sign in clicked");
                onSignInBtnClicked();
            }
        });

        mEmailText = (EditText) findViewById(R.id.user_email);
        mPassword = (EditText) findViewById(R.id.user_passwd);

        Firebase.setAndroidContext(this);
        mFirebaseRef = new Firebase(TaxiCabMainActivity.FIREBASE_URL);




    }

    private void onSignInBtnClicked() {
        final String email = String.valueOf(mEmailText.getText());

        if (!TaxiCabUtils.isValidEmail(email)) {
            Toast.makeText(this, R.string.email_invalid, Toast.LENGTH_LONG).show();
            return;
        }

        // Create a handler to handle the result of the authentication
        Firebase.AuthResultHandler authResultHandler = new Firebase.AuthResultHandler() {
            @Override
            public void onAuthenticated(AuthData authData) {
                // Authenticated successfully with payload authData
                Log.d(TAG, "User authenticated");
                hideProgressDialog();

                Intent data = new Intent();
                data.putExtra(TaxiCabMainActivity.KEY_USERNAME, mEmailText.getText().toString());
                data.putExtra(TaxiCabMainActivity.KEY_PASSWORD, mPassword.getText().toString());
                setResult(RESULT_OK, data);

                finish();
            }
            @Override
            public void onAuthenticationError(FirebaseError firebaseError) {
                // Authenticated failed with error firebaseError
                Toast.makeText(SignInActivity.this, R.string.login_failure, Toast.LENGTH_LONG).show();
                hideProgressDialog();
            }
        };

        mFirebaseRef.authWithPassword(mEmailText.getText().toString(),
                mPassword.getText().toString(), authResultHandler);
        mProgressDialog = new ProgressDialog();
        mProgressDialog.show(getFragmentManager(), "dialog");
    }

    private void hideProgressDialog() {
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
            mProgressDialog = null;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent result) {
        super.onActivityResult(requestCode, resultCode, result);
        if (resultCode == RESULT_OK) {
            Toast.makeText(this, "User authenticated", Toast.LENGTH_LONG).show();
            setResult(RESULT_OK, result);
            finish();
        } else {
            Toast.makeText(this, "Error while signing up. Please try again", Toast.LENGTH_LONG).show();
        }
    }

    private boolean radioClick(View v){
        RadioButton radio = (RadioButton)v;
        if(radio.isChecked()){
            return true;
        }
        else{
            return false;
        }
    }

}
