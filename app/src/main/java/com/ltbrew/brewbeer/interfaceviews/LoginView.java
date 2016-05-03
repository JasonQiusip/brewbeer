package com.ltbrew.brewbeer.interfaceviews;

/**
 * Created by 151117a on 2016/5/3.
 */
public interface LoginView {

    void onCheckSuccess(String state);
    void onCheckFailed(String msg);
}
