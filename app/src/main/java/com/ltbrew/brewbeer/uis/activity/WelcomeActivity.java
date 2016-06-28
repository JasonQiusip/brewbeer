package com.ltbrew.brewbeer.uis.activity;

import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.ltbrew.brewbeer.BrewApp;
import com.ltbrew.brewbeer.R;
import com.ltbrew.brewbeer.api.common.utils.HostUtil;
import com.ltbrew.brewbeer.uis.utils.AccUtils;

import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

public class WelcomeActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
//        TextView welcomeTv = (TextView)findViewById(R.id.welcomeTv);
//        welcomeTv.setTypeface(BrewApp.getInstance().textFont);

        Observable.create(new Observable.OnSubscribe<Long>() {
            @Override
            public void call(Subscriber<? super Long> subscriber) {
                boolean b = HostUtil.checkHostports(BrewApp.getInstance());
//                boolean b = true;//测试的时候打开
                if(b){
                    subscriber.onNext(3000l);
                }else{
                    subscriber.onError(new Throwable(""));
                }

            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .delay(3000, TimeUnit.MILLISECONDS).subscribe(new Action1<Long>() {
            @Override
            public void call(Long aLong) {

                if (TextUtils.isEmpty(AccUtils.getCurPwd())) {
                    Intent intent = new Intent();
                    intent.setClass(WelcomeActivity.this, LoginActivity.class);
                    startActivity(intent);
                    finish();
                    return;
                }

                Intent intent = new Intent();
                intent.setClass(WelcomeActivity.this, BrewHomeActivity.class);
                startActivity(intent);
                finish();

            }
        }, new Action1<Throwable>() {
            @Override
            public void call(Throwable throwable) {
                showSnackBar("服务连接失败");
            }
        });
    }

}
