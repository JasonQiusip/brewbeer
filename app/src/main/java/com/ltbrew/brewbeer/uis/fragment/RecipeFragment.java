package com.ltbrew.brewbeer.uis.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ltbrew.brewbeer.R;
import com.ltbrew.brewbeer.interfaceviews.RecipeView;
import com.ltbrew.brewbeer.persistence.greendao.DBRecipe;
import com.ltbrew.brewbeer.presenter.RecipePresenter;
import com.ltbrew.brewbeer.presenter.model.Recipe;
import com.ltbrew.brewbeer.thirdpartylib.MessageWindow;
import com.ltbrew.brewbeer.uis.activity.BrewDetailActivity;
import com.ltbrew.brewbeer.uis.adapter.RecipeAdapter;
import com.ltbrew.brewbeer.uis.adapter.viewholder.BaseViewHolder;
import com.ltbrew.brewbeer.uis.utils.ParamStoreUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;


public class RecipeFragment extends Fragment implements RecipeView, BaseViewHolder.OnRvItemClickListener {

    private static final String TAG = "RecipeFragment";
    @BindView(R.id.recipeRv)
    RecyclerView recipeRv;
    @BindView(R.id.recipeRefreshLayout)
    SwipeRefreshLayout recipeRefreshLayout;
    @BindView(R.id.fab)
    FloatingActionButton fab;
    private RecipeAdapter recipeAdapter;
    private RecipePresenter recipePresenter;
    private HashMap<String, DBRecipe> dbRecipeHashMap = new HashMap<>();
    private List<Recipe> recipes = new ArrayList<>();
    private MessageWindow messageWindow;
    private Handler handler = new Handler();

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

    }

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
        fab.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                Log.e("","fdsafdadsfsdafdsfa");
            }
        });
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
        recipeAdapter.setOnItemClickListener(this);
        return view;
    }

    public void getAllRecipes() {
        recipeRefreshLayout.setRefreshing(true);
        recipePresenter.getRecipes("");
    }


    @Override
    public void onGetRecipeSuccess(List<Recipe> recipes) {
        this.recipes = recipes;
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
        Log.e(TAG, "onDownloadRecipeSuccess  " + dbRecipe.getIdForFn());

        dbRecipeHashMap.put(dbRecipe.getIdForFn(), dbRecipe);
    }

    @Override
    public void onDownloadRecipeFailed() {

    }

    @Override
    public void onRvItemClick(View v, int layoutPosition) {
        Recipe recipe = recipes.get(layoutPosition);
        String id = recipe.getId();
        Log.e(TAG, "itemClick  " + id);
        DBRecipe dbRecipe = dbRecipeHashMap.get(id);
        if (dbRecipe == null) {
            if (messageWindow != null)
                return;
            messageWindow = new MessageWindow(this.getActivity()).setupWindow().showMessageWindow("提醒", "该配方还没有详情描述").setOnMsgWindowActionListener(new MessageWindow.OnMsgWindowActionListener() {
                @Override
                public void onCloseWindow() {
                    handler.removeCallbacksAndMessages(null);
                    messageWindow = null;
                }

                @Override
                public void onClickDetail() {

                }
            });
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    messageWindow.hidePopupWindow();
                    messageWindow = null;
                }
            }, 1500);
            return;
        }
        ParamStoreUtil.getInstance().setDbRecipe(dbRecipe);
        startBrewDetailActivity();
    }

    private void startBrewDetailActivity() {
        Intent intent = new Intent();
        intent.setClass(this.getActivity(), BrewDetailActivity.class);
        startActivity(intent);
    }

}
