package com.ltbrew.brewbeer.uis.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;

import com.ltbrew.brewbeer.R;
import com.ltbrew.brewbeer.interfaceviews.ForgetPwdView;
import com.ltbrew.brewbeer.presenter.ForgetPwdPresenter;
import com.ltbrew.brewbeer.uis.Constants;
import com.ltbrew.brewbeer.uis.utils.AccUtils;
import com.ltbrew.brewbeer.uis.utils.KeyboardUtil;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ForgetPwdActivity extends BaseActivity implements ForgetPwdView {

    @BindView(R.id.forget_pwd_phone_no_edt)
    EditText forgetPwdPhoneNoEdt;
    @BindView(R.id.edit_forget_pwd_code)
    EditText editForgetPwdCode;
    @BindView(R.id.btn_forget_pwd_req_code)
    Button btnForgetPwdReqCode;
    @BindView(R.id.edt_forget_pwd)
    EditText edtForgetPwd;
    @BindView(R.id.forget_pwd_confirm_pwd_edt)
    EditText forgetPwdConfirmPwdEdt;
    @BindView(R.id.bt_forget_pwd_ok)
    Button btForgetPwdOk;
    private ForgetPwdPresenter forgetPwdPresenter;
    private CountDownTimer countDownTimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forget_pwd);
        ButterKnife.bind(this);
        initToolbarWithCustomMsg("重置密码");

        forgetPwdPresenter = new ForgetPwdPresenter(this);
    }

    @OnClick(R.id.btn_forget_pwd_req_code)
    public void reqCode2SetNewPwd(){
        String phone = forgetPwdPhoneNoEdt.getText().toString();
        if(TextUtils.isEmpty(phone)){
            showSnackBar("手机号不能为空");
            return;
        }else if(phone.length() != 11){
            showSnackBar("手机号非11位国内手机号");
            return;
        }
        if(countDownTimer != null)
            return;
        KeyboardUtil.hideKeyboard(this, btnForgetPwdReqCode);
        forgetPwdPresenter.pwdLost(phone);
        activeCountDownTimer();
    }

    private void activeCountDownTimer(){
        countDownTimer = new CountDownTimer(2*60*1000, 1000) {
            int count = 120;
            @Override
            public void onTick(long l) {
                ForgetPwdActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        btnForgetPwdReqCode.setText(count-- + "秒");
                    }
                });
            }

            @Override
            public void onFinish() {
                countDownTimer = null;
                ForgetPwdActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        btnForgetPwdReqCode.setText("获取验证码");
                    }
                });
            }
        };
        countDownTimer.start();
    }

    @Override
    public void onReqNewPwdSuccess(String state) {
        if (Constants.ReqPwdLostState.SUCCESS.equals(state)) {
            showSnackBar("验证码请求成功");
        } else if (Constants.ReqPwdLostState.CHECK_PHONE_NO.equals(state)) {
            showSnackBar("请确认您的帐号为可用手机号码");
        } else if (Constants.ReqPwdLostState.REQ_CODE_TOO_OFTEN.equals(state)) {
            showSnackBar("申请太频繁，失败,请您稍候再试(每隔30分钟可申请一次)");
        }
    }


    @OnClick(R.id.bt_forget_pwd_ok)
    public void resetPwd(){
        String phone = forgetPwdPhoneNoEdt.getText().toString();
        String pwd = edtForgetPwd.getText().toString();
        String pwdConfirm = forgetPwdConfirmPwdEdt.getText().toString();
        String regCode = editForgetPwdCode.getText().toString();
        if(TextUtils.isEmpty(phone)){
            showSnackBar("手机号不能为空");
            return;
        }else if(phone.length() != 11){
            showSnackBar("手机号非11位国内手机号");
            return;
        }

        if(TextUtils.isEmpty(pwd)){
            showSnackBar("密码不能为空");
            return;
        }else if(pwd.length() < 6){
            showSnackBar("密码长度不能小于6位");
            return;
        }else if(pwd.length() >20){
            showSnackBar("密码长度不能大于20位");
            return;
        }

        if(TextUtils.isEmpty(regCode)){
            showSnackBar("验证码为空, 请确认");
            return;
        }

        if(!pwd.equals(pwdConfirm)){
            showSnackBar("两次输入的密码不一致请确认");
            return;
        }
        showDialog("正在重设密码...");
        KeyboardUtil.hideKeyboard(this, btForgetPwdOk);
        forgetPwdPresenter.setNewPwd(phone, pwd, regCode);
    }


    @Override
    public void onReqNewPwdFailed(String msg) {
        cancelTimer();
        showErrorMsg(msg);
    }

    @Override
    public void onSetNewPwdSuccess(String state) {
        hideDialog();
        cancelTimer();
        if (Constants.PwdNewState.SUCCESS.equals(state)) {
            AccUtils.storeAcc(forgetPwdPhoneNoEdt.getText().toString());
            startLoginActivity();
        } else if (Constants.PwdNewState.CHECK_PHONE_NO.equals(state)) {
            showSnackBar("请确认您的手机号是否为正确的手机号");
        } else if (Constants.PwdNewState.VAL_CODE_ERROR.equals(state)) {
            showSnackBar("验证码错误");
        } else if (Constants.PwdNewState.ACCONT_FORMAT_ERROR.equals(state)) {
            showSnackBar("帐号格式错误");
        } else{
            showSnackBar("内部错误， 请联系客服");
        }
    }

    private void startLoginActivity() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    public void onSetNewPwdFailed(String message) {
        hideDialog();
        cancelTimer();
        showErrorMsg(message);
    }

    private void cancelTimer() {
        if(countDownTimer != null) {
            countDownTimer.cancel();
            countDownTimer = null;
        }
    }
}
