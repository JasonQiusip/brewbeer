package com.ltbrew.brewbeer.uis.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ltbrew.brewbeer.R;

import butterknife.BindView;
import butterknife.ButterKnife;

public class BrewSessionFragment extends Fragment {

    @BindView(R.id.brewStateRv)
    RecyclerView brewStateRv;
    @BindView(R.id.finishedBrewRv)
    RecyclerView finishedBrewRv;

    public BrewSessionFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_brew_session, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

    }

    @Override
    public void onDetach() {
        super.onDetach();
    }


}
