package com.example.tabbardraganddrop;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.ClipData;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Point;
import android.os.Vibrator;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;

import static androidx.recyclerview.widget.ItemTouchHelper.END;
import static androidx.recyclerview.widget.ItemTouchHelper.START;
import static androidx.recyclerview.widget.ItemTouchHelper.SimpleCallback;

public class BottomMenu extends LinearLayout implements BottomMenuAdapter.ClickCallBack {

    private RelativeLayout top_recycle_view_bg;
    private RecyclerView top_recycle_view, bottom_recycle_view;
    private View mDragView;

    private TopMenuAdapter mTopMenuAdapter;
    private BottomMenuAdapter mBottomMenuAdapter;

    //    private int[] topViewList = {R.drawable.menu_gamelist, R.drawable.menu_home, R.drawable.menu_mine};
//    private String[] topViewTextList = {"賽事", "首頁", "我的", "我的"};
    private int[] topViewList;
    private String[] topViewTextList;
    private int[] bottomViewList = {R.drawable.menu_gamelist, R.drawable.menu_mine, R.drawable.menu_home};
    private ArrayList<Integer> topItemView = new ArrayList<>();
    private ArrayList<String> topItemText = new ArrayList<>();
    private ArrayList<Integer> selectedList = new ArrayList<>();

    private BottomMenuDragListener mBottomMenuDragListener;
    private GestureDetector mGestureDetector;
    private boolean isBottomCanMove = false;

    private TranslateAnimation mShowAction, mCloseAction;
    private Vibrator vib;
    private long[] vibratorPattern = {100, 200};

    private BottomMenuClickCallBack bottomMenuClickCallBack;

    public int fixedPositionInBottomMenuIndex = -1;
    public int fixedPositionInBottomMenuResId = -1;

    public BottomMenu(Context context) {
        super(context);
        initView();
    }

    public BottomMenu(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public BottomMenu(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    private void initView() {
        View view = inflate(getContext(), R.layout.layout_bottom_menu, this);
        top_recycle_view_bg = view.findViewById(R.id.top_recycle_view_bg);
        mGestureDetector = new GestureDetector(getContext(), new DragGestureListener());
        top_recycle_view = view.findViewById(R.id.top_recycle_view);
        bottom_recycle_view = view.findViewById(R.id.bottom_recycle_view);
        mBottomMenuDragListener = new BottomMenuDragListener(bottom_recycle_view);
        Button btn_select_list_finish = view.findViewById(R.id.btn_select_list_finish);
        btn_select_list_finish.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                dismissTopSelectList();
            }
        });
        setAnimation();
    }

    public void init(int[] nowBottomMenuImg, BottomMenuClickCallBack clickCallBack) {
        vib = (Vibrator) getContext().getSystemService(Service.VIBRATOR_SERVICE);
        this.bottomMenuClickCallBack = clickCallBack;
        TypedArray resIds = getContext().getResources().obtainTypedArray(R.array.tab_bar_item_drawable_array);
        int[] topViewList = new int[resIds.length()];
        for (int i = 0; i < resIds.length(); i++) {
            topViewList[i] = resIds.getResourceId(i, -1);
        }
        resIds.recycle();
        for (int item : topViewList) {
            topItemView.add(item);
        }
        topViewTextList = getResources().getStringArray(R.array.tab_bar_item_title_array);
        topItemText.addAll(Arrays.asList(topViewTextList));

        for (int value : bottomViewList) {
            selectedList.add(value);
        }

        initTopMenu();
        initBottomMenu();
    }

    private void initTopMenu() {
        top_recycle_view.setLayoutManager(new GridLayoutManager(getContext(), 4));
        top_recycle_view.addItemDecoration(new GridSpacingItemDecoration(4, 40, false));
        mTopMenuAdapter = new TopMenuAdapter(this, topItemView, topItemText, mOnTouchListener, mBottomMenuDragListener);
        mTopMenuAdapter.setSelectedItemList(selectedList);
        top_recycle_view.setAdapter(mTopMenuAdapter);
        mTopMenuAdapter.notifyDataSetChanged();
    }

    private void initBottomMenu() {
        bottom_recycle_view.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        mBottomMenuAdapter = new BottomMenuAdapter(this, selectedList, mBottomMenuDragListener, this);
        mBottomMenuAdapter.setNowPageIndex(0);
        bottom_recycle_view.setAdapter(mBottomMenuAdapter);
        mBottomMenuAdapter.notifyDataSetChanged();
        bottomMenuItemTouchHelper.attachToRecyclerView(null);
    }

    // 設定不想要被滑動的Item
    public void setFixedPositionInBottomMenu(int position) {
        if (bottomViewList == null || bottomViewList.length == 0 || mBottomMenuAdapter == null)
            return;
        fixedPositionInBottomMenuIndex = position;
        for (int i = 0; i < bottomViewList.length; i++) {
            if (fixedPositionInBottomMenuIndex != -1 && fixedPositionInBottomMenuIndex == i)
                fixedPositionInBottomMenuResId = bottomViewList[i];
        }
        mBottomMenuAdapter.setFixedPosition(position);
    }

    public void refreshTopBottomMenu(ArrayList<Integer> selectedList) {
        if (mTopMenuAdapter != null)
            mTopMenuAdapter.setSelectedItemList(selectedList);
    }

    public void enableBottomMenuDrag(boolean isBottomCanMove) {
        this.isBottomCanMove = isBottomCanMove;
        if (isBottomCanMove) {
            bottomMenuItemTouchHelper.attachToRecyclerView(bottom_recycle_view);
        } else {
            bottomMenuItemTouchHelper.attachToRecyclerView(null);
        }
    }

    private ItemTouchHelper bottomMenuItemTouchHelper = new ItemTouchHelper(new SimpleCallback(START | END,
            START | END) {
        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder from, @NonNull RecyclerView.ViewHolder target) {
            if (isBottomCanMove) {
                final int fromPos = from.getAdapterPosition();
                final int toPos = target.getAdapterPosition();

                Log.d("tag12345", "fromPos: " + fromPos + "　toPos: " + toPos);

                // 設定不可被移動的Item時判斷
                if (fixedPositionInBottomMenuIndex != -1 && toPos == fixedPositionInBottomMenuIndex) {
                    return false;
                } else {
                    mBottomMenuAdapter.moveBottomMenuItem(fromPos, toPos);
                    return true;
                }
            } else {
                return false;
            }

        }

        // 設定不可拖動移動的Item時使用
        @Override
        public int getMovementFlags(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
//            return super.getMovementFlags(recyclerView, viewHolder);
            if (viewHolder.getAdapterPosition() == fixedPositionInBottomMenuIndex) {
                return makeFlag(ItemTouchHelper.ACTION_STATE_IDLE, START | END);
            } else {
                return makeFlag(ItemTouchHelper.ACTION_STATE_DRAG,
                        START | END);
            }
        }

        @Override
        public void onSelectedChanged(@Nullable RecyclerView.ViewHolder viewHolder, int actionState) {
            super.onSelectedChanged(viewHolder, actionState);
            if (actionState != ItemTouchHelper.ACTION_STATE_IDLE) {
                if (vib != null) {
                    vib.vibrate(vibratorPattern, -1);
                }
            }
        }

        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {

        }
    });

    private OnTouchListener mOnTouchListener = new OnTouchListener() {
        @SuppressLint("ClickableViewAccessibility")
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            mDragView = v;
            return mGestureDetector.onTouchEvent(event);
        }
    };

    private class DragGestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            return super.onSingleTapConfirmed(e);
        }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            return super.onSingleTapUp(e);
        }

        @Override
        public boolean onContextClick(MotionEvent e) {
            return true;
        }

        @Override
        public void onLongPress(MotionEvent e) {
            super.onLongPress(e);
            if (vib != null) {
                vib.vibrate(vibratorPattern, -1);
            }
            ClipData data = ClipData.newPlainText("", "");
            ItemDragShadowBuilder shadowBuilder = new ItemDragShadowBuilder(
                    mDragView);
            mDragView.startDrag(data, shadowBuilder, mDragView, 0);
        }

        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }
    }

    private static class ItemDragShadowBuilder extends DragShadowBuilder {

        private final WeakReference<View> mView;

        ItemDragShadowBuilder(View view) {
            super(view);
            mView = new WeakReference<>(view);
        }

        @Override
        public void onDrawShadow(Canvas canvas) {
            canvas.scale(1.5F, 1.5F);
            super.onDrawShadow(canvas);
        }

        @Override
        public void onProvideShadowMetrics(Point shadowSize,
                                           Point shadowTouchPoint) {
            final View view = mView.get();
            if (view != null) {
                shadowSize.set((int) (view.getWidth() * 1.5F),
                        (int) (view.getHeight() * 1.5F));
                shadowTouchPoint.set(shadowSize.x / 2, shadowSize.y / 2);
            }
        }
    }

    public void showTopSelectList() {
        if (View.GONE == top_recycle_view_bg.getVisibility()) {
            if (vib != null) {
                vib.vibrate(vibratorPattern, -1);
            }
            top_recycle_view_bg.setVisibility(View.VISIBLE);
            top_recycle_view_bg.startAnimation(mShowAction);
        }
        if (mTopMenuAdapter != null)
            mTopMenuAdapter.isTopViewShow(true);
        if (mBottomMenuAdapter != null)
            mBottomMenuAdapter.isTopViewShow(true);
    }

    public void dismissTopSelectList() {
        if (View.VISIBLE == top_recycle_view_bg.getVisibility()) {
            if (mBottomMenuAdapter != null) {
                mBottomMenuAdapter.closeSelectList();
            }
            top_recycle_view_bg.startAnimation(mCloseAction);
        }
        if (mTopMenuAdapter != null)
            mTopMenuAdapter.isTopViewShow(false);
        if (mBottomMenuAdapter != null)
            mBottomMenuAdapter.isTopViewShow(false);
    }

    public boolean checkIsSelectedMenuOpen() {
        return top_recycle_view_bg.getVisibility() == View.VISIBLE;
    }

    public void setAnimation() {
        mShowAction = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, 1.0f, Animation.RELATIVE_TO_SELF, 0.0f);
        mShowAction.setDuration(300);
        mCloseAction = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, 0.0f,
                Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, 1.0f);
        mCloseAction.setDuration(300);
        mCloseAction.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                top_recycle_view_bg.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

    }

    @Override
    public void BottomMenuAdapterClickEvent(int nowPosition) {

        int nowSelectedItemResId = mBottomMenuAdapter.getBottomDrawDataList().get(nowPosition);
        String nowSelectedItemText = "";

        for (int i = 0; i < topItemView.size(); i++) {
            if (topItemView.get(i) == nowSelectedItemResId) {
                nowSelectedItemText = topItemText.get(i);
                break;
            }
        }

        if (bottomMenuClickCallBack != null)
            bottomMenuClickCallBack.BottomMenuClickEvent(nowSelectedItemText);
    }

    public interface BottomMenuClickCallBack {
        void BottomMenuClickEvent(String nowSelectedItemText);
    }

}
