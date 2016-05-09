package com.ltbrew.brewbeer.uis.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ltbrew.brewbeer.R;
import com.ltbrew.brewbeer.persistence.greendao.DBRecipe;
import com.ltbrew.brewbeer.uis.adapter.viewholder.BaseViewHolder;
import com.ltbrew.brewbeer.uis.adapter.viewholder.BrewingVH;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;

import butterknife.BindView;

/**
 * Created by 151117a on 2016/5/4.
 */
public class BrewingSessionAdapter extends RecyclerView.Adapter<BrewingVH> {

    private final Context context;
    private TreeMap<String, DBRecipe> recipeMap = new TreeMap<>();
    private BaseViewHolder.OnRvItemClickListener onRvItemClickListener;

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
        Collection<DBRecipe> values = recipeMap.values();
        DBRecipe[] dbRecipes = new DBRecipe[values.size()];
        DBRecipe[] recipes = values.toArray(dbRecipes);
        DBRecipe recipe = recipes[position];
        holder.brewingSessionItemTv.setText(recipe.getName());
        holder.brewingState.setText(recipe.getBrewSteps().get(0).getI());
    }

    @Override
    public int getItemCount() {
        return recipeMap.size();
    }

    public void setData(TreeMap<String, DBRecipe> recipeMap) {
        this.recipeMap = recipeMap;
    }

    public void setOnItemClickListener(BaseViewHolder.OnRvItemClickListener onRvItemClickListener){
        this.onRvItemClickListener = onRvItemClickListener;
    }
}
