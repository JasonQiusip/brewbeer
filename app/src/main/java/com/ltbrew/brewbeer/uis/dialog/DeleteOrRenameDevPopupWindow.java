package com.ltbrew.brewbeer.uis.dialog;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupWindow;

import com.ltbrew.brewbeer.R;
import com.ltbrew.brewbeer.uis.utils.DpSpPixUtils;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by qiusiping on 16/5/9.
 */
public class DeleteOrRenameDevPopupWindow extends PopupWindow {



    private OnButtonClickListener mOnButtonClickListener;

    public DeleteOrRenameDevPopupWindow(Context context) {
        super(context);
        View phoneNumbSettingDialog = LayoutInflater.from(context).inflate(R.layout.dialog_delete_dev, null, false);
        setContentView(phoneNumbSettingDialog);
        setFocusable(true);
        setOutsideTouchable(true);
        setBackgroundDrawable(null);
        ButterKnife.bind(this, phoneNumbSettingDialog);
    }


    @OnClick({R.id.changeNameBtn, R.id.deleteDevBtn})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.changeNameBtn:
                if(mOnButtonClickListener != null)
                    mOnButtonClickListener.onChangeNameBtnClick();
                break;
            case R.id.deleteDevBtn:
                if(mOnButtonClickListener != null)
                    mOnButtonClickListener.onDeleteDevBtnClick();
                break;
        }
        dismiss();
    }

    public void setOnButtonClickListener(OnButtonClickListener mOnButtonClickListener){
        this.mOnButtonClickListener = mOnButtonClickListener;
    }

    public interface OnButtonClickListener
    {
        void onChangeNameBtnClick();
        void onDeleteDevBtnClick();

    }
}
