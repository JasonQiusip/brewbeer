package com.ltbrew.brewbeer.presenter;

import android.text.TextUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.ltbrew.brewbeer.api.cssApi.BrewApi;
import com.ltbrew.brewbeer.api.model.HttpResponse;
import com.ltbrew.brewbeer.interfaceviews.AddPackView;
import com.ltbrew.brewbeer.presenter.util.DeviceUtil;
import com.ltbrew.brewbeer.presenter.util.RxUtil;

import rx.Observable;
import rx.Subscriber;
import rx.functions.Action1;

/**
 * Created by 151117a on 2016/5/2.
 */
public class AddPackPresenter {

    private final AddPackView addPackView;

    public AddPackPresenter(AddPackView addPackView){
        this.addPackView = addPackView;
    }
//    参数
//    字段	说明
//    pack_id	原料包id。17位长10进制字符串，如：30645782265103869
//    is_open	是否开启设备。0:不开启，只查询原料包。1:开启设备
//            应答
//
//    字段	说明
//    state	返回状态
//    formula_id	原料包对应的配方id
//    name	原料包名称
//    state说明
//
//    0	原料包有效
//    1	原料包已经被使用，无效
//    2	is_open参数错误

    public void addPackToDev(final String pack_id, final String is_open){
        final String devId = DeviceUtil.getCurrentDevId();
        if(TextUtils.isEmpty(devId))
            return;
        RxUtil.create(new Observable.OnSubscribe<String>() {
            @Override
            public void call(Subscriber<? super String> subscriber) {
                HttpResponse httpResponse = BrewApi.beginBrew(devId, pack_id, is_open);
                if(httpResponse.isSuccess()){
                    String content = httpResponse.getContent();
                    subscriber.onNext(content);

                }else{
                    subscriber.onError(new Throwable(""+httpResponse.getCode()));
                }
            }
        }).subscribe(new Action1<String>() {
            @Override
            public void call(String content) {
                JSONObject jsonObject = JSON.parseObject(content);
                Integer state = jsonObject.getInteger("state");
                String formula_id = jsonObject.getString("formula_id");
                String name = jsonObject.getString("name");
                addPackView.onAddRecipeToDevSuccess(state, formula_id, name);
            }
        }, new Action1<Throwable>() {
            @Override
            public void call(Throwable throwable) {
                addPackView.onAddRecipeToDevFailed(throwable.getMessage());
            }
        });
    }
}
