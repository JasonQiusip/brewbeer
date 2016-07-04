package com.ltbrew.brewbeer.presenter;

import android.text.TextUtils;
import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.ltbrew.brewbeer.api.cssApi.BrewApi;
import com.ltbrew.brewbeer.api.model.HttpResponse;
import com.ltbrew.brewbeer.interfaceviews.BrewSessionVeiw;
import com.ltbrew.brewbeer.persistence.greendao.DBBrewHistory;
import com.ltbrew.brewbeer.persistence.greendao.DBBrewHistoryDao;
import com.ltbrew.brewbeer.persistence.greendao.DBRecipe;
import com.ltbrew.brewbeer.persistence.greendao.DBRecipeDao;
import com.ltbrew.brewbeer.presenter.model.Recipe;
import com.ltbrew.brewbeer.presenter.util.DBManager;
import com.ltbrew.brewbeer.presenter.util.DeviceUtil;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import de.greenrobot.dao.query.QueryBuilder;
import rx.Observable;
import rx.Subscriber;
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

//    formula_id、begin_time、end_time、package_id、pid、state
    public void getBrewHistory(){
        final String devId = DeviceUtil.getCurrentDevId();
        if(TextUtils.isEmpty(devId)){
            return;
        }
        Observable.create(new Observable.OnSubscribe<String>() {
            @Override
            public void call(Subscriber<? super String> subscriber) {
                List<DBBrewHistory> brewingHistoryList = new ArrayList<>();
                List<DBBrewHistory> fermentingHistoryList = new ArrayList<>();
                List<DBBrewHistory> suspendHistoryList = new ArrayList<>();
                fermentingHistoryList = getBrewHistoriesFromDB(2);
                suspendHistoryList = getBrewHistoriesFromDB(3);
                brewSessionView.onGetFinishedSession(getBrewHistoriesFromDB(4));
                brewSessionView.onGetSuspendSession(suspendHistoryList);
                String[] date = getTodayUnixTime();
                HttpResponse brewHistory = BrewApi.getBrewHistory(devId, date[1], date[0], "");

                if(brewHistory.isSuccess()){

                    String content = brewHistory.getContent();
                    JSONArray jsonArray = JSON.parseArray(content);

                    if(jsonArray == null) {
//                        brewSessionView.onGetBrewSessionSuccess(brewingHistoryList, fermentingHistoryList);
                        return;
                    }
                    for (int i = 0, size = jsonArray.size(); i < size; i++) {
                        JSONObject jsonObject = jsonArray.getJSONObject(i);
                        String formula_id = jsonObject.getString("formula_id");
                        long formula_id_long = Long.parseLong(formula_id, 16);
                        String begin_time = jsonObject.getString("begin");
                        String end_time = jsonObject.getString("end");
                        Long package_id = jsonObject.getLong("pack_id");
                        Integer pid = jsonObject.getInteger("pid");
                        Integer state = jsonObject.getInteger("state");
                        DBBrewHistory brewHistoryModel = new DBBrewHistory();
                        brewHistoryModel.setFormula_id(formula_id_long);
                        brewHistoryModel.setBegin_time(begin_time);
                        brewHistoryModel.setEnd_time(end_time);
                        brewHistoryModel.setPackage_id(package_id);
                        brewHistoryModel.setPid(devId);
                        brewHistoryModel.setState(state);
//                        String formulaId = String.format("%08x", formula_id);
                        List<Recipe> recipesSync = recipePresenter.getRecipesSync(formula_id);
                        DBRecipe dbRecipe = recipePresenter.downloadRecipeSync(devId, recipesSync.get(0));
                        if(dbRecipe == null){
                            continue;
                        }
                        brewHistoryModel.setDBRecipe(dbRecipe);
                        DBManager.getInstance().getDBBrewHistoryDao().insertOrReplace(brewHistoryModel);
                        if(state == 0 || state == 1) {
                            brewingHistoryList.add(brewHistoryModel);
                        }else if(state == 2 ){
                            fermentingHistoryList.add(brewHistoryModel);
                        }else{
                            suspendHistoryList.add(brewHistoryModel);
                            brewSessionView.onGetSuspendSession(suspendHistoryList);
                        }
                    }
                    brewSessionView.onGetBrewSessionSuccess(brewingHistoryList, fermentingHistoryList);


                }else{
                    brewSessionView.onGetBrewSessionFailed(brewHistory.getCode()+"");
                }
            }
        }).subscribeOn(Schedulers.io()).subscribe(new Action1<String>() {
            @Override
            public void call(String s) {

            }
        }, new Action1<Throwable>() {
            @Override
            public void call(Throwable throwable) {
                throwable.printStackTrace();
            }
        });
    }

    private String[] getTodayUnixTime(){
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        String date_end = year +"-" + String.format("%02d", month + 1) + "-" + String.format("%02d", day);
        calendar.set(Calendar.DAY_OF_MONTH, day - 14);
        int year_begin = calendar.get(Calendar.YEAR);
        int month_begin = calendar.get(Calendar.MONTH);
        int day_begin = calendar.get(Calendar.DAY_OF_MONTH);
        String date_begin = year_begin +"-" + String.format("%02d", month_begin + 1) + "-" + String.format("%02d", day_begin);
        date_begin += "T00:00:00";
        date_end += "T23:59:59";
        String[] dateTime = new String[]{date_end, date_begin};
        return dateTime;

    }

    public List<DBBrewHistory> getBrewHistoriesFromDB(int state){
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        long ts = 0;
        try {
            Date date = simpleDateFormat.parse(year + "-" + (month + 1)+ "-" + day + " 00:00:00");
            long time = date.getTime();
            ts = time/1000;
        } catch (ParseException e) {
            e.printStackTrace();
        }
        QueryBuilder<DBBrewHistory> dbBrewHistoryQueryBuilder = DBManager.getInstance().getDBBrewHistoryDao().queryBuilder();
        return dbBrewHistoryQueryBuilder.where(
                DBBrewHistoryDao.Properties.State.eq(state),
                DBBrewHistoryDao.Properties.Begin_time.le(ts),
                DBBrewHistoryDao.Properties.Pid.eq(DeviceUtil.getCurrentDevId())).build().list();
    }


    public void getRecipeAfterBrewBegin(String formula_id){
        recipePresenter.getRecipes(formula_id);
        recipePresenter.setShowResultOnSeperateCb(true);
    }

    public void getRecipeInfo(String formula_id){
//        if(checkLocalDb(formula_id)){
//            return;
//        }
        recipePresenter.getRecipes(formula_id);
    }

    private boolean checkLocalDb( String fn) {
        List<DBRecipe> list = DBManager.getInstance().getDBRecipeDao().queryBuilder().where(DBRecipeDao.Properties.IdForFn.eq(fn)).list();
        Log.e("recipePresenter", list.size()+"");
        if(list != null && list.size() != 0) {
            DBRecipe dbRecipe = list.get(0);
            dbRecipe.__setDaoSession(DBManager.getInstance().getDaoSession());
            dbRecipe.getBrewSteps();
            dbRecipe.getSlots();
            brewSessionView.onDownloadRecipeSuccess(dbRecipe);
            return true;
        }
        return false;
    }
}
