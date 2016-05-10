package com.ltbrew.brewbeer.uis.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ltbrew.brewbeer.R;
import com.ltbrew.brewbeer.persistence.greendao.DBRecipe;
import com.ltbrew.brewbeer.presenter.model.BrewHistory;
import com.ltbrew.brewbeer.uis.adapter.viewholder.BaseViewHolder;
import com.ltbrew.brewbeer.uis.adapter.viewholder.FinishedBrewVH;
import com.ltbrew.brewbeer.uis.view.SwipeRevealLayout;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import butterknife.BindView;

/**
 * Created by 151117a on 2016/5/4.
 */
public class FinishedSessionAdapter extends RecyclerView.Adapter<FinishedBrewVH> {

    private final Context context;


    private BaseViewHolder.OnRvItemClickListener onRvItemClickListener;
    private List<BrewHistory> finishedHistoryList = Collections.EMPTY_LIST;
    private OnDeleteClickListener mOnDeleteClickListener;
    private HashMap<Integer, Integer> openOrCloseState = new HashMap<>();


    public FinishedSessionAdapter(Context context) {
        this.context = context;
    }

    @Override
    public FinishedBrewVH onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_finished_session, parent, false);
        FinishedBrewVH finishedBrewVH = new FinishedBrewVH(view);
        return finishedBrewVH;
    }

    @Override
    public void onBindViewHolder(final FinishedBrewVH holder, final int position) {
        BrewHistory brewHistory = finishedHistoryList.get(position);
        DBRecipe recipe = brewHistory.getDbRecipe();
        if (recipe != null) {
            holder.finishedSessionItemTv.setText(recipe.getName());
        }

        holder.finishedSessionFl.setTag(holder);
        holder.finishedSessionFl.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                FinishedBrewVH vh = (FinishedBrewVH) v.getTag();
                if (onRvItemClickListener != null)
                    onRvItemClickListener.onRvItemClick(v, vh.getLayoutPosition());
            }
        });
        holder.finishedSessionFl.setOnLongClickListener(new View.OnLongClickListener() {

            @Override
            public boolean onLongClick(View v) {
                FinishedBrewVH vh = (FinishedBrewVH) v.getTag();
//                if(vh.swipeLayout.isOpened()){
//                    vh.swipeLayout.close(true);
//                    return true;
//                }
                vh.swipeLayout.open(true);
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
        holder.deleteTv.setTag(holder);
        holder.deleteTv.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                FinishedBrewVH vh = (FinishedBrewVH) v.getTag();
                if (mOnDeleteClickListener != null)
                    mOnDeleteClickListener.onDeleteClick(v, vh.getLayoutPosition());
                notifyItemRemoved(vh.getLayoutPosition());
            }
        });
    }

    @Override
    public int getItemCount() {
        return finishedHistoryList.size();
    }

    public void setOnItemClickListener(BaseViewHolder.OnRvItemClickListener onRvItemClickListener) {
        this.onRvItemClickListener = onRvItemClickListener;
    }

    public void setData(List<BrewHistory> finishedHistoryList) {
        this.finishedHistoryList = finishedHistoryList;
    }

    public void setOnDeleteClickListener(OnDeleteClickListener mOnDeleteClickListener) {
        this.mOnDeleteClickListener = mOnDeleteClickListener;
    }

    public interface OnDeleteClickListener {
        void onDeleteClick(View v, int layoutPosition);
    }
}
