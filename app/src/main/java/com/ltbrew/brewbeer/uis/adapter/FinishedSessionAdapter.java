package com.ltbrew.brewbeer.uis.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ltbrew.brewbeer.R;
import com.ltbrew.brewbeer.uis.adapter.viewholder.BaseViewHolder;
import com.ltbrew.brewbeer.uis.adapter.viewholder.FinishedBrewVH;

import butterknife.BindView;

/**
 * Created by 151117a on 2016/5/4.
 */
public class FinishedSessionAdapter extends RecyclerView.Adapter<FinishedBrewVH> {

    private final Context context;
    private BaseViewHolder.OnRvItemClickListener onRvItemClickListener;


    public FinishedSessionAdapter(Context context) {
        this.context = context;
    }

    @Override
    public FinishedBrewVH onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_finished_session, parent, false);
        FinishedBrewVH finishedBrewVH = new FinishedBrewVH(view);
        finishedBrewVH.setOnRvItemClickListener(onRvItemClickListener);
        return finishedBrewVH;
    }

    @Override
    public void onBindViewHolder(FinishedBrewVH holder, int position) {

    }

    @Override
    public int getItemCount() {
        return 0;
    }

    public void setOnItemClickListener(BaseViewHolder.OnRvItemClickListener onRvItemClickListener){
        this.onRvItemClickListener = onRvItemClickListener;
    }
}
