package com.ltbrew.brewbeer.presenter;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.ltbrew.brewbeer.api.model.HttpResponse;
import com.ltbrew.brewbeer.api.ssoApi.RegisterApi;
import com.ltbrew.brewbeer.interfaceviews.ForgetPwdView;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * Created by 151117a on 2016/5/2.
 */
public class ForgetPwdPresenter {

    private final ForgetPwdView forgetPwdView;

    public ForgetPwdPresenter(ForgetPwdView forgetPwdView){
        this.forgetPwdView = forgetPwdView;
    }

    public void pwdLost(final String username){
        Observable.create(new Observable.OnSubscribe<String>() {
            @Override
            public void call(Subscriber<? super String> subscriber) {
                HttpResponse httpResponse = RegisterApi.reqPwdLost(username);
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
                forgetPwdView.onReqNewPwdSuccess(state);
            }
        }, new Action1<Throwable>() {
            @Override
            public void call(Throwable throwable) {
                forgetPwdView.onReqNewPwdFailed(throwable.getMessage());
            }
        });
    }

    public void setNewPwd(final String username, final String pwd, final String code){
        Observable.create(new Observable.OnSubscribe<String>() {
            @Override
            public void call(Subscriber<? super String> subscriber) {
                HttpResponse httpResponse = RegisterApi.setNewPwd(username, pwd, code);
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
                forgetPwdView.onSetNewPwdSuccess(state);
            }
        }, new Action1<Throwable>() {
            @Override
            public void call(Throwable throwable) {
                forgetPwdView.onSetNewPwdFailed(throwable.getMessage());
            }
        });
    }
}
