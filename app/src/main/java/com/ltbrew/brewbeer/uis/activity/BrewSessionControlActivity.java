package com.ltbrew.brewbeer.uis.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ltbrew.brewbeer.R;
import com.ltbrew.brewbeer.persistence.greendao.DBBrewStep;
import com.ltbrew.brewbeer.persistence.greendao.DBRecipe;
import com.ltbrew.brewbeer.persistence.greendao.DBSlot;
import com.ltbrew.brewbeer.presenter.model.BrewHistory;
import com.ltbrew.brewbeer.service.LtPushService;
import com.ltbrew.brewbeer.service.PldForCmnMsg;
import com.ltbrew.brewbeer.service.PushMsg;
import com.ltbrew.brewbeer.uis.utils.BrewSessionUtils;
import com.ltbrew.brewbeer.uis.utils.DpSpPixUtils;
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
    private Handler mHandler = new Handler();
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
            }else if(LtPushService.CMN_MSG_PUSH_ACTION.equals(action)){
                PushMsg pushMsgObj = intent.getParcelableExtra(LtPushService.PUSH_MSG_EXTRA);
                Log.e("CMN_MSG_PUSH_ACTION", pushMsgObj.toString());
                PldForCmnMsg pldForCmnMsg = intent.getParcelableExtra(LtPushService.PUSH_PLD_EXTRA);

                int ms = pldForCmnMsg.ms;
                if(ms > 100){
                    pushMsgObj.body = "煮沸";
                    BrewSessionUtils.storeBoilStartTimeStamp(System.currentTimeMillis()/1000);
                }
                brewHistory.setMs(ms);
                if(ms < 100) {
                    curState.setText("正在加热， 当前温度" + brewHistory.getMs() + "度");
                }else{
                    showTimeCountDown(brewHistory, brewHistory.getDbRecipe().getBrewSteps().get(brewHistory.getSi()));
                }
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
        Integer si = brewHistory.getSi();
        List<DBBrewStep> brewSteps = dbRecipe.getBrewSteps();
        if(brewHistory.getBrewingState() != null) {
            if("煮沸".equals(brewHistory.getBrewingState())){
                if(si != null) {
                    showTimeCountDown(brewHistory, brewSteps.get(si));
                }else{
                    curState.setText("煮沸中");
                }
            }else {
                if(brewHistory.getMs() != 0) {
                    curState.setText("正在加热， 当前温度" + brewHistory.getMs() + "度");
                }else{
                    curState.setText("加热中");
                }
            }
        }

        List<DBSlot> slots = dbRecipe.getSlots();

        int i = 0;
        for (DBBrewStep dbBrewStep : brewSteps) {
            View detailView;
            String stepId = dbBrewStep.getStepId();
            String act = dbBrewStep.getAct();
            if ("boil".equals(act)) {
                int temp = dbBrewStep.getT() / 5;
                if(temp < 100) {
                    detailView = addItemToContainer("加热到" + temp + "度，" + dbBrewStep.getK() / 60 + "分钟", "");

                }else{
                    detailView = addItemToContainer("煮沸，" + dbBrewStep.getK() / 60 + "分钟", "");
                }

            } else {
                Integer slot = dbBrewStep.getSlot();
                if(slots.size() < slot)
                    continue;
                if(slot - 1 < 0)
                    continue;
                DBSlot dbSlot = slots.get(slot - 1);
                String addMaterialToSlot = "投放" + dbSlot.getName() + "到槽" + slot;
                detailView = addItemToContainer(addMaterialToSlot, "");
            }
            if(si != null) {
                if (i < si) {
                    detailView.setBackgroundColor(getResources().getColor(R.color.colorBlue));
                } else if (i == si) {
                    detailView.setBackgroundColor(getResources().getColor(R.color.colorGreen));
                }
            }
            i++;
        }

    }


    private void showTimeCountDown(final BrewHistory brewHistory, DBBrewStep dbBrewStep) {
        final Integer totalSecondsForThisStep = dbBrewStep.getK();
        mHandler.postDelayed(new Runnable() {
            int i = 0;
            @Override
            public void run() {

                long tsCur = 0;
                Integer ratio = brewHistory.getRatio();
                tsCur = (long) (totalSecondsForThisStep * (1 - ratio/100f));
                curState.setText(String.format("%02d", tsCur/(60*60))+":"+String.format("%02d", (tsCur/60)%60)+":"+String.format("%02d", tsCur%60));
                this.i++;
            }
        }, 1000);
    }

    private View addItemToContainer(String title, String contentDes) {
        return addItemToContainer(title, contentDes, false);
    }

    private View addItemToContainer(String title, String contentDes, boolean isTitle) {

        View view = LayoutInflater.from(this).inflate(R.layout.layout_brew_detail, stepsContainer, false);
        TextView brewDetailTitle = (TextView) view.findViewById(R.id.brewDetailTitleTv);
        TextView brewDetailContentTv = (TextView) view.findViewById(R.id.brewDetailContentTv);
        brewDetailTitle.setText(title);
        if (isTitle)
            brewDetailTitle.setTextColor(getResources().getColor(R.color.colorAccent));
        brewDetailContentTv.setText(contentDes);
        stepsContainer.addView(view);
        View viewLine = new View(this);
        viewLine.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, DpSpPixUtils.dip2px(this, 0.5f)));
        viewLine.setBackgroundColor(Color.parseColor("#777777"));
        return view;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(broadcastReceiver);
        mHandler.removeCallbacksAndMessages(null);
    }
}
