package com.ltbrew.brewbeer.presenter.util;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.ltbrew.brewbeer.BrewApp;
import com.ltbrew.brewbeer.api.cssApi.BrewApi;
import com.ltbrew.brewbeer.persistence.greendao.DBBrewHistoryDao;
import com.ltbrew.brewbeer.persistence.greendao.DBBrewStep;
import com.ltbrew.brewbeer.persistence.greendao.DBBrewStepDao;
import com.ltbrew.brewbeer.persistence.greendao.DBRecipeDao;
import com.ltbrew.brewbeer.persistence.greendao.DBSlotDao;
import com.ltbrew.brewbeer.persistence.greendao.DaoMaster;
import com.ltbrew.brewbeer.persistence.greendao.DaoSession;

/**
 * Created by 151117a on 2016/5/5.
 */
public class DBManager {

    private static DaoMaster daoMaster;
    private DaoSession daoSession;
    private DBRecipeDao dbRecipeDao;
    private DBBrewStepDao dbBrewStepDao;
    private DBSlotDao dbSlotDao;
    private static DBManager dbManager;
    private DBBrewHistoryDao dbBrewHistoryDao;

    private DBManager(){
    }

    public static DBManager getInstance(){
        if (dbManager == null) {
            dbManager = new DBManager();
        }
        return dbManager;
    }

    public static void initDB(Context context){
        DaoMaster.DevOpenHelper helper = new DaoMaster.DevOpenHelper(context, "brewbeer-db", null);
        SQLiteDatabase db = helper.getWritableDatabase();
        daoMaster = new DaoMaster(db);
    }


    public DaoMaster getDaoMaster(){
        if(daoMaster == null)
            initDB(BrewApp.getInstance());
        return daoMaster;
    }

    public DaoSession getDaoSession(){
        if(daoSession == null)
            daoSession = getDaoMaster().newSession();
        return daoSession;
    }

    public DBRecipeDao getDBRecipeDao(){
        if(dbRecipeDao == null) {
            dbRecipeDao = getDaoSession().getDBRecipeDao();
        }
        return dbRecipeDao;
    }

    public DBBrewStepDao getDBBrewStepDao(){
        if(dbBrewStepDao == null) {
            dbBrewStepDao = getDaoSession().getDBBrewStepDao();
        }
        return dbBrewStepDao;
    }

    public DBSlotDao getDbSlotDao(){
        if(dbSlotDao == null) {
            dbSlotDao = getDaoSession().getDBSlotDao();
        }
        return dbSlotDao;
    }

    public DBBrewHistoryDao getDBBrewHistoryDao(){
        if(dbBrewHistoryDao == null) {
            dbBrewHistoryDao = getDaoSession().getDBBrewHistoryDao();
        }
        return dbBrewHistoryDao;
    }
}
