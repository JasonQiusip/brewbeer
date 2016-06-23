package com.ltbrew.brewbeer.uis.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.ltbrew.brewbeer.R;
import com.ltbrew.brewbeer.api.cssApi.DevApi;
import com.ltbrew.brewbeer.api.model.HttpResponse;
import com.ltbrew.brewbeer.presenter.util.RxUtil;
import com.ltbrew.brewbeer.uis.utils.KeyboardUtil;

import java.util.Timer;
import java.util.TimerTask;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

public class FollowActivity extends BaseActivity {

    @BindView(R.id.tv_msg)
    TextView tvMsg;
    @BindView(R.id.et_verify_code)
    EditText etVerifyCode;
    @BindView(R.id.tv_get_verify_code)
    Button tvGetVerifyCode;
    @BindView(R.id.next)
    Button next;
    private String qrCode;
    private String pid;
    private String admin;
    private Timer reGetCodeTimer;
    private int resetTime = 120;
    private String akey;
    private String devId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_follow);
        qrCode = getIntent().getStringExtra("qrCode");
        devId = getIntent().getStringExtra("pid");
        akey = getIntent().getStringExtra("akey");
        ButterKnife.bind(this);
    }
//    0	成功，同时返回值里包含id（表示pid），account(主帐号)
//    1	邮箱格式不对
//    2	qr码解析失败
//    3	设备未被主帐号绑定
//    4	已是主帐号
//    5	子帐号数量已到上限
//    6	子帐号电话号码格式不对
//    7	主帐号电话号码格式不对
//    8	已是子帐号
//    9	api_key验证不通过，用户帐号和api_key不匹配
//    10	app和设备不匹配

    @OnClick(R.id.tv_get_verify_code)
    public void getFollowCode(){
        tvGetVerifyCode.setEnabled(false);
        setTimer();
        Observable.create(new Observable.OnSubscribe<FollowReqResp>() {
            @Override
            public void call(Subscriber<? super FollowReqResp> subscriber) {
                if(qrCode != null || (devId != null && akey != null)) {
                    HttpResponse httpResponse = DevApi.followDev(qrCode, devId, akey);
                    if(httpResponse.isSuccess()){
                        String content = httpResponse.getContent();
                        JSONObject jsonObject = JSON.parseObject(content);
                        FollowReqResp followReqResp = new FollowReqResp();
                        followReqResp.state = jsonObject.getInteger("state");
                        followReqResp.id = jsonObject.getString("id");
                        followReqResp.account = jsonObject.getString("account");
                        followReqResp.mobile = jsonObject.getString("mobile");
                        subscriber.onNext(followReqResp);

                    }else{
                        subscriber.onError(new Throwable(httpResponse.getCode()+""));
                    }
                }
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new Action1<FollowReqResp>() {
            @Override
            public void call(FollowReqResp mFollowReqResp) {
                switch (mFollowReqResp.state){
                    case 0:
                        showSnackBar("获取验证码成功");
                        tvMsg.setText("请向管理员（"+mFollowReqResp.mobile+"）索取验证码");
                        pid = mFollowReqResp.id;
                        admin = mFollowReqResp.mobile;
                        break;
                    case 1:
                        showSnackBar("邮箱格式不对");
                        break;
                    case 2:
                        showSnackBar("qr码解析失败");
                        break;
                    case 3:
                        showSnackBar("设备未被主帐号绑定");
                        break;
                    case 4:
                        showSnackBar("已是主帐号");
                        break;
                    case 5:
                        showSnackBar("子帐号数量已到上限");
                        break;
                    case 6:
                        showSnackBar("子帐号电话号码格式不对");
                        break;
                    case 7:
                        showSnackBar("主帐号电话号码格式不对");
                        break;
                    case 8:
                        showSnackBar("已是子帐号");
                        break;
                    case 9:
                        showSnackBar("api_key验证不通过，用户帐号和api_key不匹配");
                        break;
                    case 10:
                        showSnackBar("app和设备不匹配");
                        break;

                }
            }
        }, new Action1<Throwable>() {
            @Override
            public void call(Throwable throwable) {
                showErrorMsg(throwable.getMessage());
            }
        });
    }


    // 120s计时器
    private void setTimer() {
        reGetCodeTimer = new Timer();
        reGetCodeTimer.schedule(new TimerTask() {

            @Override
            public void run() {
                runOnUiThread(new Runnable() {

                    public void run() {
                        if (resetTime == 0) {
                            tvGetVerifyCode.setEnabled(true);
                            tvGetVerifyCode.setClickable(true);
                            tvGetVerifyCode.setText("重新获取");
                            if(reGetCodeTimer != null)
                                reGetCodeTimer.cancel();
                            resetTime = 120;
                        } else
                            tvGetVerifyCode.setText(String.format("%d s", resetTime--));
                    }
                });
            }
        }, 0, 1000);
    }

//    1	子帐号邮箱格式
//    2	主帐号邮箱格式
//    3	关注验证码格式不对
//    4	设备不为主帐号所有
//    5	验证码不一致

    @OnClick(R.id.next)
    public void verifyFollowCode(){
        KeyboardUtil.hideKeyboard(this,etVerifyCode);
        final String valCode = etVerifyCode.getText().toString();
        if(TextUtils.isEmpty(valCode)){
            showSnackBar("验证码为空, 请确认");
            return;
        }
        if(pid == null || admin == null) {
            showSnackBar("请确认您是否有请求验证码？");
            return;
        }
        Observable.create(new Observable.OnSubscribe<Integer>() {
            @Override
            public void call(Subscriber<? super Integer> subscriber) {

                HttpResponse httpResponse = DevApi.checkFollow(pid, admin, valCode);
                if(httpResponse.isSuccess()){
                    String content = httpResponse.getContent();
                    JSONObject jsonObject = JSON.parseObject(content);
                    Integer state = jsonObject.getInteger("state");
                    subscriber.onNext(state);
                }else{
                    subscriber.onError(new Throwable(httpResponse.getCode()+""));
                }
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new Action1<Integer>() {
            @Override
            public void call(Integer state) {
                switch (state){
                    case 0:
                        Intent intent = new Intent(FollowActivity.this, BrewHomeActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.putExtra("from", "afaterFollowSuccess");
                        startActivity(intent);
                        break;
                    case 1:
                        showSnackBar("子帐号邮箱格式");
                        break;
                    case 2:
                        showSnackBar("主帐号邮箱格式");
                        break;
                    case 3:
                        showSnackBar("关注验证码格式不对");
                        break;
                    case 4:
                        showSnackBar("设备不为主帐号所有");
                        break;
                    case 5:
                        showSnackBar("验证码不一致");
                        break;
                }
            }
        }, new Action1<Throwable>() {
            @Override
            public void call(Throwable throwable) {
                showErrorMsg(throwable.getMessage());
            }
        });
    }

    public class FollowReqResp{
        int state;
        String id;
        String account;
        String mobile;

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(reGetCodeTimer != null)
            reGetCodeTimer.cancel();
    }
}
