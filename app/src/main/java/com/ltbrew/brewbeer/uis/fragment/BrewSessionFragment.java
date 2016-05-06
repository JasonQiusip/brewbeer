package com.ltbrew.brewbeer.uis.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ltbrew.brewbeer.R;
import com.ltbrew.brewbeer.interfaceviews.BrewSessionVeiw;
import com.ltbrew.brewbeer.persistence.greendao.DBRecipe;
import com.ltbrew.brewbeer.presenter.BrewSessionsPresenter;
import com.ltbrew.brewbeer.presenter.model.Recipe;
import com.ltbrew.brewbeer.uis.activity.AddRecipeActivity;
import com.ltbrew.brewbeer.uis.adapter.BrewingSessionAdapter;
import com.ltbrew.brewbeer.uis.adapter.FinishedSessionAdapter;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class BrewSessionFragment extends Fragment implements BrewSessionVeiw {

    @BindView(R.id.brewStateRv)
    RecyclerView brewStateRv;
    @BindView(R.id.finishedBrewRv)
    RecyclerView finishedBrewRv;
    private BrewingSessionAdapter brewingSessionAdapter;
    private FinishedSessionAdapter finishedSessionAdapter;
    private BrewSessionsPresenter brewSessionsPresenter;
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
        brewStateRv.setAdapter(brewingSessionAdapter);

        finishedBrewRv.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        finishedSessionAdapter = new FinishedSessionAdapter(getContext());
        finishedBrewRv.setAdapter(finishedSessionAdapter);
        brewSessionsPresenter = new BrewSessionsPresenter(this);
        return view;
    }

    public void getBrewHistory() {
        brewSessionsPresenter.getBrewHistory();
    }

    @Override
    public void onGetBrewSessionSuccess() {

    }

    @Override
    public void onGetBrewSessionFailed() {

    }

    @Override
    public void onGetRecipeSuccess(List<Recipe> recipes) {

    }

    @Override
    public void onGetRecipeFailed() {

    }

    @Override
    public void onDownloadRecipeSuccess(DBRecipe dbRecipe) {

    }

    @Override
    public void onDownloadRecipeFailed() {

    }
}
