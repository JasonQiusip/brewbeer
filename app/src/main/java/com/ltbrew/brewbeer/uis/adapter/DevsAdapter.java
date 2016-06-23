package com.ltbrew.brewbeer.uis.adapter;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;

import com.ltbrew.brewbeer.R;
import com.ltbrew.brewbeer.presenter.model.Device;
import com.ltbrew.brewbeer.presenter.model.Recipe;
import com.ltbrew.brewbeer.presenter.util.DeviceUtil;
import com.ltbrew.brewbeer.uis.adapter.viewholder.BaseViewHolder;
import com.ltbrew.brewbeer.uis.adapter.viewholder.DevsNameVH;
import com.ltbrew.brewbeer.uis.adapter.viewholder.RecipeVH;

import java.util.Collections;
import java.util.List;

/**
 * Created by 151117a on 2016/5/4.
 */
public class DevsAdapter extends RecyclerView.Adapter<DevsNameVH> {

    private final Context context;
    private List<Device> devs = Collections.EMPTY_LIST;
    private BaseViewHolder.OnRvItemClickListener onRvItemClickListener;
    private int selectedPosition = -1;
    private OnRvItemLongClickListener onRvItemLongClickListener;

    public DevsAdapter(Context context) {
        this.context = context;
    }

    public int getSelectedPosition() {
        return selectedPosition;
    }

    public void setSelectedPosition(int selectedPosition) {
        this.selectedPosition = selectedPosition;
    }

    @Override
    public DevsNameVH onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_dev_names, parent, false);
        DevsNameVH devsNameVH = new DevsNameVH(view);
        return devsNameVH;
    }

    @Override
    public void onBindViewHolder(DevsNameVH holder, final int position) {
        Device device = devs.get(position);
        String devNickName = DeviceUtil.getDevNickName(device.getId());
        if (!TextUtils.isEmpty(devNickName)) {
            holder.devNameTv.setText(devNickName);
        } else {
            holder.devNameTv.setText(device.getId());
        }
        holder.devNameTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedPosition = position;
                notifyDataSetChanged();
                onRvItemClickListener.onRvItemClick(v, position);
            }
        });
        holder.devNameTv.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                onRvItemLongClickListener.onItemLongClick(v, position);
                return true;
            }
        });
        if(selectedPosition == -1){
            return;
        }

        if(selectedPosition == position){
            holder.devNameTv.setBackgroundColor(Color.parseColor("#11000000"));
        }else{
            holder.devNameTv.setBackgroundColor(Color.parseColor("#00000000"));

        }
    }

    @Override
    public int getItemCount() {
        return devs.size();
    }

    public void setData(List<Device> devs) {
        this.devs = devs;
    }

    public void setOnItemClickListener(BaseViewHolder.OnRvItemClickListener onRvItemClickListener){
        this.onRvItemClickListener = onRvItemClickListener;
    }

    public void setOnItemLongClickListener(OnRvItemLongClickListener onRvItemLongClickListener) {
        this.onRvItemLongClickListener = onRvItemLongClickListener;
    }

    public interface OnRvItemLongClickListener {
        void onItemLongClick(View v, int position);
    }
}
