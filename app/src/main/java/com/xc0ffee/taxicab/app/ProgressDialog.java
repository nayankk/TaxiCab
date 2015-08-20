package com.xc0ffee.taxicab.app;

import android.app.Dialog;
import android.app.DialogFragment;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import com.xc0ffee.taxicab.R;

public class ProgressDialog extends DialogFragment {

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog d = new Dialog(getActivity(), android.R.style.Theme_Light_NoTitleBar);
        d.setContentView(R.layout.progress_dialog);
        Drawable alphaDrawable = new ColorDrawable(Color.BLACK);
        alphaDrawable.setAlpha(130);
        d.getWindow().setBackgroundDrawable(alphaDrawable);
        return d;
    }
}
