package com.ltbrew.brewbeer.uis.adapter.viewholder;

import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.ltbrew.brewbeer.R;
import com.ltbrew.brewbeer.uis.view.SwipeRevealLayout;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by 151117a on 2016/5/4.
 */
public class FinishedBrewVH extends BaseViewHolder {

    @BindView(R.id.finishedSessionItemTv)
    public TextView finishedSessionItemTv;

    @BindView(R.id.finishedFl)
    public FrameLayout finishedSessionFl;

    @BindView(R.id.finishedSessionDeleteTv)
    public TextView deleteTv;

    @BindView(R.id.swipe_layout)
    public SwipeRevealLayout swipeLayout;

    public FinishedBrewVH(View itemView) {
        super(itemView);
    }
}
