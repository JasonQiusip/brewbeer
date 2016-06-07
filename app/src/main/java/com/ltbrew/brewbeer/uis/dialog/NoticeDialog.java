package com.ltbrew.brewbeer.uis.dialog;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ltbrew.brewbeer.R;
import com.ltbrew.brewbeer.uis.utils.DpSpPixUtils;

/**
 * Created by qiusiping on 16/5/9.
 */
public class NoticeDialog extends Dialog implements View.OnClickListener {

    TextView tv_notice;
    private OnPositiveButtonClickListener onPositiveButtonClickListener;
    private OnNegativeButtonClickListener onNegativeButtonClickListener;

    public NoticeDialog(Context context) {
        super(context);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setBackgroundDrawableResource(R.drawable.bg_dialog);
        View phoneNumbSettingDialog = LayoutInflater.from(context).inflate(R.layout.dialog_notice, null, false);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(DpSpPixUtils.dip2px(context, 300), LinearLayout.LayoutParams.MATCH_PARENT);
        setContentView(phoneNumbSettingDialog, layoutParams);
        Button cancelBtn = (Button) phoneNumbSettingDialog.findViewById(R.id.cancelBtn);
        Button okBtn = (Button) phoneNumbSettingDialog.findViewById(R.id.okBtn);
        tv_notice = (TextView)phoneNumbSettingDialog.findViewById(R.id.tv_notice);

        okBtn.setOnClickListener(this);
        cancelBtn.setOnClickListener(this);
    }

    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.cancelBtn:
                if(onNegativeButtonClickListener != null)
                    onNegativeButtonClickListener.onNegativeButtonClick();
                break;
            case R.id.okBtn:
                if(onPositiveButtonClickListener != null)
                    onPositiveButtonClickListener.onPositiveButtonClick();
                break;
        }
        dismiss();
    }

    public NoticeDialog setMsg(String txt){
        tv_notice.setText(txt);
        return this;
    }

    public NoticeDialog setOnPositiveButtonClickListener(OnPositiveButtonClickListener onPositiveButtonClickListener){
        this.onPositiveButtonClickListener = onPositiveButtonClickListener;
        return this;
    }

    public NoticeDialog setOnNegativeButtonClickListener(OnNegativeButtonClickListener onNegativeButtonClickListener){
        this.onNegativeButtonClickListener = onNegativeButtonClickListener;
        return this;
    }
}
