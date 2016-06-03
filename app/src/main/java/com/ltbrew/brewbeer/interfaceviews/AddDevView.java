package com.ltbrew.brewbeer.interfaceviews;

import com.ltbrew.brewbeer.presenter.model.AddDevResp;
import com.ltbrew.brewbeer.presenter.model.Device;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by qiusiping on 16/5/7.
 */
public interface AddDevView{

    void onReqAddDevSuccess(AddDevResp state);
    void onAddDevFailed(String message);

    void onSetPhoneNumbSuccess();
    void onSetPhoneNumbFailed(String message);

    void onFoundDevSuccess(ArrayList<Device> devices);
    void onFoundDevFailed(String msg);

    void onReqIotFailed(String message);

    void onReqIotSuccess(Integer state);
}
