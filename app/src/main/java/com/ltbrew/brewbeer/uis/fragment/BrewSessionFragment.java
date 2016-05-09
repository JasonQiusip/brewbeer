package com.ltbrew.brewbeer.uis.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ltbrew.brewbeer.R;
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

import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;

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

    private BrewingSessionAdapter brewingSessionAdapter;
    private FinishedSessionAdapter finishedSessionAdapter;
    private BrewSessionsPresenter brewSessionsPresenter;
    private HashMap<String, Integer> brewingFormulaIdToPosition = new HashMap<>();
    private HashMap<String, Integer> finishedFormulaIdToPosition = new HashMap<>();


    public static final String PACK_IS_SENT = "com.ltbrew.beer.AddRecipeActivity.PACK_IS_SENT_TO_DEV";
    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(PACK_IS_SENT.equals(action)) {
                String formula_id = intent.getStringExtra(AddRecipeActivity.FORMULA_ID_EXTRA);
                String recipeName = intent.getStringExtra(AddRecipeActivity.RECIPE_NAME_EXTRA);
                if (brewSessionsPresenter != null) {
                    brewSessionsPresenter.getRecipeAfterBrewBegin(formula_id);
                }
            }else if(LtPushService.CMN_PRGS_PUSH_ACTION.equals(action)){
                PushMsg pushMsgObj = intent.getParcelableExtra(LtPushService.PUSH_MSG_EXTRA);
                BrewHistory brewHistory = brewingHistoryList.get(0);
                brewHistory.setBrewingState(pushMsgObj.body);
                brewingSessionAdapter.notifyDataSetChanged();
            }else if(LtPushService.CMD_RPT_ACTION.equals(action)){
                BrewHistory brewHistory = brewingHistoryList.get(0);
                brewHistory.setBrewingState("设备已开始酿酒");
                brewingSessionAdapter.notifyDataSetChanged();
            }
        }
    };
    private List<BrewHistory> brewingHistoryList;
    private List<BrewHistory> finishedHistoryList;
    private BrewSessionFragment.onBrewingSessionClickListener onBrewingSessionClickListener;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        onBrewingSessionClickListener = (BrewSessionFragment.onBrewingSessionClickListener) context;
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
                onBrewingSessionClickListener.onBrewingSessionClick();
                BrewHistory brewHistory = brewingHistoryList.get(layoutPosition);
                ParamStoreUtil.getInstance().setBrewHistory(brewHistory); //store data to local cache
                startBrewControlActivity();
            }
        });
        brewStateRv.setAdapter(brewingSessionAdapter);

        finishedBrewRv.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        finishedSessionAdapter = new FinishedSessionAdapter(getContext());
        finishedSessionAdapter.setOnItemClickListener(new BaseViewHolder.OnRvItemClickListener() {
            @Override
            public void onRvItemClick(View v, int layoutPosition) {

            }
        });
        finishedBrewRv.setAdapter(finishedSessionAdapter);
        brewSessionsPresenter = new BrewSessionsPresenter(this);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(PACK_IS_SENT);
        intentFilter.addAction(LtPushService.CMN_PRGS_PUSH_ACTION);
        intentFilter.addAction(LtPushService.CMD_RPT_ACTION);
        this.getActivity().registerReceiver(broadcastReceiver, intentFilter);
        return view;
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
        Log.e("onGetBrewSessionSuccess", "brewingHistoryList "+brewingHistories+"finishedHistoryList "+finishedHistories);
        this.brewingHistoryList = brewingHistories;
        this.finishedHistoryList = finishedHistories;
        this.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                brewingSessionAdapter.setData(brewingHistoryList);
                brewingSessionAdapter.notifyDataSetChanged();

                finishedSessionAdapter.setData(finishedHistoryList);
                finishedSessionAdapter.notifyDataSetChanged();
                for (int i = 0, size = brewingHistoryList.size(); i < size; i++) {
                    Long formula_id = brewingHistoryList.get(i).getFormula_id();
                    String formulaId = String.format("%08x", formula_id);
                    brewingFormulaIdToPosition.put(formulaId, i);
                    brewSessionsPresenter.getRecipeInfo(formulaId);
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


    @Override
    public void onGetBrewSessionFailed(int code) {
    }

    @Override
    public void onGetRecipeSuccess(List<Recipe> recipes) {
    }

    @Override
    public void onGetRecipeFailed() {
    }

    @Override
    public void onDownloadRecipeSuccess(DBRecipe dbRecipe) {
        Log.e("onDownloadRecipeSuccess", dbRecipe.getIdForFn());
        Integer position = brewingFormulaIdToPosition.get(dbRecipe.getIdForFn());
        if(position != null && brewingHistoryList != null && brewingHistoryList.size() > position){
            BrewHistory brewHistory = brewingHistoryList.get(position);
            brewHistory.setDbRecipe(dbRecipe);
            brewingSessionAdapter.setData(brewingHistoryList);
            brewingSessionAdapter.notifyItemChanged(position);
        }
        Integer positionForFinishedSession = finishedFormulaIdToPosition.get(dbRecipe.getIdForFn());
        if(positionForFinishedSession != null && finishedHistoryList != null && finishedHistoryList.size() > positionForFinishedSession){

            BrewHistory brewHistory = finishedHistoryList.get(positionForFinishedSession);
            brewHistory.setDbRecipe(dbRecipe);
            finishedSessionAdapter.setData(finishedHistoryList);
            brewingSessionAdapter.notifyItemChanged(positionForFinishedSession);

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

    public interface onBrewingSessionClickListener{
        void onBrewingSessionClick();
    }
}
