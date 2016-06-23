package com.ltbrew.brewbeer.uis.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.ltbrew.brewbeer.R;
import com.ltbrew.brewbeer.uis.view.OnAddDevActionListener;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;


public class AddDevByIdFragment extends Fragment {


    @BindView(R.id.edt_dev_id)
    EditText edtDevId;
    @BindView(R.id.edt_akey_id)
    EditText edtAkeyId;
    @BindView(R.id.btn_add_dev)
    Button btnAddDev;
    private OnAddDevActionListener onAddActionListener;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_add_dev_by_id, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @OnClick(R.id.btn_add_dev)
    public void clickAddDevBtn(){
        String devId = edtDevId.getText().toString();
        if(TextUtils.isEmpty(devId))
        {
        }
        onAddActionListener.onClickAddDevByIdBtn(devId, edtAkeyId.getText().toString());
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        onAddActionListener = (OnAddDevActionListener) context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

}
