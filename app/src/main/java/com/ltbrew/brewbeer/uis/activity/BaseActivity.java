package com.ltbrew.brewbeer.uis.activity;

import android.app.ProgressDialog;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.ltbrew.brewbeer.BrewApp;
import com.ltbrew.brewbeer.R;
import com.ltbrew.brewbeer.thirdpartylib.MessageWindow;
import com.ltbrew.brewbeer.uis.Constants;

public class BaseActivity extends AppCompatActivity {

    private ImageView backIv;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public void initToolbar(){
        initToolbarWithCustomMsg(null);
    }

    public void initToolbarWithCustomMsg(String msg){
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null) {
            TextView centerTitle = (TextView) toolbar.findViewById(R.id.centerTitle);
            centerTitle.setTypeface(BrewApp.getInstance().textFont);
            if(msg != null) {
                centerTitle.setTextSize(20);
                centerTitle.setText(msg);
            }

            backIv = (ImageView) toolbar.findViewById(R.id.backIv);
            backIv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                }
            });
        }

    }

    public void hideBackIv(){
        if(backIv != null){
            backIv.setVisibility(View.GONE);
        }
    }

    public void showSnackBar(String msg){
        Snackbar.make(findViewById(android.R.id.content), msg, Snackbar.LENGTH_SHORT).show();
    }

    protected void showErrorMsg(String msg) {
        if(Constants.NETWORK_ERROR.equals(msg)){
            //网络错误
            showSnackBar("网络错误，请检查您的网络！");
            return;
        }else if(Constants.PASSWORD_ERROR.equals(msg)){
            showSnackBar("用户名或密码出错，请重试！");
            return;
        }
        //服务错误
        showSnackBar("服务器或APP出错，请联系客服，错误信息：" + msg);
    }

    public MessageWindow showMsgWindow(String title, String msg, MessageWindow.OnMsgWindowActionListener onCloseWindowListener){
        MessageWindow messageWindow = new MessageWindow(this);
        messageWindow.setupWindow().setOnMsgWindowActionListener(onCloseWindowListener).showMessageWindow(title, msg);
        return messageWindow;
    }

    public void showDialog(String msg){
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(msg);
        progressDialog.show();
    }

    public void hideDialog(){
        if(progressDialog != null && progressDialog.isShowing())
            progressDialog.dismiss();
    }
}
