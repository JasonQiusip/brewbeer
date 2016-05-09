package com.ltbrew.brewbeer.presenter;

import com.ltbrew.brewbeer.api.cssApi.DevApi;
import com.ltbrew.brewbeer.api.model.HttpResponse;
import com.ltbrew.brewbeer.interfaceviews.AddDevView;
import com.ltbrew.brewbeer.presenter.util.RxUtil;
import com.ltbrew.brewbeer.uis.activity.AddDevActivity;

import rx.Observable;
import rx.Subscriber;
import rx.functions.Action1;

/**
 * Created by 151117a on 2016/5/2.
 */
public class AddDevPresenter {

    private final AddDevView addDevView;

    public AddDevPresenter(AddDevView addDevView) {
        this.addDevView = addDevView;
    }

    public void addDev(final String qrCode){
        RxUtil.create(new Observable.OnSubscribe<String>() {
            @Override
            public void call(Subscriber<? super String> subscriber) {
                HttpResponse httpResponse = DevApi.bindDev(qrCode);
                if(httpResponse.isSuccess()){
                    String content = httpResponse.getContent();
                }else{
                    subscriber.onError(new Throwable(""+httpResponse.getCode()));
                }
            }
        }).subscribe(new Action1<String>() {
            @Override
            public void call(String state) {
                addDevView.onReqAddDevSuccess(state);
            }
        }, new Action1<Throwable>() {
            @Override
            public void call(Throwable throwable) {
                addDevView.onAddDevFailed(throwable.getMessage());
            }
        });
    }
}
