package com.ltbrew.brewbeer.uis.activity;

import android.os.Bundle;
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
import com.ltbrew.brewbeer.presenter.model.BrewHistory;
import com.ltbrew.brewbeer.uis.utils.ParamStoreUtil;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class BrewSessionControlActivity extends AppCompatActivity {

    @BindView(R.id.recipeName)
    TextView recipeName;
    @BindView(R.id.curState)
    TextView curState;
    @BindView(R.id.stepsContainer)
    LinearLayout stepsContainer;
    private DBRecipe dbRecipe;
    private ImageView backIv;
    private BrewHistory brewHistory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_brew_session_control);
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
        showRecipe();
    }

    private void showRecipe() {
        brewHistory = ParamStoreUtil.getInstance().getBrewHistory();
        dbRecipe = brewHistory.getDbRecipe();
        if(dbRecipe == null)
            return;
        recipeName.setText(dbRecipe.getName());
        if(brewHistory.getBrewingState() != null) {
            curState.setText(brewHistory.getBrewingState());
        }
        List<DBSlot> slots = dbRecipe.getSlots();
        List<DBBrewStep> brewSteps = dbRecipe.getBrewSteps();
        int i = 0;
        for (DBBrewStep dbBrewStep : brewSteps) {
            String stepId = dbBrewStep.getStepId();
            String act = dbBrewStep.getAct();
            if ("boil".equals(act)) {
                if(i == 0)
                {
                    curState.setText(dbBrewStep.getI());
                }
                addItemToContainer(dbBrewStep.getI(), "");
            } else {
                Integer slot = dbBrewStep.getSlot();
                DBSlot dbSlot = slots.get(slot);
                String addMaterialToSlot = "投放" + dbSlot.getName() + "到槽" + slot;
                addItemToContainer(addMaterialToSlot, "");
            }
            i++;
        }

    }

    private void addItemToContainer(String title, String contentDes) {
        addItemToContainer(title, contentDes, false);
    }

    private void addItemToContainer(String title, String contentDes, boolean isTitle) {

        View view = LayoutInflater.from(this).inflate(R.layout.layout_brew_detail, stepsContainer, false);
        TextView brewDetailTitle = (TextView) view.findViewById(R.id.brewDetailTitleTv);
        TextView brewDetailContentTv = (TextView) view.findViewById(R.id.brewDetailContentTv);
        brewDetailTitle.setText(title);
        if (isTitle)
            brewDetailTitle.setTextColor(getResources().getColor(R.color.colorAccent));
        brewDetailContentTv.setText(contentDes);
        stepsContainer.addView(view);
    }

}
