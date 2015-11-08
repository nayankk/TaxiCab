package com.xc0ffee.taxicab.app;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.firebase.client.AuthData;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.xc0ffee.taxicab.R;
import com.xc0ffee.taxicab.utils.ObscuredSharedPreferences;
import com.xc0ffee.taxicab.utils.TaxiCabUtils;

import java.security.NoSuchAlgorithmException;

public class TaxiCabMainActivity extends AppCompatActivity {

    final static private String PREF_KEY = "unknown";
    final static private String DEFAULT_KEY = "UNKNOWN_KEY";

    final static public String FIREBASE_URL = "https://taxi-cab.firebaseio.com/";
    final static public String KEY_USERNAME = "key_user_name";
    final static public String KEY_PASSWORD = "key_password";

    private static final String TAG = "TaxiCabMainActivity";

    private static final int LOCATION_PERMISSION_REQUEST_ID = 1 << 2;

    private GreetingsDialog mGreetingsDialog;

    private String mUsername;

    private String mPassword;

    private Firebase mFirebaseRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_taxi_cab_main);

        mGreetingsDialog = new GreetingsDialog(this);
        mGreetingsDialog.show();

        Firebase.setAndroidContext(this);
        mFirebaseRef = new Firebase(FIREBASE_URL);

        new CheckUserCredentials().execute();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        hideGreetings();
    }

    private void hideGreetings() {
        if (mGreetingsDialog != null) {
            mGreetingsDialog.cancel();
            mGreetingsDialog = null;
        }
    }

    private class CheckUserCredentials extends AsyncTask<Void, Void, Boolean> {
        @Override
        protected Boolean doInBackground(Void... params) {

            // First get the key
            final SharedPreferences prefs = TaxiCabMainActivity.this.getSharedPreferences(
                    TaxiCabUtils.PREF_FILE_NAME, Context.MODE_PRIVATE);
            String key = prefs.getString(PREF_KEY, null);
            if (key == null) {
                try {
                    key = ObscuredSharedPreferences.generateKey();
                    prefs.edit().putString(PREF_KEY, key).apply();
                } catch (NoSuchAlgorithmException e) {
                    key = DEFAULT_KEY;
                    prefs.edit().putString(PREF_KEY, key).apply();
                }
            }

            try {
                ObscuredSharedPreferences obscuredPrefs = new ObscuredSharedPreferences(TaxiCabMainActivity.this,
                        TaxiCabMainActivity.this.getSharedPreferences(TaxiCabUtils.PREF_FILE_NAME, Context.MODE_PRIVATE));
                obscuredPrefs.setKey(key);
                if (obscuredPrefs.getString(TaxiCabUtils.PREF_USERNAME, null) == null ||
                        obscuredPrefs.getString(TaxiCabUtils.PREF_PASSWORD, null) == null) {
                    return Boolean.FALSE;
                }

                mUsername = obscuredPrefs.getString(TaxiCabUtils.PREF_USERNAME, null);
                mPassword = obscuredPrefs.getString(TaxiCabUtils.PREF_PASSWORD, null);
                if (mUsername == null || mPassword == null)
                    return Boolean.FALSE;
                return Boolean.TRUE;
            } catch (Exception e) {
                return Boolean.FALSE;
            }
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);

            if (result == Boolean.FALSE) {
                // Launch sign-in activity
                Log.d(TAG, "No saved session, launch login activity");
                hideGreetings();
                TaxiCabUtils.launchSignInActivity(TaxiCabMainActivity.this);
            } else {
                mFirebaseRef.authWithPassword(mUsername, mPassword, new Firebase.AuthResultHandler() {
                    @Override
                    public void onAuthenticated(AuthData authData) {
                        // Authenticated successfully with payload authData
                        Log.d(TAG, "User authenticated");
                        onUserAuthenticated();
                    }
                    @Override
                    public void onAuthenticationError(FirebaseError firebaseError) {
                        Log.d(TAG, "Authentication error");
                        TaxiCabUtils.launchSignInActivity(TaxiCabMainActivity.this);
                    }
                });
            }
        }
    }

    private void onUserAuthenticated() {
        hideGreetings();
        if (TaxiCabUtils.isM()) {
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                showMapsActivity();
            } else {
                requestPermissions(
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_ID);
            }
        } else
            showMapsActivity();
    }

    public void showMapsActivity() {
        Intent intent = new Intent(this, MapsActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_CANCELED)
            finish();
        else if (resultCode == RESULT_OK) {
            secureStoreCredentials(data);
        }
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_ID) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
                showMapsActivity();
            else {
                Toast.makeText(this, "No permission", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    private void secureStoreCredentials(final Intent data) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                final SharedPreferences prefs = TaxiCabMainActivity.this.getSharedPreferences(
                        TaxiCabUtils.PREF_FILE_NAME, Context.MODE_PRIVATE);
                String key = prefs.getString(PREF_KEY, null);
                if (key == null) {
                    try {
                        key = ObscuredSharedPreferences.generateKey();
                        prefs.edit().putString(PREF_KEY, key).apply();
                    } catch (NoSuchAlgorithmException e) {
                        key = DEFAULT_KEY;
                        prefs.edit().putString(PREF_KEY, key).apply();
                    }
                }

                ObscuredSharedPreferences obscuredPrefs = new ObscuredSharedPreferences(TaxiCabMainActivity.this,
                        TaxiCabMainActivity.this.getSharedPreferences(TaxiCabUtils.PREF_FILE_NAME, Context.MODE_PRIVATE));
                obscuredPrefs.setKey(key);

                obscuredPrefs.edit().putString(TaxiCabUtils.PREF_USERNAME, data.getStringExtra(KEY_USERNAME)).apply();
                obscuredPrefs.edit().putString(TaxiCabUtils.PREF_PASSWORD, data.getStringExtra(KEY_PASSWORD)).apply();
            }
        }).run();
    }

}
