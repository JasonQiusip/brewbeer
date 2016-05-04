package com.ltbrew.brewbeer.uis.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ltbrew.brewbeer.R;
import com.ltbrew.brewbeer.uis.adapter.viewholder.BrewingVH;

import butterknife.BindView;

/**
 * Created by 151117a on 2016/5/4.
 */
public class BrewingSessionAdapter extends RecyclerView.Adapter<BrewingVH> {

    private final Context context;

    public BrewingSessionAdapter(Context context) {
        this.context = context;
    }

    @Override
    public BrewingVH onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_brewing_session, parent, false);
        BrewingVH brewingVH = new BrewingVH(view);
        return brewingVH;
    }

    @Override
    public void onBindViewHolder(BrewingVH holder, int position) {

    }

    @Override
    public int getItemCount() {
        return 1;
    }
}
