package com.ltbrew.brewbeer.uis.activity;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.RadioButton;
import android.widget.TextView;

import com.ltbrew.brewbeer.BrewApp;
import com.ltbrew.brewbeer.R;
import com.ltbrew.brewbeer.interfaceviews.BrewHomeView;
import com.ltbrew.brewbeer.presenter.BrewHomePresenter;
import com.ltbrew.brewbeer.presenter.model.Device;
import com.ltbrew.brewbeer.presenter.util.DeviceUtil;
import com.ltbrew.brewbeer.service.LtPushService;
import com.ltbrew.brewbeer.uis.Constants;
import com.ltbrew.brewbeer.uis.adapter.SectionsPagerAdapter;
import com.ltbrew.brewbeer.uis.dialog.DeleteOrRenameDevPopupWindow;
import com.ltbrew.brewbeer.uis.dialog.SetDevNameDialog;
import com.ltbrew.brewbeer.uis.fragment.BrewSessionFragment;
import com.ltbrew.brewbeer.uis.fragment.RecipeFragment;
import com.ltbrew.brewbeer.uis.utils.ReqSessionStateQueue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.locks.Lock;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnCheckedChanged;
import butterknife.OnClick;

public class BrewHomeActivity extends BaseActivity
        implements NavigationView.OnNavigationItemSelectedListener, BrewHomeView, View.OnClickListener, BrewSessionFragment.onBrewingSessionListener {

    @BindView(R.id.homeCenterTitle)
    TextView homeCenterTitle;
    @BindView(R.id.homeCreateBrewSession)
    TextView homeCreateBrewSession;
    @BindView(R.id.brewRb)
    RadioButton brewRb;
    @BindView(R.id.recipeRb)
    RadioButton recipeRb;
    public static final String ADD_DEV_SUCCESS_ACTION = "ADD_DEV_SUCCESS_ACTION";
    ImageView leftArrowIv;
    TextView devIdTv;
    ImageView rightArrowIv;
    private BrewSessionFragment brewSessionFragment = new BrewSessionFragment();
    private RecipeFragment recipeFragment = new RecipeFragment();
    private Fragment[] fragments = new Fragment[]{brewSessionFragment, recipeFragment};

    private ViewPager mViewPager;
    private BrewHomePresenter brewHomePresenter;
    private List<Device> devices = Collections.EMPTY_LIST;
    private boolean serviceIsConnected;
    private int positionCurrentDevInDevices;
    private LtPushService ltPushService;
    private ReqSessionStateQueue reqSessionStateQueue;
    private DeleteOrRenameDevPopupWindow deleteOrRenameDevPopupWindow;
    private Handler handler = new Handler();
    private long recordTimeMillis = 0;

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (LtPushService.UNBIND_ACTION.equals(action)) {
                if (brewHomePresenter != null)
                    brewHomePresenter.getDevs();
                return;
            }
            ArrayList<Device> devs = intent.getParcelableArrayListExtra(AddDevActivity.DEVICES_EXTRA);
            devices = devs;
            positionCurrentDevInDevices = findWhereIsCurrentDevInDevices();
            updateUIForDev();
            reqDataFromServer();
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_brew_home);
        ButterKnife.bind(this);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        homeCenterTitle.setTypeface(BrewApp.getInstance().textFont);

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ADD_DEV_SUCCESS_ACTION);
        intentFilter.addAction(LtPushService.UNBIND_ACTION);
        registerReceiver(broadcastReceiver, intentFilter);

        setupViewPager();

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        if (drawer != null) {
            drawer.setDrawerListener(toggle);
        }
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        if (navigationView != null) {
            navigationView.setNavigationItemSelectedListener(this);
        }
        if (navigationView != null) {
            View view = navigationView.getHeaderView(0);
            leftArrowIv = (ImageView) view.findViewById(R.id.leftArrowIv);
            devIdTv = (TextView) view.findViewById(R.id.devIdTv);
            rightArrowIv = (ImageView) view.findViewById(R.id.rightArrowIv);
            leftArrowIv.setOnClickListener(this);
            devIdTv.setOnClickListener(this);
            rightArrowIv.setOnClickListener(this);
        }
        brewHomePresenter = new BrewHomePresenter(this);
        brewHomePresenter.getDevs();
        initMessageQueue();
    }


    //初始化ViewPager
    private void setupViewPager() {
        SectionsPagerAdapter mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        mSectionsPagerAdapter.setFragments(fragments);
        mViewPager = (ViewPager) findViewById(R.id.brewHomeContainer);
        if (mViewPager != null) {
            mViewPager.setAdapter(mSectionsPagerAdapter);
        }
        mViewPager.setOffscreenPageLimit(2);
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                if (position == 0) {
                    brewRb.setChecked(true);
                } else {
                    recipeRb.setChecked(true);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
    }

    //初始化一个消息队列
    private void initMessageQueue() {
        reqSessionStateQueue = new ReqSessionStateQueue();
        reqSessionStateQueue.setLtPushService(ltPushService);
        reqSessionStateQueue.start();
    }

    //点击底部酿造的按钮
    @OnCheckedChanged(R.id.brewRb)
    public void brewRb(boolean checked) {
        if (!checked)
            return;
        mViewPager.setCurrentItem(0);
    }

    //点击底部配方按钮
    @OnCheckedChanged(R.id.recipeRb)
    public void recipeRb(boolean checked) {
        if (!checked)
            return;
        mViewPager.setCurrentItem(1);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer != null && drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            if(System.currentTimeMillis() - recordTimeMillis < 2000){
                super.onBackPressed();
            }
            recordTimeMillis = System.currentTimeMillis();
            showSnackBar("再按一次退出");
        }
    }

    //侧面导航栏点击事件
    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.nav_add_dev) {
            startAddDevActivity();
        } else if (id == R.id.nav_about) {
            startAboutActivity();
        } else if (id == R.id.nav_exit) {
            DeviceUtil.clearData();
            stopService(new Intent(this, LtPushService.class));
            startLoginActivity();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer != null) {
            drawer.closeDrawer(GravityCompat.START);
        }
        return true;
    }

    //打开添加设备界面
    void startAddDevActivity() {
        Intent intent = new Intent();
        intent.setClass(this, AddDevActivity.class);
        startActivity(intent);
    }

    //打开关于我们界面
    void startAboutActivity() {
        Intent intent = new Intent();
        intent.setClass(this, AboutActivity.class);
        startActivity(intent);
    }

    //打开登录界面
    private void startLoginActivity() {
        Intent intent = new Intent();
        intent.setClass(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        startActivity(intent);
        finish();
    }

    //点击新建酿造按钮
    @OnClick(R.id.homeCreateBrewSession)
    public void createBrewSession() {
        if (devices.size() == 0 || DeviceUtil.getCurrentDevId() == null) {
            showMsgWindow("提醒", "您还未添加任何设备， 请先添加", null);
            return;
        }
        if(brewSessionFragment.getBrewingSessionCount() == 6){
            showMsgWindow("提醒", "您已创建了六个酿造任务， 无法再创建更多", null);
            return;
        }
        startAddRecipeActivity();
    }

    //打开新建酿造界面
    private void startAddRecipeActivity() {
        Intent intent = new Intent();
        intent.setClass(this, AddRecipeActivity.class);
        startActivity(intent);
    }

    //=========================== 获取设备列表回调 start===========================
    //设备列表获得成功
    @Override
    public void onGetDevsSuccess(List<Device> devices) {
        updateUIForDev();
        this.devices = devices;
        reqDataFromServer();
        startPushService();
        positionCurrentDevInDevices = findWhereIsCurrentDevInDevices();
    }

    private void updateUIForDev() {
        String currentDevId = DeviceUtil.getCurrentDevId();
        if (!TextUtils.isEmpty(currentDevId)) {
            homeCenterTitle.setTypeface(null);
            String devNickName = DeviceUtil.getDevNickName(currentDevId);
            if (!TextUtils.isEmpty(devNickName)) {
                setDeviceName(devNickName);

            } else {
                setDeviceName(currentDevId);

            }
        }
    }


    //请求酿造历史和获取配方列表
    public void reqDataFromServer() {
        if (brewSessionFragment == null)
            return;
        if (recipeFragment == null)
            return;
        brewSessionFragment.getBrewHistory();
        recipeFragment.getAllRecipes();
    }

    //启动后台推送服务
    private void startPushService() {
        Intent intent = new Intent(this, LtPushService.class);
        bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE);
        startService(intent);
    }

    //绑定服务
    ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            ltPushService = ((LtPushService.ServiceBinder) iBinder).getService();
            reqSessionStateQueue.setLtPushService(ltPushService);
            serviceIsConnected = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            serviceIsConnected = false;
            ltPushService = null;
        }
    };

    //找到当前设备在设备列表中的位置
    public int findWhereIsCurrentDevInDevices() {
        for (int i = 0, size = devices.size(); i < size; i++) {
            String currentDevId = DeviceUtil.getCurrentDevId();
            if (currentDevId.equals(devices.get(i).getId())) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public void onGetDevsFailed(String message) {
        if (Constants.NETWORK_ERROR.equals(message)) {
            showSnackBar("设备获取失败，请确认网络");
            return;
        }else if(Constants.PASSWORD_ERROR.equals(message)){
            showSnackBar("用户名或密码出错，请重试！");
            return;
        }
        showSnackBar("获取设备失败，服务器或APP出错："+message+"， 请联系客服");

    }


    //=========================== 获取设备列表回调 end===========================

    //============================删除设备回调==========================

    //    state值	描述
    //    1	帐号格式不对
    //    2	设备id格式不正确/api_key来源非法
    //    3	已经是解绑状态
    //    4	设备不属于当前帐号
    //    5	子帐号解关注成功
    //    6	设备io参数以及主帐号不匹配（实际上和2有些重复）
    @Override
    public void onReqDeleteDevSuccess(int state) {
        switch (state){
            case 0:
                showSnackBar("设备解绑成功");
                switchToNextDevInList();
                break;
            case 1:
                showSnackBar("帐号格式不对");
                break;
            case 2:
                showSnackBar("设备id格式不正确或api_key来源非法");
                break;
            case 3:
                showSnackBar("已经是解绑状态");
                break;
            case 4:
                showSnackBar("设备不属于当前帐号");
                break;
            case 5:
                showSnackBar("子帐号解关注成功");
                break;
            case 6:
                showSnackBar("设备io参数以及主帐号不匹配");
                break;
        }
    }

    @Override
    public void onDeleteDevFailed(String message) {
        showSnackBar(message);
    }
    //============================删除设备回调==========================
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (broadcastReceiver != null)
            unregisterReceiver(broadcastReceiver);
        if (mServiceConnection != null && serviceIsConnected)
            unbindService(mServiceConnection);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.leftArrowIv:
                switchToPrevDevInList();
                break;
            case R.id.devIdTv:
                showPopupToChangeNameOrDelete();

                break;
            case R.id.rightArrowIv:
                switchToNextDevInList();
                break;
        }
    }
    //点击向左的箭头切换上一个设备
    private void switchToPrevDevInList() {
        if (devices.size() == 0 || positionCurrentDevInDevices == -1)
            return;
        handler.removeCallbacksAndMessages(null);
        if (positionCurrentDevInDevices == 0) {
            positionCurrentDevInDevices = devices.size() - 1;
        } else {
            positionCurrentDevInDevices--;
        }
        Device device = devices.get(positionCurrentDevInDevices);
        DeviceUtil.storeCurrentDevId(device.getId());
        String devNickName = DeviceUtil.getDevNickName(device.getId());
        if (!TextUtils.isEmpty(devNickName)) {
            setDeviceName(devNickName);
        } else {
            setDeviceName(device.getId());
        }
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                reqDataFromServer();
            }
        }, 500);

    }
    //点击设备id显示弹出框
    private void showPopupToChangeNameOrDelete() {
        if (TextUtils.isEmpty(DeviceUtil.getCurrentDevId()))
            return;
        if (deleteOrRenameDevPopupWindow != null)
            return;
        deleteOrRenameDevPopupWindow = new DeleteOrRenameDevPopupWindow(this);
        deleteOrRenameDevPopupWindow.setOnButtonClickListener(new DeleteOrRenameDevPopupWindow.OnButtonClickListener() {
            @Override
            public void onChangeNameBtnClick() {
                showChangeNameDialog();
            }

            @Override
            public void onDeleteDevBtnClick() {
                deleteDevice();
            }
        });
//        deleteOrRenameDevPopupWindow.setWidth(devIdTv.getWidth());
        deleteOrRenameDevPopupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                deleteOrRenameDevPopupWindow = null;
            }
        });
//        deleteOrRenameDevPopupWindow.showAsDropDown(devIdTv, 0, 16);
    }

    private void showChangeNameDialog() {
        SetDevNameDialog setDevNameDialog = new SetDevNameDialog(this);
        setDevNameDialog.setOnSetNameForDevListener(new SetDevNameDialog.OnSetNameForDevListener() {
            @Override
            public void onSetDevName(String name) {
                DeviceUtil.setDevNickName(DeviceUtil.getCurrentDevId(), name);
                setDeviceName(DeviceUtil.getDevNickName(DeviceUtil.getCurrentDevId()));
            }
        });
        setDevNameDialog.show();
    }

    private void deleteDevice() {
        if(brewHomePresenter != null)
            brewHomePresenter.unbindDev();
    }
    //点击向右的箭头切换下一个设备
    private void switchToNextDevInList() {
        if (devices.size() == 0 || positionCurrentDevInDevices == -1)
            return;
        handler.removeCallbacksAndMessages(null);
        if (positionCurrentDevInDevices == devices.size() - 1) {
            positionCurrentDevInDevices = 0;
        } else {
            positionCurrentDevInDevices++;
        }
        Device device = devices.get(positionCurrentDevInDevices);
        DeviceUtil.storeCurrentDevId(device.getId());
        String devNickName = DeviceUtil.getDevNickName(device.getId());
        if (!TextUtils.isEmpty(devNickName)) {
            setDeviceName(devNickName);
        } else {
            setDeviceName(device.getId());
        }
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                reqDataFromServer();
            }
        }, 500);
    }


    private void setDeviceName(String devNickName) {
        devIdTv.setText(devNickName);
        homeCenterTitle.setText(devNickName);
    }
    @Override
    public void onReqBrewingSession(Long package_id) {
        Message msg = new Message();
        msg.obj = package_id;
        reqSessionStateQueue.handler.sendMessage(msg); //将消息发送到消息队列进行排队处理
    }

    @Override
    public void onReceiveSessionState() {
        Lock lock = reqSessionStateQueue.lock;
        synchronized (lock) {
            lock.notifyAll();
        }
    }
}
