package com.ltbrew.brewbeer.uis.utils;

import com.ltbrew.brewbeer.persistence.greendao.DBBrewHistory;
import com.ltbrew.brewbeer.persistence.greendao.DBRecipe;

/**
 * Created by qiusiping on 16/5/7.
 */
public class ParamStoreUtil {

    private static ParamStoreUtil paramStoreUtil;
    private DBRecipe dbRecipe;
    private DBRecipe dbCreatingRecipe;
    private ParamSetObserver paramSetObserver;
    public boolean isRecipeSet;
    private DBBrewHistory brewHistory;

    private ParamStoreUtil(){}

    public static ParamStoreUtil getInstance(){
        if(paramStoreUtil == null){
            paramStoreUtil = new ParamStoreUtil();
        }
        return paramStoreUtil;
    }

    public void setDbRecipe(DBRecipe dbRecipe){
        this.dbRecipe = dbRecipe;
        if(paramSetObserver != null){
            paramSetObserver.onSetRecipe();
        }
    }

    public DBRecipe getDbRecipe(){
        return dbRecipe;
    }

    /**
     * 在BrewSessionFragment 进行设置
     * @param dbRecipe
     */
    public void setCurrentCreatingRecipe(DBRecipe dbRecipe){
        isRecipeSet = true;
        this.dbCreatingRecipe = dbRecipe;
        if(paramSetObserver != null){
            paramSetObserver.onSetRecipe();
        }
    }

    /**
     * 在RecipeDetailActivity调用该函数获取配方内容
     * @return
     */
    public DBRecipe getCurrentCreatingRecipe(){
        isRecipeSet = false;
        return dbCreatingRecipe;
    }


    /**
     * 用于等待BrewSessionFragment设置完值之后， 通知RecipeDetailActivity去获取
     * @param paramSetObserver
     */
    public void setParamSetObserver(ParamSetObserver paramSetObserver){
        this.paramSetObserver = paramSetObserver;
    }

    public void setBrewHistory(DBBrewHistory brewHistory) {
        this.brewHistory = brewHistory;
    }

    public DBBrewHistory getBrewHistory(){
        return this.brewHistory;
    }
}
