package com.ltbrew.brewbeer.uis.adapter.viewholder;

import android.support.v7.widget.RecyclerView;
import android.view.View;

import butterknife.ButterKnife;

/**
 * Created by 151117a on 2016/5/4.
 */
public class BaseViewHolder extends RecyclerView.ViewHolder {
    private OnRvItemClickListener onRvItemClickListener;

    public BaseViewHolder(View itemView) {
        super(itemView);
        ButterKnife.bind(this, itemView);
        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(onRvItemClickListener != null)
                    onRvItemClickListener.onRvItemClick(v, getLayoutPosition());
            }
        });
    }

    public void setOnRvItemClickListener(OnRvItemClickListener onRvItemClickListener){
        this.onRvItemClickListener = onRvItemClickListener;
    }

    public interface OnRvItemClickListener{
        void onRvItemClick(View v, int layoutPosition);
    }
}
