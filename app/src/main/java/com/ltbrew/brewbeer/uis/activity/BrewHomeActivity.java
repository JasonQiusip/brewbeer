package com.ltbrew.brewbeer.uis.activity;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.provider.Settings;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.RadioButton;
import android.widget.TextView;

import com.ltbrew.brewbeer.R;
import com.ltbrew.brewbeer.interfaceviews.BrewHomeView;
import com.ltbrew.brewbeer.presenter.BrewHomePresenter;
import com.ltbrew.brewbeer.presenter.model.Device;
import com.ltbrew.brewbeer.presenter.util.DeviceUtil;
import com.ltbrew.brewbeer.service.ILtPushServiceAidlInterface;
import com.ltbrew.brewbeer.service.LtPushService;
import com.ltbrew.brewbeer.thirdpartylib.MessageWindow;
import com.ltbrew.brewbeer.uis.Constants;
import com.ltbrew.brewbeer.uis.adapter.DevsAdapter;
import com.ltbrew.brewbeer.uis.adapter.SectionsPagerAdapter;
import com.ltbrew.brewbeer.uis.adapter.viewholder.BaseViewHolder;
import com.ltbrew.brewbeer.uis.dialog.DeleteOrRenameDevPopupWindow;
import com.ltbrew.brewbeer.uis.dialog.NoticeDialog;
import com.ltbrew.brewbeer.uis.dialog.OnNegativeButtonClickListener;
import com.ltbrew.brewbeer.uis.dialog.OnPositiveButtonClickListener;
import com.ltbrew.brewbeer.uis.dialog.SetDevNameDialog;
import com.ltbrew.brewbeer.uis.fragment.BrewSessionFragment;
import com.ltbrew.brewbeer.uis.fragment.RecipeFragment;
import com.ltbrew.brewbeer.uis.utils.AccUtils;
import com.ltbrew.brewbeer.uis.utils.NetworkConnectionUtil;
import com.ltbrew.brewbeer.uis.utils.ReqSessionStateQueue;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.locks.Lock;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnCheckedChanged;
import butterknife.OnClick;

public class BrewHomeActivity extends BaseActivity
        implements NavigationView.OnNavigationItemSelectedListener, BrewHomeView, View.OnClickListener, BrewSessionFragment.onBrewingSessionListener {

    private static final int SYSTEM_ALERT = 21;

    @BindView(R.id.homeCenterTitle)
    ImageView homeCenterTitle;
    @BindView(R.id.homeCreateBrewSession)
    TextView homeCreateBrewSession;
    @BindView(R.id.brewRb)
    RadioButton brewRb;
    @BindView(R.id.recipeRb)
    RadioButton recipeRb;
    public static final String ADD_DEV_SUCCESS_ACTION = "ADD_DEV_SUCCESS_ACTION";
    public static final String PUSH_REQ_SESSION_ACTION = "push_action_to_queue";
    public static final String PACK_ID_EXTRA = "packId";

    @BindView(R.id.accAvatar)
    ImageView accAvatar;
    @BindView(R.id.accTv)
    TextView accTv;
    @BindView(R.id.rightArrowIv)
    ImageView rightArrowIv;
    @BindView(R.id.devsRv)
    RecyclerView devsRv;
    @BindView(R.id.nav_add_dev)
    TextView navAddDev;
    @BindView(R.id.nav_about)
    TextView navAbout;
    @BindView(R.id.nav_exit)
    TextView navExit;

    private BrewSessionFragment brewSessionFragment = new BrewSessionFragment();
    private RecipeFragment recipeFragment = new RecipeFragment();
    private Fragment[] fragments = new Fragment[]{brewSessionFragment, recipeFragment};

    private ViewPager mViewPager;
    private BrewHomePresenter brewHomePresenter;
    private List<Device> devices = Collections.EMPTY_LIST;
    private boolean serviceIsConnected;
    private int positionCurrentDevInDevices;
    private ILtPushServiceAidlInterface ltPushService;
    private ReqSessionStateQueue reqSessionStateQueue;
    private DeleteOrRenameDevPopupWindow deleteOrRenameDevPopupWindow;
    private Handler handler = new Handler();
    private long recordTimeMillis = 0;


    private MessageWindow msgWin;
    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.e("broadcastReceiver", "brewhome...");
            String action = intent.getAction();
            if (LtPushService.UNBIND_ACTION.equals(action)) {
                if (brewHomePresenter != null)
                    brewHomePresenter.getDevs();
                return;
            } else if (PUSH_REQ_SESSION_ACTION.equals(action)) {
                long packId = intent.getLongExtra(PACK_ID_EXTRA, 0);
                if (packId == 0)
                    return;
                onReqBrewingSession(packId);
                return;
            }else if(LtPushService.SOCKET_IS_KICKED_OUT.equals(action)){
                if(msgWin != null)
                    msgWin.hidePopupWindow();
                msgWin = showMsgWindow("提醒", "推送服务未连上，请确认帐号是否已在其它设备上登录", new MessageWindow.OnMsgWindowActionListener() {
                    @Override
                    public void onCloseWindow() {
                        msgWin = null;
                    }

                    @Override
                    public void onClickDetail() {

                    }
                });
                return;
            }else if(LtPushService.SOCKET_INIT_FAILED.equals(action)){
                if(msgWin != null)
                    msgWin.hidePopupWindow();
                msgWin = showMsgWindow("提醒", "推送服务初始化失败， 请重试", new MessageWindow.OnMsgWindowActionListener(){

                    @Override
                    public void onCloseWindow() {
                        msgWin = null;
                        if(ltPushService != null)
                            try {
                                ltPushService.startLongConn();
                            } catch (RemoteException e) {
                                e.printStackTrace();
                            }
                    }

                    @Override
                    public void onClickDetail() {

                    }
                }).setTextForCloseTv("确定");
            }
            if (brewHomePresenter != null)
                brewHomePresenter.getDevs();
        }
    };
    private DevsAdapter devsAdapter;
    private View selectedView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_brew_home);
        ButterKnife.bind(this);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ADD_DEV_SUCCESS_ACTION);
        intentFilter.addAction(PUSH_REQ_SESSION_ACTION);
        intentFilter.addAction(LtPushService.UNBIND_ACTION);
        intentFilter.addAction(LtPushService.SOCKET_IS_KICKED_OUT);
        intentFilter.addAction(LtPushService.SOCKET_INIT_FAILED);
        registerReceiver(broadcastReceiver, intentFilter);

        setupViewPager();

        final DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        if (drawer != null) {
            drawer.setDrawerListener(toggle);
        }
        toggle.syncState();

        devsRv.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        devsAdapter = new DevsAdapter(this);
        devsAdapter.setData(devices);
        devsRv.setAdapter(devsAdapter);
        devsAdapter.setOnItemClickListener(new BaseViewHolder.OnRvItemClickListener() {
            @Override
            public void onRvItemClick(View v, int layoutPosition) {
                selectedView = v;
                Device device = devices.get(layoutPosition);
                if (drawer != null) {
                    drawer.closeDrawer(GravityCompat.START);
                }
                DeviceUtil.storeCurrentDevId(device.getId());
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (brewSessionFragment == null)
                            return;
                        brewSessionFragment.setActivelyRefreshStart(true);
                        brewSessionFragment.getBrewHistory();
                    }
                }, 1000);
            }
        });
        devsAdapter.setOnItemLongClickListener(new DevsAdapter.OnRvItemLongClickListener(){

            @Override
            public void onItemLongClick(View v, int position) {
                showPopupToChangeNameOrDelete(v);
            }
        });
        accTv.setText(AccUtils.getAcc());
        navAddDev.setOnClickListener(this);
        navAbout.setOnClickListener(this);
        navExit.setOnClickListener(this);
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

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        String from = intent.getStringExtra("from");
        if ("afaterFollowSuccess".equals(from)) {
            brewHomePresenter.getDevs();
        } else {
            startService(new Intent(this, LtPushService.class));
        }
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
            if (System.currentTimeMillis() - recordTimeMillis < 2000) {
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
        if (devices.size() == 0 || TextUtils.isEmpty(DeviceUtil.getCurrentDevId())) {
            showMsgWindow("提醒", "您还未添加任何设备， 请先添加", null);
            return;
        }
        if (brewSessionFragment.getBrewingSessionCount() == 6) {
            showMsgWindow("提醒", "您已创建了六个酿造任务， 无法再创建更多", null);
            return;
        }
        if (brewSessionFragment.getBrewingSessionCount() != 0) {
            showMsgWindow("提醒", "当前有正在酿造的任务，无法添加更多", null);
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
        this.devices = devices;

        if (this.devices != null && this.devices.size() == 0) {
            DeviceUtil.storeCurrentDevId("");
            if(brewSessionFragment != null)
                brewSessionFragment.clearData();
        }
        Log.e("onGetDevsSuccess", devices.toString());

        startPushService();
        reqDataFromServer();
        positionCurrentDevInDevices = findWhereIsCurrentDevInDevices();
        devsAdapter.setSelectedPosition(positionCurrentDevInDevices);
        devsAdapter.setData(devices);
        devsAdapter.notifyDataSetChanged();
    }


    //请求酿造历史和获取配方列表
    public void reqDataFromServer() {
        if (brewSessionFragment == null)
            return;
        if (recipeFragment == null)
            return;
        brewSessionFragment.getBrewHistory();
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
            ltPushService = ILtPushServiceAidlInterface.Stub.asInterface(iBinder);
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
        } else if (Constants.PASSWORD_ERROR.equals(message)) {
            showSnackBar("用户名或密码出错，请重试！");
            return;
        }
        showSnackBar("获取设备失败，服务器或APP出错：" + message + "， 请联系客服");

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
        switch (state) {
            case 0:
                showSnackBar("设备解绑成功");
                brewHomePresenter.getDevs();
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
            case R.id.accAvatar:
                break;
            case R.id.accTv:

                break;
            case R.id.nav_add_dev:
                startAddDevActivity();
                break;
            case R.id.nav_about:
                startAboutActivity();
                break;
            case R.id.nav_exit:
                DeviceUtil.clearData();
                AccUtils.clearData();
                stopService(new Intent(this, LtPushService.class));
                startLoginActivity();
                break;
        }
    }

    //点击设备id显示弹出框
    private void showPopupToChangeNameOrDelete(View view) {
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
        deleteOrRenameDevPopupWindow.setWidth(view.getWidth());
        deleteOrRenameDevPopupWindow.setHeight(ViewPager.LayoutParams.WRAP_CONTENT);
        deleteOrRenameDevPopupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                deleteOrRenameDevPopupWindow = null;
            }
        });
        try {
            deleteOrRenameDevPopupWindow.showAsDropDown(view, 0, 8);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showChangeNameDialog() {
        SetDevNameDialog setDevNameDialog = new SetDevNameDialog(this);
        setDevNameDialog.setOnSetNameForDevListener(new SetDevNameDialog.OnSetNameForDevListener() {
            @Override
            public void onSetDevName(String name) {
                DeviceUtil.setDevNickName(DeviceUtil.getCurrentDevId(), name);
                devsAdapter.notifyDataSetChanged();
            }
        });
        setDevNameDialog.show();
    }

    private void deleteDevice() {
        if (brewHomePresenter != null)
            brewHomePresenter.unbindDev();
    }


    @Override
    public void onReqBrewingSession(Long package_id) {
        Message msg = new Message();
        msg.what = ReqSessionStateQueue.CHECK_CMN_PRGS;
        msg.obj = package_id;
        reqSessionStateQueue.handler.sendMessage(msg); //将消息发送到消息队列进行排队处理
    }

    @Override
    public void unlockLockerToExecuteNextMsg() {
        Lock lock = reqSessionStateQueue.lock;
        synchronized (lock) {
            lock.notifyAll();
        }
    }

    @Override
    public void onFinishReqBrewHistory() {
        if(recipeFragment != null)
            recipeFragment.getAllRecipes();
    }

    @Override
    public void onCheckTemp(long package_id) {
        Message msg = new Message();
        msg.what = ReqSessionStateQueue.CHECK_CMN_MSG;
        msg.obj = package_id;
        reqSessionStateQueue.handler.sendMessage(msg); //将消息发送到消息队列进行排队处理
    }
}
