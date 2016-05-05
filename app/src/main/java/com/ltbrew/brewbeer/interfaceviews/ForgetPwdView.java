package com.ltbrew.brewbeer.interfaceviews;

/**
 * Created by 151117a on 2016/5/5.
 */
public interface ForgetPwdView {
    void onReqNewPwdSuccess(String state);
    void onReqNewPwdFailed(String msg);
    void onSetNewPwdSuccess(String state);
    void onSetNewPwdFailed(String message);
}
