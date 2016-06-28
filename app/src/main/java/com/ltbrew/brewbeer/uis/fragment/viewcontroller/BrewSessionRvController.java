package com.ltbrew.brewbeer.uis.fragment.viewcontroller;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.ltbrew.brewbeer.persistence.greendao.DBBrewHistory;
import com.ltbrew.brewbeer.presenter.util.DBManager;
import com.ltbrew.brewbeer.uis.activity.BrewSessionControlActivity;
import com.ltbrew.brewbeer.uis.adapter.BrewingSessionAdapter;
import com.ltbrew.brewbeer.uis.adapter.viewholder.BaseViewHolder;
import com.ltbrew.brewbeer.uis.utils.ParamStoreUtil;

import java.util.List;

/**
 * Created by qiusiping on 16/6/26.
 */
public class BrewSessionRvController {

    private RecyclerView rv;
    private BrewingSessionAdapter brewingSessionAdapter;
    private List<DBBrewHistory> brewHistories;
    private Context context;
    private int type;

    public BrewSessionRvController( List<DBBrewHistory> brewHistories, int type){
        this.type = type;
        this.brewHistories = brewHistories;
    }

    public BrewingSessionAdapter buildRv(Context context, RecyclerView rv){
        this.context = context;
        rv.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false));
        this.brewingSessionAdapter = new BrewingSessionAdapter(context);
        this.brewingSessionAdapter.setOnItemClickListener(new BaseViewHolder.OnRvItemClickListener() {
            @Override
            public void onRvItemClick(View v, int layoutPosition) {

                DBBrewHistory brewHistory = brewHistories.get(layoutPosition);
                ParamStoreUtil.getInstance().setBrewHistory(brewHistory); //store data to local cache
                startBrewControlActivity();

            }
        });
        this.brewingSessionAdapter.setOnDeleteClickListener(new BrewingSessionAdapter.OnDeleteClickListener() {
            @Override
            public void onDeleteClick(View v, int layoutPosition) {
                DBBrewHistory dbBrewHistory = brewHistories.get(layoutPosition);
                DBManager.getInstance().getDBBrewHistoryDao().delete(dbBrewHistory);
                brewHistories.remove(layoutPosition);
                brewingSessionAdapter.notifyItemRemoved(layoutPosition);
            }
        });
        this.brewingSessionAdapter.setData(brewHistories);
        rv.setAdapter(brewingSessionAdapter);
        return brewingSessionAdapter;
    }

    public void setBrewHistories(List<DBBrewHistory> brewHistories){
        this.brewHistories = brewHistories;
    }

    private void startBrewControlActivity() {
        if(context == null)
            return;
        Intent intent = new Intent();
        intent.setClass(context, BrewSessionControlActivity.class);
        intent.putExtra("type", type);
        context.startActivity(intent);
    }

    public void notifyDataSetChanged(){
        brewingSessionAdapter.notifyDataSetChanged();
    }

    public void setType(int type) {
        this.type = type;
    }
}
