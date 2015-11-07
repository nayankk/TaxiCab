package com.xc0ffee.taxicab.utils;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;

import com.xc0ffee.taxicab.app.SignInActivity;

public class TaxiCabUtils {

    final static public String PREF_FILE_NAME = "secret.xml";

    final static public String PREF_USERNAME = "username";

    final static public String PREF_PASSWORD = "password";

    public final static boolean isValidEmail(CharSequence target) {
        if (target == null)
            return false;
        return android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
    }

    public static boolean isM() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M;
    }

    public static boolean isTestStubEnabled() {
        return true;
    }

    public static void launchSignInActivity(Activity activity) {
        Intent intent = new Intent(activity, SignInActivity.class);
        activity.startActivityForResult(intent, 0);
    }
}
