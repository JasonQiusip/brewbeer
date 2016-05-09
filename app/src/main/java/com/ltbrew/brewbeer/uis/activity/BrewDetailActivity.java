package com.ltbrew.brewbeer.uis.activity;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ltbrew.brewbeer.R;
import com.ltbrew.brewbeer.persistence.greendao.DBBrewStep;
import com.ltbrew.brewbeer.persistence.greendao.DBRecipe;
import com.ltbrew.brewbeer.persistence.greendao.DBSlot;
import com.ltbrew.brewbeer.uis.utils.ParamStoreUtil;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class BrewDetailActivity extends AppCompatActivity {

    @BindView(R.id.brewDetailContainer)
    LinearLayout brewDetailContainer;
    @BindView(R.id.backIv)
    ImageView backIv;
    @BindView(R.id.fab)
    FloatingActionButton fab;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_brew_detail);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ButterKnife.bind(this);


        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "功能还未开启，敬请期待", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        DBRecipe dbRecipe = ParamStoreUtil.getInstance().getDbRecipe();
        addItemToContainer("配方详情", "", true);
        addItemToContainer("配方名称", dbRecipe.getName());
        Integer wr = dbRecipe.getWr();
        if (wr != null) {
            addItemToContainer("水温", String.format("%.1f", wr / 5f) + " 度");
        }
        addItemToContainer("加水容积", dbRecipe.getWq() + " 升");
        addItemToContainer("设备槽", "", true);
        List<DBSlot> slots = dbRecipe.getSlots();
        for (DBSlot dbSlot : slots) {
            String slotStepId = dbSlot.getSlotStepId();
            addItemToContainer(slotStepId.replace("s_", "设备槽"), dbSlot.getName());
        }
        addItemToContainer("步骤", "", true);
        List<DBBrewStep> brewSteps = dbRecipe.getBrewSteps();
        for (DBBrewStep dbBrewStep : brewSteps) {
            String stepId = dbBrewStep.getStepId();
            String act = dbBrewStep.getAct();
            if ("boil".equals(act)) {
                addItemToContainer("步骤" + stepId.split("s_")[1], dbBrewStep.getI());
            } else {
                addItemToContainer("步骤" + stepId.split("s_")[1], "投放原料到槽" + dbBrewStep.getSlot());
            }
        }

    }

    private void addItemToContainer(String title, String contentDes) {
        addItemToContainer(title, contentDes, false);
    }

    private void addItemToContainer(String title, String contentDes, boolean isTitle) {

        View view = LayoutInflater.from(this).inflate(R.layout.layout_brew_detail, brewDetailContainer, false);
        TextView brewDetailTitle = (TextView) view.findViewById(R.id.brewDetailTitleTv);
        TextView brewDetailContentTv = (TextView) view.findViewById(R.id.brewDetailContentTv);
        brewDetailTitle.setText(title);
        if (isTitle)
            brewDetailTitle.setTextColor(getResources().getColor(R.color.colorAccent));
        brewDetailContentTv.setText(contentDes);
        brewDetailContainer.addView(view);
    }

    @OnClick(R.id.backIv)
    public void clickBackIv(){
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ParamStoreUtil.getInstance().setDbRecipe(null);
    }
}
