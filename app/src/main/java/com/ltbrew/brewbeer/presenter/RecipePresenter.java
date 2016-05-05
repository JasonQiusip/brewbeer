package com.ltbrew.brewbeer.presenter;

import android.text.TextUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.ltbrew.brewbeer.api.cssApi.BrewApi;
import com.ltbrew.brewbeer.api.generalApi.GeneralApi;
import com.ltbrew.brewbeer.api.model.HttpResponse;
import com.ltbrew.brewbeer.interfaceviews.RecipeView;

import rx.Observable;
import rx.Subscriber;
import rx.functions.Action1;

/**
 * Created by 151117a on 2016/5/5.
 */
public class RecipePresenter {

    private final RecipeView recipeView;

    public RecipePresenter(RecipeView recipeView){
        this.recipeView = recipeView;
    }

    public void getAllRecipes(final String devId, final String formula_id){
        RxUtil.create(new Observable.OnSubscribe<String>() {
            @Override
            public void call(Subscriber<? super String> subscriber) {
                HttpResponse brewRecipes = BrewApi.getBrewRecipes(devId, formula_id);
                if(brewRecipes.isSuccess()){
                    String content = brewRecipes.getContent();
                    parseFormula(devId, content);
                }else{

                }
            }
        }).subscribe(new Action1<String>() {
            @Override
            public void call(String s) {

            }
        }, new Action1<Throwable>() {
            @Override
            public void call(Throwable throwable) {

            }
        });

    }

    private void parseFormula(String devId, String content) {
        JSONArray jsonArray = JSON.parseArray(content);
        for (int i = 0, size = jsonArray.size(); i < size; i++) {
            JSONObject jsonObject = jsonArray.getJSONObject(i);
            String name = jsonObject.getString("name");
            String ref = jsonObject.getString("ref");
            if(TextUtils.isEmpty(name) || TextUtils.isEmpty(ref))
                continue;

            downloadRecipe(devId, name, ref);

        }
    }

    private void downloadRecipe(String devId, String name, String ref) {

        HttpResponse httpResponse = BrewApi.downloadRecipe(devId, name, ref);
        if(httpResponse.isSuccess()){
            byte[] file = httpResponse.getFile();
            String recipe = new String(file);
            JSONObject jsonObject = JSON.parseObject(recipe);
            System.out.print(recipe);
        }
    }

}
