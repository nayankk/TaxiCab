package com.xc0ffee.taxicab.utils;

public class TaxiCabUtils {

    public final static boolean isValidEmail(CharSequence target) {
        if (target == null)
            return false;
        return android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
    }
}
