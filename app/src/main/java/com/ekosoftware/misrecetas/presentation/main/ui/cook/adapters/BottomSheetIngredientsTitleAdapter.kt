package com.ekosoftware.misrecetas.presentation.main.ui.cook.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.ekosoftware.misrecetas.R
import com.google.android.material.bottomsheet.BottomSheetBehavior
import kotlinx.android.synthetic.main.item_bottom_sheet_ingredients_title.view.*

class BottomSheetIngredientsTitleAdapter(private val context: Context, private val title: String, private val listener: OnStateImagePressedListener?) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var bottomSheetState = BottomSheetBehavior.STATE_COLLAPSED

    fun setStateIndicator(state: Int) {
        bottomSheetState = state
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_bottom_sheet_ingredients_title, parent, false)
        return IngredientsTitleViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as IngredientsTitleViewHolder).bind(context, title, bottomSheetState)
    }

    override fun getItemCount(): Int = 1

    inner class IngredientsTitleViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(context: Context, title: String, bottomSheetState: Int) {
            itemView.title.text = title
            if (bottomSheetState == BottomSheetBehavior.STATE_COLLAPSED) {
                itemView.bottom_sheet_drag_view.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_outline_keyboard_arrow_up_24))
            } else {
                itemView.bottom_sheet_drag_view.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_outline_keyboard_arrow_down_24))
            }
            itemView.bottom_sheet_drag_view.setOnClickListener {
                listener?.onPressed()
            }
        }
    }

    interface OnStateImagePressedListener {
        fun onPressed()
    }
}