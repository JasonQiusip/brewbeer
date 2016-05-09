package com.ltbrew.brewbeer.interfaceviews;

import com.ltbrew.brewbeer.persistence.greendao.DBRecipe;
import com.ltbrew.brewbeer.presenter.model.Recipe;

import java.util.List;
import java.util.TreeMap;

/**
 * Created by 151117a on 2016/5/5.
 */
public interface RecipeView {

    void onGetRecipeSuccess(List<Recipe> recipes);
    void onGetRecipeFailed();

    void onDownloadRecipeSuccess(DBRecipe dbRecipe);
    void onDownloadRecipeFailed();

    void onDownLoadRecipeAfterBrewBegin(DBRecipe dbRecipe);
}
