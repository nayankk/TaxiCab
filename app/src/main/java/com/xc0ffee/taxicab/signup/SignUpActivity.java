package com.xc0ffee.taxicab.signup;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.telephony.PhoneNumberFormattingTextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.xc0ffee.taxicab.R;
import com.xc0ffee.taxicab.app.ProgressDialog;
import com.xc0ffee.taxicab.utils.TaxiCabUtils;

import java.util.Random;

public class SignUpActivity extends AppCompatActivity {

    private static final String TAG = "SignUpActivity";

    private static final int RANDOM_MIN = 100000;
    private static final int RANDOM_MAX = 1000000;

    public static final String KEY_NAME = "user-name";
    public static final String KEY_PASSWORD = "user-passwd";
    public static final String KEY_EMAIL = "user-email";
    public static final String KEY_PHNUMBER = "user-phone";
    public static final String KEY_RANDOM_NUMBER = "random-number";

    private TwilioManager mTwilioManager;

    private EditText mEmailText;
    private EditText mPasswordText;
    private EditText mPasswordConfirmText;
    private EditText mName;
    private EditText mPhoneNumber;

    private ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.signup);

        mTwilioManager = new TwilioManager(getApplicationContext());

        mEmailText = (EditText) findViewById(R.id.user_email);
        mPasswordText = (EditText) findViewById(R.id.user_passwd);
        mPasswordConfirmText = (EditText) findViewById(R.id.user_conf_password);
        mName = (EditText) findViewById(R.id.user_name);
        mPhoneNumber = (EditText) findViewById(R.id.user_phone);
        mPhoneNumber.addTextChangedListener(new PhoneNumberFormattingTextWatcher());

        mEmailText.setText(getIntent().getStringExtra(KEY_EMAIL));
        mPasswordText.setText(getIntent().getStringExtra(KEY_PASSWORD));
    }

    public void onSignUpClicked(View view) {
        String email = String.valueOf(mEmailText.getText());
        String password = String.valueOf(mPasswordText.getText());
        String confirmPassword = String.valueOf(mPasswordConfirmText.getText());
        String name = String.valueOf(mName.getText());
        String phNumber = String.valueOf(mPhoneNumber.getText());

        if (!TaxiCabUtils.isValidEmail(email)) {
            Toast.makeText(this, R.string.email_invalid, Toast.LENGTH_LONG).show();
            return;
        }

        if (email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()
                || name.isEmpty() || phNumber.isEmpty()) {
            Toast.makeText(this, R.string.required_fields, Toast.LENGTH_LONG).show();
            return;
        }

        if (!password.equals(confirmPassword)) {
            Toast.makeText(this, R.string.passwd_mismatch, Toast.LENGTH_LONG).show();
            return;
        }

        String phoneNumber = mPhoneNumber.getText().toString();
        if (!phoneNumber.startsWith("+1") && !phoneNumber.startsWith("1"))
            phoneNumber = "+1" + phoneNumber;
        else if (phoneNumber.startsWith("1"))
            phoneNumber = "+" + phoneNumber;

        showSmsVerificationDialog(phoneNumber);
    }

    private class SendSmsListener implements TwilioManager.SendSmsResult {

        private int mRandom;

        SendSmsListener(int randomNumber) {
            mRandom = randomNumber;
        }

        @Override
        public void onSmsSendSuccess() {
            Log.d(TAG, "onSmsSendSuccess");
            hideProgressDialog();
             /*
            Intent intent = new Intent(SignUpActivity.this, SmsVerificationActivity.class);
            intent.putExtra(KEY_NAME, String.valueOf(mName.getText()));
            intent.putExtra(KEY_EMAIL, String.valueOf(mEmailText.getText()));
            intent.putExtra(KEY_PASSWORD, String.valueOf(mPasswordText.getText()));
            intent.putExtra(KEY_PHNUMBER, String.valueOf(mPhoneNumber.getText()));
            intent.putExtra(KEY_RANDOM_NUMBER, Integer.toString(mRandom));
            startActivity(intent); */
        }

        @Override
        public void onSmsSendFailed() {
            Log.d(TAG, "onSmsSendFailed");
            hideProgressDialog();
            verificationFailed();
        }
    }

    private void sendSms(String phoneNumber) {
        Random random = new Random();
        int nextInt = random.nextInt(RANDOM_MAX - RANDOM_MIN + 1) + RANDOM_MIN;
        if (mTwilioManager.sendSms(phoneNumber, nextInt, new SendSmsListener(nextInt))) {
            Log.d(TAG, "Sending SMS success");
            mProgressDialog = new ProgressDialog();
            mProgressDialog.show(getFragmentManager(), "dialog");
        }
        else {
            Log.d(TAG, "SMS sending failed");
            verificationFailed();
        }
    }

    public void showSmsVerificationDialog(final String phoneNumber) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        String message = String.format(getResources().getString(R.string.sms_verification_msg), phoneNumber);
        builder.setTitle(R.string.sms_verification);
        builder.setCancelable(true);
        builder.setMessage(message);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                SignUpActivity.this.sendSms(phoneNumber);
                dialog.dismiss();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        final AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void verificationFailed() {
        Toast.makeText(this, R.string.sms_sending_failed, Toast.LENGTH_LONG).show();
    }

    private void hideProgressDialog() {
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
            mProgressDialog = null;
        }
    }
}
