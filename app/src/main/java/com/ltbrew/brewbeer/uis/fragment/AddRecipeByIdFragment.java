package com.ltbrew.brewbeer.uis.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.ltbrew.brewbeer.R;
import com.ltbrew.brewbeer.uis.OnAddRecipeActionListener;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class AddRecipeByIdFragment extends Fragment {

    @BindView(R.id.edt_pack_id)
    EditText edtPackId;
    @BindView(R.id.btn_add_pack)
    Button btnAddPack;
    private OnAddRecipeActionListener onAddActionListener;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_add_recipe_by_id, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @OnClick(R.id.btn_add_pack)
    public void addPack(){
        onAddActionListener.onClickAddPackByIdBtn(edtPackId.getText().toString());
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        onAddActionListener = (OnAddRecipeActionListener) context;

    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

}
