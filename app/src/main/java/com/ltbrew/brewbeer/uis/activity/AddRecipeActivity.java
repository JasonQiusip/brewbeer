package com.ltbrew.brewbeer.uis.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.RadioButton;

import com.ltbrew.brewbeer.R;
import com.ltbrew.brewbeer.interfaceviews.AddPackView;
import com.ltbrew.brewbeer.persistence.greendao.DBRecipe;
import com.ltbrew.brewbeer.presenter.AddPackPresenter;
import com.ltbrew.brewbeer.presenter.model.Recipe;
import com.ltbrew.brewbeer.thirdpartylib.MessageWindow;
import com.ltbrew.brewbeer.thirdpartylib.activity.CaptureActivity;
import com.ltbrew.brewbeer.uis.OnAddRecipeActionListener;
import com.ltbrew.brewbeer.uis.fragment.AddRecipeByIdFragment;
import com.ltbrew.brewbeer.uis.fragment.AddRecipeByQrFragment;
import com.ltbrew.brewbeer.uis.utils.ParamStoreUtil;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnCheckedChanged;

public class AddRecipeActivity extends BaseActivity implements OnAddRecipeActionListener,AddPackView {


    public static final String CHECK_RECIPE = "0";
    private static final int CAMERA = 11;
    @BindView(R.id.scanRb)
    RadioButton scanRb;
    @BindView(R.id.byIdRb)
    RadioButton byIdRb;

    private static final int REQUEST_CODE_SCAN_QR = 10;
    public static final String PACK_ID_EXTRA = "packId";

    private SectionsPagerAdapter mSectionsPagerAdapter;
    private ViewPager mViewPager;
    private AddRecipeByIdFragment addRecipeByIdFragment = new AddRecipeByIdFragment();
    private AddRecipeByQrFragment addRecipeByQrFragment = new AddRecipeByQrFragment();
    private Fragment[] fragments = new Fragment[]{ addRecipeByQrFragment,addRecipeByIdFragment};
    private AddPackPresenter addPackPresenter;
    private ImageView backIv;
    private String packId;
    private MessageWindow messageWindow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_recipe);
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
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        mViewPager = (ViewPager) findViewById(R.id.brewHomeContainer);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                if(position == 0){
                    scanRb.setChecked(true);
                }else{
                    byIdRb.setChecked(true);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        addPackPresenter = new AddPackPresenter(this);
    }

    @Override
    public void onClickQrScanBtn() {
        startQrScan();
    }

    public void startQrScan() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            //申请CAMERA权限
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA},
                    CAMERA);
        }else{
            startActivityForResult(new Intent(this, CaptureActivity.class), REQUEST_CODE_SCAN_QR);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission Granted
                startActivityForResult(new Intent(this, CaptureActivity.class), REQUEST_CODE_SCAN_QR);
            } else {
                // Permission Denied
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_SCAN_QR) {
            if (resultCode == RESULT_OK) {
                String scanQrCode = data.getStringExtra("result");
                this.packId = scanQrCode;
                messageWindow = showMsgWindow("提醒", "同步原料包数据并获取酿造配方", null);
                addPackPresenter.addPackToDev(scanQrCode, CHECK_RECIPE);
            }
        }
    }

    @Override
    public void onClickAddPackByIdBtn(String pack_id) {
        if(TextUtils.isEmpty(pack_id)){
            showSnackBar("原料包ID不能为空");
            return;
        }
        this.packId = pack_id;
        messageWindow = showMsgWindow("提醒", "同步原料包数据并获取酿造配方", null);
        addPackPresenter.addPackToDev(pack_id, CHECK_RECIPE);
    }

    @Override
    public void onAddRecipeToDevSuccess(Integer state, String formula_id, String name) {
        if(messageWindow != null)
            messageWindow.hidePopupWindow();

        if(state == 0){
            addPackPresenter.getRecipeAfterBrewBegin(formula_id);
            startRecipeDetail();
            finish();
        }else if(state == 1){
            showMsgWindow("提醒", "原料包无效, 已被使用", null);
        }else if(state == 2){
            showMsgWindow("提醒", "请求参数错误， 请联系客服", null);
        }else{
        }
    }

    void startRecipeDetail(){
        Intent intent = new Intent();
        intent.putExtra(PACK_ID_EXTRA, packId);
        intent.setClass(this, RecipeStepsActivity.class);
        startActivity(intent);
    }

    @Override
    public void onAddRecipeToDevFailed(String message) {
        if(messageWindow != null)
            messageWindow.hidePopupWindow();
        showErrorMsg(message);
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
        ParamStoreUtil.getInstance().setCurrentCreatingRecipe(null);

    }

    @Override
    public void onDownLoadRecipeAfterBrewBegin(DBRecipe dbRecipe) {
        ParamStoreUtil.getInstance().setCurrentCreatingRecipe(dbRecipe);
    }


    public class SectionsPagerAdapter extends FragmentPagerAdapter {
        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }
        @Override
        public Fragment getItem(int position) {
            return fragments[position];
        }

        @Override
        public int getCount() {
            return fragments.length;
        }
    }


    @OnCheckedChanged(R.id.scanRb)
    public void scanRb(boolean checked){
        if(!checked)
            return;
        mViewPager.setCurrentItem(0);
    }

    @OnCheckedChanged(R.id.byIdRb)
    public void byIdRb(boolean checked){
        if(!checked)
            return;
        mViewPager.setCurrentItem(1);
    }

}
