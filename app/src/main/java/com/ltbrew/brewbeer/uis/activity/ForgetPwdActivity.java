package com.ltbrew.brewbeer.uis.activity;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.ltbrew.brewbeer.R;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ForgetPwdActivity extends AppCompatActivity {

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forget_pwd);
        ButterKnife.bind(this);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
    }

}
