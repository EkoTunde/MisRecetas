package com.ekosoftware.misrecetas.presentation.main.ui.cook

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ekosoftware.misrecetas.R
import com.ekosoftware.misrecetas.base.BaseViewHolder
import kotlinx.android.synthetic.main.item_checkable_ingredients.view.*

class CheckableIngredientsRecyclerAdapter(private val context: Context, private val listener: OnIngredientCheckedListener?) :
    RecyclerView.Adapter<BaseViewHolder<*>>() {

    private var checkableIngredients: List<CheckableIngredient> = listOf()

    fun submitList(checkableIngredients: List<CheckableIngredient>) {
        this.checkableIngredients = checkableIngredients
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<*> {
        val view = LayoutInflater.from(context).inflate(R.layout.item_checkable_ingredients, parent, false)
        return CheckableIngredientsViewHolder(view, listener)
    }

    override fun onBindViewHolder(holder: BaseViewHolder<*>, position: Int) {
        when (holder) {
            is CheckableIngredientsViewHolder -> holder.bind(checkableIngredients[position], position)
            else -> throw IllegalArgumentException("${holder.javaClass.name} is not a valid subclass or implementation of CheckableIngredientsViewHolder")
        }
    }

    override fun getItemCount(): Int = checkableIngredients.size

    inner class CheckableIngredientsViewHolder(itemView: View, private val listener: OnIngredientCheckedListener?) :
        BaseViewHolder<CheckableIngredient>(itemView) {
        override fun bind(item: CheckableIngredient, position: Int) {
            itemView.check_box_ingredient.apply {
                text = item.ingredient
                isChecked = item.isChecked
                setOnCheckedChangeListener { _, isChecked -> listener?.onChecked(position, isChecked) }
            }
        }
    }

    interface OnIngredientCheckedListener {
        fun onChecked(position: Int, isChecked: Boolean)
    }
}

data class CheckableIngredient(val ingredient: String, val isChecked: Boolean)