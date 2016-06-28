package com.ltbrew.brewbeer.uis.dialog;

import android.app.Dialog;
import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ltbrew.brewbeer.R;
import com.ltbrew.brewbeer.uis.utils.DpSpPixUtils;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by qiusiping on 16/5/9.
 */
public class FermentingDialog extends Dialog implements View.OnClickListener {

    TextView tv_notice;
    @BindView(R.id.finishFermentingTv)
    TextView finishFermentingTv;
    @BindView(R.id.restartFermenting)
    TextView restartFermenting;
    @BindView(R.id.cancelTv)
    TextView cancelTv;
    private FermentingDialogCallback mFermentingDialogCallback;

    public FermentingDialog(Context context) {
        super(context);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setBackgroundDrawableResource(R.drawable.bg_dialog_transparent);
        getWindow().setGravity(Gravity.BOTTOM);
        View phoneNumbSettingDialog = LayoutInflater.from(context).inflate(R.layout.layout_bottom_pop_window, null, false);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(DpSpPixUtils.dip2px(context, 300), LinearLayout.LayoutParams.MATCH_PARENT);
        layoutParams.gravity = Gravity.BOTTOM;
        setContentView(phoneNumbSettingDialog, layoutParams);
        ButterKnife.bind(this, phoneNumbSettingDialog);
    }
    @OnClick({R.id.finishFermentingTv, R.id.restartFermenting, R.id.cancelTv})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.finishFermentingTv:
                if (mFermentingDialogCallback != null)
                    mFermentingDialogCallback.onClickFinishBtn();
                break;
            case R.id.restartFermenting:
                if (mFermentingDialogCallback != null)
                    mFermentingDialogCallback.onClickRestartBtn();
                break;
            case R.id.cancelTv:
                if (mFermentingDialogCallback != null)
                    mFermentingDialogCallback.onClickCancelBtn();
                break;
        }
        dismiss();
    }


    public FermentingDialog setMsg(String txt) {
        tv_notice.setText(txt);
        return this;
    }


    public FermentingDialog setFermentingDialogCallback(FermentingDialogCallback mFermentingDialogCallback) {
        this.mFermentingDialogCallback = mFermentingDialogCallback;
        return this;
    }

    public interface FermentingDialogCallback{
        void onClickFinishBtn();
        void onClickRestartBtn();
        void onClickCancelBtn();
    }


}
