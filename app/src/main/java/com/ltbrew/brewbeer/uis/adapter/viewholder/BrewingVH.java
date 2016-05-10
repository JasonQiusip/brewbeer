package com.ltbrew.brewbeer.uis.adapter.viewholder;

import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ltbrew.brewbeer.R;
import com.ltbrew.brewbeer.uis.view.MagicProgressBar;
import com.ltbrew.brewbeer.uis.view.SwipeRevealLayout;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by 151117a on 2016/5/4.
 */
public class BrewingVH extends BaseViewHolder {
    @BindView(R.id.brewingSessionItemTv)
    public TextView brewingSessionItemTv;
    @BindView(R.id.brewingState)
    public TextView brewingState;
    @BindView(R.id.magicPb)
    public MagicProgressBar magicPb;
    @BindView(R.id.deleteTv)
    public TextView deleteTv;
    @BindView(R.id.contentRL)
    public RelativeLayout contentRL;
    @BindView(R.id.swipe_layout)
    public SwipeRevealLayout swipeLayout;

    public BrewingVH(View itemView) {
        super(itemView);
    }
}
