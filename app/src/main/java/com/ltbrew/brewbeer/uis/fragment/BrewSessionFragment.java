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
import com.ltbrew.brewbeer.api.longconnection.process.ParsePackKits;
import com.ltbrew.brewbeer.interfaceviews.BrewSessionVeiw;
import com.ltbrew.brewbeer.persistence.greendao.DBRecipe;
import com.ltbrew.brewbeer.presenter.BrewSessionsPresenter;
import com.ltbrew.brewbeer.presenter.model.BrewHistory;
import com.ltbrew.brewbeer.presenter.model.Recipe;
import com.ltbrew.brewbeer.service.LtPushService;
import com.ltbrew.brewbeer.service.PushMsg;
import com.ltbrew.brewbeer.uis.activity.AddRecipeActivity;
import com.ltbrew.brewbeer.uis.activity.BrewSessionControlActivity;
import com.ltbrew.brewbeer.uis.adapter.BrewingSessionAdapter;
import com.ltbrew.brewbeer.uis.adapter.FinishedSessionAdapter;
import com.ltbrew.brewbeer.uis.adapter.viewholder.BaseViewHolder;
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
    @BindView(R.id.finishedBrewRv)
    RecyclerView finishedBrewRv;
    @BindView(R.id.noBrewingTaskTv)
    TextView noBrewingTaskTv;
    @BindView(R.id.noFinishedTaskTv)
    TextView noFinishedTaskTv;
    @BindView(R.id.reboundScrollView)
    ReboundScrollView reboundScrollView;
    @BindView(R.id.brewStateTitle)
    TextView brewStateTitle;
    @BindView(R.id.spin_kit)
    SpinKitView spinKit;

    private BrewingSessionAdapter brewingSessionAdapter;
    private FinishedSessionAdapter finishedSessionAdapter;
    private BrewSessionsPresenter brewSessionsPresenter;
    private HashMap<String, Integer> brewingFormulaIdToPosition = new HashMap<>();
    private HashMap<String, Integer> finishedFormulaIdToPosition = new HashMap<>();
    private List<BrewHistory> brewingHistoryList = Collections.EMPTY_LIST;
    private List<BrewHistory> finishedHistoryList;
    private onBrewingSessionListener onBrewingSessionListener;

    public static final String PACK_IS_SENT = "com.ltbrew.beer.AddRecipeActivity.PACK_IS_SENT_TO_DEV";
    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (PACK_IS_SENT.equals(action)) {
                String formula_id = intent.getStringExtra(AddRecipeActivity.FORMULA_ID_EXTRA);
                String recipeName = intent.getStringExtra(AddRecipeActivity.RECIPE_NAME_EXTRA);
                if (brewSessionsPresenter != null) {
                    brewSessionsPresenter.getRecipeAfterBrewBegin(formula_id);
                }
            } else if (LtPushService.FILE_SOCKET_IS_READY_ACTION.equals(action)) {
                if (brewingHistoryList.size() != 0) {
//                    for (int i = 0, size = brewingHistoryList.size(); i < size; i++) {
                        BrewHistory brewHistory = brewingHistoryList.get(0);
                        onBrewingSessionListener.onReqBrewingSession(brewHistory.getPackage_id());
//                    }
                }

            } else if (LtPushService.CMN_PRGS_CHECK_ACTION.equals(action)) {
                PushMsg pushMsgObj = intent.getParcelableExtra(LtPushService.PUSH_MSG_EXTRA);
                Log.e("CMN_PRGS_PUSH_ACTION", pushMsgObj.toString());
                String st = pushMsgObj.st;
                BrewHistory brewHistory;
                if(st != null) {
                    String package_id = st.split(":")[1];
                    brewHistory = findBrewHistory(package_id);
                }else{
                    brewHistory = brewingHistoryList.get(0);
                }
                brewHistory.setRatio(pushMsgObj.ratio);
                brewHistory.setSi(pushMsgObj.si);
                brewHistory.setBrewingState(pushMsgObj.body);
                brewHistory.setSt(st);
                brewingSessionAdapter.setData(brewingHistoryList);
                brewingSessionAdapter.notifyDataSetChanged();
            } else if (LtPushService.CMN_PRGS_PUSH_ACTION.equals(action)) {
                if(onBrewingSessionListener != null)
                    onBrewingSessionListener.onReceiveSessionState();
                PushMsg pushMsgObj = intent.getParcelableExtra(LtPushService.PUSH_MSG_EXTRA);
                Log.e("CMN_PRGS_PUSH_ACTION", pushMsgObj.toString());
                String st = pushMsgObj.st;
                BrewHistory brewHistory;
                if(st != null) {
                    String package_id = st.split(":")[1];
                    brewHistory = findBrewHistory(package_id);
                }else{
                    if(brewingHistoryList.size() == 0)
                        return;
                    brewHistory = brewingHistoryList.get(0);
                }
                if(brewHistory == null)
                    return;
                brewHistory.setRatio(pushMsgObj.ratio);
                brewHistory.setSi(pushMsgObj.si);
                brewHistory.setBrewingState(pushMsgObj.body);
                brewHistory.setSt(st);
                brewingSessionAdapter.setData(brewingHistoryList);
                brewingSessionAdapter.notifyDataSetChanged();
            } else if (LtPushService.CMD_RPT_ACTION.equals(action)) {
                BrewHistory brewHistory = brewingHistoryList.get(0);
                brewHistory.setBrewingState("设备已开始酿酒");
                brewingSessionAdapter.notifyDataSetChanged();
            }
        }
    };

    private BrewHistory findBrewHistory(String package_id){
        for (int i = 0, size = brewingHistoryList.size(); i < size; i++) {
            BrewHistory brewHistory = brewingHistoryList.get(i);
            if(package_id != null && ParsePackKits.isNumber(package_id) &&brewHistory.getPackage_id() == Long.valueOf(package_id)){
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
                onBrewingSessionListener.onReqBrewingSession(brewHistory.getPackage_id());
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

        finishedBrewRv.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        finishedSessionAdapter = new FinishedSessionAdapter(getContext());
        finishedSessionAdapter.setOnItemClickListener(new BaseViewHolder.OnRvItemClickListener() {
            @Override
            public void onRvItemClick(View v, int layoutPosition) {
                BrewHistory brewHistory = finishedHistoryList.get(layoutPosition);
                ParamStoreUtil.getInstance().setBrewHistory(brewHistory); //store data to local cache
                startBrewControlActivity();
            }
        });
        finishedBrewRv.setAdapter(finishedSessionAdapter);
        brewSessionsPresenter = new BrewSessionsPresenter(this);

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(PACK_IS_SENT);
        intentFilter.addAction(LtPushService.CMN_PRGS_PUSH_ACTION);
        intentFilter.addAction(LtPushService.CMD_RPT_ACTION);
        intentFilter.addAction(LtPushService.FILE_SOCKET_IS_READY_ACTION);
        intentFilter.addAction(LtPushService.CMN_PRGS_CHECK_ACTION);
        this.getActivity().registerReceiver(broadcastReceiver, intentFilter);

        decideWeatherReboundScrollViewShouldMove();
        setRefreshListener();

        return view;
    }


    private void decideWeatherReboundScrollViewShouldMove() {
        brewStateTitle.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                Log.e("onTouch", event.getAction() + "");
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
        brewSessionsPresenter.getBrewHistory();
    }

    @Override
    public void onGetBrewSessionSuccess(List<BrewHistory> brewingHistories, List<BrewHistory> finishedHistories) {
        this.brewingHistoryList = brewingHistories;
        this.finishedHistoryList = finishedHistories;
        this.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(spinKit.isShown())
                    animateProgressView(View.GONE, R.anim.anim_popup_close_progress);

                if(brewingHistoryList.size() != 0){
                    noBrewingTaskTv.setVisibility(View.GONE);
                }
                if(finishedHistoryList.size() != 0){
                    noFinishedTaskTv.setVisibility(View.GONE);
                }

                brewingSessionAdapter.setData(brewingHistoryList);
                brewingSessionAdapter.notifyDataSetChanged();

                finishedSessionAdapter.setData(finishedHistoryList);
                finishedSessionAdapter.notifyDataSetChanged();

                for (int i = 0, size = brewingHistoryList.size(); i < size; i++) {
                    BrewHistory brewHistory = brewingHistoryList.get(i);
                    Long formula_id = brewHistory.getFormula_id();
                    String formulaId = String.format("%08x", formula_id);
                    brewingFormulaIdToPosition.put(formulaId, i);
                    brewSessionsPresenter.getRecipeInfo(formulaId);
                    onBrewingSessionListener.onReqBrewingSession(brewHistory.getPackage_id());
                }

                for (int i = 0, size = finishedHistoryList.size(); i < size; i++) {
                    Long formula_id = finishedHistoryList.get(i).getFormula_id();
                    String formulaId = String.format("%08x", formula_id);
                    finishedFormulaIdToPosition.put(formulaId, i);
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
        Integer positionForFinishedSession = finishedFormulaIdToPosition.get(dbRecipe.getIdForFn());
        if (positionForFinishedSession != null && finishedHistoryList != null && finishedHistoryList.size() > positionForFinishedSession) {

            BrewHistory brewHistory = finishedHistoryList.get(positionForFinishedSession);
            brewHistory.setDbRecipe(dbRecipe);
            finishedSessionAdapter.setData(finishedHistoryList);
            finishedSessionAdapter.notifyItemChanged(positionForFinishedSession);

        }
    }

    @Override
    public void onDownloadRecipeFailed() {
        ParamStoreUtil.getInstance().setCurrentCreatingRecipe(null);
    }

    @Override
    public void onDownLoadRecipeAfterBrewBegin(DBRecipe dbRecipe) {
        ParamStoreUtil.getInstance().setCurrentCreatingRecipe(dbRecipe);
        BrewHistory brewHistory = new BrewHistory();
        brewHistory.setDbRecipe(dbRecipe);
        brewingHistoryList.add(0, brewHistory);
        brewingSessionAdapter.notifyDataSetChanged();
    }

    public interface onBrewingSessionListener {
        void onReqBrewingSession(Long package_id);
        void onReceiveSessionState();
    }

    public int getBrewingSessionCount(){
        if(brewingHistoryList == null)
            return 0;
        return brewingHistoryList.size();
    }
}
