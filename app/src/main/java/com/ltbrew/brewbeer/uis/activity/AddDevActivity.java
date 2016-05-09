package com.ltbrew.brewbeer.uis.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;

import com.ltbrew.brewbeer.R;
import com.ltbrew.brewbeer.interfaceviews.AddDevView;
import com.ltbrew.brewbeer.presenter.AddDevPresenter;
import com.ltbrew.brewbeer.thirdpartylib.activity.CaptureActivity;

import butterknife.ButterKnife;
import butterknife.OnClick;


public class AddDevActivity extends BaseActivity implements AddDevView {

    private static final int REQUEST_CODE_SCAN_DEV_QR = 11;
    private AddDevPresenter addDevPresenter;
    private ImageView backIv;

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
                String scanQrCode = data.getStringExtra("result");
                addDevPresenter.addDev(scanQrCode);
            }
        }
    }

    @Override
    public void onReqAddDevSuccess(String state) {

    }

    @Override
    public void onAddDevFailed(String message) {

    }
}
