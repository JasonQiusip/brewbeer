package com.ltbrew.brewbeer.uis.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ltbrew.brewbeer.R;
import com.ltbrew.brewbeer.interfaceviews.AddPackView;
import com.ltbrew.brewbeer.persistence.greendao.DBBrewStep;
import com.ltbrew.brewbeer.persistence.greendao.DBRecipe;
import com.ltbrew.brewbeer.persistence.greendao.DBSlot;
import com.ltbrew.brewbeer.presenter.AddPackPresenter;
import com.ltbrew.brewbeer.presenter.model.BrewHistory;
import com.ltbrew.brewbeer.presenter.model.Recipe;
import com.ltbrew.brewbeer.service.LtPushService;
import com.ltbrew.brewbeer.service.PldForCmnMsg;
import com.ltbrew.brewbeer.service.PushMsg;
import com.ltbrew.brewbeer.uis.fragment.BrewSessionFragment;
import com.ltbrew.brewbeer.uis.utils.BrewSessionUtils;
import com.ltbrew.brewbeer.uis.utils.DpSpPixUtils;
import com.ltbrew.brewbeer.uis.utils.ParamStoreUtil;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;


public class BrewSessionControlActivity extends BaseActivity implements AddPackView {

    @BindView(R.id.recipeName)
    TextView recipeName;
    @BindView(R.id.curState)
    TextView curState;
    @BindView(R.id.stepsContainer)
    LinearLayout stepsContainer;
    private DBRecipe dbRecipe;
    private ImageView backIv;
    private BrewHistory brewHistory;
    public static final String OPEN_DEV_START_BREWING = "1";
    public static final String PACK_ID_EXTRA = "packId";
    public static final String FORMULA_ID_EXTRA = "formulat_id";
    public static final String RECIPE_NAME_EXTRA = "name";

    private TextView brewDetailDes;
    private boolean showStrartToBrewTxt;
    private AddPackPresenter addPackPresenter;

    private Handler mHandler = new Handler();
    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(LtPushService.CMN_PRGS_PUSH_ACTION.equals(action)){

                PushMsg pushMsgObj = intent.getParcelableExtra(LtPushService.PUSH_MSG_EXTRA);
                Log.e("CMN_PRGS control", pushMsgObj.toString());
                pushMsgObj.body = pushMsgObj.body == null ? pushMsgObj.des : pushMsgObj.body;

                if(pushMsgObj.body.contains("糖化中 ")){
                    String timeStamp = pushMsgObj.body.replace("糖化中 ", "");
                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    try {
                        Date date = simpleDateFormat.parse(timeStamp);
                        BrewSessionUtils.storeStepStartTimeStamp(date.getTime());

                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }
                if(brewHistory != null) {

                    brewHistory.setRatio(pushMsgObj.ratio);
                    brewHistory.setSi(pushMsgObj.si);
                    brewHistory.setBrewingState(pushMsgObj.body);
                    brewHistory.setSt(pushMsgObj.st);
                }
                showRecipeState();
            }else if (LtPushService.CMN_PRGS_CHECK_ACTION.equals(action)) {
                PushMsg pushMsgObj = intent.getParcelableExtra(LtPushService.PUSH_MSG_EXTRA);
                Log.e("CMN_PRGS_CHECK_ACTION", pushMsgObj.toString());
                String st = pushMsgObj.st;
                if(brewHistory != null) {
                    brewHistory.setRatio(pushMsgObj.ratio);
                    brewHistory.setSi(pushMsgObj.si);
                    brewHistory.setBrewingState(pushMsgObj.body);
                    brewHistory.setSt(st);
                }
                showRecipeState();
            }else if(LtPushService.CMN_MSG_PUSH_ACTION.equals(action)){
                PushMsg pushMsgObj = intent.getParcelableExtra(LtPushService.PUSH_MSG_EXTRA);
                Log.e("CMN_MSG_PUSH_ACTION", pushMsgObj.toString());
                PldForCmnMsg pldForCmnMsg = intent.getParcelableExtra(LtPushService.PUSH_PLD_EXTRA);

                int ms = pldForCmnMsg.ms;
                if(ms > 100){
                    pushMsgObj.body = "煮沸";
                }
                if(brewHistory != null) {
                    brewHistory.setBrewingState(pushMsgObj.body);
                    brewHistory.setMs(ms);
                }
                showRecipeState();
            }
        }
    };

    private void showRecipeState() {
        Integer si = brewHistory.getSi();
        List<DBBrewStep> brewSteps = dbRecipe.getBrewSteps();
        showCurState(si, brewSteps);
        for(int i = 0; i < si*2; i ++) {
            if (si != null) {
                View childAtIndex = stepsContainer.getChildAt(i);
                if(childAtIndex == null)
                    continue;
                TextView contentDes = (TextView) childAtIndex.findViewById(R.id.brewDetailContentTv);
                if(contentDes == null){
                    continue;
                }
                if (i < si) {
                    contentDes.setText("完成");
                    childAtIndex.setBackgroundColor(getResources().getColor(R.color.colorBlue));
                } else if (i == si) {
                    if(brewHistory.getRatio() == 100){
                        childAtIndex.setBackgroundColor(getResources().getColor(R.color.colorBlue));
                        contentDes.setText("完成");
                    }else {
                        childAtIndex.setBackgroundColor(getResources().getColor(R.color.colorGreen));
                        contentDes.setText(curState.getText().toString());
                        brewDetailDes = contentDes;
                    }

                }
            }
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_brew_session_control);
        ButterKnife.bind(this);
        Intent intent1 = getIntent();
        String from = intent1.getStringExtra("From");
        if(from != null && "recipeStepsAty".equals(from)){
            showStrartToBrewTxt = true;
        }

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
        brewHistory = ParamStoreUtil.getInstance().getBrewHistory();
        Intent intent = new Intent(BrewHomeActivity.PUSH_REQ_SESSION_ACTION);
        intent.putExtra(BrewHomeActivity.PACK_ID_EXTRA, brewHistory.getPackage_id());
        sendBroadcast(intent);

        addPackPresenter = new AddPackPresenter(this);

        showRecipe();
    }

    @OnClick(R.id.curState)
    public void clickCurState(){
        if("开始酿造".equals(curState.getText().toString())){
            curState.setBackgroundDrawable(null);
            addPackPresenter.addPackToDev(brewHistory.getPackage_id()+"", OPEN_DEV_START_BREWING);
        }
    }

    private void showRecipe() {
        if(brewHistory == null)
            return;
        dbRecipe = brewHistory.getDbRecipe();
        if(dbRecipe == null)
            return;
        recipeName.setText(dbRecipe.getName());
        Integer si = brewHistory.getSi();
        Log.e("showRecipe ", si+" si");
        List<DBBrewStep> brewSteps = dbRecipe.getBrewSteps();
        showCurState(si, brewSteps);

        List<DBSlot> slots = dbRecipe.getSlots();

        int i = 0;
        for (DBBrewStep dbBrewStep : brewSteps) {
            View detailView;
            String stepId = dbBrewStep.getStepId();
            String act = dbBrewStep.getAct();
            if ("boil".equals(act)) {
                int temp = dbBrewStep.getT() / 5;
                if(temp < 100) {
                    detailView = addItemToContainer("糖化\n"+dbBrewStep.getK() / 60 + "分钟"+"@" + temp + "℃"  , "");

                }else{
                    detailView = addItemToContainer("煮沸\n" + dbBrewStep.getK() / 60 + "分钟", "");
                }

            } else {
                Integer slot = dbBrewStep.getSlot();
                if(slots.size() < slot)
                    continue;
                if(slot - 1 < 0)
                    continue;
                DBSlot dbSlot = slots.get(slot - 1);
                String addMaterialToSlot = "增补\n往"+slot+"号槽投放" + dbSlot.getName();
                detailView = addItemToContainer(addMaterialToSlot, "");
            }
            if(si != null) {
                TextView contentDes = (TextView) detailView.findViewById(R.id.brewDetailContentTv);
                if (i < si) {
                    contentDes.setText("完成");
                    detailView.setBackgroundColor(getResources().getColor(R.color.colorBlue));
                } else if (i == si) {
                    if(brewHistory.getRatio() == 100){
                        detailView.setBackgroundColor(getResources().getColor(R.color.colorBlue));
                        contentDes.setText("完成");
                    }else {
                        detailView.setBackgroundColor(getResources().getColor(R.color.colorGreen));
                        contentDes.setText(curState.getText().toString());
                        brewDetailDes = contentDes;
                    }

                }
            }
            i++;
        }

    }

    private void showCurState(Integer si, List<DBBrewStep> brewSteps) {
        if(brewHistory.getBrewingState() != null) {
            if(brewHistory.getRatio() == 100){
                curState.setText("完成");
                return;
            }
            curState.setBackgroundDrawable(null);
            if(brewHistory.getBrewingState().equals("糖化中")){

            }


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
                    curState.setText("正在加热");
                }
            }
        }else if(showStrartToBrewTxt){
            curState.setBackgroundResource(R.drawable.bg_btn_red_corn_nomal);
            curState.setText("开始酿造");
        }
    }


    private void showTimeCountDown(final BrewHistory brewHistory, DBBrewStep dbBrewStep) {
        final Integer totalSecondsForThisStep = dbBrewStep.getK();
        final long stepStartTimeStamp = BrewSessionUtils.getStepStartTimeStamp();

        mHandler.postDelayed(new Runnable() {
            int i = 0;
            @Override
            public void run() {
                long tsCur = 0;
                long timePassed = System.currentTimeMillis() - stepStartTimeStamp;
                tsCur = (long) (totalSecondsForThisStep - timePassed/1000);
                curState.setText(String.format("%02d", tsCur/(60*60))+":"+String.format("%02d", (tsCur/60)%60)+":"+String.format("%02d", tsCur%60));
                if(brewDetailDes != null){
                    brewDetailDes.setText(curState.getText().toString());
                }
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
        view.setBackgroundColor(Color.parseColor("#eeeeee"));
        View viewLine = new View(this);
        viewLine.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, DpSpPixUtils.dip2px(this, 0.5f)));
        viewLine.setBackgroundColor(Color.parseColor("#777777"));
        stepsContainer.addView(viewLine);

        return view;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(broadcastReceiver);
        mHandler.removeCallbacksAndMessages(null);
    }

    @Override
    public void onAddRecipeToDevSuccess(Integer state, String formula_id, String content) {
        if (state == 0) {
            showMsgWindow("提醒", "配方成功下发", null);
            curState.setText("正在加热");
            Intent intent = new Intent(BrewSessionFragment.PACK_IS_SENT);
            intent.putExtra(FORMULA_ID_EXTRA, formula_id);
            intent.putExtra(PACK_ID_EXTRA, brewHistory.getPackage_id()+"");
            sendBroadcast(intent);
        } else if (state == 1) {
            showMsgWindow("提醒", "原料包无效已被使用", null);
        } else if (state == 2) {
            showMsgWindow("提醒", "请求参数错误， 请联系客服", null);
        } else {
        }
    }

    @Override
    public void onAddRecipeToDevFailed(String message) {
    }
    @Override
    public void onGetRecipeSuccess(List<Recipe> recipes) {
    }

    @Override
    public void onGetRecipeFailed() {
    }
    @Override
    public void onDownloadRecipeSuccess(DBRecipe dbRecipe) {
    }
    @Override
    public void onDownloadRecipeFailed() {
    }
    @Override
    public void onDownLoadRecipeAfterBrewBegin(DBRecipe dbRecipe) {
    }
}
