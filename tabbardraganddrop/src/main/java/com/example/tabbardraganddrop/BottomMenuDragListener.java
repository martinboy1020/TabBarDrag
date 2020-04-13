package com.example.tabbardraganddrop;

import android.view.DragEvent;
import android.view.View;

import androidx.recyclerview.widget.RecyclerView;

public class BottomMenuDragListener implements View.OnDragListener {

    private RecyclerView bottom_recycle_view;

    BottomMenuDragListener(RecyclerView bottom_recycle_view) {
        this.bottom_recycle_view = bottom_recycle_view;
    }

    @Override
    public boolean onDrag(View v, DragEvent event) {

        switch (event.getAction()) {
            case DragEvent.ACTION_DRAG_STARTED:
                // Do nothing
                break;
            case DragEvent.ACTION_DRAG_ENTERED:
                v.setAlpha(0.5F);
                break;
            case DragEvent.ACTION_DRAG_EXITED:
                v.setAlpha(1F);
                break;
            case DragEvent.ACTION_DROP:
                View view = (View) event.getLocalState();
                boolean isOutSideView = true;
                BottomMenuAdapter bottomMenuAdapter = (BottomMenuAdapter) bottom_recycle_view.getAdapter();
                if (bottomMenuAdapter != null) {

                    for (int i = 0, j = bottom_recycle_view.getChildCount(); i < j; i++) {
                        View child = bottom_recycle_view.getChildAt(i).findViewById(R.id.img_item_menu);
                        if ((int) child.getTag() == (int) view.getTag()) {
                            isOutSideView = false;
                            break;
                        }
                    }

                    for (int i = 0, j = bottom_recycle_view.getChildCount(); i < j; i++) {

                        View child = bottom_recycle_view.getChildAt(i).findViewById(R.id.img_item_menu);

                        if (child == v && isOutSideView) {
                            // 当前位置
                            bottomMenuAdapter.refreshBottomMenuItem(i, (int) view.getTag());
                            break;
                        }
                    }
                    break;

                }
            case DragEvent.ACTION_DRAG_ENDED:
                v.setAlpha(1F);
            default:
                break;
        }
        return true;
    }
}
