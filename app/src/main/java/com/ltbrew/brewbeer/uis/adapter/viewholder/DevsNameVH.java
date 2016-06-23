package com.ltbrew.brewbeer.uis.adapter.viewholder;

import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.ltbrew.brewbeer.R;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by 151117a on 2016/5/4.
 */
public class DevsNameVH extends BaseViewHolder {

    @BindView(R.id.devNameTv)
    public TextView devNameTv;

    public DevsNameVH(View itemView) {
        super(itemView);
        ButterKnife.bind(this, itemView);

    }
}
