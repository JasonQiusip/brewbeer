package com.ltbrew.brewbeer.presenter;

import android.text.TextUtils;

import com.ltbrew.brewbeer.api.cssApi.BrewApi;
import com.ltbrew.brewbeer.api.model.HttpResponse;
import com.ltbrew.brewbeer.interfaceviews.BrewSessionVeiw;
import com.ltbrew.brewbeer.presenter.util.DeviceUtil;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * Created by 151117a on 2016/5/2.
 */
public class BrewSessionsPresenter {

    private final BrewSessionVeiw brewSessionView;
    private final RecipePresenter recipePresenter;

    public BrewSessionsPresenter(BrewSessionVeiw brewSessionVeiw){
        this.brewSessionView = brewSessionVeiw;
        recipePresenter = new RecipePresenter(brewSessionView);
    }

    public void getBrewHistory(){
        final String devId = DeviceUtil.getCurrentDevId();
        if(TextUtils.isEmpty(devId)){
            return;
        }
        Observable.create(new Observable.OnSubscribe<String>() {
            @Override
            public void call(Subscriber<? super String> subscriber) {
                HttpResponse brewHistory = BrewApi.getBrewHistory(devId, "", "", "");
                if(brewHistory.isSuccess()){

                }else{

                }
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new Action1<String>() {
            @Override
            public void call(String s) {

            }
        }, new Action1<Throwable>() {
            @Override
            public void call(Throwable throwable) {

            }
        });
    }

    public void getRecipeInfo(String formula_id){
        recipePresenter.getRecipes(formula_id);
    }
}
