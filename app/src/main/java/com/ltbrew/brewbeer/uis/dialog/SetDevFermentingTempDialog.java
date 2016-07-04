package com.ltbrew.brewbeer.uis.dialog;

import android.app.Dialog;
import android.content.Context;
import android.support.v7.widget.AppCompatSpinner;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.ltbrew.brewbeer.R;
import com.ltbrew.brewbeer.uis.utils.DpSpPixUtils;

/**
 * Created by qiusiping on 16/5/9.
 */
public class SetDevFermentingTempDialog extends Dialog implements View.OnClickListener {

    private final AppCompatSpinner edt_fermenting_temp;
    public final String[] temps = {"-10℃ 到 0℃", "0℃ 到 10℃", "10℃ 到 20℃", "20℃ 到 30℃", "30℃ 到 40℃"};
    private OnSetFermentingTempForDevListener onSetPhoneNumbForDevListener;
    private String temp;
    private int position;

    public SetDevFermentingTempDialog(Context context) {
        super(context);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setBackgroundDrawableResource(R.drawable.bg_dialog);
        temp = "-10℃ 到 0℃";
        View phoneNumbSettingDialog = LayoutInflater.from(context).inflate(R.layout.dialog_set_dev_fermenting_temp, null, false);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(DpSpPixUtils.dip2px(context, 300), LinearLayout.LayoutParams.MATCH_PARENT);
        setContentView(phoneNumbSettingDialog, layoutParams);
        Button cancelBtn = (Button) phoneNumbSettingDialog.findViewById(R.id.cancelBtn);
        Button okBtn = (Button) phoneNumbSettingDialog.findViewById(R.id.okBtn);
        edt_fermenting_temp = (AppCompatSpinner)phoneNumbSettingDialog.findViewById(R.id.edt_fermenting_temp);
        edt_fermenting_temp.setAdapter(new ArrayAdapter<String>(context, android.R.layout.simple_spinner_dropdown_item, temps));
        edt_fermenting_temp.setOnItemSelectedListener(new AppCompatSpinner.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                temp = temps[position];
                SetDevFermentingTempDialog.this.position = position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }

        });
        okBtn.setOnClickListener(this);
        cancelBtn.setOnClickListener(this);
    }

    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.cancelBtn:
                break;
            case R.id.okBtn:
                if(onSetPhoneNumbForDevListener != null)
                    onSetPhoneNumbForDevListener.onSetFermentingTemp(temp, position);
                break;
        }
        dismiss();

    }

    public SetDevFermentingTempDialog setOnSetPhoneNumbForDevListener(OnSetFermentingTempForDevListener onSetPhoneNumbForDevListener) {
        this.onSetPhoneNumbForDevListener = onSetPhoneNumbForDevListener;
        return this;
    }

    public interface OnSetFermentingTempForDevListener{
        void onSetFermentingTemp(String s, int postion);
    }
}
