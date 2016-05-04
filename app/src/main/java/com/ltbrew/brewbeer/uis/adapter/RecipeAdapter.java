package com.ltbrew.brewbeer.uis.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

import com.ltbrew.brewbeer.uis.adapter.viewholder.RecipeVH;

/**
 * Created by 151117a on 2016/5/4.
 */
public class RecipeAdapter extends RecyclerView.Adapter<RecipeVH> {

    private final Context context;

    public RecipeAdapter(Context context){
        this.context = context;
    }

    @Override
    public RecipeVH onCreateViewHolder(ViewGroup parent, int viewType) {
        return null;
    }

    @Override
    public void onBindViewHolder(RecipeVH holder, int position) {

    }

    @Override
    public int getItemCount() {
        return 0;
    }
}
