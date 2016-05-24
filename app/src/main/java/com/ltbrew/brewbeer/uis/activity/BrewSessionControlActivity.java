package com.ltbrew.brewbeer.uis.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
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
import com.ltbrew.brewbeer.service.LtPushService;
import com.ltbrew.brewbeer.service.PushMsg;
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
    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(LtPushService.CMN_PRGS_PUSH_ACTION.equals(action)){
                PushMsg pushMsgObj = intent.getParcelableExtra(LtPushService.PUSH_MSG_EXTRA);
                int si = pushMsgObj.si;
                if(brewHistory != null) {
                    brewHistory.setRatio(pushMsgObj.ratio);
                    brewHistory.setSi(pushMsgObj.si);
                    brewHistory.setBrewingState(pushMsgObj.body);
                    brewHistory.setSt(pushMsgObj.st);
                }
                View view = stepsContainer.getChildAt(si);
                view.setBackgroundColor(getResources().getColor(R.color.colorAccent));
                curState.setText(pushMsgObj.body);
            }else if (LtPushService.CMN_PRGS_CHECK_ACTION.equals(action)) {
                PushMsg pushMsgObj = intent.getParcelableExtra(LtPushService.PUSH_MSG_EXTRA);
                Log.e("CMN_PRGS_PUSH_ACTION", pushMsgObj.toString());
                String st = pushMsgObj.st;
                if(brewHistory != null) {
                    brewHistory.setRatio(pushMsgObj.ratio);
                    brewHistory.setSi(pushMsgObj.si);
                    brewHistory.setBrewingState(pushMsgObj.body);
                    brewHistory.setSt(st);
                }
                int si = pushMsgObj.si;
                View view = stepsContainer.getChildAt(si);
                view.setBackgroundColor(getResources().getColor(R.color.colorAccent));
                curState.setText(pushMsgObj.body);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_brew_session_control);
        ButterKnife.bind(this);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(LtPushService.CMN_PRGS_CHECK_ACTION);
        intentFilter.addAction(LtPushService.CMN_PRGS_PUSH_ACTION);
        intentFilter.addAction(LtPushService.FILE_SOCKET_IS_READY_ACTION);
        registerReceiver(broadcastReceiver, intentFilter);
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
                int temp = dbBrewStep.getT() / 5;
                if(temp < 100) {
                    addItemToContainer("加热到" + temp + "度，" + dbBrewStep.getK() / 60 + "分钟", "");
                }else{
                    addItemToContainer("煮沸，" + dbBrewStep.getK() / 60 + "分钟", "");
                }

            } else {
                Integer slot = dbBrewStep.getSlot();
                if(slots.size() < slot)
                    continue;
                if(slot - 1 < 0)
                    continue;
                DBSlot dbSlot = slots.get(slot - 1);
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(broadcastReceiver);
    }
}
