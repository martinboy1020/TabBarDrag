package com.example.tabbardraganddrop;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class TopMenuAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private ArrayList<Integer> topDrawIdList;
    private ArrayList<String> topTextList;
    private ArrayList<Integer> selectedItemList;
    private View.OnTouchListener onTouchListener;
    private View.OnDragListener onDragListener;
    private Animation mSnake;
    private boolean isTopViewShow = false;
    private int isNowDragViewTag = 0;

    TopMenuAdapter(BottomMenu bottomMenu, ArrayList<Integer> topDrawIdList, ArrayList<String> topTextList, View.OnTouchListener onTouchListener, View.OnDragListener onDragListener) {
        this.onTouchListener = onTouchListener;
        this.onDragListener = onDragListener;
        this.topDrawIdList = topDrawIdList;
        this.topTextList = topTextList;
        mSnake = AnimationUtils.loadAnimation(bottomMenu.getContext(), R.anim.tab_shake);
    }

    void setSelectedItemList(ArrayList<Integer> selectedItemList) {
        this.selectedItemList = selectedItemList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_top_menu, null);
        return new ViewHolder(view);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ((ViewHolder) holder).imageView.setBackgroundResource(topDrawIdList.get(position));
        ((ViewHolder) holder).imageView.setTag(topDrawIdList.get(position));

        ((ViewHolder) holder).imageView.setSelected(checkItemIsSelected(topDrawIdList.get(position)));

        if(isTopViewShow) {
            if (isNowDragViewTag != 0 && isNowDragViewTag == topDrawIdList.get(position)) {
                ((ViewHolder) holder).imageView.setAnimation(null);
                ((ViewHolder) holder).imageView.setSelected(false);
            } else {
                ((ViewHolder) holder).imageView.setAnimation(mSnake);
                ((ViewHolder) holder).imageView.setSelected(checkItemIsSelected(topDrawIdList.get(position)));
            }
        } else {
            ((ViewHolder) holder).imageView.setAnimation(null);
        }

        if (checkItemIsSelected(topDrawIdList.get(position))) {
            ((ViewHolder) holder).imageView.setOnTouchListener(null);
            ((ViewHolder) holder).imageView.setOnDragListener(null);
        } else {
            ((ViewHolder) holder).imageView.setOnTouchListener(onTouchListener);
            ((ViewHolder) holder).imageView.setOnDragListener(onDragListener);
        }

        ((ViewHolder) holder).text_item_menu.setText(topTextList.get(position));

    }

    void isTopViewShow(boolean isTopViewShow) {
        this.isTopViewShow = isTopViewShow;
        notifyDataSetChanged();
    }

    private boolean checkItemIsSelected(int imgTag) {
        if (selectedItemList != null && selectedItemList.size() > 0) {
            for (int bottomItem : selectedItemList) {
                if (imgTag == bottomItem) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public int getItemCount() {
        return topDrawIdList.size();
    }

    ArrayList<Integer> getTopDrawIdList() {
        return topDrawIdList;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        TextView text_item_menu;
        ImageView imageView;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            text_item_menu = itemView.findViewById(R.id.text_item_menu);
            imageView = itemView.findViewById(R.id.img_item_menu);
        }
    }

    void startDragItem(View view) {
        if(isNowDragViewTag == 0) {
            isNowDragViewTag = (int) view.getTag();
            for (int i = 0; i < topDrawIdList.size(); i++) {
                if (topDrawIdList.get(i) == isNowDragViewTag) {
                    notifyItemChanged(i);
                    break;
                }
            }
        }
    }

    void endDragItem(View view) {
        isNowDragViewTag = 0;
        notifyDataSetChanged();
    }

}