package com.ltbrew.brewbeer.presenter;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.ltbrew.brewbeer.api.cssApi.DevApi;
import com.ltbrew.brewbeer.api.model.HttpResponse;
import com.ltbrew.brewbeer.interfaceviews.AddDevView;
import com.ltbrew.brewbeer.presenter.model.AddDevResp;
import com.ltbrew.brewbeer.presenter.model.Device;
import com.ltbrew.brewbeer.presenter.util.DeviceUtil;
import com.ltbrew.brewbeer.presenter.util.RxUtil;
import com.ltbrew.brewbeer.uis.activity.AddDevActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * Created by 151117a on 2016/5/2.
 */
public class AddDevPresenter {

    private final AddDevView addDevView;
    private int retry;
    private Subscription subscribe;

    public AddDevPresenter(AddDevView addDevView) {
        this.addDevView = addDevView;
    }

    public void setPhoneNumb(final String qrCode, final String phoneNumb){
        RxUtil.create(new Observable.OnSubscribe<String>() {
            @Override
            public void call(Subscriber<? super String> subscriber) {
                HttpResponse httpResponse = DevApi.setPhoneNo2Dev(qrCode, phoneNumb);
                if(httpResponse.isSuccess()){
                    String content = httpResponse.getContent();
                }else{
                    subscriber.onError(new Throwable(""+httpResponse.getCode()));
                }
            }
        }).subscribe(new Action1<String>() {
            @Override
            public void call(String state) {
                addDevView.onSetPhoneNumbSuccess();
            }
        }, new Action1<Throwable>() {
            @Override
            public void call(Throwable throwable) {
                addDevView.onSetPhoneNumbFailed(throwable.getMessage());
            }
        });
    }

    public void addDev(final String qrCode){
        Observable.create(new Observable.OnSubscribe<AddDevResp>() {
            @Override
            public void call(Subscriber<? super AddDevResp> subscriber) {
                HttpResponse httpResponse = DevApi.bindDev(qrCode);
                if(httpResponse.isSuccess()){
                    String content = httpResponse.getContent();
                    JSONObject jsonObject = JSON.parseObject(content);
                    Integer state = jsonObject.getInteger("state");
                    String id = jsonObject.getString("id");
                    AddDevResp addDevResp = new AddDevResp();
                    addDevResp.id = id;
                    addDevResp.state = state;
                    subscriber.onNext(addDevResp);
                }else{
                    subscriber.onError(new Throwable(""+httpResponse.getCode()));
                }
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new Action1<AddDevResp>() {
            @Override
            public void call(AddDevResp addDevResp) {
                addDevView.onReqAddDevSuccess(addDevResp);
            }
        }, new Action1<Throwable>() {
            @Override
            public void call(Throwable throwable) {
                addDevView.onAddDevFailed(throwable.getMessage());
            }
        });
    }

    public void checkDev(final String id) {
        retry = 6;
        subscribe = Observable.interval(20, 15, TimeUnit.SECONDS).flatMap(new Func1<Long, Observable<ArrayList<Device>>>() {
            @Override
            public Observable<ArrayList<Device>> call(Long aLong) {
                return getDevsOnce(id);
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new Subscriber<ArrayList<Device>>() {
            @Override
            public void onCompleted() {
                retry --;
                if(retry < 0){
                    subscribe.unsubscribe();
                }
            }

            @Override
            public void onError(Throwable e) {
                addDevView.onFoundDevFailed(e.getMessage());
                subscribe.unsubscribe();
            }

            @Override
            public void onNext(ArrayList<Device> devices) {
                DeviceUtil.storeCurrentDevId(id);
                addDevView.onFoundDevSuccess(devices);
                subscribe.unsubscribe();
            }
        });
    }

    public Observable<ArrayList<Device>> getDevsOnce(final String id) {
        return Observable.create(new Observable.OnSubscribe<ArrayList<Device>>() {
            @Override
            public void call(Subscriber<? super ArrayList<Device>> subscriber) {
                HttpResponse devsResp = DevApi.getDevs();
                if(devsResp.isSuccess()){
                    String content = devsResp.getContent();
                    if(!content.contains(id)){
                        subscriber.onCompleted();
                    }else {
                        ArrayList<Device> devices = new ArrayList();
                        subscriber.onNext(devices);
                    }
                }else{
                    subscriber.onError(new Throwable(""+devsResp.getCode()));
                }
            }
        }).subscribeOn(Schedulers.io());
    }
}
