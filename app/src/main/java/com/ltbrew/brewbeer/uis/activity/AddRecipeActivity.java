package com.ltbrew.brewbeer.uis.activity;

import android.content.Intent;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.text.TextUtils;

import com.ltbrew.brewbeer.R;
import com.ltbrew.brewbeer.interfaceviews.AddPackView;
import com.ltbrew.brewbeer.presenter.AddPackPresenter;
import com.ltbrew.brewbeer.thirdpartylib.MessageWindow;
import com.ltbrew.brewbeer.thirdpartylib.activity.CaptureActivity;
import com.ltbrew.brewbeer.uis.OnAddActionListener;
import com.ltbrew.brewbeer.uis.fragment.AddRecipeByIdFragment;
import com.ltbrew.brewbeer.uis.fragment.AddRecipeByQrFragment;
import com.ltbrew.brewbeer.uis.fragment.BrewSessionFragment;

public class AddRecipeActivity extends BaseActivity implements OnAddActionListener,AddPackView {

    private static final int REQUEST_CODE_SCAN_QR = 10;
    public static final String FORMULA_ID_EXTRA = "formulat_id";
    public static final String RECIPE_NAME_EXTRA = "name";
    private SectionsPagerAdapter mSectionsPagerAdapter;
    private ViewPager mViewPager;
    private AddRecipeByIdFragment addRecipeByIdFragment = new AddRecipeByIdFragment();
    private AddRecipeByQrFragment addRecipeByQrFragment = new AddRecipeByQrFragment();
    private Fragment[] fragments = new Fragment[]{ addRecipeByQrFragment,addRecipeByIdFragment};
    private AddPackPresenter addPackPresenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_recipe);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        mViewPager = (ViewPager) findViewById(R.id.brewHomeContainer);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        addPackPresenter = new AddPackPresenter(this);
    }

    @Override
    public void onClickQrScanBtn() {
        startQrScan();
    }

    public void startQrScan() {
        startActivityForResult(new Intent(this, CaptureActivity.class), REQUEST_CODE_SCAN_QR);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_SCAN_QR) {
            if (resultCode == RESULT_OK) {
                String scanQrCode = data.getStringExtra("result");
                addPackPresenter.addPackToDev(scanQrCode);
            }
        }
    }

    @Override
    public void onClickAddPackByIdBtn(String pack_id) {
        if(TextUtils.isEmpty(pack_id)){
            showSnackBar("原料包ID不能为空");
            return;
        }
        addPackPresenter.addPackToDev(pack_id);
    }

    @Override
    public void onAddRecipeToDevSuccess(Integer state, String formula_id, String name) {
        if(state == 0){
            Intent intent = new Intent(BrewSessionFragment.PACK_IS_SENT);
            intent.putExtra(FORMULA_ID_EXTRA, formula_id);
            intent.putExtra(RECIPE_NAME_EXTRA, name);
            sendBroadcast(intent);
            showMsgWindow("提醒", name + " 已创建成功", new MessageWindow.OnCloseWindowListener() {
                @Override
                public void onCloseWindow() {
                    finish();
                }
            });
        }else if(state == 1){
            showMsgWindow("提醒", "原料包无效已被使用", null);
        }else if(state == 2){
            showMsgWindow("提醒", "请求参数错误， 请联系客服", null);
        }else{
        }
    }

    @Override
    public void onAddRecipeToDevFailed(String message) {
        showErrorMsg(message);
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

}
