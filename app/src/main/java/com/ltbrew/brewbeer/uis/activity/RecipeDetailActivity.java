package com.ltbrew.brewbeer.uis.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ltbrew.brewbeer.R;
import com.ltbrew.brewbeer.interfaceviews.AddPackView;
import com.ltbrew.brewbeer.persistence.greendao.DBBrewStep;
import com.ltbrew.brewbeer.persistence.greendao.DBRecipe;
import com.ltbrew.brewbeer.persistence.greendao.DBSlot;
import com.ltbrew.brewbeer.presenter.AddPackPresenter;
import com.ltbrew.brewbeer.thirdpartylib.MessageWindow;
import com.ltbrew.brewbeer.uis.utils.ParamSetObserver;
import com.ltbrew.brewbeer.uis.utils.ParamStoreUtil;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class RecipeDetailActivity extends BaseActivity implements AddPackView {

    public static final String OPEN_DEV_START_BREWING = "1";
    @BindView(R.id.recipeDetailLl)
    LinearLayout recipeDetailLl;

    DBRecipe dbRecipe;
    @BindView(R.id.backIv)
    ImageView backIv;
    @BindView(R.id.sendRecipe)
    FloatingActionButton sendRecipe;
    private AddPackPresenter addPackPresenter;
    private String packId;
    private MessageWindow messageWindow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_detail);
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
            showRecipe();
        } else {
            showMsgWindow("提醒", "正在获取配方数据， 请稍后...", null);
            ParamStoreUtil.getInstance().setParamSetObserver(new ParamSetObserver() {
                @Override
                public void onSetRecipe() {
                    showRecipe();
                }
            });
        }

        addPackPresenter = new AddPackPresenter(this);
    }

    private void showRecipe() {
        dbRecipe = ParamStoreUtil.getInstance().getCurrentCreatingRecipe();
        if (dbRecipe == null)
            return;
        addItemToContainer("配方详情", "", true);
        addItemToContainer("配方名称", dbRecipe.getName());
        Integer wr = dbRecipe.getWr();
        if (wr != null) {
            addItemToContainer("水温", String.format("%.1f", wr / 5f) + " 度");
        }
        addItemToContainer("加水容积", dbRecipe.getWq() + " 升");
        List<DBSlot> slots = dbRecipe.getSlots();
        addItemToContainer("步骤", "", true);
        List<DBBrewStep> brewSteps = dbRecipe.getBrewSteps();
        for (DBBrewStep dbBrewStep : brewSteps) {
            String stepId = dbBrewStep.getStepId();
            String act = dbBrewStep.getAct();
            if ("boil".equals(act)) {
                addItemToContainer("步骤" + stepId.split("s_")[1], dbBrewStep.getI());
            } else {
                Integer slot = dbBrewStep.getSlot();
                DBSlot dbSlot = slots.get(slot);
                addItemToContainer("步骤" + stepId.split("s_")[1], "投放" + dbSlot.getName() + "到槽" + slot);
            }
        }
        addItemToContainer("", "");//防止需显示的内容被fab遮挡

    }

    private void addItemToContainer(String title, String contentDes) {
        addItemToContainer(title, contentDes, false);
    }

    private void addItemToContainer(String title, String contentDes, boolean isTitle) {

        View view = LayoutInflater.from(this).inflate(R.layout.layout_brew_detail, recipeDetailLl, false);
        TextView brewDetailTitle = (TextView) view.findViewById(R.id.brewDetailTitleTv);
        TextView brewDetailContentTv = (TextView) view.findViewById(R.id.brewDetailContentTv);
        brewDetailTitle.setText(title);
        if (isTitle)
            brewDetailTitle.setTextColor(getResources().getColor(R.color.colorAccent));
        brewDetailContentTv.setText(contentDes);
        recipeDetailLl.addView(view);
    }

    @OnClick(R.id.sendRecipe)
    public void clickSendRecipeFab() {
        messageWindow = showMsgWindow("提醒", "正在下发原料包...", null);
        addPackPresenter.addPackToDev(packId, OPEN_DEV_START_BREWING);
    }

    @OnClick(R.id.backIv)
    public void clickBackIv() {
        finish();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        ParamStoreUtil.getInstance().setParamSetObserver(null);
    }

    @Override
    public void onAddRecipeToDevSuccess(Integer state, String formula_id, String content) {
        if(messageWindow != null)
            messageWindow.hidePopupWindow();
        if (state == 0) {
            showMsgWindow("提醒", "原料包下发成功", getOnWindowActionListener());
        } else if (state == 1) {
            showMsgWindow("提醒", "原料包无效已被使用", getOnWindowActionListener());
        } else if (state == 2) {
            showMsgWindow("提醒", "请求参数错误， 请联系客服", getOnWindowActionListener());
        } else {
        }

    }

    @NonNull
    private MessageWindow.OnMsgWindowActionListener getOnWindowActionListener() {
        return new MessageWindow.OnMsgWindowActionListener() {
            @Override
            public void onCloseWindow() {
                finish();
            }

            @Override
            public void onClickDetail() {

            }
        };
    }

    @Override
    public void onAddRecipeToDevFailed(String message) {
        showErrorMsg(message);
    }
}
