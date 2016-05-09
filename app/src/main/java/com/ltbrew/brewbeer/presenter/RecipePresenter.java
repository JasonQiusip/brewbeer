package com.ltbrew.brewbeer.presenter;

import android.text.TextUtils;
import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.ltbrew.brewbeer.api.cssApi.BrewApi;
import com.ltbrew.brewbeer.api.generalApi.GeneralApi;
import com.ltbrew.brewbeer.api.model.HttpResponse;
import com.ltbrew.brewbeer.interfaceviews.RecipeView;
import com.ltbrew.brewbeer.persistence.greendao.DBBrewStep;
import com.ltbrew.brewbeer.persistence.greendao.DBRecipe;
import com.ltbrew.brewbeer.persistence.greendao.DBSlot;
import com.ltbrew.brewbeer.presenter.model.Recipe;
import com.ltbrew.brewbeer.presenter.util.DBManager;
import com.ltbrew.brewbeer.presenter.util.DeviceUtil;
import com.ltbrew.brewbeer.presenter.util.RxUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * Created by 151117a on 2016/5/5.
 */
public class RecipePresenter {

    private final RecipeView recipeView;

    public RecipePresenter(RecipeView recipeView){
        this.recipeView = recipeView;
    }

    public void getRecipes(final String formula_id){
        final String devId = DeviceUtil.getCurrentDevId();
        if(TextUtils.isEmpty(devId)){
            return;
        }
        Observable.create(new Observable.OnSubscribe< List<Recipe>>() {
            @Override
            public void call(Subscriber<? super  List<Recipe>> subscriber) {
                HttpResponse brewRecipes = BrewApi.getBrewRecipes(devId, formula_id);
                if (brewRecipes.isSuccess()) {
                    String content = brewRecipes.getContent();
                    List<Recipe> recipes = parseFormula(devId, content);
                    subscriber.onNext(recipes);
                } else {

                }
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new Action1< List<Recipe>>() {
            @Override
            public void call( List<Recipe> recipes) {
                recipeView.onGetRecipeSuccess(recipes);
            }
        }, new Action1<Throwable>() {
            @Override
            public void call(Throwable throwable) {
                throwable.printStackTrace();
            }
        });

    }
    //[{"cus": "", "id_typ": 0, "ref": "", "id": "001672f7", "name": "\u5e1d\u90fd\u5564\u9152\u82b1"}, ...]

    private List<Recipe> parseFormula(String devId, String content) {
        List<Recipe> recipesList = new ArrayList<>();
        JSONArray jsonArray = JSON.parseArray(content);
        for (int i = 0, size = jsonArray.size(); i < size; i++) {
            JSONObject jsonObject = jsonArray.getJSONObject(i);
            String id = jsonObject.getString("id");
            int id_type = jsonObject.getInteger("id_typ");
            String name = jsonObject.getString("name");
            String cus = jsonObject.getString("cus");
            String ref = jsonObject.getString("ref");
            Recipe recipe = new Recipe();
            recipe.setId(id);
            recipe.setId_type(id_type + "");
            recipe.setName(name);
            recipe.setCus(cus);
            recipe.setRef(ref);
            recipesList.add(recipe);
            downloadSingleRecipe(devId, recipe);
        }
        return recipesList;
    }

    private static final String STEP_PREFIX = "s_";

    private void downloadSingleRecipe(final String devId, final Recipe recipe){
        Observable.create(new Observable.OnSubscribe<DBRecipe>() {
            @Override
            public void call(Subscriber<? super DBRecipe> subscriber) {
                String fn = recipe.getId();
                String ref = recipe.getRef();
                if(TextUtils.isEmpty(ref)){
                    subscriber.onCompleted();
                    return;
                }

                HttpResponse httpResponse = BrewApi.downloadRecipe(devId, fn, ref);
                if(httpResponse.isSuccess()){
                    byte[] file = httpResponse.getFile();
                    if(file == null)
                        return;
                    String recipe = new String(file);
                    System.out.println(recipe);

                    if(TextUtils.isEmpty(recipe))
                        return;
                    JSONObject jsonObject = JSON.parseObject(recipe);
                    if(jsonObject == null)
                        return;
                    JSONObject formula = jsonObject.getJSONObject("formula");
                    if(formula == null)
                        return;
                    JSONObject slots = formula.getJSONObject("slot");
                    if(slots == null)
                        return;

                    DBRecipe dbRecipe = new DBRecipe();
                    dbRecipe.setIdForFn(fn);
                    dbRecipe.setRef(ref);

                    parseRecipeProperties(dbRecipe, formula);
                    parseSlots(slots, dbRecipe);
                    parseSteps(formula, dbRecipe);

                    System.out.print(dbRecipe);
                    subscriber.onNext(dbRecipe);
                }else{
                    subscriber.onCompleted();
                }
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new Action1<DBRecipe>() {
            @Override
            public void call(DBRecipe dbRecipe) {
                recipeView.onDownloadRecipeSuccess(dbRecipe);
            }
        }, new Action1<Throwable>() {
            @Override
            public void call(Throwable throwable) {
                recipeView.onGetRecipeFailed();
            }
        });

    }

    private void parseRecipeProperties(DBRecipe dbRecipe, JSONObject formula) {
        Integer id = formula.getInteger("id");
        String cus = formula.getString("cus");
        String name = formula.getString("name");
        Integer wr = formula.getInteger("wr");
        Integer wq = formula.getInteger("wq");
        dbRecipe.setFormulaId(id);
        dbRecipe.setCus(cus);
        dbRecipe.setName(name);
        dbRecipe.setWr(wr);
        dbRecipe.setWq(wq);
        DBManager.getInstance().getDBRecipeDao().insertOrReplace(dbRecipe);
    }

    private  List<DBSlot> parseSlots(JSONObject slots, DBRecipe dbRecipe) {
        List<DBSlot> dbSlots = new ArrayList<>();
        int sc = slots.getInteger("sc");
        for (int i = 0; i < sc; i++) {
            String slotStepId = STEP_PREFIX + String.format("%02d", i);
            JSONObject slot = slots.getJSONObject(slotStepId);
            int id = slot.getInteger("id");
            String name = slot.getString("name");
            DBSlot dbSlot = new DBSlot();
            dbSlot.setSlotStepId(slotStepId);
            dbSlot.setName(name);
            dbSlot.setSlotId(id);
            dbSlot.setRecipeId(dbRecipe.getId());
            dbSlot.setDBRecipe(dbRecipe);
            DBManager.getInstance().getDbSlotDao().insertOrReplace(dbSlot);
            dbSlots.add(dbSlot);
        }
        dbRecipe.__setDaoSession(DBManager.getInstance().getDaoSession());
        return dbRecipe.getSlots();
    }

    private List<DBBrewStep> parseSteps(JSONObject formula, DBRecipe dbRecipe) {
        List<DBBrewStep> dbBrewSteps = new ArrayList<>();
        int stepCount = formula.getInteger("sc");
        Log.e("parseSteps", "sc =======> " + stepCount + "  "+dbRecipe.getId());
        for (int i = 0; i < stepCount; i++) {
            String stepId = STEP_PREFIX + String.format("%02d", i);
            JSONObject step = formula.getJSONObject(stepId);
            String act = step.getString("act");

            DBBrewStep dbBrewStep = new DBBrewStep();
            dbBrewStep.setStepId(dbRecipe.getIdForFn()+stepId);
            dbBrewStep.setAct(act);
            if("boil".equals(act)){
                Integer f = step.getInteger("f");
                Integer k = step.getInteger("k");
                Integer pid = step.getInteger("pid");
                Integer t = step.getInteger("t");
                Integer drn = step.getInteger("drn");
                String des = step.getString("i");
                dbBrewStep.setF(f);
                dbBrewStep.setK(k);
                dbBrewStep.setPid(pid);
                dbBrewStep.setT(t);
                dbBrewStep.setDrn(drn);
                dbBrewStep.setI(des);
            }else if("drop".equals(act)){
                Integer s = step.getInteger("s");
                dbBrewStep.setSlot(s);
            }
            dbBrewStep.setRecipeId(dbRecipe.getId());
            dbBrewStep.setDBRecipe(dbRecipe);
            dbBrewSteps.add(dbBrewStep);
        }
        DBManager.getInstance().getDBBrewStepDao().insertOrReplaceInTx(dbBrewSteps);

        dbRecipe.__setDaoSession(DBManager.getInstance().getDaoSession());
        return dbRecipe.getBrewSteps();
    }
    //{"formula":
    // {"slot": {"sc": 3, "s_02": {"id": 1044975, "name": "material_02"}, "s_00": {"id": 975310, "name": "material_01"}, "s_01": {"id": 1044975, "name": "material_02"}},

    // "s_04": {"f": 1275, "k": 6000, "act": "boil", "pid": 0, "t": 1270, "drn": 0},
    // "cus": "",
    // "name": "\u6d4b\u8bd5\u5564\u9152\u914d\u65b9",
    // "s_02": {"f": 1275, "k": "", "act": "boil", "pid": 0, "t": 400, "drn": 0},
    // "s_03": {"s": 1, "act": "drop"}, "s_00": {"f": 1275, "k": "", "act": "boil", "pid": 0, "t": 300, "drn": 0},
    // "s_01": {"s": 0, "act": "drop"},
    // "wr": 5,
    // "wq": 5,
    // "s_05": {"s": 2, "act": "drop"},
    // "sc": 6,
    // "id": 67615779}}

}
