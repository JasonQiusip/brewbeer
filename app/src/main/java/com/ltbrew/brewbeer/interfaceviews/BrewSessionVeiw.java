package com.ltbrew.brewbeer.interfaceviews;

import com.ltbrew.brewbeer.presenter.model.BrewHistory;

import java.util.List;

/**
 * Created by 151117a on 2016/5/6.
 */
public interface BrewSessionVeiw extends RecipeView {

    void onGetBrewSessionSuccess(List<BrewHistory> brewingHistoryList, List<BrewHistory> finishedHistoryList);
    void onGetBrewSessionFailed(int code);
}
