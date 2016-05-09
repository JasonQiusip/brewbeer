package com.ltbrew.brewbeer.uis.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;

import com.ltbrew.brewbeer.R;

/**
 * Created by qiusiping on 16/5/9.
 */
public class SetDevPhoneNumbDialog extends Dialog{

    public SetDevPhoneNumbDialog(Context context) {
        super(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_set_phone_numb);
    }
}
