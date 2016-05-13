package com.ltbrew.brewbeer.uis.dialog;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.ltbrew.brewbeer.R;
import com.ltbrew.brewbeer.uis.utils.DpSpPixUtils;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by qiusiping on 16/5/9.
 */
public class SetDevNameDialog extends Dialog implements View.OnClickListener {

    @BindView(R.id.edt_dev_name)
    EditText edtDevName;
    private OnSetNameForDevListener onSetNameForDevListener;

    public SetDevNameDialog(Context context) {
        super(context);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setBackgroundDrawableResource(R.drawable.bg_dialog);
        View devNameDialog = LayoutInflater.from(context).inflate(R.layout.dialog_set_device_name, null, false);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(DpSpPixUtils.dip2px(context, 300), LinearLayout.LayoutParams.MATCH_PARENT);
        setContentView(devNameDialog, layoutParams);
        ButterKnife.bind(this, devNameDialog);

    }

    public void setOnSetNameForDevListener(OnSetNameForDevListener onSetNameForDevListener) {
        this.onSetNameForDevListener = onSetNameForDevListener;
    }

    @OnClick({R.id.cancelBtn, R.id.okBtn})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.cancelBtn:
                break;
            case R.id.okBtn:
                if (onSetNameForDevListener != null)
                    onSetNameForDevListener.onSetDevName(edtDevName.getText().toString());
                break;
        }
        dismiss();
    }

    public interface OnSetNameForDevListener {
        void onSetDevName(String s);
    }
}
