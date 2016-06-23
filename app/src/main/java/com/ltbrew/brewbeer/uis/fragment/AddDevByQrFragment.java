package com.ltbrew.brewbeer.uis.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.ltbrew.brewbeer.R;
import com.ltbrew.brewbeer.uis.OnAddRecipeActionListener;
import com.ltbrew.brewbeer.uis.view.OnAddDevActionListener;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;


public class AddDevByQrFragment extends Fragment {


    @BindView(R.id.btn_scan)
    Button btnScan;
    private OnAddDevActionListener onAddActionListener;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_add_dev_by_qr, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @OnClick(R.id.btn_scan)
    public void clickQrScan(){
        onAddActionListener.onClickQrScanBtn();
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
