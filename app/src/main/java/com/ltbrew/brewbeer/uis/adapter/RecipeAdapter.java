package com.ltbrew.brewbeer.uis.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.ltbrew.brewbeer.R;
import com.ltbrew.brewbeer.presenter.model.Recipe;
import com.ltbrew.brewbeer.uis.adapter.viewholder.BaseViewHolder;
import com.ltbrew.brewbeer.uis.adapter.viewholder.RecipeVH;

import java.util.Collections;
import java.util.List;

import butterknife.BindView;

/**
 * Created by 151117a on 2016/5/4.
 */
public class RecipeAdapter extends RecyclerView.Adapter<RecipeVH> {

    private final Context context;
    private List<Recipe> recipes = Collections.EMPTY_LIST;
    private BaseViewHolder.OnRvItemClickListener onRvItemClickListener;

    public RecipeAdapter(Context context) {
        this.context = context;
    }

    @Override
    public RecipeVH onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_recipe, parent, false);
        RecipeVH recipeVH = new RecipeVH(view);
        recipeVH.setOnRvItemClickListener(onRvItemClickListener);
        return recipeVH;
    }

    @Override
    public void onBindViewHolder(RecipeVH holder, int position) {
        final Recipe recipe = recipes.get(position);
        holder.recipeNameTv.setText(recipe.getName());
        holder.recipeCheckbox.setChecked(recipe.isChecked());
        holder.recipeCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                recipe.setChecked(isChecked);
            }
        });
    }

    @Override
    public int getItemCount() {
        return recipes.size();
    }

    public void setData(List<Recipe> recipes) {
        this.recipes = recipes;
    }

    public void setOnItemClickListener(BaseViewHolder.OnRvItemClickListener onRvItemClickListener){
        this.onRvItemClickListener = onRvItemClickListener;
    }
}
