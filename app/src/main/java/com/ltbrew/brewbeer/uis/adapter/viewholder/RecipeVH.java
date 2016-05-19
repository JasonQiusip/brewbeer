package com.ltbrew.brewbeer.uis.adapter.viewholder;

import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.ltbrew.brewbeer.R;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by 151117a on 2016/5/4.
 */
public class RecipeVH extends BaseViewHolder {
    @BindView(R.id.recipeCheckbox)
    public CheckBox recipeCheckbox;
    @BindView(R.id.recipeNameTv)
    public TextView recipeNameTv;
    @BindView(R.id.recipeIv)
    public ImageView recipeIv;
    public RecipeVH(View itemView) {
        super(itemView);
    }
}
