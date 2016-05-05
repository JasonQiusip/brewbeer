package com.ltbrew.brewbeer.uis.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ltbrew.brewbeer.BrewApp;
import com.ltbrew.brewbeer.R;
import com.ltbrew.brewbeer.interfaceviews.RecipeView;
import com.ltbrew.brewbeer.presenter.RecipePresenter;
import com.ltbrew.brewbeer.uis.adapter.BrewingSessionAdapter;
import com.ltbrew.brewbeer.uis.adapter.FinishedSessionAdapter;
import com.ltbrew.brewbeer.uis.adapter.RecipeAdapter;

import butterknife.BindView;
import butterknife.ButterKnife;

public class RecipeFragment extends Fragment implements RecipeView {

    @BindView(R.id.recipeRv)
    RecyclerView recipeRv;
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
        recipePresenter.getAllRecipes(BrewApp.getInstance().getCurrentDev(), "");
        return view;
    }


    @Override
    public void onGetRecipeSuccess() {

    }

    @Override
    public void onGetRecipeFailed() {

    }
}
