package com.ltbrew.brewbeer.presenter;

import rx.Observable;
import rx.Scheduler;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by 151117a on 2016/5/4.
 */
public class RxUtil {

    public static Observable<String> create(Observable.OnSubscribe<String> onSubscribe){
        return Observable.create(onSubscribe).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }
}
