package com.ltbrew.brewbeer.presenter;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.ltbrew.brewbeer.api.model.HttpResponse;
import com.ltbrew.brewbeer.api.ssoApi.RegisterApi;
import com.ltbrew.brewbeer.interfaceviews.RegisterView;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * Created by 151117a on 2016/5/2.
 */
public class RegisterPresenter {

    private final RegisterView registerView;

    public RegisterPresenter(RegisterView registerView){
        this.registerView = registerView;
    }

    public void reqRegCode(final String username){
        Observable.create(new Observable.OnSubscribe<String>() {
            @Override
            public void call(Subscriber<? super String> subscriber) {
                HttpResponse httpResponse = RegisterApi.reqRegCode(username);
                if(httpResponse.isSuccess()){
                    String content = httpResponse.getContent();
                    subscriber.onNext(content);
                }else{
                    subscriber.onError(new Throwable(httpResponse.getCode()+""));
                }
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new Action1<String>() {
            @Override
            public void call(String s) {
                registerView.onReqRegCodeSuccess();
            }
        }, new Action1<Throwable>() {
            @Override
            public void call(Throwable throwable) {
                registerView.onReqRegCodeFailed(throwable.getMessage());
            }
        });
    }

    public void register(final String username, final String pwd, final String code){
        Observable.create(new Observable.OnSubscribe<String>() {
            @Override
            public void call(Subscriber<? super String> subscriber) {
                HttpResponse httpResponse = RegisterApi.register(username, pwd, code);
                if(httpResponse.isSuccess()){
                    String content = httpResponse.getContent();
                    JSONObject jsonObject = JSON.parseObject(content);
                    String state = jsonObject.getString("state");
                    subscriber.onNext(state);
                }else{
                    subscriber.onError(new Throwable(httpResponse.getCode()+""));
                }
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new Action1<String>() {
            @Override
            public void call(String state) {
                registerView.onRegReqSuccess(state);

            }
        }, new Action1<Throwable>() {
            @Override
            public void call(Throwable throwable) {
                registerView.onRegFailed(throwable.getMessage());
            }
        });
    }
}
