package com.ltbrew.brewbeer.uis.activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.ltbrew.brewbeer.R;

public class AboutActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        initToolbar();
    }
}
