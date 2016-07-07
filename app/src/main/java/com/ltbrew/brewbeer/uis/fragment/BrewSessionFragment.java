package com.ltbrew.brewbeer.uis.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import com.github.ybq.android.spinkit.SpinKitView;
import com.ltbrew.brewbeer.R;
import com.ltbrew.brewbeer.interfaceviews.BrewSessionVeiw;
import com.ltbrew.brewbeer.persistence.greendao.DBBrewHistory;
import com.ltbrew.brewbeer.persistence.greendao.DBBrewStep;
import com.ltbrew.brewbeer.persistence.greendao.DBRecipe;
import com.ltbrew.brewbeer.presenter.BrewSessionsPresenter;
import com.ltbrew.brewbeer.presenter.model.Recipe;
import com.ltbrew.brewbeer.presenter.util.DBManager;
import com.ltbrew.brewbeer.service.LtPushService;
import com.ltbrew.brewbeer.service.PldForBrewSession;
import com.ltbrew.brewbeer.service.PldForCmnMsg;
import com.ltbrew.brewbeer.service.PldForCmnPrgs;
import com.ltbrew.brewbeer.service.PushMsg;
import com.ltbrew.brewbeer.uis.Constants;
import com.ltbrew.brewbeer.uis.activity.BrewSessionControlActivity;
import com.ltbrew.brewbeer.uis.adapter.BrewingSessionAdapter;
import com.ltbrew.brewbeer.uis.fragment.viewcontroller.BrewSessionRvController;
import com.ltbrew.brewbeer.uis.utils.BrewSessionUtils;
import com.ltbrew.brewbeer.uis.utils.ParamStoreUtil;
import com.ltbrew.brewbeer.uis.view.ReboundScrollView;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;

public class BrewSessionFragment extends Fragment implements BrewSessionVeiw {

    public static final String PACK_IS_SENT = "com.ltbrew.beer.AddRecipeActivity.PACK_IS_SENT_TO_DEV";
    public String TAG = this.getClass().getName();
    @BindView(R.id.brewStateRv)
    RecyclerView brewStateRv;
    @BindView(R.id.fermentingBrewRv)
    RecyclerView fermentingBrewRv;
    @BindView(R.id.noBrewingTaskTv)
    TextView noBrewingTaskTv;
    @BindView(R.id.noFermentingTaskTv)
    TextView noFermentingTaskTv;
    @BindView(R.id.reboundScrollView)
    ReboundScrollView reboundScrollView;
    @BindView(R.id.brewStateTitle)
    TextView brewStateTitle;
    @BindView(R.id.spin_kit)
    SpinKitView spinKit;
    @BindView(R.id.suspendBrewRv)
    RecyclerView suspendBrewRv;
    @BindView(R.id.suspendTaskTv)
    TextView suspendTaskTv;
    @BindView(R.id.finishedBrewRv)
    RecyclerView finishedBrewRv;
    @BindView(R.id.noFinishedTaskTv)
    TextView noFinishedTaskTv;
    int i = 20;
    private BrewingSessionAdapter brewingSessionAdapter;
    private BrewingSessionAdapter fermentingSessionAdapter;
    private BrewingSessionAdapter suspendSessionAdapter;
    private BrewingSessionAdapter finishedSessionAdapter;
    private BrewSessionsPresenter brewSessionsPresenter;
    private HashMap<String, Integer> brewingFormulaIdToPosition = new HashMap<>();
    private HashMap<String, Integer> fermentingFormulaIdToPosition = new HashMap<>();
    private HashMap<Long, Subscription> sessionProcessingMap = new HashMap<>();
    private List<DBBrewHistory> brewingHistoryList = Collections.EMPTY_LIST;
    private List<DBBrewHistory> fermentingHistoryList = Collections.EMPTY_LIST;
    private List<DBBrewHistory> suspendHistoryList = Collections.EMPTY_LIST;
    private List<DBBrewHistory> finishedHistoryList = Collections.EMPTY_LIST;
    private onBrewingSessionListener onBrewingSessionListener;
    private String packId;
    private boolean activelyRefreshStart = false;

    private BrewSessionRvController brewingController;
    private BrewSessionRvController fermentingController;
    private BrewSessionRvController suspendController;
    private BrewSessionRvController finishedController;

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (PACK_IS_SENT.equals(action)) {
                String formula_id = intent.getStringExtra(BrewSessionControlActivity.FORMULA_ID_EXTRA);
                packId = intent.getStringExtra(BrewSessionControlActivity.PACK_ID_EXTRA);
                if (brewSessionsPresenter != null) {
                    brewSessionsPresenter.getRecipeAfterBrewBegin(formula_id);
                }
            } else if (LtPushService.FILE_SOCKET_IS_READY_ACTION.equals(action)) {
                if (brewingHistoryList.size() != 0) {
                    for (int i = 0, size = brewingHistoryList.size(); i < size; i++) {
                        DBBrewHistory brewHistory = brewingHistoryList.get(i);
                        onBrewingSessionListener.onReqBrewingSession(brewHistory.getPackage_id());
                    }
                }

            } else if (LtPushService.CMN_PRGS_CHECK_ACTION.equals(action)) {
                if (onBrewingSessionListener != null)
                    onBrewingSessionListener.unlockLockerToExecuteNextMsg();
                PushMsg pushMsgObj = intent.getParcelableExtra(LtPushService.PUSH_MSG_EXTRA);
                Log.e(TAG + "CMN_PRGS_CHECK_ACTION", pushMsgObj.toString());
                String st = pushMsgObj.st;
                DBBrewHistory brewHistory;
                if (st != null) {
                    String package_id = st.split(":")[1];
                    brewHistory = findBrewHistory(package_id);
                    if (brewHistory == null)
                        return;
                } else {
                    brewHistory = brewingHistoryList.get(0);
                }
                brewHistory.setRatio(pushMsgObj.ratio);
                brewHistory.setSi(pushMsgObj.si);
                if (pushMsgObj.des != null && pushMsgObj.des.equals("-1")) {
                    pushMsgObj.des = "煮沸";
                }
                Log.e(TAG + "CMN_PRGS_CHECK_ACTION", brewHistory+"");
                if(pushMsgObj.des != null && pushMsgObj.des.contains("加热中")&&onBrewingSessionListener != null){
                    onBrewingSessionListener.onCheckTemp(brewHistory.getPackage_id());
                }
                brewHistory.setBrewingState(pushMsgObj.des);
                setTimeLeft(pushMsgObj, brewHistory);
                brewHistory.setSt(st);
                brewingSessionAdapter.setData(brewingHistoryList);
                brewingSessionAdapter.notifyDataSetChanged();
            } else if (LtPushService.CMN_PRGS_PUSH_ACTION.equals(action)) {

                PushMsg pushMsgObj = intent.getParcelableExtra(LtPushService.PUSH_MSG_EXTRA);
                PldForCmnPrgs pldForCmnPrgs = intent.getParcelableExtra(LtPushService.PUSH_PLD_EXTRA);
                Log.e(TAG + "CMN_PRGS_PUSH_ACTION", pushMsgObj.toString());
                String st = pldForCmnPrgs.st;
                DBBrewHistory brewHistory;
                if (st != null) {
                    String package_id = st.split(":")[1];
                    brewHistory = findBrewHistory(package_id);
                    if (brewHistory == null)
                        return;
                } else {
                    if (brewingHistoryList.size() == 0)
                        return;
                    brewHistory = brewingHistoryList.get(0);
                }
                if (brewHistory == null)
                    return;
                Log.e(TAG + "CMN_PRGS_PUSH_ACTION1", brewHistory+"");
                if (pushMsgObj.des != null && pushMsgObj.des.equals("-1")) {
                    pushMsgObj.des = "煮沸";
                }
                brewHistory.setRatio(pldForCmnPrgs.ratio);
                brewHistory.setSi(pldForCmnPrgs.si);
                brewHistory.setBrewingState(pushMsgObj.des);
                setTimeLeft(pushMsgObj, brewHistory);
                brewHistory.setSt(st);
                brewingSessionAdapter.setData(brewingHistoryList);
                brewingSessionAdapter.notifyDataSetChanged();
            } else if (LtPushService.CMN_MSG_PUSH_ACTION.equals(action)) {
                if (onBrewingSessionListener != null)
                    onBrewingSessionListener.unlockLockerToExecuteNextMsg();
                PushMsg pushMsgObj = intent.getParcelableExtra(LtPushService.PUSH_MSG_EXTRA);
                Log.e(TAG + "CMN_MSG_PUSH_ACTION", pushMsgObj.toString());
                PldForCmnMsg pldForCmnMsg = intent.getParcelableExtra(LtPushService.PUSH_PLD_EXTRA);

                int ms = pldForCmnMsg.ms;
                if (ms >= 90) {
                    pushMsgObj.des = "煮沸";
                }
                String tk = pldForCmnMsg.tk;
                DBBrewHistory brewHistory;
                if (tk != null) {
                    String package_id = tk.split(":")[1];
                    brewHistory = findBrewHistory(package_id);
                    if (brewHistory == null)
                        return;
                } else {
                    brewHistory = brewingHistoryList.get(0);
                }
                brewHistory.setMs(ms);
                brewHistory.setBrewingCmnMsg(pushMsgObj.des);
                brewingSessionAdapter.setData(brewingHistoryList);
                brewingSessionAdapter.notifyDataSetChanged();
            } else if (LtPushService.BREW_SESSION_PUSH_ACTION.equals(action)) {
                PushMsg pushMsgObj = intent.getParcelableExtra(LtPushService.PUSH_MSG_EXTRA);
                Log.e(TAG + "CMN_MSG_PUSH_ACTION", pushMsgObj.toString());
                PldForBrewSession pldForBrewSession = intent.getParcelableExtra(LtPushService.PUSH_PLD_EXTRA);
                if (pldForBrewSession.state == 1) {
                    brewSessionsPresenter.getBrewHistory();
                }

            } else if (LtPushService.REQUEST_BREW_SESSION_FAILED.equals(action)) {
                if (onBrewingSessionListener != null)
                    onBrewingSessionListener.unlockLockerToExecuteNextMsg();
            }
        }

        private void setTimeLeft(final PushMsg pushMsgObj, final DBBrewHistory brewHistory) {
            if (brewHistory == null )
                return;

            Subscription subscription = sessionProcessingMap.get(brewHistory.getPackage_id());
            if(subscription != null)
                subscription.unsubscribe();

            if ("糖化中".equals(pushMsgObj.des) || "煮沸中".equals(pushMsgObj.des)) {
                Subscription subscribe = Observable.interval(0, 30, TimeUnit.SECONDS).flatMap(new Func1<Long, Observable<Long>>() {
                    @Override
                    public Observable<Long> call(Long aLong) {
                        return calTimeLeftAndShowOb(brewHistory, pushMsgObj);
                    }
                }).subscribe(new Action1<Long>() {
                    @Override
                    public void call(Long aLong) {
                        Log.e("setTimeLeft call", "========" + aLong + "===========");
                        if (brewingSessionAdapter != null)
                            brewingSessionAdapter.notifyDataSetChanged();

                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {

                        Log.e("setTimeLeft call", "========stop===========");
                        throwable.printStackTrace();
                    }
                });
                sessionProcessingMap.put(brewHistory.getPackage_id(), subscribe);
            }
        }
    };

    private Observable<Long> calTimeLeftAndShowOb(final DBBrewHistory brewHistory, final PushMsg pushMsgObj) {
        return Observable.create(new Observable.OnSubscribe<Long>() {
            @Override
            public void call(Subscriber<? super Long> subscriber) {
                long timePassed = System.currentTimeMillis() - BrewSessionUtils.getStepStartTimeStamp(brewHistory.getPackage_id()+"");
                DBRecipe dbRecipe = brewHistory.getDBRecipe();
                if (dbRecipe == null)
                    return;
                List<DBBrewStep> brewSteps = dbRecipe.getBrewSteps();
                if (brewSteps != null && brewSteps.size() > pushMsgObj.si) {
                    DBBrewStep dbBrewStep = brewSteps.get(pushMsgObj.si);
                    Integer k = dbBrewStep.getK(); //总时间
                    if (k != null) {
                        long timeLeft = k / 60 - timePassed / (60 * 1000);
                        long hourLeft = timeLeft / 60;
                        if (timeLeft > 0) {
                            brewHistory.setRatio((int) ((timePassed*100)/1000/k));
                            brewHistory.setBrewingStageInfo("剩" + (hourLeft == 0 ? "" : hourLeft + "小时") + timeLeft % 60 + "分钟");
                            subscriber.onNext(timeLeft);
                        }else{
                            subscriber.onError(new Throwable("session end"));
                        }
                    }
                }
            }
        }).subscribeOn(AndroidSchedulers.mainThread());
    }


    private DBBrewHistory findBrewHistory(String package_id) {
        for (int i = 0, size = brewingHistoryList.size(); i < size; i++) {
            DBBrewHistory brewHistory = brewingHistoryList.get(i);
            if (String.valueOf(brewHistory.getPackage_id()).equals(package_id)) {
                return brewHistory;
            }
        }
        return null;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        onBrewingSessionListener = (onBrewingSessionListener) context;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_brew_session, container, false);
        ButterKnife.bind(this, view);

        brewingController = new BrewSessionRvController(brewingHistoryList, Constants.BrewSessionType.BREWING);
        fermentingController = new BrewSessionRvController(fermentingHistoryList, Constants.BrewSessionType.FERMENTING);
        suspendController = new BrewSessionRvController(suspendHistoryList, Constants.BrewSessionType.SUSPEND);
        finishedController = new BrewSessionRvController(finishedHistoryList, Constants.BrewSessionType.FINSHED);

        brewingSessionAdapter = brewingController.buildRv(this.getActivity(), brewStateRv);
        fermentingSessionAdapter = fermentingController.buildRv(this.getActivity(), fermentingBrewRv);
        suspendSessionAdapter = suspendController.buildRv(this.getActivity(), suspendBrewRv);
        finishedSessionAdapter = finishedController.buildRv(this.getActivity(), finishedBrewRv);
        Log.e("oncreateView", brewingSessionAdapter + " <- brewingSessionAdapter" + brewStateRv + " <- brewStateRv");

        brewSessionsPresenter = new BrewSessionsPresenter(this);

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(PACK_IS_SENT);
        intentFilter.addAction(LtPushService.CMN_PRGS_PUSH_ACTION);
        intentFilter.addAction(LtPushService.CMD_RPT_ACTION);
        intentFilter.addAction(LtPushService.FILE_SOCKET_IS_READY_ACTION);
        intentFilter.addAction(LtPushService.CMN_PRGS_CHECK_ACTION);
        intentFilter.addAction(LtPushService.BREW_SESSION_PUSH_ACTION);
        intentFilter.addAction(LtPushService.REQUEST_BREW_SESSION_FAILED);
        intentFilter.addAction(LtPushService.CMN_MSG_PUSH_ACTION);
        this.getActivity().registerReceiver(broadcastReceiver, intentFilter);

        decideWeatherReboundScrollViewShouldMove();
        setRefreshListener();
        return view;
    }

    private void decideWeatherReboundScrollViewShouldMove() {
        brewStateTitle.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    reboundScrollView.setCanScroll(true);
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    reboundScrollView.setCanScroll(false);
                }
                return true;
            }
        });
        brewStateRv.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                reboundScrollView.setCanScroll(false);

            }
        });
        brewStateRv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                reboundScrollView.setCanScroll(false);
            }
        });
    }

    private void setRefreshListener() {
        reboundScrollView.setOnRefreshListener(new ReboundScrollView.OnRefreshListener() {
            @Override
            public void onRefresh() {
                activelyRefreshStart = true;
                getBrewHistory();
            }

            @Override
            public void onShowProgress() {
                if (!spinKit.isShown())
                    animateProgressView(View.VISIBLE, R.anim.anim_popup_open_progress);
            }
        });
    }

    public void setActivelyRefreshStart(boolean activelyRefreshStart) {
        this.activelyRefreshStart = activelyRefreshStart;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        this.getActivity().unregisterReceiver(broadcastReceiver);
    }

    public void getBrewHistory() {
        if (brewSessionsPresenter != null)
            brewSessionsPresenter.getBrewHistory();

    }

    @Override
    public void onGetBrewSessionSuccess(List<DBBrewHistory> brewingHistories, List<DBBrewHistory> fermentingHistories) {
        brewingHistoryList.clear();
        fermentingHistoryList.clear();
        this.brewingHistoryList = brewingHistories;
        this.fermentingHistoryList = fermentingHistories;
        this.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (spinKit.isShown())
                    animateProgressView(View.GONE, R.anim.anim_popup_close_progress);
                if (brewingHistoryList.size() != 0) {
                    noBrewingTaskTv.setVisibility(View.GONE);
                } else {
                    noBrewingTaskTv.setVisibility(View.VISIBLE);
                }
                if (fermentingHistoryList.size() != 0) {
                    noFermentingTaskTv.setVisibility(View.GONE);
                } else {
                    noFermentingTaskTv.setVisibility(View.VISIBLE);
                }
                brewingController.setBrewHistories(brewingHistoryList);
                brewingSessionAdapter.setData(brewingHistoryList);
                brewingSessionAdapter.notifyDataSetChanged();

                fermentingController.setBrewHistories(fermentingHistoryList);
                fermentingSessionAdapter.setData(fermentingHistoryList);
                fermentingSessionAdapter.notifyDataSetChanged();

                reqSessionStateByTcpReqQueue();

                showFermentingState();
                if(!activelyRefreshStart)
                    onBrewingSessionListener.onFinishReqBrewHistory();
                activelyRefreshStart = false;
            }
        });

    }

    @Override
    public void onGetFinishedSession(final List<DBBrewHistory> finishedBrewHistories) {
        this.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(finishedBrewHistories == null ||
                        (finishedBrewHistories != null && finishedBrewHistories.size() == 0)){
                    suspendTaskTv.setVisibility(View.VISIBLE);
                }else{
                    suspendTaskTv.setVisibility(View.GONE);

                }
                finishedHistoryList = finishedBrewHistories;
                finishedController.setBrewHistories(finishedBrewHistories);
                finishedSessionAdapter.setData(finishedBrewHistories);
                finishedController.notifyDataSetChanged();
            }
        });

    }

    @Override
    public void onGetSuspendSession(final List<DBBrewHistory> suspendBrewHistories) {
        this.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(suspendBrewHistories == null ||
                        (suspendBrewHistories != null && suspendBrewHistories.size() == 0)){
                    suspendTaskTv.setVisibility(View.VISIBLE);
                }else{
                    suspendTaskTv.setVisibility(View.GONE);

                }
                suspendHistoryList = suspendBrewHistories;
                suspendController.setBrewHistories(suspendBrewHistories);
                suspendSessionAdapter.setData(suspendBrewHistories);
                suspendController.notifyDataSetChanged();
            }
        });
    }

    private void reqSessionStateByTcpReqQueue() {
        for (int i = 0, size = brewingHistoryList.size(); i < size; i++) {
            DBBrewHistory brewHistory = brewingHistoryList.get(i);
            Long formula_id = brewHistory.getFormula_id();
            String formulaId = String.format("%08x", formula_id);
            brewingFormulaIdToPosition.put(formulaId, i);
            onBrewingSessionListener.onReqBrewingSession(brewHistory.getPackage_id());
        }
    }

    private void showFermentingState() {
        int fermentTotalTime = 10 * 24 * 60 * 60;
        for (int i = 0, size = fermentingHistoryList.size(); i < size; i++) {
            DBBrewHistory brewHistory = fermentingHistoryList.get(i);
            brewHistory.setShowStepInfo(false);
            showFermentingTime(fermentTotalTime, brewHistory);

            Long formula_id = brewHistory.getFormula_id();
            String formulaId = String.format("%08x", formula_id);
            fermentingFormulaIdToPosition.put(formulaId, i);
        }
    }

    //TODO 写在服务里面， 后台提醒发酵时间
    private void showFermentingTime(final int fermentTotalTime, final DBBrewHistory brewHistory) {
        if(brewHistory == null)
            return;
        Subscription subscription = sessionProcessingMap.get(brewHistory.getPackage_id());
        if(subscription != null)
            subscription.unsubscribe();

        Subscription subscribe = Observable.interval(0, 30, TimeUnit.SECONDS).flatMap(new Func1<Long, Observable<Long>>() {
            @Override
            public Observable<Long> call(Long aLong) {
                return Observable.create(new Observable.OnSubscribe<Long>() {
                    @Override
                    public void call(Subscriber<? super Long> subscriber) {
                        long fermentingStartTimeStamp = BrewSessionUtils.getFermentingStartTimeStamp(brewHistory.getPackage_id());
                        Log.e("timer", fermentingStartTimeStamp + " s");
                        if (fermentingStartTimeStamp != 0) {
                            long timePassed = System.currentTimeMillis() - fermentingStartTimeStamp;
                            int ratio = (int) (((timePassed / 1000) * 100) / fermentTotalTime);
                            long timeLeft = fermentTotalTime - timePassed / 1000;
                            if (timeLeft > 0) {
                                long day = timeLeft / (60 * 60 * 24);
                                long hour = (timeLeft / (60 * 60)) % 24;
                                long minute = (timeLeft / 60) % 60;
                                String hourStr = hour == 0 ? "" : hour + "小时";
                                String dayStr = day == 0 ? "" : day + "天";
                                brewHistory.setRatio(ratio);
                                brewHistory.setBrewingState("发酵中");
                                brewHistory.setBrewingStageInfo("剩" + dayStr + hourStr + minute + "分钟");
                                subscriber.onNext(timeLeft);
                            } else {
                                brewHistory.setBrewingState("酿造完成");
                                subscriber.onError(new Throwable(Constants.FermentDoneMsg)); // stop interval
                            }
                        } else {
                            brewHistory.setBrewingState("待发酵");
                            subscriber.onError(new Throwable("ferment not started"));  // stop interval

                        }
                    }
                }).subscribeOn(AndroidSchedulers.mainThread());
            }
        }).subscribe(new Subscriber<Long>() {
            @Override
            public void onCompleted() {

            }

            @Override
            public void onError(Throwable e) {
                if (Constants.FermentDoneMsg.equals(e.getMessage())) {
                    brewHistory.setState(4);
                    brewHistory.setBrewingState("酿造完成");
                    DBManager.getInstance().getDBBrewHistoryDao().update(brewHistory);
                    updateFermentingAdapter(brewHistory);

                }

            }

            @Override
            public void onNext(Long aLong) {
                Log.e("onNext", aLong + " s");
                updateFermentingAdapter(brewHistory);

            }
        });
        sessionProcessingMap.put(brewHistory.getPackage_id(), subscribe);

    }

    private void updateFermentingAdapter(DBBrewHistory brewHistory) {
        Long formula_id = brewHistory.getFormula_id();
        String formulaId = String.format("%08x", formula_id);
        Integer position = fermentingFormulaIdToPosition.get(formulaId);
        if(position != null)
            fermentingSessionAdapter.notifyItemChanged(position);
        else
            fermentingSessionAdapter.notifyDataSetChanged();
    }

    private void animateProgressView(int gone, int anim_popup_close) {
        spinKit.setVisibility(gone);
        Animation animation = AnimationUtils.loadAnimation(getContext(), anim_popup_close);
        spinKit.setAnimation(animation);
    }


    @Override
    public void onGetBrewSessionFailed(String code) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (spinKit.isShown())
                    animateProgressView(View.GONE, R.anim.anim_popup_close_progress);
            }
        });
    }

    @Override
    public void onGetRecipeSuccess(List<Recipe> recipes) {
    }

    @Override
    public void onGetRecipeFailed() {
    }

    @Override
    public void onDownloadRecipeSuccess(DBRecipe dbRecipe) {
//        Log.e("onDownloadRecipeSuccess", dbRecipe + "  ---   " +dbRecipe.getBrewSteps());
        Integer position = brewingFormulaIdToPosition.get(dbRecipe.getIdForFn());
        if (position != null && brewingHistoryList != null && brewingHistoryList.size() > position) {
            DBBrewHistory brewHistory = brewingHistoryList.get(position);
            brewHistory.setDBRecipe(dbRecipe);
            brewingSessionAdapter.setData(brewingHistoryList);
            brewingSessionAdapter.notifyItemChanged(position);
        }
        Integer positionForFinishedSession = fermentingFormulaIdToPosition.get(dbRecipe.getIdForFn());
        if (positionForFinishedSession != null && fermentingHistoryList != null && fermentingHistoryList.size() > positionForFinishedSession) {

            DBBrewHistory brewHistory = fermentingHistoryList.get(positionForFinishedSession);
            brewHistory.setDBRecipe(dbRecipe);
            fermentingSessionAdapter.setData(fermentingHistoryList);
            fermentingSessionAdapter.notifyItemChanged(positionForFinishedSession);

        }
    }

    @Override
    public void onDownloadRecipeFailed() {
        ParamStoreUtil.getInstance().setCurrentCreatingRecipe(null);
    }

    @Override
    public void onDownLoadRecipeAfterBrewBegin(DBRecipe dbRecipe) {
        Log.e("onDownLoadRecipe", "calling==========================");
        ParamStoreUtil.getInstance().setCurrentCreatingRecipe(dbRecipe);
        DBBrewHistory brewHistory = new DBBrewHistory();
        brewHistory.setDBRecipe(dbRecipe);
        if (packId != null) {
            brewHistory.setPackage_id(Long.valueOf(packId));
            packId = null;
        }
        brewingHistoryList.add(0, brewHistory);

        if (brewingHistoryList.size() != 0) {
            noBrewingTaskTv.setVisibility(View.GONE);
        }
        brewingSessionAdapter.notifyDataSetChanged();
    }

    public void clearData() {
        brewingHistoryList.clear();
        fermentingHistoryList.clear();
        brewingSessionAdapter.notifyDataSetChanged();
        fermentingSessionAdapter.notifyDataSetChanged();
    }

    public int getBrewingSessionCount() {
        if (brewingHistoryList == null)
            return 0;
        return brewingHistoryList.size();
    }

    public interface onBrewingSessionListener {
        void onReqBrewingSession(Long package_id);

        void unlockLockerToExecuteNextMsg();

        void onFinishReqBrewHistory();

        void onCheckTemp(long package_id);
    }
}
