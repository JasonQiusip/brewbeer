package com.ltbrew.brewbeer.uis.dialog;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ltbrew.brewbeer.R;
import com.ltbrew.brewbeer.uis.utils.DpSpPixUtils;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * Created by qiusiping on 16/5/9.
 */
public class SetDevPhoneNumbDialog extends Dialog implements View.OnClickListener {

    EditText edtPhoneNumb;
    private OnSetPhoneNumbForDevListener onSetPhoneNumbForDevListener;

    public SetDevPhoneNumbDialog(Context context) {
        super(context);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setBackgroundDrawableResource(R.drawable.bg_dialog);
        View phoneNumbSettingDialog = LayoutInflater.from(context).inflate(R.layout.dialog_set_phone_numb, null, false);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(DpSpPixUtils.dip2px(context, 300), LinearLayout.LayoutParams.MATCH_PARENT);
        setContentView(phoneNumbSettingDialog, layoutParams);
        Button cancelBtn = (Button) phoneNumbSettingDialog.findViewById(R.id.cancelBtn);
        Button okBtn = (Button) phoneNumbSettingDialog.findViewById(R.id.okBtn);
        edtPhoneNumb = (EditText)phoneNumbSettingDialog.findViewById(R.id.edt_phone_numb);

        okBtn.setOnClickListener(this);
        cancelBtn.setOnClickListener(this);
    }

    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.cancelBtn:
                dismiss();
                break;
            case R.id.okBtn:
                if(onSetPhoneNumbForDevListener != null)
                    onSetPhoneNumbForDevListener.onSetPhoneNumb(edtPhoneNumb.getText().toString());
                break;
        }
    }

    public void setOnSetPhoneNumbForDevListener(OnSetPhoneNumbForDevListener onSetPhoneNumbForDevListener){
        this.onSetPhoneNumbForDevListener = onSetPhoneNumbForDevListener;
    }

    public interface OnSetPhoneNumbForDevListener{
        void onSetPhoneNumb(String s);
    }
}
