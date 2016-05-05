package com.ltbrew.brewbeer.interfaceviews;

/**
 * Created by 151117a on 2016/5/5.
 */
public interface RegisterView {

    void onReqRegCodeSuccess();
    void onReqRegCodeFailed(String message);

    void onRegReqSuccess(String state);
    void onRegFailed(String message);

}
