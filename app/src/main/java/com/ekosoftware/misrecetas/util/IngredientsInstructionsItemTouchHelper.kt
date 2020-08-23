package com.ekosoftware.misrecetas.util

import android.graphics.Color
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.ekosoftware.misrecetas.R

class IngredientsInstructionsItemTouchHelper(
    private val adapter: ItemTouchHelperAdapter
) : ItemTouchHelper.Callback() {

    override fun isLongPressDragEnabled(): Boolean {
        // Disabled because this would be handle by ViewHolder inside RecyclerView
        return false /*super.isLongPressDragEnabled()*/
    }

    override fun isItemViewSwipeEnabled(): Boolean {
        // For being able to swipe the views
        return true
    }

    override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
        super.clearView(recyclerView, viewHolder)
        // Change ViewHolder when dragged item is released
        viewHolder.itemView.setBackgroundColor(ContextCompat.getColor(viewHolder.itemView.context, R.color.colorPlainBackground))
    }

    override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
        super.onSelectedChanged(viewHolder, actionState)
        if (actionState == ItemTouchHelper.ACTION_STATE_DRAG) {
            viewHolder?.itemView?.setBackgroundColor(Color.LTGRAY)
        }
    }

    override fun getMovementFlags(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder): Int {
        val dragFlags = ItemTouchHelper.UP or ItemTouchHelper.DOWN
        val swipeFlags = ItemTouchHelper.START or ItemTouchHelper.END
        return makeMovementFlags(dragFlags, swipeFlags)
    }

    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean {
        adapter.onItemMoved(viewHolder.absoluteAdapterPosition, target.absoluteAdapterPosition)
        // True because we want to consume the action
        return true
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        adapter.onItemSwiped(viewHolder.absoluteAdapterPosition)
    }
}