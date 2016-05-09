package com.ltbrew.brewbeer.uis.dialog;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.ltbrew.brewbeer.R;
import com.ltbrew.brewbeer.uis.utils.DpSpPixUtils;

import butterknife.BindView;

/**
 * Created by qiusiping on 16/5/9.
 */
public class DeleteDevDialog extends Dialog {

    @BindView(R.id.edt_phone_numb)
    EditText edtPhoneNumb;
    @BindView(R.id.cancelBtn)
    Button cancelBtn;
    @BindView(R.id.okBtn)
    Button okBtn;

    public DeleteDevDialog(Context context) {
        super(context);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setBackgroundDrawableResource(R.drawable.bg_dialog);
        View phoneNumbSettingDialog = LayoutInflater.from(context).inflate(R.layout.dialog_delete_dev, null, false);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(DpSpPixUtils.dip2px(context, 300), LinearLayout.LayoutParams.MATCH_PARENT);
        setContentView(phoneNumbSettingDialog, layoutParams);
    }


}
