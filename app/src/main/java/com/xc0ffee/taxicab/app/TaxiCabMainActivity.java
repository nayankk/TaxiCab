package com.xc0ffee.taxicab.app;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.firebase.client.AuthData;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.xc0ffee.taxicab.R;
import com.xc0ffee.taxicab.utils.ObscuredSharedPreferences;

import java.security.NoSuchAlgorithmException;

public class TaxiCabMainActivity extends AppCompatActivity {

    final static private String PREF_FILE_NAME = "secret.xml";
    final static private String PREF_USERNAME = "username";
    final static private String PREF_PASSWORD = "password";
    final static private String PREF_KEY = "unknown";
    final static private String DEFAULT_KEY = "UNKNOWN_KEY";

    final static public String FIREBASE_URL = "https://taxi-cab.firebaseio.com/";
    final static public String KEY_USERNAME = "key_user_name";
    final static public String KEY_PASSWORD = "key_password";

    private static final String TAG = "TaxiCabMainActivity";

    private static final String STRING_PROFILE = "Your profile";
    private static final String STRING_LOGOUT = "Logout";

    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private String[] mDrawerListItems = {STRING_PROFILE, STRING_LOGOUT};

    private GreetingsDialog mGreetingsDialog;
    private String mUsername;
    private String mPassword;

    private Firebase mFirebaseRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_taxi_cab_main);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.left_drawer);
        mDrawerList.setAdapter(new ArrayAdapter<>(this, R.layout.drawer_list_item, mDrawerListItems));
        mDrawerList.setOnItemClickListener(new DrawerItemClickListener());

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
            final SharedPreferences prefs = TaxiCabMainActivity.this.getSharedPreferences(PREF_FILE_NAME, Context.MODE_PRIVATE);
            String key = prefs.getString(PREF_KEY, null);
            if (key == null) {
                try {
                    key = ObscuredSharedPreferences.generateKey();
                    prefs.edit().putString(PREF_KEY, key).commit();
                } catch (NoSuchAlgorithmException e) {
                    key = DEFAULT_KEY;
                    prefs.edit().putString(PREF_KEY, key).commit();
                }
            }

            try {
                ObscuredSharedPreferences obscuredPrefs = new ObscuredSharedPreferences(TaxiCabMainActivity.this,
                        TaxiCabMainActivity.this.getSharedPreferences(PREF_FILE_NAME, Context.MODE_PRIVATE));
                obscuredPrefs.setKey(key);
                if (obscuredPrefs.getString(PREF_USERNAME, null) == null ||
                        obscuredPrefs.getString(PREF_PASSWORD, null) == null) {
                    return Boolean.FALSE;
                }

                mUsername = obscuredPrefs.getString(PREF_USERNAME, null);
                mPassword = obscuredPrefs.getString(PREF_PASSWORD, null);
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
                launchSignInActivity();
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
                    }
                });
            }
        }
    }

    private void onUserAuthenticated() {
        hideGreetings();
    }

    private void launchSignInActivity() {
        Intent intent = new Intent(TaxiCabMainActivity.this, SignInActivity.class);
        startActivityForResult(intent, 0);
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

    private void secureStoreCredentials(final Intent data) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                final SharedPreferences prefs = TaxiCabMainActivity.this.getSharedPreferences(PREF_FILE_NAME, Context.MODE_PRIVATE);
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
                        TaxiCabMainActivity.this.getSharedPreferences(PREF_FILE_NAME, Context.MODE_PRIVATE));
                obscuredPrefs.setKey(key);

                obscuredPrefs.edit().putString(PREF_USERNAME, data.getStringExtra(KEY_USERNAME)).commit();
                obscuredPrefs.edit().putString(PREF_PASSWORD, data.getStringExtra(KEY_PASSWORD)).commit();
            }
        }).run();
    }

    private class DrawerItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            selectItem(position);
        }
    }

    private void selectItem(int position) {
        switch (mDrawerListItems[position]) {
            case STRING_PROFILE:
                Log.d("NAYAN", "Your profile clicked");
                break;
            case STRING_LOGOUT:
                discardCredentials();
                launchSignInActivity();
                break;
        }
        mDrawerLayout.closeDrawers();
    }

    private void discardCredentials() {
        try {
            ObscuredSharedPreferences obscuredPrefs = new ObscuredSharedPreferences(TaxiCabMainActivity.this,
                    TaxiCabMainActivity.this.getSharedPreferences(PREF_FILE_NAME, Context.MODE_PRIVATE));
            obscuredPrefs.edit().putString(PREF_USERNAME, null).apply();
            obscuredPrefs.edit().putString(PREF_PASSWORD, null).apply();
        } catch (Exception e) {}
    }
}
