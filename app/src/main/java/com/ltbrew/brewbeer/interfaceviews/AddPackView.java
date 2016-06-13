package com.ltbrew.brewbeer.interfaceviews;

import com.ltbrew.brewbeer.persistence.greendao.DBRecipe;
import com.ltbrew.brewbeer.presenter.model.Recipe;

import java.util.List;

/**
 * Created by 151117a on 2016/5/6.
 */
public interface AddPackView extends RecipeView {
    void onAddRecipeToDevSuccess(Integer state, String formula_id, String content);
    void onAddRecipeToDevFailed(String message);

    @Override
    void onGetRecipeSuccess(List<Recipe> recipes);

    @Override
    void onGetRecipeFailed();

    @Override
    void onDownloadRecipeSuccess(DBRecipe dbRecipe);

    @Override
    void onDownloadRecipeFailed();

    @Override
    void onDownLoadRecipeAfterBrewBegin(DBRecipe dbRecipe);
}
