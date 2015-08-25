package com.xc0ffee.taxicab.signup;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.telephony.SmsMessage;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.xc0ffee.taxicab.R;

public class SignUpSmsVerificationFragment extends Fragment implements SignUpActivity.OnBackPressedListener {

    private static final String TAG = "SignUpSmsVerifFrag";

    private static final int VERIFICATION_SUCCESSFULL = 0;
    private static final int VERIFICATION_FAILED = 1;

    private SmsListener mSmsListener = new SmsListener();
    private Handler mHandler;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.provider.Telephony.SMS_RECEIVED");
        getActivity().registerReceiver(mSmsListener, filter);
        mHandler = new MyHandler(Looper.getMainLooper());
        ((SignUpActivity) getActivity()).setOnBackPressedListener(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getActivity().unregisterReceiver(mSmsListener);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_sms_verification, container, false);
    }

    public class SmsListener extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals("android.provider.Telephony.SMS_RECEIVED")) {
                Bundle bundle = intent.getExtras();
                SmsMessage[] msgs;
                String msg_from;
                if (bundle != null) {
                    try {
                        Object[] pdus = (Object[]) bundle.get("pdus");
                        msgs = new SmsMessage[pdus.length];
                        for (int i = 0; i < msgs.length; i++) {
                            msgs[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
                            msg_from = msgs[i].getOriginatingAddress();
                            String msgBody = msgs[i].getMessageBody();
                            Log.d(TAG, "Message from " + msg_from + ", msgBody = " + msgBody);
                            if (msg_from.equals(TwilioManager.TWILIO_PHONE_NUMBER)) {
                                if (msgBody.contains(Integer.toString(((SignUpActivity)getActivity()).getVerificationCode()))) {
                                    Message msg = mHandler.obtainMessage(VERIFICATION_SUCCESSFULL);
                                    msg.sendToTarget();
                                } else {
                                    Message msg = mHandler.obtainMessage(VERIFICATION_FAILED);
                                    msg.sendToTarget();
                                }
                            }
                        }
                    } catch (Exception e) {
                        Log.d("Exception caught", e.getMessage());
                    }
                }
            }
        }
    }

    private class MyHandler extends Handler {

        public MyHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            switch (msg.what) {
                case VERIFICATION_SUCCESSFULL:
                    Log.d(TAG, "Verification successfull");
                    FragmentManager manager = getFragmentManager();
                    FragmentTransaction transaction = manager.beginTransaction();
                    Fragment verificationSuccess = new SignUpSmsVerified();
                    transaction.replace(R.id.fragment, verificationSuccess);
                    transaction.addToBackStack(null);
                    transaction.commit();
                    break;
                case VERIFICATION_FAILED:
                    Log.d(TAG, "Verification failed");
                    break;
            }
        }
    }

    @Override
    public void doBack() {
        // Do nothing
    }
}
