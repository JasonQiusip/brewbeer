package com.ltbrew.brewbeer.presenter;

import android.text.TextUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.ltbrew.brewbeer.api.cssApi.DevApi;
import com.ltbrew.brewbeer.api.model.HttpResponse;
import com.ltbrew.brewbeer.interfaceviews.BrewHomeView;
import com.ltbrew.brewbeer.presenter.model.Device;
import com.ltbrew.brewbeer.presenter.util.DeviceUtil;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.Scheduler;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * Created by 151117a on 2016/5/5.
 */
public class BrewHomePresenter {

    private final BrewHomeView brewHomeView;

    public BrewHomePresenter(BrewHomeView brewHomeView){
        this.brewHomeView = brewHomeView;
    }

    public void getDevs(){
        getDevsOnce().observeOn(AndroidSchedulers.mainThread()).subscribe(new Action1<List<Device>>() {
            @Override
            public void call(List<Device> devices) {
                brewHomeView.onGetDevsSuccess(devices);
            }
        }, new Action1<Throwable>() {
            @Override
            public void call(Throwable throwable) {
                brewHomeView.onGetDevsFailed(throwable.getMessage());
            }
        });
    }

    public Observable<List<Device>> getDevsOnce() {
        return Observable.create(new Observable.OnSubscribe<List<Device>>() {
            @Override
            public void call(Subscriber<? super List<Device>> subscriber) {
                HttpResponse devsResp = DevApi.getDevs();
                if(devsResp.isSuccess()){
                    String content = devsResp.getContent();
                    if(content.equals("[]")) {
                        DeviceUtil.storeCurrentDevId("");
                        return;
                    }
                    ArrayList<Device> devices = new ArrayList();
                    parseDevsResp(content, devices);
                    subscriber.onNext(devices);
                }else{
                    subscriber.onError(new Throwable(""+devsResp.getCode()));
                }
            }
        }).subscribeOn(Schedulers.io());
    }

    private void parseDevsResp(String content, ArrayList<Device> devices) {
        JSONArray jsonArray = JSON.parseArray(content);
        boolean findDev = false;
        for (int i = 0, size = jsonArray.size(); i < size; i++) {
            JSONObject jsonObject = jsonArray.getJSONObject(i);
            String id = jsonObject.getString("id");
            int p = jsonObject.getInteger("p");
            Device device = new Device();
            device.setId(id);
            device.setP(p);
            devices.add(device);
            if(id.equals(DeviceUtil.getCurrentDevId())){
                findDev = true;
            }
        }
        if(!findDev) {
            DeviceUtil.storeCurrentDevId(devices.get(0).getId());
        }
    }
}
