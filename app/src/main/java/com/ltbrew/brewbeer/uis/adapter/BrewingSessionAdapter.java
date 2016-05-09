package com.ltbrew.brewbeer.uis.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ltbrew.brewbeer.R;
import com.ltbrew.brewbeer.persistence.greendao.DBRecipe;
import com.ltbrew.brewbeer.presenter.model.BrewHistory;
import com.ltbrew.brewbeer.uis.adapter.viewholder.BaseViewHolder;
import com.ltbrew.brewbeer.uis.adapter.viewholder.BrewingVH;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;

import butterknife.BindView;

/**
 * Created by 151117a on 2016/5/4.
 */
public class BrewingSessionAdapter extends RecyclerView.Adapter<BrewingVH> {

    private final Context context;
    private BaseViewHolder.OnRvItemClickListener onRvItemClickListener;
    private List<BrewHistory> brewingHitories = Collections.EMPTY_LIST;

    public BrewingSessionAdapter(Context context) {
        this.context = context;
    }

    @Override
    public BrewingVH onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_brewing_session, parent, false);
        BrewingVH brewingVH = new BrewingVH(view);
        brewingVH.setOnRvItemClickListener(onRvItemClickListener);
        return brewingVH;
    }

    @Override
    public void onBindViewHolder(BrewingVH holder, int position) {

        BrewHistory brewHistory = brewingHitories.get(position);
        DBRecipe recipe = brewHistory.getDbRecipe();
        if(recipe != null) {
            holder.brewingSessionItemTv.setText(recipe.getName());
        }
        holder.brewingState.setText(brewHistory.getBrewingState());

    }

    @Override
    public int getItemCount() {
        return brewingHitories.size();
    }

    public void setData(List<BrewHistory> brewingHitories) {
        this.brewingHitories = brewingHitories;
    }

    public void setOnItemClickListener(BaseViewHolder.OnRvItemClickListener onRvItemClickListener){
        this.onRvItemClickListener = onRvItemClickListener;
    }
}
