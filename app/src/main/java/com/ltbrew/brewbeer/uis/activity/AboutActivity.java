package com.ltbrew.brewbeer.uis.activity;

import android.os.Bundle;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.ltbrew.brewbeer.R;
import com.ltbrew.brewbeer.api.cssApi.DevApi;
import com.ltbrew.brewbeer.api.model.HttpResponse;
import com.ltbrew.brewbeer.presenter.util.DeviceUtil;
import com.ltbrew.brewbeer.presenter.util.RxUtil;

import butterknife.BindView;
import butterknife.ButterKnife;
import rx.Observable;
import rx.Subscriber;
import rx.functions.Action1;

public class AboutActivity extends BaseActivity {

    @BindView(R.id.appVerTv)
    TextView appVerTv;
    @BindView(R.id.qscVerTv)
    TextView qscVerTv;
    @BindView(R.id.wifiVerTv)
    TextView wifiVerTv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        ButterKnife.bind(this);
        initToolbar();

        RxUtil.create(new Observable.OnSubscribe<String>() {
            @Override
            public void call(Subscriber<? super String> subscriber) {
                HttpResponse devInfoResp = DevApi.getDevInfo(DeviceUtil.getCurrentDevId());
                if (devInfoResp.isSuccess()) {
                    String content = devInfoResp.getContent();
                    subscriber.onNext(content);
                } else {
                    subscriber.onError(new Throwable(devInfoResp.getCode() + ""));
                }
            }
        }).subscribe(new Action1<String>() {
            @Override
            public void call(String s) {
                JSONObject jsonObject = JSON.parseObject(s);
                String qsc_ver = jsonObject.getString("qsc_ver");
                String wifi_ver = jsonObject.getString("wifi_ver");
                qscVerTv.setText(qsc_ver);
                wifiVerTv.setText(wifi_ver);

            }
        }, new Action1<Throwable>() {
            @Override
            public void call(Throwable throwable) {
                showSnackBar("获取设备信息失败");
            }
        });
    }
}
