package com.ltbrew.brewbeer.uis.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ltbrew.brewbeer.R;
import com.ltbrew.brewbeer.persistence.greendao.DBBrewHistory;
import com.ltbrew.brewbeer.persistence.greendao.DBBrewStep;
import com.ltbrew.brewbeer.persistence.greendao.DBRecipe;
import com.ltbrew.brewbeer.uis.adapter.viewholder.BaseViewHolder;
import com.ltbrew.brewbeer.uis.adapter.viewholder.BrewingVH;
import com.ltbrew.brewbeer.uis.view.SwipeRevealLayout;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;

/**
 * Created by 151117a on 2016/5/4.
 */
public class BrewingSessionAdapter extends RecyclerView.Adapter<BrewingVH> {

    private final Context context;

    private BaseViewHolder.OnRvItemClickListener onRvItemClickListener;
    private List<DBBrewHistory> brewingHitories = Collections.EMPTY_LIST;
    private OnDeleteClickListener mOnDeleteClickListener;
    private HashMap<Integer, Integer> openOrCloseState = new HashMap<>();

    public BrewingSessionAdapter(Context context) {
        this.context = context;
    }

    @Override
    public BrewingVH onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_brewing_session, parent, false);
        BrewingVH brewingVH = new BrewingVH(view);
        return brewingVH;
    }

    @Override
    public void onBindViewHolder(final BrewingVH holder, final int position) {

        DBBrewHistory brewHistory = brewingHitories.get(position);
        DBRecipe dbRecipe = brewHistory.getDBRecipe();
        DBRecipe recipe = dbRecipe;
        if (recipe != null) {
            holder.brewingSessionItemTv.setText(recipe.getName());
        }
        holder.brewingState.setText(brewHistory.getBrewingState());
        Integer ratio = brewHistory.getRatio();
        if (ratio != null) {
            holder.magicPb.setSmoothPercent(ratio / 100f);
        }
        holder.deleteTv.setTag(holder);
        holder.deleteTv.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                BrewingVH vh = (BrewingVH) v.getTag();
                if (mOnDeleteClickListener != null)
                    mOnDeleteClickListener.onDeleteClick(v, vh.getLayoutPosition());
                openOrCloseState.put(position, 0);

            }
        });
        holder.contentRL.setTag(holder);
        holder.contentRL.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                BrewingVH vh = (BrewingVH) v.getTag();
                if (onRvItemClickListener != null)
                    onRvItemClickListener.onRvItemClick(v, vh.getLayoutPosition());
            }
        });
        holder.contentRL.setOnLongClickListener(new View.OnLongClickListener(){

            @Override
            public boolean onLongClick(View v) {
                BrewingVH holder = (BrewingVH) v.getTag();
                holder.swipeLayout.open(true);
                return true;
            }
        });

        holder.swipeLayout.setSwipeListener(new SwipeRevealLayout.SwipeListener() {
            @Override
            public void onClosed(SwipeRevealLayout view) {
                openOrCloseState.put(position, 0);
            }

            @Override
            public void onOpened(SwipeRevealLayout view) {
                openOrCloseState.put(position, 1);
            }

            @Override
            public void onSlide(SwipeRevealLayout view, float slideOffset) {

            }
        });
        if(openOrCloseState.get(position) == null || openOrCloseState.get(position) == 0){
            holder.swipeLayout.close(false);
        }else{
            holder.swipeLayout.open(true);
        }
        Integer si = brewHistory.getSi();
        String brewingStageInfo = brewHistory.getBrewingStageInfo();
        if(si != null && dbRecipe != null && brewingStageInfo == null) {
            holder.brewingStage.setText("");
//
//            List<DBBrewStep> brewSteps = dbRecipe.getBrewSteps();
//            if(brewSteps != null && brewSteps.size() > si) {
//                String stageInfo = getStageInfo(brewSteps.get(si));
//                holder.brewingStage.setText(stageInfo);
//            }
        }else{
            holder.brewingStage.setText(brewingStageInfo);
        }
    }

    private String getStageInfo(DBBrewStep dbBrewStep){
        String stepId = dbBrewStep.getStepId();
        String act = dbBrewStep.getAct();
        if ("boil".equals(act)) {
            int temp = dbBrewStep.getT() / 5;
            if(temp < 100) {
                 return "加热到" + temp + "度，" + dbBrewStep.getK() / 60 + "分钟";
            }else{
                return "煮沸，" + dbBrewStep.getK() / 60 + "分钟";
            }
        } else {
            return "投放原料到槽" + dbBrewStep.getSlot();
        }
    }

    @Override
    public int getItemCount() {
        return brewingHitories.size();
    }

    public void setData(List<DBBrewHistory> brewingHitories) {
        this.brewingHitories = brewingHitories;
    }

    public void setOnItemClickListener(BaseViewHolder.OnRvItemClickListener onRvItemClickListener) {
        this.onRvItemClickListener = onRvItemClickListener;
    }

    public void setOnDeleteClickListener(OnDeleteClickListener mOnDeleteClickListener) {
        this.mOnDeleteClickListener = mOnDeleteClickListener;
    }

    public interface OnDeleteClickListener {
        void onDeleteClick(View v, int layoutPosition);
    }
}
