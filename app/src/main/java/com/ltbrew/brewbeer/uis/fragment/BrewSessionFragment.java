package com.ltbrew.brewbeer.uis.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
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
    private TreeMap<String, DBRecipe> idToRecipeMap = new TreeMap<>();
    private HashMap<String, Integer> brewingFormulaIdToPosition = new HashMap<>();
    private HashMap<String, Integer> finishedFormulaIdToPosition = new HashMap<>();


    public static final String PACK_IS_SENT = "com.ltbrew.beer.AddRecipeActivity.PACK_IS_SENT_TO_DEV";
    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String formula_id = intent.getStringExtra(AddRecipeActivity.FORMULA_ID_EXTRA);
            String recipeName = intent.getStringExtra(AddRecipeActivity.RECIPE_NAME_EXTRA);
            if(brewSessionsPresenter != null)
                brewSessionsPresenter.getRecipeInfo(formula_id);
        }
    };
    private List<BrewHistory> brewingHistoryList;
    private List<BrewHistory> finishedHistoryList;

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
                DBRecipe dbRecipe = (DBRecipe)idToRecipeMap.values().toArray()[layoutPosition];
                ParamStoreUtil.getInstance().setDbRecipe(dbRecipe); //store data to local cache
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
    public void onGetBrewSessionSuccess(List<BrewHistory> brewingHistoryList, List<BrewHistory> finishedHistoryList) {
        this.brewingHistoryList = brewingHistoryList;
        this.finishedHistoryList = finishedHistoryList;
        for (int i = 0, size = brewingHistoryList.size(); i < size; i++) {
            Long formula_id = brewingHistoryList.get(i).getFormula_id();
            brewingFormulaIdToPosition.put(formula_id+"", i);
            brewSessionsPresenter.getRecipeInfo(formula_id+"");
        }

        for (int i = 0, size = finishedHistoryList.size(); i < size; i++) {
            Long formula_id = finishedHistoryList.get(i).getFormula_id();
            finishedFormulaIdToPosition.put(formula_id+"", i);
            brewSessionsPresenter.getRecipeInfo(formula_id+"");
        }
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
        Integer position = brewingFormulaIdToPosition.get(dbRecipe.getFormulaId());
        if(position != null && brewingHistoryList != null && brewingHistoryList.size() > position){
            BrewHistory brewHistory = brewingHistoryList.get(position);
            brewHistory.setDbRecipe(dbRecipe);
            brewingSessionAdapter.notifyItemChanged(position);
        }

        Integer position1 = finishedFormulaIdToPosition.get(dbRecipe.getFormulaId());
        if(position != null && finishedHistoryList != null && finishedHistoryList.size() > position){
            BrewHistory brewHistory = finishedHistoryList.get(position1);
            brewHistory.setDbRecipe(dbRecipe);
            finishedSessionAdapter.notifyItemChanged(position);

        }

        ParamStoreUtil.getInstance().setCurrentCreatingRecipe(dbRecipe);
        idToRecipeMap.put(dbRecipe.getFormulaId()+"", dbRecipe);
        brewingSessionAdapter.setData(idToRecipeMap);
        brewingSessionAdapter.notifyDataSetChanged();
    }

    @Override
    public void onDownloadRecipeFailed() {
        ParamStoreUtil.getInstance().setCurrentCreatingRecipe(null);
    }
}
