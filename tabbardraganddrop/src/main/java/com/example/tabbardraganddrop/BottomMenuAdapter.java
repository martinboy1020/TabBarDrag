package com.example.tabbardraganddrop;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Collections;

public class BottomMenuAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private BottomMenu bottomMenu;
    private ArrayList<Integer> bottomDrawIdList;
    private View.OnDragListener onDragListener;
    private int mBottomItemWidth;
    private int nowPageIndex = 0;
    private boolean isOpenSelectList = false;
    private boolean isTopViewShow = false;
    private Animation mSnake;
    private BottomMenuAdapter.ClickCallBack bottomMenuAdapterCallback;
    private int fixedPosition = -1;
    public static final int TYPE_NO_DRAG = 0;
    public static final int TYPE_CAN_DRAG = 1;

    BottomMenuAdapter(BottomMenu bottomMenu, ArrayList<Integer> bottomDrawIdList, View.OnDragListener onDragListener, BottomMenuAdapter.ClickCallBack bottomMenuAdapterCallback) {
        this.bottomMenu = bottomMenu;
        this.onDragListener = onDragListener;
        this.bottomDrawIdList = bottomDrawIdList;
        this.bottomMenuAdapterCallback = bottomMenuAdapterCallback;
        mSnake = AnimationUtils.loadAnimation(bottomMenu.getContext(), R.anim.tab_shake);
        mBottomItemWidth = defineViewWidth(bottomMenu.getContext());
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_bottom_menu, null);
        return new ViewHolder(view, mBottomItemWidth);
    }

    @Override
    public int getItemViewType(int position) {

        if (position == 2) {
            return TYPE_NO_DRAG;
        } else {
            return TYPE_CAN_DRAG;
        }

//        return TYPE_CAN_DRAG;

    }

    private int defineViewWidth(Context context) {
        int definedWidth = (int) context.getResources().getDimension(R.dimen.item_bottom_menu_width);
        DisplayMetrics dm = new DisplayMetrics();
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        if (wm != null)
            wm.getDefaultDisplay().getMetrics(dm);

        if (definedWidth * bottomDrawIdList.size() > dm.widthPixels) {
            return dm.widthPixels / bottomDrawIdList.size();
        }
        return definedWidth;
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ((ViewHolder) holder).imageView.setBackgroundResource(bottomDrawIdList.get(position));
        ((ViewHolder) holder).imageView.setTag(bottomDrawIdList.get(position));
        ((ViewHolder) holder).imageView.setOnDragListener(onDragListener);
        ((ViewHolder) holder).imageView.setSelected(position == nowPageIndex);
        if (isTopViewShow) {
            ((ViewHolder) holder).imageView.setAnimation(mSnake);
        } else {
            ((ViewHolder) holder).imageView.setAnimation(null);
        }
        ((ViewHolder) holder).imageView.setOnLongClickListener(onLongClickListener);
        ((ViewHolder) holder).imageView.setOnClickListener(new ClickListener(this, position, new ClickCallBack() {
            @Override
            public void BottomMenuAdapterClickEvent(int nowPosition) {
                setNowPageIndex(nowPosition);
                bottomMenuAdapterCallback.BottomMenuAdapterClickEvent(nowPosition);
            }
        }));
        ((ViewHolder) holder).item_bottom_bg.setTag(bottomDrawIdList.get(position));
        ((ViewHolder) holder).item_bottom_bg.setOnLongClickListener(onLongClickListener);
        ((ViewHolder) holder).item_bottom_bg.setOnClickListener(new ClickListener(this, position, new ClickCallBack() {
            @Override
            public void BottomMenuAdapterClickEvent(int nowPosition) {
                setNowPageIndex(nowPosition);
                bottomMenuAdapterCallback.BottomMenuAdapterClickEvent(nowPosition);
            }
        }));
    }

    @Override
    public int getItemCount() {
        return bottomDrawIdList.size();
    }

    void isTopViewShow(boolean isTopViewShow) {
        this.isTopViewShow = isTopViewShow;
        notifyDataSetChanged();
    }

    void setNowPageIndex(int pageIndex) {
        nowPageIndex = pageIndex;
        notifyDataSetChanged();
    }

    void setFixedPosition(int fixedPosition) {
        this.fixedPosition = fixedPosition;
    }

    void closeSelectList() {
        if (isOpenSelectList)
            isOpenSelectList = false;
    }

    void refreshBottomMenuItem(int index, int imgTag) {
        //目前頁面不能被換掉 或是已設定為不可拖曳項目不可被換掉
        if (index == nowPageIndex || getItemViewType(index) == TYPE_NO_DRAG)
            return;
        bottomDrawIdList.remove(index);
        bottomDrawIdList.add(index, imgTag);
        notifyItemChanged(index);
        bottomMenu.refreshTopBottomMenu(bottomDrawIdList);
    }

    void moveBottomMenuItem(int fromPos, int toPos) {
        if (fromPos == nowPageIndex) {
            nowPageIndex = toPos;
        } else if (toPos == nowPageIndex) {
            nowPageIndex = fromPos;
        }
        Collections.swap(bottomDrawIdList, fromPos, toPos);
        notifyItemMoved(fromPos, toPos);
    }

    ArrayList<Integer> getBottomDrawDataList() {
        return bottomDrawIdList;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        RelativeLayout item_bottom_bg;
        ImageView imageView;

        ViewHolder(@NonNull View itemView, int bottomItemWidth) {
            super(itemView);
            imageView = itemView.findViewById(R.id.img_item_menu);
            item_bottom_bg = itemView.findViewById(R.id.item_bottom_bg);
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            params.width = bottomItemWidth;
            item_bottom_bg.setLayoutParams(params);
        }
    }

    private View.OnLongClickListener onLongClickListener = new View.OnLongClickListener() {
        @Override
        public boolean onLongClick(View v) {
            if (!isOpenSelectList) {
                bottomMenu.showTopSelectList();
                bottomMenu.enableBottomMenuDrag(true);
                isOpenSelectList = true;
            }
            return true;
        }
    };

    private static class ClickListener implements View.OnClickListener {

        private BottomMenuAdapter bottomMenuAdapter;
        private ClickCallBack clickCallBack;
        private int position;

        ClickListener(BottomMenuAdapter bottomMenuAdapter, int position, ClickCallBack clickCallBack) {
            this.bottomMenuAdapter = bottomMenuAdapter;
            this.clickCallBack = clickCallBack;
            this.position = position;
        }

        @Override
        public void onClick(View v) {
            if (bottomMenuAdapter.isOpenSelectList || position == bottomMenuAdapter.nowPageIndex)
                return;

            clickCallBack.BottomMenuAdapterClickEvent(position);

            //Library不能使用switch...case 故使用If...else
//            if((int) v.getTag() == R.drawable.menu_gamelist) {
//                clickCallBack.ClickEvent(position);
//            } else if ((int) v.getTag() == R.drawable.menu_home) {
//                clickCallBack.ClickEvent(position);
//            } else if ((int) v.getTag() == R.drawable.menu_mine) {
//                clickCallBack.ClickEvent(position);
//            } else {
//                clickCallBack.ClickEvent(position);
//            }

//            switch ((int) v.getTag()) {
//                case R.drawable.menu_gamelist:
//                    Toast.makeText(context, "Game List", Toast.LENGTH_SHORT).show();
//                    clickCallBack.ClickEvent(position);
//                    break;
//                case R.drawable.menu_home:
//                    Toast.makeText(context, "Home", Toast.LENGTH_SHORT).show();
//                    clickCallBack.ClickEvent(position);
//                    break;
//                case R.drawable.menu_mine:
//                    Toast.makeText(context, "Mine", Toast.LENGTH_SHORT).show();
//                    clickCallBack.ClickEvent(position);
//                    break;
//            }
        }
    }

    public interface ClickCallBack {
        void BottomMenuAdapterClickEvent(int nowPosition);
    }

}