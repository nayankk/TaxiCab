package com.xc0ffee.taxicab.app;

import android.app.Dialog;
import android.content.Context;

import com.xc0ffee.taxicab.R;

public class GreetingsDialog extends Dialog {

    public GreetingsDialog(Context context) {
        this(context, android.R.style.Theme_Light_NoTitleBar);
    }

    public GreetingsDialog(Context context, int themeResId) {
        super(context, themeResId);
        setContentView(R.layout.greetings_dialog);
    }
}
