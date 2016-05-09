package com.ltbrew.brewbeer.presenter;

import android.text.TextUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.ltbrew.brewbeer.api.cssApi.BrewApi;
import com.ltbrew.brewbeer.api.model.HttpResponse;
import com.ltbrew.brewbeer.interfaceviews.BrewSessionVeiw;
import com.ltbrew.brewbeer.presenter.model.BrewHistory;
import com.ltbrew.brewbeer.presenter.util.DeviceUtil;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

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

//    formula_id、begin_time、end_time、package_id、pid、state
    public void getBrewHistory(){
        final String devId = DeviceUtil.getCurrentDevId();
        if(TextUtils.isEmpty(devId)){
            return;
        }
        Observable.create(new Observable.OnSubscribe<String>() {
            @Override
            public void call(Subscriber<? super String> subscriber) {
                String[] date = getTodayUnixTime();
                HttpResponse brewHistory = BrewApi.getBrewHistory(devId, date[1], date[0], "");
                if(brewHistory.isSuccess()){

                    String content = brewHistory.getContent();
                    JSONObject brewHistoryJson = JSON.parseObject(content);
                    Integer state1 = brewHistoryJson.getInteger("state");
                    if(state1 == 1){
                        return;
                    }
                    JSONArray jsonArray = brewHistoryJson.getJSONArray("history");
                    List<BrewHistory> brewingHistoryList = new ArrayList<>();
                    List<BrewHistory> finishedHistoryList = new ArrayList<>();
                    for (int i = 0, size = jsonArray.size(); i < size; i++) {
                        JSONObject jsonObject = jsonArray.getJSONObject(i);
                        Long formula_id = jsonObject.getLong("formula_id");
                        String begin_time = jsonObject.getString("begin_time");
                        String end_time = jsonObject.getString("end_time");
                        Long package_id = jsonObject.getLong("package_id");
                        Long pid = jsonObject.getLong("pid");
                        Integer state = jsonObject.getInteger("state");
                        BrewHistory brewHistoryModel = new BrewHistory();
                        brewHistoryModel.setFormula_id(formula_id);
                        brewHistoryModel.setBegin_time(begin_time);
                        brewHistoryModel.setEnd_time(end_time);
                        brewHistoryModel.setPackage_id(package_id);
                        brewHistoryModel.setPid(pid);
                        brewHistoryModel.setState(state);
                        if(state == 1) {
                            brewingHistoryList.add(brewHistoryModel);
                        }else if(state == 2){
                            finishedHistoryList.add(brewHistoryModel);
                        }else{

                        }
                    }
                    brewSessionView.onGetBrewSessionSuccess(brewingHistoryList, finishedHistoryList);


                }else{
                    brewSessionView.onGetBrewSessionFailed(brewHistory.getCode());
                }
            }
        }).subscribeOn(Schedulers.io()).subscribe();
    }

    private String[] getTodayUnixTime(){
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        String date_begin = year +"-" + String.format("%02d", month + 1) + "-" + String.format("%02d", day);
        calendar.set(Calendar.DAY_OF_MONTH, day - 7);
        int year_begin = calendar.get(Calendar.YEAR);
        int month_begin = calendar.get(Calendar.MONTH);
        int day_begin = calendar.get(Calendar.DAY_OF_MONTH);
        String date_end = year_begin +"-" + String.format("%02d", month_begin + 1) + "-" + String.format("%02d", day_begin);

        String[] dateTime = new String[]{date_begin, date_end};
        return dateTime;

    }

    public void getRecipeInfo(String formula_id){
        recipePresenter.getRecipes(formula_id);
    }
}
