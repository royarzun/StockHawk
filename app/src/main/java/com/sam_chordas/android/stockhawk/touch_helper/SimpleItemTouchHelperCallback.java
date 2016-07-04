package com.sam_chordas.android.stockhawk.touch_helper;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.View;

import com.sam_chordas.android.stockhawk.R;

/**
 * Created by sam_chordas on 10/6/15.
 * credit to Paul Burke (ipaulpro)
 * this class enables swipe to delete in RecyclerView
 */
public class SimpleItemTouchHelperCallback extends ItemTouchHelper.Callback{
  private static final String LOG_TAG = SimpleItemTouchHelperCallback.class.getSimpleName();
  private final ItemTouchHelperAdapter mAdapter;
  private Paint p = new Paint();
  public static final float ALPHA_FULL = 1.0f;

  public SimpleItemTouchHelperCallback(ItemTouchHelperAdapter adapter){
    mAdapter = adapter;
  }

  @Override
  public boolean isItemViewSwipeEnabled(){
    return true;
  }

  @Override
  public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder){
    final int dragFlags = 0;
    final int swipeFlags = ItemTouchHelper.START;
    return makeMovementFlags(dragFlags, swipeFlags);
  }

  @Override
  public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder sourceViewHolder, RecyclerView.ViewHolder targetViewHolder){
    return true;
  }

  @Override
  public void onSwiped(RecyclerView.ViewHolder viewHolder, int i){
    mAdapter.onItemDismiss(viewHolder.getAdapterPosition());
  }

  @Override
  public void onSelectedChanged(RecyclerView.ViewHolder viewHolder, int actionState){
    if (actionState != ItemTouchHelper.ACTION_STATE_IDLE){
      ItemTouchHelperViewHolder itemViewHolder = (ItemTouchHelperViewHolder) viewHolder;
      itemViewHolder.onItemSelected();
    }

    super.onSelectedChanged(viewHolder, actionState);
  }

  @Override
  public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder,
                          float dX, float dY, int actionState, boolean isCurrentlyActive) {
    Bitmap icon;
    if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
        View itemView = viewHolder.itemView;
        float height = (float) itemView.getBottom() - (float) itemView.getTop();
        float width = height / 3;

        if (dX < 0) {
          p.setColor(Color.parseColor("#D32F2F"));
          RectF background = new RectF((float) itemView.getRight() + dX, (float) itemView.getTop(),(float) itemView.getRight(), (float) itemView.getBottom());
          c.drawRect(background,p);
          icon = BitmapFactory.decodeResource(recyclerView.getResources(), R.drawable.ic_action_delete);
          RectF icon_dest = new RectF((float) itemView.getRight() - 2*width ,(float) itemView.getTop() + width,(float) itemView.getRight() - width,(float)itemView.getBottom() - width);
          c.drawBitmap(icon,null,icon_dest,p);
        }

    }
    super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
  }

  @Override
  public void clearView(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
    super.clearView(recyclerView, viewHolder);

    ItemTouchHelperViewHolder itemViewHolder = (ItemTouchHelperViewHolder) viewHolder;
    itemViewHolder.onItemClear();
  }
}
