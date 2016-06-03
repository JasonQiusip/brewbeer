package com.ltbrew.brewbeer.uis.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;

import com.ltbrew.brewbeer.R;
import com.ltbrew.brewbeer.interfaceviews.AddDevView;
import com.ltbrew.brewbeer.presenter.AddDevPresenter;
import com.ltbrew.brewbeer.presenter.model.AddDevResp;
import com.ltbrew.brewbeer.presenter.model.Device;
import com.ltbrew.brewbeer.thirdpartylib.MessageWindow;
import com.ltbrew.brewbeer.thirdpartylib.activity.CaptureActivity;
import com.ltbrew.brewbeer.uis.dialog.SetDevPhoneNumbDialog;
import com.ltbrew.brewbeer.uis.utils.KeyboardUtil;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.OnClick;


public class AddDevActivity extends BaseActivity implements AddDevView {

    private static final int REQUEST_CODE_SCAN_DEV_QR = 11;
    public static final String DEVICES_EXTRA = "devices";
    private AddDevPresenter addDevPresenter;
    private ImageView backIv;
    private SetDevPhoneNumbDialog setDevPhoneNumbDialog;
    private String scanQrCode;
    private MessageWindow messageWindow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_dev);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        backIv = (ImageView) toolbar.findViewById(R.id.backIv);
        backIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        ButterKnife.bind(this);
        addDevPresenter = new AddDevPresenter(this);

    }

    @OnClick(R.id.btn_scan_dev)
    public void startScanDevQr(){
        startActivityForResult(new Intent(this, CaptureActivity.class), REQUEST_CODE_SCAN_DEV_QR);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_SCAN_DEV_QR) {
            if (resultCode == RESULT_OK) {
                scanQrCode = data.getStringExtra("result");
                messageWindow = showMsgWindow("提醒", "正在检测二维码...", null);
                if(scanQrCode != null)
                    addDevPresenter.verifyIot(scanQrCode);

            }
        }
    }
    @Override
    public void onReqIotFailed(String message) {

    }

    @Override
    public void onReqIotSuccess(Integer state) {
        switch (state){
            case 0:
                messageWindow.hidePopupWindow();
                showSetPhoneNumbDialog();
                break;
            case 1:
            case 3:
                if(messageWindow != null)
                    messageWindow.setMessage("正在请求绑定...");
                else
                    messageWindow = showMsgWindow("提醒", "正在请求绑定...", null);
                addDevPresenter.addDev(scanQrCode);
                break;
            case 2:
                showSnackBar("二维码错误");
                break;
        }
    }

    private void showSetPhoneNumbDialog() {
        setDevPhoneNumbDialog = new SetDevPhoneNumbDialog(this);
        setDevPhoneNumbDialog.setOnSetPhoneNumbForDevListener(new SetDevPhoneNumbDialog.OnSetPhoneNumbForDevListener() {
            @Override
            public void onSetPhoneNumb(String phoneNumb) {
                if(scanQrCode != null)
                    addDevPresenter.setPhoneNumb(scanQrCode, phoneNumb);
                setDevPhoneNumbDialog.dismiss();
                messageWindow = showMsgWindow("提醒", "正在设置设备号码...", null);
                KeyboardUtil.hideKeyboard(AddDevActivity.this, backIv);

            }
        });
        setDevPhoneNumbDialog.show();
    }


    @Override
    public void onSetPhoneNumbSuccess() {
        hideMsgWindow();
        messageWindow = showMsgWindow("提醒", "正在请求绑定...", null);
        addDevPresenter.addDev(scanQrCode);
    }

    @Override
    public void onSetPhoneNumbFailed(String message) {
        hideMsgWindow();
        showSnackBar("设置手表号码失败");
    }

//    状态	说明
//    无/0	绑定成功
//    1	绑定过程中，常见于需短信回执的设备，例如pt30
//    2	工厂未登记
//    3	已被其他人绑定
//    4	二维码格式错误
//    5	api_key验证失败，app和帐号不匹配
//    6	app和设备不匹配

    @Override
    public void onReqAddDevSuccess(AddDevResp addDevResp) {
        Integer state = addDevResp.state;
        if(state == null) {
            showMsgWindow("提醒", "您已绑定该设备", null);
            return;
        }
        hideMsgWindow();
        switch (state){
            case 0:
                break;
            case 1:
                messageWindow = showMsgWindow("提醒", "正在绑定设备...", null);
                addDevPresenter.checkDev(addDevResp.id);
                break;
            case 2:
                showMsgWindow("提醒", "工厂未登记", null);
                break;
            case 3:
                showMsgWindow("提醒", "已被其他人绑定", null);
                break;
            case 4:
                showMsgWindow("提醒", "二维码格式错误", null);
                break;
            case 5:
                showMsgWindow("提醒", "api_key验证失败，app和帐号不匹配", null);
                break;
            case 6:
                showMsgWindow("提醒", "app和设备不匹配", null);
                break;
        }
    }

    @Override
    public void onAddDevFailed(String message) {
        showErrorMsg(message);
    }

    @Override
    public void onFoundDevSuccess(ArrayList<Device> devices) {
        hideMsgWindow();
        Intent intent = new Intent();
        intent.putParcelableArrayListExtra(DEVICES_EXTRA, devices);
        intent.setAction(BrewHomeActivity.ADD_DEV_SUCCESS_ACTION);
        sendBroadcast(intent);
        finish();
    }

    @Override
    public void onFoundDevFailed(String msg) {
        hideMsgWindow();
        showErrorMsg(msg);
    }



    private void hideMsgWindow() {
        if(messageWindow != null) {
            messageWindow.hidePopupWindow();
            messageWindow = null;
        }
    }

}
