package com.ltbrew.brewbeer.uis.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;

import com.ltbrew.brewbeer.R;
import com.ltbrew.brewbeer.interfaceviews.RegisterView;
import com.ltbrew.brewbeer.presenter.RegisterPresenter;
import com.ltbrew.brewbeer.uis.Constants;
import com.ltbrew.brewbeer.uis.utils.KeyboardUtil;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class RegisterActivity extends BaseActivity implements RegisterView {

    @BindView(R.id.reg_phone_no_edt)
    EditText regPhoneNoEdt;
    @BindView(R.id.edit_reg_code)
    EditText editRegCode;
    @BindView(R.id.btn_req_code)
    Button btnReqCode;
    @BindView(R.id.edt_reg_pwd)
    EditText edtRegPwd;
    @BindView(R.id.ed_psw_2)
    EditText edPsw2;
    @BindView(R.id.bt_regist_ok)
    Button btRegistOk;
    private RegisterPresenter registerPresenter;
    private CountDownTimer countDownTimer;
    private String tag = this.getClass().getName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        initToolbarWithCustomMsg("注册");

        ButterKnife.bind(this);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        initToolbar();
        registerPresenter = new RegisterPresenter(this);
    }

    @OnClick(R.id.btn_req_code)
    public void reqRegCode(){
        String phone = regPhoneNoEdt.getText().toString();
        if(TextUtils.isEmpty(phone)){
            showSnackBar("手机号不能为空");
            return;
        }else if(phone.length() != 11){
            showSnackBar("手机号非11位国内手机号");
            return;
        }
        if(countDownTimer != null)
            return;
        KeyboardUtil.hideKeyboard(this, btnReqCode);
        registerPresenter.reqRegCode(phone);
        activeCountDownTimer();
    }

    private void activeCountDownTimer(){
        countDownTimer = new CountDownTimer(2*60*1000, 1000) {
            int count = 120;
            @Override
            public void onTick(long l) {
                Log.e(tag, "onTick  "+l);
                RegisterActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        btnReqCode.setText(count-- + "秒");
                    }
                });
            }

            @Override
            public void onFinish() {
                countDownTimer = null;
                RegisterActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        btnReqCode.setText("获取验证码");
                    }
                });
            }
        };
        countDownTimer.start();
    }

    @Override
    public void onReqRegCodeSuccess() {
        showSnackBar("验证码请求成功");
    }

    @Override
    public void onReqRegCodeFailed(String message) {
        cancelTimer();
        if(Constants.CheckAccState.NOT_PHONE_NOTICE.equals(message)){
            showSnackBar(Constants.CheckAccState.NOT_PHONE_NOTICE);
            return;
        }else if(Constants.CheckAccState.ACC_REGISTERED_NOTICE.equals(message)){
            showSnackBar(Constants.CheckAccState.ACC_REGISTERED_NOTICE);
            return;
        }
        showErrorMsg(message);
    }


    @OnClick(R.id.bt_regist_ok)
    public void register(){
        String phone = regPhoneNoEdt.getText().toString();
        String pwd = edtRegPwd.getText().toString();
        String pwdConfirm = edPsw2.getText().toString();
        String regCode = editRegCode.getText().toString();
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
            showSnackBar("两次输入的密码不一致, 请确认");
            return;
        }
        showDialog("正在进行注册...");
        KeyboardUtil.hideKeyboard(this, btRegistOk);
        registerPresenter.register(phone, pwd, regCode);
    }

    @Override
    public void onRegReqSuccess(String state) {
        cancelTimer();
        hideDialog();
        if (Constants.RegisterState.SUCCESS.equals(state)) {
            showSnackBar("注册成功");
            startLoginActivity();
        } else if (Constants.RegisterState.APIKEY_NO_NEED_TO_ACTIVE.equals(state)) {
            showSnackBar("该apikey无需激活");
        } else if (Constants.RegisterState.CODE_ERROR.equals(state)) {
            showSnackBar("验证码错误");
        } else if (Constants.RegisterState.CHECK_PHONE_NO.equals(state)) {
            showSnackBar("请确认您的手机号是否为正确的手机号");
        } else if (Constants.RegisterState.CODE_ERROR_AGAIN.equals(state)) {
            showSnackBar("验证码错误");
        }else if (Constants.RegisterState.CHECK_YOUR_PARAM.equals(state)) {
            showSnackBar("内部请求参数错误， 请联系客服");
        }else{
            showSnackBar("内部错误， 请联系客服");
        }

    }

    private void startLoginActivity() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    @Override
    public void onRegFailed(String message) {
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
