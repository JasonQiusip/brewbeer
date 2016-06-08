package com.ltbrew.brewbeer.uis.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.ltbrew.brewbeer.R;
import com.ltbrew.brewbeer.persistence.greendao.DBRecipe;
import com.ltbrew.brewbeer.thirdpartylib.MessageWindow;
import com.ltbrew.brewbeer.uis.utils.ParamSetObserver;
import com.ltbrew.brewbeer.uis.utils.ParamStoreUtil;

import butterknife.BindView;
import butterknife.ButterKnife;

public class RecipeStepsActivity extends BaseActivity {

    @BindView(R.id.backIv)
    ImageView backIv;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.brewStepDes)
    TextView brewStepDes;
    private MessageWindow messageWindow;
    private DBRecipe dbRecipe;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_steps);
        ButterKnife.bind(this);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        backIv = (ImageView) toolbar.findViewById(R.id.backIv);
        backIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        boolean isRecipeSet = ParamStoreUtil.getInstance().isRecipeSet;
        if (isRecipeSet) {//如果但前获取不到值
            showRecipe();
        } else {
            messageWindow = showMsgWindow("提醒", "正在获取配方数据， 请稍后...", null);
            ParamStoreUtil.getInstance().setParamSetObserver(new ParamSetObserver() {
                @Override
                public void onSetRecipe() {
                    showRecipe();
                }
            });
        }

    }

    private void showRecipe() {
        dbRecipe = ParamStoreUtil.getInstance().getCurrentCreatingRecipe();
        brewStepDes.setText("");
    }

}
