package com.xc0ffee.taxicab.signup;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.twilio.client.Twilio;

import java.util.HashMap;

public class TwilioManager implements Twilio.InitListener {

    public static final String TWILIO_PHONE_NUMBER = "6503895669";

    private static final String TAG = "TwilioManager";
    private static final String TWILIO_SERVR_URL = "https://limitless-brook-2269.herokuapp.com/sms";

    public TwilioManager(Context context) {
        Twilio.initialize(context, this);
    }

    @Override
    public void onInitialized() {
        Log.d(TAG, "Twilio SDK is ready");
    }

    @Override
    public void onError(Exception e) {
        Log.e(TAG, "Couldn't initialize twilio SDK :" + e.getMessage());
    }

    public boolean sendSms(String phoneNumber, int randomNumber, SendSmsResult resultCallback) {
        if (Twilio.isInitialized()) {
            new SendSms(resultCallback).execute(phoneNumber, Integer.toString(randomNumber));
            return true;
        } else {
            Log.d(TAG, "Twilio is not initialized. Try again");
            return false;
        }
    }

    public interface SendSmsResult {
        void onSmsSendSuccess();

        void onSmsSendFailed();
    }

    private class SendSms extends AsyncTask<String, Void, Boolean> {

        private SendSmsResult mSendSmsCallback;

        SendSms(SendSmsResult callback) {
            mSendSmsCallback = callback;
        }

        @Override
        protected Boolean doInBackground(String... params) {
            try {
                HashMap<String, String> map = new HashMap<>();
                map.put("phnumber", params[0]);
                map.put("random", params[1]);
                HTTPHelper.performPostCall(TWILIO_SERVR_URL, map);
            } catch (Exception e) {
                Log.d(TAG, e.getMessage());
                return false;
            }
            return true;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            if (result)
                mSendSmsCallback.onSmsSendSuccess();
            else
                mSendSmsCallback.onSmsSendFailed();
        }
    }

    public void shutdown() {
        Twilio.shutdown();
    }

}
