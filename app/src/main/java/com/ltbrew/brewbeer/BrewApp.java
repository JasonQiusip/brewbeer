package com.ltbrew.brewbeer;

import android.app.Application;
import android.graphics.Typeface;

/**
 * Created by 151117a on 2016/5/2.
 */
public class BrewApp extends Application {

    private static BrewApp app;
    public Typeface textFont;

    @Override
    public void onCreate() {
        super.onCreate();
        app = this;
        textFont = Typeface.createFromAsset(getAssets(), "fonts/blesd.otf");
    }

    public static BrewApp getInstance(){
        return app;
    }
}
