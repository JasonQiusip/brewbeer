package com.ltbrew.brewbeer;

import android.app.Application;
import android.graphics.Typeface;

import com.ltbrew.brewbeer.api.ConfigApi;
import com.ltbrew.brewbeer.presenter.util.DBManager;

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
        textFont = Typeface.createFromAsset(getAssets(), "fonts/DancingScript-Regular.otf");
        DBManager.initDB(this);
        ConfigApi.init(this);
        ConfigApi.api_key = "840ebe7c2bfe4d529181063433ece0ef";
        ConfigApi.api_secret = "426e26e82c704e5984b4a30071cc3775";
    }

    public static BrewApp getInstance(){
        return app;
    }

}
