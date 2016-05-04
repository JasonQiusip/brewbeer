package com.ltbrew.brewbeer.uis.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.Button;
import android.widget.EditText;

import com.ltbrew.brewbeer.R;

import butterknife.BindView;
import butterknife.ButterKnife;

public class RegisterActivity extends AppCompatActivity {

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        ButterKnife.bind(this);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


    }

}
