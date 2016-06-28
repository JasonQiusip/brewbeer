package com.ltbrew.brewbeer.interfaceviews;

import com.ltbrew.brewbeer.persistence.greendao.DBBrewHistory;

import java.util.List;

/**
 * Created by 151117a on 2016/5/6.
 */
public interface BrewSessionVeiw extends RecipeView {

    void onGetBrewSessionSuccess(List<DBBrewHistory> brewingHistoryList, List<DBBrewHistory> finishedHistoryList);
    void onGetFinishedSession(List<DBBrewHistory> finishedBrewHistories);
    void onGetSuspendSession(List<DBBrewHistory> suspendBrewHistories);
    void onGetBrewSessionFailed(String code);
}
