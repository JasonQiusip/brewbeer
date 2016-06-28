package com.ltbrew.brewbeer.uis.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;

import com.ltbrew.brewbeer.R;
import com.ltbrew.brewbeer.interfaceviews.AddDevView;
import com.ltbrew.brewbeer.presenter.AddDevPresenter;
import com.ltbrew.brewbeer.presenter.model.AddDevResp;
import com.ltbrew.brewbeer.presenter.model.Device;
import com.ltbrew.brewbeer.thirdpartylib.MessageWindow;
import com.ltbrew.brewbeer.thirdpartylib.activity.CaptureActivity;
import com.ltbrew.brewbeer.uis.dialog.NoticeDialog;
import com.ltbrew.brewbeer.uis.dialog.OnNegativeButtonClickListener;
import com.ltbrew.brewbeer.uis.dialog.OnPositiveButtonClickListener;
import com.ltbrew.brewbeer.uis.dialog.SetDevPhoneNumbDialog;
import com.ltbrew.brewbeer.uis.fragment.AddDevByIdFragment;
import com.ltbrew.brewbeer.uis.fragment.AddDevByQrFragment;
import com.ltbrew.brewbeer.uis.utils.KeyboardUtil;
import com.ltbrew.brewbeer.uis.view.CenterDrawableButton;
import com.ltbrew.brewbeer.uis.view.OnAddDevActionListener;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnCheckedChanged;
import butterknife.OnClick;


public class AddDevActivity extends BaseActivity implements AddDevView, OnAddDevActionListener {

    private static final int REQUEST_CODE_SCAN_DEV_QR = 11;
    public static final String DEVICES_EXTRA = "devices";
    private static final int CAMERA = 12;
    @BindView(R.id.backIv)
    ImageView backIv;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.brewHomeContainer)
    ViewPager brewHomeContainer;
    @BindView(R.id.scanRb)
    CenterDrawableButton scanRb;
    @BindView(R.id.byIdRb)
    CenterDrawableButton byIdRb;
    @BindView(R.id.bottomTabRg)
    RadioGroup bottomTabRg;
    @BindView(R.id.main_content)
    RelativeLayout mainContent;
    private AddDevPresenter addDevPresenter;
    private SetDevPhoneNumbDialog setDevPhoneNumbDialog;
    private String scanQrCode;
    private MessageWindow messageWindow;
    private ViewPager mViewPager;
    private AddDevByIdFragment addDevByIdFragment = new AddDevByIdFragment();
    private AddDevByQrFragment addDevByQrFragment = new AddDevByQrFragment();
    private Fragment[] fragments = new Fragment[]{addDevByQrFragment, addDevByIdFragment};
    private SectionsPagerAdapter mSectionsPagerAdapter;
    private String devId;
    private String akey;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_dev);
        ButterKnife.bind(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        backIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        addDevPresenter = new AddDevPresenter(this);
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        mViewPager = (ViewPager) findViewById(R.id.brewHomeContainer);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                if (position == 0) {
                    scanRb.setChecked(true);
                } else {
                    byIdRb.setChecked(true);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

    }


    @Override
    public void onClickQrScanBtn() {
        startQrScan();
//        startActivityForResult(new Intent(this, CaptureActivity.class), REQUEST_CODE_SCAN_DEV_QR);
    }

    public void startQrScan() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            //申请CAMERA权限
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA},
                    CAMERA);
        }else{
            startActivityForResult(new Intent(this, CaptureActivity.class), REQUEST_CODE_SCAN_DEV_QR);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission Granted
                startActivityForResult(new Intent(this, CaptureActivity.class), REQUEST_CODE_SCAN_DEV_QR);
            } else {
                // Permission Denied
            }
        }
    }

    @Override
    public void onClickAddDevByIdBtn(String devId, String akey) {
        this.devId = devId;
        this.akey = akey;
        addDevPresenter.verifyIotById(devId, akey);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_SCAN_DEV_QR) {
            if (resultCode == RESULT_OK) {
                scanQrCode = data.getStringExtra("result");
                messageWindow = showMsgWindow("提醒", "正在检测二维码...", null);
                if (scanQrCode != null)
                    addDevPresenter.verifyIot(scanQrCode);

            }
        }
    }

    @Override
    public void onReqIotFailed(String message) {

    }

    @Override
    public void onReqIotSuccess(Integer state) {
        switch (state) {
            case 0:
                if(messageWindow != null)
                    messageWindow.hidePopupWindow();
                showSetPhoneNumbDialog();
                break;
            case 1:
            case 3:
                if (messageWindow != null)
                    messageWindow.setMessage("正在请求绑定...");
                else
                    messageWindow = showMsgWindow("提醒", "正在请求绑定...", null);
                if(scanQrCode != null)
                    addDevPresenter.addDev(scanQrCode);
                else if(devId != null && akey != null)
                    addDevPresenter.addDevById(devId, akey);
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
                if (scanQrCode != null)
                    addDevPresenter.setPhoneNumb(scanQrCode, phoneNumb);
                else if(devId != null && akey != null)
                    addDevPresenter.setPhoneNumbById(devId, akey, phoneNumb);

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
        if(scanQrCode != null)
            addDevPresenter.addDev(scanQrCode);
        else if(devId != null && akey != null)
            addDevPresenter.addDevById(devId, akey);
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
        if(messageWindow != null)
            messageWindow.hidePopupWindow();
        Integer state = addDevResp.state;
        if (state == null) {
            showMsgWindow("提醒", "您已绑定该设备", null);
            return;
        }
        hideMsgWindow();
        switch (state) {
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
                new NoticeDialog(this).setOnPositiveButtonClickListener(new OnPositiveButtonClickListener() {
                    @Override
                    public void onPositiveButtonClick() {
                        Intent intent = new Intent(AddDevActivity.this, FollowActivity.class);
                        intent.putExtra("qrCode", scanQrCode);
                        intent.putExtra("pid", devId);
                        intent.putExtra("akey", akey);
                        startActivity(intent);
                    }
                }).setOnNegativeButtonClickListener(new OnNegativeButtonClickListener() {
                    @Override
                    public void onNegativeButtonClick() {

                    }
                }).show();
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
        if (messageWindow != null) {
            messageWindow.hidePopupWindow();
            messageWindow = null;
        }
    }




    public class SectionsPagerAdapter extends FragmentPagerAdapter {
        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return fragments[position];
        }

        @Override
        public int getCount() {
            return fragments.length;
        }
    }

    @OnCheckedChanged(R.id.scanRb)
    public void scanRb(boolean checked){
        if(!checked)
            return;
        mViewPager.setCurrentItem(0);
    }

    @OnCheckedChanged(R.id.byIdRb)
    public void byIdRb(boolean checked){
        if(!checked)
            return;
        mViewPager.setCurrentItem(1);
    }

}
