package com.ltbrew.brewbeer.presenter;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.ltbrew.brewbeer.api.cssApi.DevApi;
import com.ltbrew.brewbeer.api.model.HttpResponse;
import com.ltbrew.brewbeer.interfaceviews.BrewHomeView;
import com.ltbrew.brewbeer.presenter.model.Device;
import com.ltbrew.brewbeer.presenter.util.DeviceUtil;
import com.ltbrew.brewbeer.presenter.util.RxUtil;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;
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
                throwable.printStackTrace();
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
                    ArrayList<Device> devices = new ArrayList();
                    if(content.equals("[]")) { //没有获取到设备置空
                        DeviceUtil.storeCurrentDevId("");
                        subscriber.onNext(devices);
                        return;
                    }
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


    public void unbindDev(){
        RxUtil.createWithIntResp(new Observable.OnSubscribe<Integer>() {
            @Override
            public void call(Subscriber<? super Integer> subscriber) {
                String currentDevId = DeviceUtil.getCurrentDevId();
                HttpResponse httpResponse = DevApi.unbindDev(currentDevId);
                if(httpResponse.isSuccess()){
                    if(!"".equals(httpResponse.getContent())) {
                        JSONObject jsonObject = JSON.parseObject(httpResponse.getContent());
                        Integer state = jsonObject.getInteger("state");
                        subscriber.onNext(state);
                        return;
                    }
                    subscriber.onNext(0);
                }else{
                    subscriber.onError(new Throwable(httpResponse.getCode()+""));
                }
            }
        }).subscribe(new Action1<Integer>() {
            @Override
            public void call(Integer state) {
                brewHomeView.onReqDeleteDevSuccess(state);
            }
        }, new Action1<Throwable>() {
            @Override
            public void call(Throwable throwable) {
                brewHomeView.onDeleteDevFailed(throwable.getMessage());

            }
        });
    }
}
