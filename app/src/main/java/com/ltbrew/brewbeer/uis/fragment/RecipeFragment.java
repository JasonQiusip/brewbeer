package com.ltbrew.brewbeer.uis.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ltbrew.brewbeer.R;
import com.ltbrew.brewbeer.interfaceviews.RecipeView;
import com.ltbrew.brewbeer.persistence.greendao.DBRecipe;
import com.ltbrew.brewbeer.presenter.RecipePresenter;
import com.ltbrew.brewbeer.presenter.model.Recipe;
import com.ltbrew.brewbeer.uis.adapter.RecipeAdapter;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class RecipeFragment extends Fragment implements RecipeView {

    @BindView(R.id.recipeRv)
    RecyclerView recipeRv;
    @BindView(R.id.recipeRefreshLayout)
    SwipeRefreshLayout recipeRefreshLayout;
    private RecipeAdapter recipeAdapter;
    private RecipePresenter recipePresenter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_recipe, container, false);
        ButterKnife.bind(this, view);
        recipeRv.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        recipeAdapter = new RecipeAdapter(getContext());
        recipeRv.setAdapter(recipeAdapter);
        recipePresenter = new RecipePresenter(this);
        recipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getAllRecipes();
            }
        });
        return view;
    }

    public void getAllRecipes() {
        recipeRefreshLayout.setRefreshing(true);
        recipePresenter.getRecipes("");
    }


    @Override
    public void onGetRecipeSuccess(List<Recipe> recipes) {
        recipeRefreshLayout.setRefreshing(false);
        recipeAdapter.setData(recipes);
        recipeAdapter.notifyDataSetChanged();
    }

    @Override
    public void onGetRecipeFailed() {
        recipeRefreshLayout.setRefreshing(false);
    }

    @Override
    public void onDownloadRecipeSuccess(DBRecipe dbRecipe) {

    }

    @Override
    public void onDownloadRecipeFailed() {

    }
}
