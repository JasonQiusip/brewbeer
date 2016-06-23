package com.ltbrew.brewbeer.uis.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.ltbrew.brewbeer.R;
import com.ltbrew.brewbeer.persistence.greendao.DBBrewStep;
import com.ltbrew.brewbeer.persistence.greendao.DBRecipe;
import com.ltbrew.brewbeer.persistence.greendao.DBSlot;
import com.ltbrew.brewbeer.presenter.model.BrewHistory;
import com.ltbrew.brewbeer.thirdpartylib.MessageWindow;
import com.ltbrew.brewbeer.uis.utils.ParamSetObserver;
import com.ltbrew.brewbeer.uis.utils.ParamStoreUtil;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class RecipeStepsActivity extends BaseActivity {

    @BindView(R.id.backIv)
    ImageView backIv;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.brewStepDes)
    TextView brewStepDes;
    @BindView(R.id.materialsReadyBtn)
    Button materialsReadyBtn;

    private MessageWindow messageWindow;
    private DBRecipe dbRecipe;
    private String packId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_steps);
        Intent intent = getIntent();
        packId = intent.getStringExtra(AddRecipeActivity.PACK_ID_EXTRA);

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
            dbRecipe = ParamStoreUtil.getInstance().getCurrentCreatingRecipe();
            if(dbRecipe == null)
                return;
            showRecipe();
        } else {
//            messageWindow = showMsgWindow("提醒", "正在获取配方数据， 请稍后...", null);
            ParamStoreUtil.getInstance().setParamSetObserver(new ParamSetObserver() {
                @Override
                public void onSetRecipe() {
                    dbRecipe = ParamStoreUtil.getInstance().getCurrentCreatingRecipe();
                    if(dbRecipe == null) {
                        messageWindow.hidePopupWindow();
                        showSnackBar("获取配方数据失败");
                    }
                    showRecipe();
                }
            });
        }

    }

    private void showRecipe() {


        StringBuilder desStr = new StringBuilder();
        Integer wr = dbRecipe.getWr();
        Integer wq = dbRecipe.getWq();
        desStr.append("步骤1： 如有麦芽，将麦芽投放至糖化槽中\n");
        desStr.append("步骤2： 将" + wq + "L水装在水槽中\n");

        List<DBSlot> slots = dbRecipe.getSlots();
        List<DBBrewStep> brewSteps = dbRecipe.getBrewSteps();
        int i = 3;
        for (DBBrewStep dbBrewStep : brewSteps) {
            String stepId = dbBrewStep.getStepId();
            stepId = stepId.split("s_")[1];
            int id = Integer.parseInt(stepId, 16);
            String act = dbBrewStep.getAct();

            if (!"boil".equals(act)) {

                Integer slot = dbBrewStep.getSlot();
                if (slots.size() < slot)
                    continue;
                DBSlot dbSlot = slots.get(slot - 1);
                desStr.append("步骤" + (i++) + "： 将" + dbSlot.getName() + "放在小槽" + slot + "中\n");
            }
        }
        brewStepDes.setText(desStr.toString());
    }

    @OnClick(R.id.materialsReadyBtn)
    public void ClickFinishBtn(){
        BrewHistory brewHistory = new BrewHistory();
        brewHistory.setDbRecipe(dbRecipe);
        brewHistory.setPackage_id(Long.valueOf(packId));
        ParamStoreUtil.getInstance().setBrewHistory(brewHistory); //store data to local cache
        Intent intent = new Intent(this, BrewSessionControlActivity.class);
        intent.putExtra("From", "recipeStepsAty");
        startActivity(intent);
        finish();
    }

}
