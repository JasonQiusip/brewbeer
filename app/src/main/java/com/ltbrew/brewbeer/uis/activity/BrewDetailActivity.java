package com.ltbrew.brewbeer.uis.activity;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.ltbrew.brewbeer.R;
import com.ltbrew.brewbeer.persistence.greendao.DBBrewStep;
import com.ltbrew.brewbeer.persistence.greendao.DBRecipe;
import com.ltbrew.brewbeer.persistence.greendao.DBSlot;
import com.ltbrew.brewbeer.uis.utils.ParamStoreUtil;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class BrewDetailActivity extends BaseActivity {

    @BindView(R.id.brewDetailContainer)
    LinearLayout brewDetailContainer;
    @BindView(R.id.backIv)
    ImageView backIv;

    String jm = null; //酵母
    String jmzl = null; //酵母重量
    String thsl = null; //糖化水量
    private String TAG = this.getClass().getName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_brew_detail);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setTitle("");
        ButterKnife.bind(this);


        DBRecipe dbRecipe = ParamStoreUtil.getInstance().getDbRecipe();
        addItemToContainer("配方详情", "", true);
        addItemToContainer("配方名称", dbRecipe.getName());
        Integer wr = dbRecipe.getWr();
        addItemToContainer("加水容积", dbRecipe.getWq() + " 升");
        //cus 数据格式 jm:米啤酒酵母,jmzl:50g,thsl:10L
        String cus = dbRecipe.getCus();
        String[] cusArray = cus.split(",");
        int length = cusArray.length;

        if(length > 0){
            String cus0 = cusArray[0];
            showCus(cus0);
        }
        if(length > 1){
            String cus1 = cusArray[1];
            showCus(cus1);
        }
        if(length > 2){
            String cus2 = cusArray[2];
            showCus(cus2);
        }
        addItemToContainer("酵母", jm == null ? "未提供信息" : jm);
        addItemToContainer("酵母重量", jmzl == null ? "未提供信息" : jmzl);
        addItemToContainer("糖化水量", thsl == null ? "未提供信息" : thsl);

        showWheat(dbRecipe);
        showBeerHops(dbRecipe);
        showSteps(dbRecipe, wr);
    }

    public void showCus(String cus){
        String jmzl = parseCusJmzl(cus);
        if(jmzl != null) {
            this.jmzl = jmzl;
            return;
        }
        String jm = parseCusJm(cus);
        if(jm != null) {
            this.jm = jm;
            return;
        }
        thsl = parseCusThsl(cus);
    }

    public String parseCusJm(String cusPart){
        String result = null;
        if(cusPart.contains("jm")){
            String[] cusPartArr = cusPart.split(":");
            if(cusPartArr.length > 0) {
                result = cusPartArr[1];
            }
        }
        return result;
    }

    public String parseCusJmzl(String cusPart){
        String result = null;
        if(cusPart.contains("jmzl")){
            String[] cusPartArr = cusPart.split(":");
            if(cusPartArr.length > 0) {
                result = cusPartArr[1];
            }
        }
        return result;
    }

    public String parseCusThsl(String cusPart){
        String result = null;
        if(cusPart.contains("thsl")){
            String[] cusPartArr = cusPart.split(":");
            if(cusPartArr.length > 0) {
                result = cusPartArr[1];
            }
        }
        return result;
    }

    //"ndrops":{"n_00":{"id":136973441,"w":1000,"name":"\u5927\u9ea6"},"nc":1
    private void showWheat(DBRecipe dbRecipe) {

        String ndrops = dbRecipe.getNdrops();
        Log.e(TAG, ndrops +"  ");
        if(ndrops == null)
            return;
        View wheatView = addItemToContainer("谷物", "", true);
        int totalWeight = 0;
        JSONObject ndropsJson = JSON.parseObject(ndrops);
        Integer nc = ndropsJson.getInteger("nc");
        Log.e(TAG, nc +" nc ");

        if(nc == null)
            return;
        for(int i = 0; i < nc; i++){
            String key = "n_"+String.format("%02d", i);
            JSONObject ndrop = ndropsJson.getJSONObject(key);
            ndrop.getInteger("id");
            Integer weight = ndrop.getInteger("w");
            String name = ndrop.getString("name");
            if(weight == null){
                continue;
            }
            totalWeight += weight;
            addItemToContainer(name, weight/1000f+"kg");
        }
        TextView brewDetailContentTv = (TextView) wheatView.findViewById(R.id.brewDetailContentTv);
        brewDetailContentTv.setText(totalWeight/1000f+"KG");

    }

    private void showBeerHops(DBRecipe dbRecipe) {
        addItemToContainer("增补[啤酒花/其它]", "", true);
        List<DBSlot> slots = dbRecipe.getSlots();
        Log.e("slots", slots+" ");
        for (DBSlot dbSlot : slots) {
            String slotStepId = dbSlot.getSlotStepId();
            slotStepId = slotStepId.split("s_")[1];;
            int id = Integer.parseInt(slotStepId, 16);

            addItemToContainer("增补"+(id + 1), dbSlot.getName());
        }
    }

    private void showSteps(DBRecipe dbRecipe, Integer wr) {
        addItemToContainer("糖化/煮沸步骤", "", true);
        List<DBBrewStep> brewSteps = dbRecipe.getBrewSteps();
        for (DBBrewStep dbBrewStep : brewSteps) {
            String stepId = dbBrewStep.getStepId();
            stepId = stepId.split("s_")[1];
            int id = Integer.parseInt(stepId, 16);
            String act = dbBrewStep.getAct();
            if ("boil".equals(act)) {
                int temp = dbBrewStep.getT() / wr;
                if(temp < 100) {
                    addItemToContainer("步骤" + +(id + 1), "加热到" + temp + "度，" + dbBrewStep.getK() / 60 + "分钟");
                }else{
                    addItemToContainer("步骤" + +(id + 1), "煮沸，" + dbBrewStep.getK() / 60 + "分钟");
                }

            } else {
                addItemToContainer("步骤" + (id+1), "投放原料到槽" + dbBrewStep.getSlot());
            }
        }
    }



    private void addItemToContainer(String title, String contentDes) {
        addItemToContainer(title, contentDes, false);
    }

    private View addItemToContainer(String title, String contentDes, boolean isTitle) {

        View view = LayoutInflater.from(this).inflate(R.layout.layout_brew_detail, brewDetailContainer, false);
        TextView brewDetailTitle = (TextView) view.findViewById(R.id.brewDetailTitleTv);
        TextView brewDetailContentTv = (TextView) view.findViewById(R.id.brewDetailContentTv);
        brewDetailTitle.setText(title);
        if (isTitle)
            brewDetailTitle.setTextColor(getResources().getColor(R.color.colorAccent));
        brewDetailContentTv.setText(contentDes);
        brewDetailContainer.addView(view);
        return view;
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
