package com.ltbrew.brewbeer.uis.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ltbrew.brewbeer.R;
import com.ltbrew.brewbeer.uis.adapter.BrewingSessionAdapter;
import com.ltbrew.brewbeer.uis.adapter.FinishedSessionAdapter;

import butterknife.BindView;
import butterknife.ButterKnife;

public class BrewSessionFragment extends Fragment {

    @BindView(R.id.brewStateRv)
    RecyclerView brewStateRv;
    @BindView(R.id.finishedBrewRv)
    RecyclerView finishedBrewRv;
    private BrewingSessionAdapter brewingSessionAdapter;
    private FinishedSessionAdapter finishedSessionAdapter;

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
        brewStateRv.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        brewingSessionAdapter = new BrewingSessionAdapter(getContext());
        brewStateRv.setAdapter(brewingSessionAdapter);

        finishedBrewRv.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        finishedSessionAdapter = new FinishedSessionAdapter(getContext());
        finishedBrewRv.setAdapter(finishedSessionAdapter);
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
