package com.ltbrew.brewbeer.uis.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.ScrollView;

import com.ltbrew.brewbeer.R;
import com.ltbrew.brewbeer.interfaceviews.LoginView;
import com.ltbrew.brewbeer.presenter.LoginPresenter;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import com.ltbrew.brewbeer.uis.Constants.*;
import com.ltbrew.brewbeer.uis.utils.AccUtils;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends BaseActivity implements LoginView {

    @BindView(R.id.login_progress)
    ProgressBar loginProgress;
    @BindView(R.id.account)
    AutoCompleteTextView account;
    @BindView(R.id.password)
    EditText password;
    @BindView(R.id.sign_in_button)
    Button signInButton;
    @BindView(R.id.forget_pwd_btn)
    Button forgetPwdBtn;
    @BindView(R.id.register_btn)
    Button registerBtn;
    @BindView(R.id.login_form)
    ScrollView loginForm;
    private LoginPresenter loginPresenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        initToolbar();
        ButterKnife.bind(this);
        loginPresenter = new LoginPresenter(this);
        if(!AccUtils.isAccEmpty()){
            account.setText(AccUtils.getAcc());
        }
    }
    //---------------------
    @OnClick(R.id.sign_in_button)
    public void signIn(){
        String acc = account.getText().toString();
        if(TextUtils.isEmpty(acc)){
            showSnackBar("帐号不能为空");
            return;
        }
        String pwd = password.getText().toString();
        if(TextUtils.isEmpty(pwd)){
            showSnackBar("密码不能为空");
            return;
        }
        if(pwd.length() < 6){
            showSnackBar("密码不能少于6位");
            return;
        }
        loginPresenter.checkAccount(acc);
    }

    @Override
    public void onCheckSuccess(String state) {
        if(CheckAccState.PHONE_NOT_REGISTERED.equals(state)){
            showSnackBar("帐号未注册");
        }else if(CheckAccState.NUMB_NOT_PHONE.equals(state)){
            showSnackBar("帐号非正确的手机号码");
        }else if(CheckAccState.ACC_REGISTERED.equals(state)){
            loginPresenter.signIn(account.getText().toString(), password.getText().toString());
        }
    }

    @Override
    public void onCheckFailed(String msg) {
        showErrorMsg(msg);
    }

    @Override
    public void onLoginSuccess() {
        AccUtils.storeAcc(account.getText().toString());
        startBrewSessionActivity();
    }
    private void startBrewSessionActivity() {
        startActivity(new Intent(this, BrewHomeActivity.class));
    }

    @Override
    public void onLoginFailed(String msg) {
        showErrorMsg(msg);
    }

    @OnClick(R.id.forget_pwd_btn)
    public void clickForgetPwdBtn(){
        startForgetPwdActivity();
    }

    private void startForgetPwdActivity() {
        startActivity(new Intent(this, ForgetPwdActivity.class));
    }

    @OnClick(R.id.register_btn)
    public void clickRegisterBtn(){
        startRegisterActivity();
    }

    private void startRegisterActivity() {
        startActivity(new Intent(this, RegisterActivity.class));
    }

}

