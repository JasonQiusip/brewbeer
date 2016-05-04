package com.ltbrew.brewbeer.uis.adapter.viewholder;

import android.view.View;
import android.widget.TextView;

import com.ltbrew.brewbeer.R;

import butterknife.BindView;

/**
 * Created by 151117a on 2016/5/4.
 */
public class FinishedBrewVH extends BaseViewHolder {

    @BindView(R.id.finishedSessionItemTv)
    TextView finishedSessionItemTv;

    public FinishedBrewVH(View itemView) {
        super(itemView);
    }
}
