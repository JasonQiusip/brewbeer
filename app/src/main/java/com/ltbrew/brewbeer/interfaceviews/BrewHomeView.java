package com.ltbrew.brewbeer.interfaceviews;

import com.ltbrew.brewbeer.presenter.model.Device;

import java.util.List;

/**
 * Created by 151117a on 2016/5/5.
 */
public interface BrewHomeView {

    void onGetDevsSuccess(List<Device> devices);
    void onGetDevsFailed(String message);
}
