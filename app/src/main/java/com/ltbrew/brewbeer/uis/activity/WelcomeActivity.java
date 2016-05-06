package com.ltbrew.brewbeer.uis.activity;

import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.ltbrew.brewbeer.BrewApp;
import com.ltbrew.brewbeer.R;

import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.functions.Action1;

public class WelcomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
        TextView welcomeTv = (TextView)findViewById(R.id.welcomeTv);
        welcomeTv.setTypeface(Typeface.createFromAsset(getAssets(), "fonts/cursive.TTF"));
        Observable.timer(3000, TimeUnit.MILLISECONDS).subscribe(new Action1<Long>() {
            @Override
            public void call(Long aLong) {
                Intent intent = new Intent();
                intent.setClass(WelcomeActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });
    }

}
