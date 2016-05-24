package com.ltbrew.brewbeer.presenter;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.ltbrew.brewbeer.api.model.HttpResponse;
import com.ltbrew.brewbeer.api.ssoApi.LoginApi;
import com.ltbrew.brewbeer.interfaceviews.LoginView;
import com.ltbrew.brewbeer.presenter.util.RxUtil;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * Created by 151117a on 2016/5/2.
 */
public class LoginPresenter {

    private final LoginView loginView;

    public LoginPresenter(LoginView loginView){
        this.loginView = loginView;
    }

    public void checkAccount(final String username){
        RxUtil.create(new Observable.OnSubscribe<String>() {
            @Override
            public void call(Subscriber<? super String> subscriber) {
                HttpResponse httpResponse = LoginApi.checkAccount(username);
                if (httpResponse.isSuccess()) {
                    String content = httpResponse.getContent();
                    JSONObject stateJson = JSON.parseObject(content);
                    String state = stateJson.getString("state");
                    subscriber.onNext(state);
                } else {
                    subscriber.onError(new Throwable("" + httpResponse.getCode()));
                }
            }
        }).subscribe(new Action1<String>() {
            @Override
            public void call(String state) {
                loginView.onCheckSuccess(state);
            }
        }, new Action1<Throwable>() {
            @Override
            public void call(Throwable throwable) {
                loginView.onCheckFailed(throwable.getMessage());
            }
        });
    }

    public void signIn(final String username, final String pwd){
        RxUtil.create(new Observable.OnSubscribe<String>() {
            @Override
            public void call(Subscriber<? super String> subscriber) {
                HttpResponse httpResponse = LoginApi.loginAsync(username, pwd);
                if(httpResponse.isSuccess()){
                    String content = httpResponse.getContent();
                    subscriber.onNext(content);
                }else{
                    subscriber.onError(new Throwable(""+httpResponse.getCode()));
                }
            }
        }).subscribe(new Action1<String>() {
            @Override
            public void call(String s) {
                loginView.onLoginSuccess();
            }
        }, new Action1<Throwable>() {
            @Override
            public void call(Throwable throwable) {
                throwable.printStackTrace();
                loginView.onLoginFailed(throwable.getMessage());
            }
        });
    }
}
