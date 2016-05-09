package com.ltbrew.brewbeer.interfaceviews;

/**
 * Created by 151117a on 2016/5/6.
 */
public interface AddPackView{
    void onAddRecipeToDevSuccess(Integer state, String formula_id, String content);
    void onAddRecipeToDevFailed(String message);
}
