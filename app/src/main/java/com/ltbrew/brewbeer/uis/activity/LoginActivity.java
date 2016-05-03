package com.ltbrew.brewbeer.uis.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
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

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends AppCompatActivity implements LoginView {

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
    @BindView(R.id.registet_btn)
    Button registetBtn;
    @BindView(R.id.login_form)
    ScrollView loginForm;
    private LoginPresenter loginPresenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);
        loginPresenter = new LoginPresenter(this);
    }

    @OnClick(R.id.sign_in_button)
    public void signIn(){

    }

    @Override
    public void onCheckSuccess(String state) {

    }

    @Override
    public void onCheckFailed(String msg) {

    }
}

