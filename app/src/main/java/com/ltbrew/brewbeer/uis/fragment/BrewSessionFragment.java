package com.ltbrew.brewbeer.uis.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.widget.TextView;

import com.github.ybq.android.spinkit.SpinKitView;
import com.ltbrew.brewbeer.R;
import com.ltbrew.brewbeer.interfaceviews.BrewSessionVeiw;
import com.ltbrew.brewbeer.persistence.greendao.DBBrewStep;
import com.ltbrew.brewbeer.persistence.greendao.DBRecipe;
import com.ltbrew.brewbeer.presenter.BrewSessionsPresenter;
import com.ltbrew.brewbeer.presenter.model.BrewHistory;
import com.ltbrew.brewbeer.presenter.model.Recipe;
import com.ltbrew.brewbeer.service.LtPushService;
import com.ltbrew.brewbeer.service.PldForBrewSession;
import com.ltbrew.brewbeer.service.PldForCmnMsg;
import com.ltbrew.brewbeer.service.PldForCmnPrgs;
import com.ltbrew.brewbeer.service.PushMsg;
import com.ltbrew.brewbeer.uis.activity.BrewSessionControlActivity;
import com.ltbrew.brewbeer.uis.adapter.BrewingSessionAdapter;
import com.ltbrew.brewbeer.uis.adapter.viewholder.BaseViewHolder;
import com.ltbrew.brewbeer.uis.utils.BrewSessionUtils;
import com.ltbrew.brewbeer.uis.utils.ParamStoreUtil;
import com.ltbrew.brewbeer.uis.view.ReboundScrollView;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class BrewSessionFragment extends Fragment implements BrewSessionVeiw {

    @BindView(R.id.brewStateRv)
    RecyclerView brewStateRv;
    @BindView(R.id.fermentingBrewRv)
    RecyclerView fermentingBrewRv;
    @BindView(R.id.noBrewingTaskTv)
    TextView noBrewingTaskTv;
    @BindView(R.id.noFermentingTaskTv)
    TextView noFinishedTaskTv;
    @BindView(R.id.reboundScrollView)
    ReboundScrollView reboundScrollView;
    @BindView(R.id.brewStateTitle)
    TextView brewStateTitle;
    @BindView(R.id.spin_kit)
    SpinKitView spinKit;
    public String TAG = this.getClass().getName();

    private BrewingSessionAdapter brewingSessionAdapter;
    private BrewingSessionAdapter fermentingSessionAdapter;

    private BrewSessionsPresenter brewSessionsPresenter;
    private HashMap<String, Integer> brewingFormulaIdToPosition = new HashMap<>();
    private HashMap<String, Integer> fermentingFormulaIdToPosition = new HashMap<>();
    private List<BrewHistory> brewingHistoryList = Collections.EMPTY_LIST;
    private List<BrewHistory> fermentingHistoryList = Collections.EMPTY_LIST;
    private onBrewingSessionListener onBrewingSessionListener;

    public static final String PACK_IS_SENT = "com.ltbrew.beer.AddRecipeActivity.PACK_IS_SENT_TO_DEV";
    private String packId;
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
                        BrewHistory brewHistory = brewingHistoryList.get(i);
                        onBrewingSessionListener.onReqBrewingSession(brewHistory.getPackage_id());
                    }
                }

            } else if (LtPushService.CMN_PRGS_CHECK_ACTION.equals(action)) {
                if(onBrewingSessionListener != null)
                    onBrewingSessionListener.unlockLockerToExecuteNextMsg();
                PushMsg pushMsgObj = intent.getParcelableExtra(LtPushService.PUSH_MSG_EXTRA);
                Log.e(TAG+"CMN_PRGS_CHECK_ACTION", pushMsgObj.toString());
                String st = pushMsgObj.st;
                BrewHistory brewHistory;
                if(st != null) {
                    String package_id = st.split(":")[1];
                    brewHistory = findBrewHistory(package_id);
                    if(brewHistory == null)
                        return;
                }else{
                    brewHistory = brewingHistoryList.get(0);
                }
                brewHistory.setRatio(pushMsgObj.ratio);
                brewHistory.setSi(pushMsgObj.si);
                if(pushMsgObj.des != null && pushMsgObj.des.equals("-1")){
                    pushMsgObj.des = "煮沸";
                }
                brewHistory.setBrewingState(pushMsgObj.des);
                brewHistory.setBrewingStageInfo(null);
                setTimeLeft(pushMsgObj, brewHistory);
                brewHistory.setSt(st);
                brewingSessionAdapter.setData(brewingHistoryList);
                brewingSessionAdapter.notifyDataSetChanged();
            } else if (LtPushService.CMN_PRGS_PUSH_ACTION.equals(action)) {

                PushMsg pushMsgObj = intent.getParcelableExtra(LtPushService.PUSH_MSG_EXTRA);
                PldForCmnPrgs pldForCmnPrgs = intent.getParcelableExtra(LtPushService.PUSH_PLD_EXTRA);
                Log.e(TAG+"CMN_PRGS_PUSH_ACTION", pushMsgObj.toString());
                String st = pldForCmnPrgs.st;
                BrewHistory brewHistory;
                if(st != null) {
                    String package_id = st.split(":")[1];
                    brewHistory = findBrewHistory(package_id);
                    if(brewHistory == null)
                        return;
                }else{
                    if(brewingHistoryList.size() == 0)
                        return;
                    brewHistory = brewingHistoryList.get(0);
                }
                if(brewHistory == null)
                    return;
                Log.e(TAG+"CMN_PRGS_PUSH_ACTION1", brewHistory.toString());
                if(pushMsgObj.des != null && pushMsgObj.des.equals("-1")){
                    pushMsgObj.des = "煮沸";
                }
                brewHistory.setRatio(pldForCmnPrgs.ratio);
                brewHistory.setSi(pldForCmnPrgs.si);
                brewHistory.setBrewingState(pushMsgObj.des);
                brewHistory.setBrewingStageInfo(null);
                setTimeLeft(pushMsgObj, brewHistory);
                brewHistory.setSt(st);
                brewingSessionAdapter.setData(brewingHistoryList);
                brewingSessionAdapter.notifyDataSetChanged();
            } else if (LtPushService.CMD_RPT_ACTION.equals(action)) {

//                BrewHistory brewHistory = brewingHistoryList.get(0);
//                brewHistory.setBrewingState("等待设备开始酿酒");
//                brewingSessionAdapter.notifyDataSetChanged();
            } else if(LtPushService.CMN_MSG_PUSH_ACTION.equals(action)){
                if(onBrewingSessionListener != null)
                    onBrewingSessionListener.unlockLockerToExecuteNextMsg();
                PushMsg pushMsgObj = intent.getParcelableExtra(LtPushService.PUSH_MSG_EXTRA);
                Log.e(TAG+"CMN_MSG_PUSH_ACTION", pushMsgObj.toString());
                PldForCmnMsg pldForCmnMsg = intent.getParcelableExtra(LtPushService.PUSH_PLD_EXTRA);

                int ms = pldForCmnMsg.ms;
                if(ms >= 90){
                    pushMsgObj.des = "煮沸";
                }
                String tk = pldForCmnMsg.tk;
                BrewHistory brewHistory;
                if(tk != null) {
                    String package_id = tk.split(":")[1];
                    brewHistory = findBrewHistory(package_id);
                    if(brewHistory == null)
                        return;
                }else{
                    brewHistory = brewingHistoryList.get(0);
                }
                brewHistory.setMs(ms);
                brewHistory.setBrewingCmnMsg(pushMsgObj.des);
                brewingSessionAdapter.setData(brewingHistoryList);
                brewingSessionAdapter.notifyDataSetChanged();
            }else if(LtPushService.BREW_SESSION_PUSH_ACTION.equals(action)){
                PushMsg pushMsgObj = intent.getParcelableExtra(LtPushService.PUSH_MSG_EXTRA);
                Log.e(TAG+"CMN_MSG_PUSH_ACTION", pushMsgObj.toString());
                PldForBrewSession pldForBrewSession = intent.getParcelableExtra(LtPushService.PUSH_PLD_EXTRA);
                if(pldForBrewSession.state == 1){
                    brewSessionsPresenter.getBrewHistory();
                }

            }else if(LtPushService.REQUEST_BREW_SESSION_FAILED.equals(action)){
                if(onBrewingSessionListener != null)
                    onBrewingSessionListener.unlockLockerToExecuteNextMsg();
            }
        }

        private void setTimeLeft(PushMsg pushMsgObj, BrewHistory brewHistory) {
            if(brewHistory == null)
                return;
            if("糖化中".equals(pushMsgObj.des) || "煮沸中".equals(pushMsgObj.des)){
                long timePassed = System.currentTimeMillis() - BrewSessionUtils.getStepStartTimeStamp();
                DBRecipe dbRecipe = brewHistory.getDbRecipe();
                if(dbRecipe == null)
                    return;
                List<DBBrewStep> brewSteps = dbRecipe.getBrewSteps();
                if(brewSteps != null && brewSteps.size() > pushMsgObj.si) {
                    DBBrewStep dbBrewStep = brewSteps.get(pushMsgObj.si);
                    Integer k = dbBrewStep.getK(); //总时间
                    if(k != null){
                        long timeLeft = k / 60 - timePassed / (60 * 1000);
                        long hourLeft = timeLeft / 60;
                        if(timeLeft > 0) {
                            brewHistory.setBrewingStageInfo("剩" + (hourLeft==0 ? "" : hourLeft+"小时") + timeLeft%60 + "分钟");
                        }
                    }
                }
            }
        }
    };

    private BrewHistory findBrewHistory(String package_id){
        for (int i = 0, size = brewingHistoryList.size(); i < size; i++) {
            BrewHistory brewHistory = brewingHistoryList.get(i);
            if(String.valueOf(brewHistory.getPackage_id()).equals(package_id)){
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
        brewStateRv.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        brewingSessionAdapter = new BrewingSessionAdapter(getContext());
        brewingSessionAdapter.setOnItemClickListener(new BaseViewHolder.OnRvItemClickListener() {
            @Override
            public void onRvItemClick(View v, int layoutPosition) {
                BrewHistory brewHistory = brewingHistoryList.get(layoutPosition);
                ParamStoreUtil.getInstance().setBrewHistory(brewHistory); //store data to local cache
                startBrewControlActivity();

            }
        });
        brewingSessionAdapter.setOnDeleteClickListener(new BrewingSessionAdapter.OnDeleteClickListener() {
            @Override
            public void onDeleteClick(View v, int layoutPosition) {
                brewingHistoryList.remove(layoutPosition);
                brewingSessionAdapter.notifyItemRemoved(layoutPosition);
            }
        });
        brewStateRv.setAdapter(brewingSessionAdapter);

        fermentingBrewRv.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        fermentingSessionAdapter = new BrewingSessionAdapter(getContext());
        fermentingSessionAdapter.setOnItemClickListener(new BaseViewHolder.OnRvItemClickListener() {
            @Override
            public void onRvItemClick(View v, int layoutPosition) {
                BrewHistory brewHistory = fermentingHistoryList.get(layoutPosition);
                ParamStoreUtil.getInstance().setBrewHistory(brewHistory); //store data to local cache
                startBrewControlActivity();
            }
        });
        fermentingBrewRv.setAdapter(fermentingSessionAdapter);


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
                getBrewHistory();
            }

            @Override
            public void onShowProgress() {
                if(!spinKit.isShown())
                    animateProgressView(View.VISIBLE, R.anim.anim_popup_open_progress);
            }
        });
    }


    private void startBrewControlActivity() {
        Intent intent = new Intent();
        intent.setClass(this.getActivity(), BrewSessionControlActivity.class);
        startActivity(intent);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        this.getActivity().unregisterReceiver(broadcastReceiver);
    }


    public void getBrewHistory() {
        if(brewSessionsPresenter != null)
            brewSessionsPresenter.getBrewHistory();
    }

    @Override
    public void onGetBrewSessionSuccess(List<BrewHistory> brewingHistories, List<BrewHistory> finishedHistories) {
        brewingHistoryList.clear();
        fermentingHistoryList.clear();
        this.brewingHistoryList = brewingHistories;
        this.fermentingHistoryList = finishedHistories;
        this.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(spinKit.isShown())
                    animateProgressView(View.GONE, R.anim.anim_popup_close_progress);
                if(brewingHistoryList.size() != 0){
                    noBrewingTaskTv.setVisibility(View.GONE);
                }else{
                    noBrewingTaskTv.setVisibility(View.VISIBLE);
                }
                if(fermentingHistoryList.size() != 0){
                    noFinishedTaskTv.setVisibility(View.GONE);
                }

                brewingSessionAdapter.setData(brewingHistoryList);
                brewingSessionAdapter.notifyDataSetChanged();

                fermentingSessionAdapter.setData(fermentingHistoryList);
                fermentingSessionAdapter.notifyDataSetChanged();

                for (int i = 0, size = brewingHistoryList.size(); i < size; i++) {
                    BrewHistory brewHistory = brewingHistoryList.get(i);
                    Long formula_id = brewHistory.getFormula_id();
                    String formulaId = String.format("%08x", formula_id);
                    brewingFormulaIdToPosition.put(formulaId, i);
                    brewSessionsPresenter.getRecipeInfo(formulaId);
                    onBrewingSessionListener.onReqBrewingSession(brewHistory.getPackage_id());
                }

                int fermentTotalTime = 10 * 24 * 60;
                for (int i = 0, size = fermentingHistoryList.size(); i < size; i++) {
                    BrewHistory brewHistory = fermentingHistoryList.get(i);
                    brewHistory.setShowStepInfo(false);

                    long fermentingStartTimeStamp = BrewSessionUtils.getFermentingStartTimeStamp(brewHistory.getPackage_id());
                    if(fermentingStartTimeStamp != 0){
                        long timePassed = System.currentTimeMillis() - fermentingStartTimeStamp;
                        long timeLeft = fermentTotalTime - timePassed / (60 * 1000);
                        if(timeLeft > 0) {
                            long day = timeLeft/60/24;
                            long hour = timeLeft/60;
                            long minute = timeLeft%60;
                            String hourStr = hour == 0 ? "" : hour + "小时";
                            String dayStr = day == 0 ? "" : day + "天";
                            brewHistory.setBrewingState("发酵中");
                            brewHistory.setBrewingStageInfo("剩" +dayStr + hourStr + minute + "分钟");
                        }
                    }else{
                        brewHistory.setBrewingState("待发酵");

                    }
                    Long formula_id = brewHistory.getFormula_id();
                    String formulaId = String.format("%08x", formula_id);
                    fermentingFormulaIdToPosition.put(formulaId, i);
                    brewSessionsPresenter.getRecipeInfo(formulaId);
                }
            }
        });

    }

    private void animateProgressView(int gone, int anim_popup_close) {
        spinKit.setVisibility(gone);
        Animation animation = android.view.animation.AnimationUtils.loadAnimation(getContext(), anim_popup_close);
        spinKit.setAnimation(animation);
    }


    @Override
    public void onGetBrewSessionFailed(String code) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(spinKit.isShown())
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
            BrewHistory brewHistory = brewingHistoryList.get(position);
            brewHistory.setDbRecipe(dbRecipe);
            brewingSessionAdapter.setData(brewingHistoryList);
            brewingSessionAdapter.notifyItemChanged(position);
        }
        Integer positionForFinishedSession = fermentingFormulaIdToPosition.get(dbRecipe.getIdForFn());
        if (positionForFinishedSession != null && fermentingHistoryList != null && fermentingHistoryList.size() > positionForFinishedSession) {

            BrewHistory brewHistory = fermentingHistoryList.get(positionForFinishedSession);
            brewHistory.setDbRecipe(dbRecipe);
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
        BrewHistory brewHistory = new BrewHistory();
        brewHistory.setDbRecipe(dbRecipe);
        if(packId != null) {
            brewHistory.setPackage_id(Long.valueOf(packId));
            packId = null;
        }
        brewingHistoryList.add(0, brewHistory);

        if(brewingHistoryList.size() != 0){
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

    public interface onBrewingSessionListener {
        void onReqBrewingSession(Long package_id);
        void unlockLockerToExecuteNextMsg();
    }

    public int getBrewingSessionCount(){
        if(brewingHistoryList == null)
            return 0;
        return brewingHistoryList.size();
    }
}
