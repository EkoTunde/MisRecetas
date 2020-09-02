package com.ekosoftware.misrecetas.presentation.main.ui.cook.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.ekosoftware.misrecetas.R
import com.ekosoftware.misrecetas.domain.model.Ingredient
import kotlinx.android.synthetic.main.item_checkable_ingredients.view.*

class BottomSheetIngredientsRecyclerAdapter(private val context: Context, private val listener: OnIngredientCheckedListener?) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val diffCallback = object : DiffUtil.ItemCallback<Ingredient>() {
        override fun areItemsTheSame(oldItem: Ingredient, newItem: Ingredient): Boolean = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Ingredient, newItem: Ingredient): Boolean = oldItem == newItem
    }

    private val differ = AsyncListDiffer(this, diffCallback)

    fun submitList(ingredients: List<Ingredient>) = differ.submitList(ingredients)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_checkable_ingredients, parent, false)
        return CheckableIngredientsViewHolder(view, listener)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        /*when (holder) {
            is CheckableIngredientsViewHolder ->*/
        (holder as CheckableIngredientsViewHolder).bind(differ.currentList[position])
        /*  else -> throw IllegalArgumentException("${holder.javaClass.name} is not a valid subclass or implementation of CheckableIngredientsViewHolder")
        }*/
    }

    override fun getItemCount(): Int = differ.currentList.size

    class CheckableIngredientsViewHolder(itemView: View, private val listener: OnIngredientCheckedListener?) :
        RecyclerView.ViewHolder(itemView) {
        fun bind(item: Ingredient) {
            itemView.check_box_ingredient.apply {
                text = item.name
                check_box_ingredient.isChecked = item.isChecked
                setOnCheckedChangeListener(null)
                setOnCheckedChangeListener { _, isChecked ->
                    listener?.onChecked(bindingAdapterPosition, isChecked)
                }
            }
        }
    }

    interface OnIngredientCheckedListener {
        fun onChecked(position: Int, isChecked: Boolean)
    }
}