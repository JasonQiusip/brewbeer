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
import com.ltbrew.brewbeer.persistence.greendao.DBBrewHistory;
import com.ltbrew.brewbeer.persistence.greendao.DBBrewStep;
import com.ltbrew.brewbeer.persistence.greendao.DBRecipe;
import com.ltbrew.brewbeer.persistence.greendao.DBSlot;
import com.ltbrew.brewbeer.presenter.AddPackPresenter;
import com.ltbrew.brewbeer.presenter.model.Recipe;
import com.ltbrew.brewbeer.presenter.util.DBManager;
import com.ltbrew.brewbeer.presenter.util.DeviceUtil;
import com.ltbrew.brewbeer.service.LtPushService;
import com.ltbrew.brewbeer.service.PldForCmnMsg;
import com.ltbrew.brewbeer.service.PldForCmnPrgs;
import com.ltbrew.brewbeer.service.PushMsg;
import com.ltbrew.brewbeer.uis.Constants;
import com.ltbrew.brewbeer.uis.dialog.FermentingDialog;
import com.ltbrew.brewbeer.uis.dialog.NoticeForBrewEndDialog;
import com.ltbrew.brewbeer.uis.dialog.OnPositiveButtonClickListener;
import com.ltbrew.brewbeer.uis.dialog.SetDevFermentingTempDialog;
import com.ltbrew.brewbeer.uis.fragment.BrewSessionFragment;
import com.ltbrew.brewbeer.uis.utils.BrewSessionUtils;
import com.ltbrew.brewbeer.uis.utils.DpSpPixUtils;
import com.ltbrew.brewbeer.uis.utils.ParamStoreUtil;

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
    @BindView(R.id.controlOpTv)
    TextView controlOpTv;
    private DBRecipe dbRecipe;
    private ImageView backIv;
    private DBBrewHistory brewHistory;
    public static final String OPEN_DEV_START_BREWING = "1";
    public static final String PACK_ID_EXTRA = "packId";
    public static final String FORMULA_ID_EXTRA = "formulat_id";
    public static final String RECIPE_NAME_EXTRA = "name";

    private TextView brewDetailDes;
    private boolean showStrartToBrewTxt;
    private AddPackPresenter addPackPresenter;
    private String tag = this.getClass().getName();
    private int type;

    private Handler mHandler = new Handler();
    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (LtPushService.CMN_PRGS_PUSH_ACTION.equals(action)) { //酿酒状态上报

                PushMsg pushMsgObj = intent.getParcelableExtra(LtPushService.PUSH_MSG_EXTRA);
                PldForCmnPrgs pldForCmnPrgs = intent.getParcelableExtra(LtPushService.PUSH_PLD_EXTRA);
                if(pldForCmnPrgs.st != null && !pldForCmnPrgs.st.contains(brewHistory.getPackage_id()+"")){
                    return;
                }
                if (brewHistory != null && pldForCmnPrgs != null) {

                    brewHistory.setRatio(pldForCmnPrgs.ratio);
                    brewHistory.setSi(pldForCmnPrgs.si);
                    brewHistory.setBrewingState(pushMsgObj.des);
                    brewHistory.setSt(pldForCmnPrgs.st);
                }
                Log.e("CMN_PRGS control", brewHistory.toString());

                changeRecipeState();
            } else if (LtPushService.CMN_PRGS_CHECK_ACTION.equals(action)) {//主动查询酿酒状态回复
                PushMsg pushMsgObj = intent.getParcelableExtra(LtPushService.PUSH_MSG_EXTRA);
                Log.e("CMN_PRGS_CHECK_ACTION", pushMsgObj.toString());

                String st = pushMsgObj.st;
                if(st != null && !st.contains(brewHistory.getPackage_id()+"")){
                    return;
                }
                if (brewHistory != null) {
                    brewHistory.setRatio(pushMsgObj.ratio);
                    brewHistory.setSi(pushMsgObj.si);
                    brewHistory.setBrewingState(pushMsgObj.des);
                    brewHistory.setSt(st);
                }
                changeRecipeState();
            } else if (LtPushService.CMN_MSG_PUSH_ACTION.equals(action)) {//温度状态上报
                PushMsg pushMsgObj = intent.getParcelableExtra(LtPushService.PUSH_MSG_EXTRA);
                PldForCmnMsg pldForCmnMsg = intent.getParcelableExtra(LtPushService.PUSH_PLD_EXTRA);

                Log.e("CMN_MSG_PUSH_ACTION", pushMsgObj.toString()+DeviceUtil.getCurrentDevId()+" "+pldForCmnMsg.id);

                if(!DeviceUtil.getCurrentDevId().equals(pushMsgObj.id))
                    return;
                int ms = pldForCmnMsg.ms;
                if (ms > 90) {
                    pushMsgObj.des = "煮沸";
                }
                if (brewHistory != null) {
                    brewHistory.setBrewingCmnMsg(pushMsgObj.des);
                    brewHistory.setMs(ms);
                }
                changeRecipeState();
            }
        }
    };
    private TextView brewTitle;

    private void changeRecipeState() {
        Integer si = brewHistory.getSi();
        dbRecipe = brewHistory.getDBRecipe();
        if (dbRecipe == null)
            return;
        List<DBBrewStep> brewSteps = dbRecipe.getBrewSteps();
        if (brewSteps == null)
            return;
        int totalStepCount = brewSteps.size();
        showCurState(si, brewSteps);
        if (si == null)
            return;
        Log.e(tag + "changeRecipeState", totalStepCount + "   si : " + si + " brewstate " + brewHistory.getBrewingState());
        //修改每个步骤的内容
        for (int i = 0; i <= si; i++) {

            if (si != null) {
                //此处乘以2是因为一个行的view包含两个子view： 一个view显示内容的， 一个是内容下面的横线, 内容的view都在偶数位置
                View childAtIndex = stepsContainer.getChildAt(i * 2);
                if (childAtIndex == null)
                    continue;
                TextView contentDes = (TextView) childAtIndex.findViewById(R.id.brewDetailContentTv);
                TextView title = (TextView) childAtIndex.findViewById(R.id.brewDetailTitleTv);
                if (contentDes == null) {
                    continue;
                }
                //小于当前步骤的都是完成的步骤
                if (i < si) {
                    contentDes.setText("完成");
                    childAtIndex.setBackgroundColor(getResources().getColor(R.color.colorBlue));
                    //当前非发酵步骤
                } else if (i <= totalStepCount - 1) {
                    if (brewHistory.getRatio() == 100) {
                        childAtIndex.setBackgroundColor(getResources().getColor(R.color.colorBlue));
                        contentDes.setText("完成");
                        final NoticeForBrewEndDialog noticeForBrewEndDialog = new NoticeForBrewEndDialog(this);
                        noticeForBrewEndDialog.show();
                        noticeForBrewEndDialog.setOnPositiveButtonClickListener(new OnPositiveButtonClickListener() {
                            @Override
                            public void onPositiveButtonClick() {
                                noticeForBrewEndDialog.setMsg("麦汁排出完成， 待麦汁冷却到18℃时投放酵母");
                                noticeForBrewEndDialog.setOnPositiveButtonClickListener(null);
                            }
                        });
                    } else {
                        childAtIndex.setBackgroundColor(getResources().getColor(R.color.colorGreen));
                        contentDes.setText(brewHistory.getBrewingState());
                        brewDetailDes = contentDes;
                    }
                    //发酵步骤， 发酵不在服务器配方中， 所以会大于步骤的最大index
                } else if (i > totalStepCount - 1) {

                    childAtIndex.setBackgroundColor(getResources().getColor(R.color.colorGreen));
                    contentDes.setText("");
                    brewDetailDes = contentDes;
                    brewTitle = title;
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
        if (from != null && "recipeStepsAty".equals(from)) {
            showStrartToBrewTxt = true;
        }
        int type = intent1.getIntExtra("type", 0);
        this.type = type;
        if (type == Constants.BrewSessionType.FERMENTING) {
            controlOpTv.setVisibility(View.VISIBLE);
        } else {
            controlOpTv.setVisibility(View.GONE);
        }
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(LtPushService.CMN_PRGS_CHECK_ACTION);
        intentFilter.addAction(LtPushService.CMN_PRGS_PUSH_ACTION);
        intentFilter.addAction(LtPushService.CMN_MSG_PUSH_ACTION);
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

    @OnClick(R.id.controlOpTv)
    public void onControlOpTvClick() {
        FermentingDialog fermentingDialog = new FermentingDialog(this);
        fermentingDialog.show();
        fermentingDialog.setFermentingDialogCallback(new FermentingDialog.FermentingDialogCallback() {
            @Override
            public void onClickFinishBtn() {
                brewHistory.setState(4);
                brewHistory.setBrewingState("发酵完成");
                DBManager.getInstance().getDBBrewHistoryDao().update(brewHistory);
            }

            @Override
            public void onClickRestartBtn() {
                BrewSessionUtils.storeFermentingStartTimeStamp(brewHistory.getPackage_id(), System.currentTimeMillis());
                mHandler.removeCallbacksAndMessages(null);
                showFermentingCountDown();
            }

            @Override
            public void onClickCancelBtn() {

            }
        });
    }
    @OnClick(R.id.curState)
    public void clickCurState() {
        if ("开始酿造".equals(curState.getText().toString())) {

            curState.setBackgroundDrawable(null);
            addPackPresenter.addPackToDev(brewHistory.getPackage_id() + "", OPEN_DEV_START_BREWING);
        } else if ("开始发酵".equals(curState.getText().toString())) {

            SetDevFermentingTempDialog setDevFermentingTempDialog = new SetDevFermentingTempDialog(this);
            setDevFermentingTempDialog.setOnSetPhoneNumbForDevListener(new SetDevFermentingTempDialog.OnSetFermentingTempForDevListener() {
                @Override
                public void onSetFermentingTemp(String temp, int position) {
                    if(brewTitle != null){
                        brewTitle.setText("发酵\n"+temp);
                    }
                    BrewSessionUtils.storeFermentingStartTimeStamp(brewHistory.getPackage_id(), System.currentTimeMillis());
                    curState.setBackgroundDrawable(null);
                    showFermentingCountDown();
                }
            }).show();

        }
    }

    private void showRecipe() {
        if (brewHistory == null)
            return;
        dbRecipe = brewHistory.getDBRecipe();
        if (dbRecipe == null)
            return;
        recipeName.setText(dbRecipe.getName());
        Integer si = brewHistory.getSi();
        Log.e("showRecipe ", si + " si");
        List<DBBrewStep> brewSteps = dbRecipe.getBrewSteps();
        showCurState(si, brewSteps);

        List<DBSlot> slots = dbRecipe.getSlots();

        int i = 0;
        for (DBBrewStep dbBrewStep : brewSteps) {
            View detailView;
            String stepId = dbBrewStep.getStepId();
            String act = dbBrewStep.getAct();
            if ("boil".equals(act)) {
                detailView = showBoilStep(dbBrewStep);

            } else {
                Integer slot = dbBrewStep.getSlot();
                if (slots.size() < slot)
                    continue;
                if (slot - 1 < 0)
                    continue;
                DBSlot dbSlot = slots.get(slot - 1);
                String addMaterialToSlot = "增补\n往" + slot + "号槽投放" + dbSlot.getName();
                detailView = addItemToContainer(addMaterialToSlot, "");
            }

            if (si != null) {
                showStepState(si, i, detailView);
            }
            i++;
        }
        addItemToContainer("发酵\n" + "10天@18℃", "");

    }

    private View showBoilStep(DBBrewStep dbBrewStep) {
        View detailView;
        int temp = dbBrewStep.getT() / 5;
        if (temp < 90) {
            detailView = addItemToContainer("糖化\n" + dbBrewStep.getK() / 60 + "分钟" + "@" + temp + "℃", "");

        } else {
            detailView = addItemToContainer("煮沸\n" + dbBrewStep.getK() / 60 + "分钟", "");
        }
        return detailView;
    }

    private void showStepState(Integer si, int i, View detailView) {
        TextView contentDes = (TextView) detailView.findViewById(R.id.brewDetailContentTv);
        if (i < si) {
            contentDes.setText("完成");
            detailView.setBackgroundColor(getResources().getColor(R.color.colorBlue));
        } else if (i == si) {
            if (brewHistory.getRatio() == 100) {
                detailView.setBackgroundColor(getResources().getColor(R.color.colorBlue));
                contentDes.setText("完成");
            } else {
                detailView.setBackgroundColor(getResources().getColor(R.color.colorGreen));
                if (brewHistory.getBrewingState() != null) {
                    contentDes.setText(brewHistory.getBrewingState());
                }
                brewDetailDes = contentDes;
            }
        }
    }


    private void showCurState(Integer si, List<DBBrewStep> brewSteps) {
        if (brewSteps.size() == 0)
            return;

        if(type == Constants.BrewSessionType.FINSHED){
            curState.setText("酿造完成");
            return;
        }
        //发酵
        if (si != null && si == brewSteps.size() && brewHistory.getRatio() != null && brewHistory.getRatio() == 100) {
            showFermentingInfo();
            return;
        }

        if(si != null && brewSteps.size() <= si) {
            return;
        }

        //如果有温度数据上报
        if (brewHistory.getBrewingCmnMsg() != null) {
            curState.setText(brewHistory.getBrewingCmnMsg());
            if (brewHistory.getBrewingState() != null) {
                //状态为糖化加热中， 或者煮沸加热中， 不修改当前状态显示； 保留温度显示
                if (brewHistory.getBrewingState().contains("加热中"))
                    return;
                if (brewHistory.getBrewingState().contains("准备"))
                    return;
            }
        } else if ((brewHistory.getBrewingState() == null
                || (brewHistory.getBrewingState() != null&& brewHistory.getBrewingState().contains("加热中")))
                && !showStrartToBrewTxt) {
            if(si != null && si > 0)
                curState.setText("加热中");
            return;
        }

        //如果是正常的状态上报
        if (brewHistory.getBrewingState() != null) {

            brewHistory.setBrewingCmnMsg(null);
            if (brewHistory.getRatio() != null && brewHistory.getRatio() == 100) {
                curState.setText("完成");
                return;
            }
            curState.setBackgroundDrawable(null);

            if (brewHistory.getBrewingState().contains("糖化中")) {
                mHandler.removeCallbacksAndMessages(null);
                if (si != null)
                    showTimeCountDown(brewSteps.get(si));
                else
                    curState.setText("糖化中");
                return;
            }

            mHandler.removeCallbacksAndMessages(null);
            if (brewHistory.getBrewingState().contains("煮沸")) {
                if (si != null) {
                    showTimeCountDown(brewSteps.get(si));
                } else {
                    curState.setText("煮沸中");
                }
            } else {
                curState.setText(brewHistory.getBrewingState());
            }
        } else if (showStrartToBrewTxt) {
            curState.setBackgroundResource(R.drawable.bg_btn_red_corn_nomal);
            curState.setText("开始酿造");
        }else{
            curState.setText(brewHistory.getBrewingState());
        }
    }

    private void showFermentingInfo() {
        if (showFermentingCountDown()) return;

        mHandler.removeCallbacksAndMessages(null);
        curState.setBackgroundResource(R.drawable.bg_btn_red_corn_nomal);
        curState.setText("开始发酵");
        if (brewDetailDes != null)
            brewDetailDes.setText("发酵准备");
        return;
    }

    private boolean showFermentingCountDown() {
        long fermentingStartTimeStamp = BrewSessionUtils.getFermentingStartTimeStamp(brewHistory.getPackage_id());
        if (fermentingStartTimeStamp != 0) {
            countdown(10 * 24 * 60 * 60, fermentingStartTimeStamp);
            return true;
        }
        return false;
    }


    private void showTimeCountDown(DBBrewStep dbBrewStep) {
        final Integer totalSecondsForThisStep = dbBrewStep.getK();
        if (totalSecondsForThisStep == null)
            return;
        final long stepStartTimeStamp = BrewSessionUtils.getStepStartTimeStamp(brewHistory.getPackage_id()+"");

        countdown(totalSecondsForThisStep, stepStartTimeStamp);
    }

    private void countdown(final Integer totalSecondsForThisStep, final long stepStartTimeStamp) {
        mHandler.postDelayed(new Runnable() {
            int i = 0;

            @Override
            public void run() {
                long tsCur = 0;
                long timePassed = System.currentTimeMillis() - stepStartTimeStamp;
                tsCur = (long) (totalSecondsForThisStep - timePassed / 1000);
                if (tsCur <= 0) {
                    curState.setText("完成");
                    mHandler.removeCallbacksAndMessages(null);
                    return;
                }
                String dayStr = "";
                long day = tsCur / (60 * 60) / 24;
                if (day != 0) {
                    dayStr = day + "天";
                }
                curState.setText(dayStr + String.format("%02d", (tsCur / (60 * 60)) % 24) + ":" + String.format("%02d", (tsCur / 60) % 60) + ":" + String.format("%02d", tsCur % 60));
                if (brewDetailDes != null) {
                    brewDetailDes.setText(curState.getText().toString());
                }
                this.i++;
                mHandler.postDelayed(this, 1000);
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
            intent.putExtra(PACK_ID_EXTRA, brewHistory.getPackage_id() + "");
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
