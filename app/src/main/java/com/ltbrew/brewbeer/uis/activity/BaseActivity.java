package com.ltbrew.brewbeer.uis.activity;

import android.graphics.Typeface;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.widget.TextView;

import com.ltbrew.brewbeer.BrewApp;
import com.ltbrew.brewbeer.R;
import com.ltbrew.brewbeer.thirdpartylib.MessageWindow;
import com.ltbrew.brewbeer.uis.Constants;

public class BaseActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public void initToolbar(){
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null) {
            TextView centerTitle = (TextView) toolbar.findViewById(R.id.centerTitle);
            centerTitle.setTypeface(BrewApp.getInstance().textFont);
        }
    }

    public void showSnackBar(String msg){
        Snackbar.make(findViewById(android.R.id.content), msg, Snackbar.LENGTH_SHORT).show();
    }

    protected void showErrorMsg(String msg) {
        if(Constants.NETWORK_ERROR.equals(msg)){
            //网络错误
            showSnackBar("网络错误，请检查您的网络！");
        }
        //服务错误
        showSnackBar("服务器或APP出错，请联系客服，错误信息：" + msg);
    }

    public void showMsgWindow(String title, String msg, MessageWindow.OnCloseWindowListener onCloseWindowListener){
        MessageWindow messageWindow = new MessageWindow(this);
        messageWindow.setupWindow().setOnCloseWindowListener(onCloseWindowListener).showMessageWindow(title, msg);
    }
}
