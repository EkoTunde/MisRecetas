package com.ekosoftware.misrecetas.presentation.main.ui.cook.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ekosoftware.misrecetas.R
import com.ekosoftware.misrecetas.base.BaseViewHolder
import kotlinx.android.synthetic.main.item_checkable_ingredients.view.*

class BottomSheetIngredientsRecyclerAdapter(private val context: Context/*, private val listener: OnIngredientCheckedListener?*/) :
    RecyclerView.Adapter<BaseViewHolder<*>>() {

    private var ingredients: List<String> = listOf()

    fun submitList(ingredients: List<String>) {
        this.ingredients = ingredients
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<*> {
        val view = LayoutInflater.from(context).inflate(R.layout.item_checkable_ingredients, parent, false)
        return CheckableIngredientsViewHolder(view /*, listener*/)
    }

    override fun onBindViewHolder(holder: BaseViewHolder<*>, position: Int) {
        when (holder) {
            is CheckableIngredientsViewHolder -> holder.bind(ingredients[position], position)
            else -> throw IllegalArgumentException("${holder.javaClass.name} is not a valid subclass or implementation of CheckableIngredientsViewHolder")
        }
    }

    override fun getItemCount(): Int = ingredients.size

    class CheckableIngredientsViewHolder(itemView: View/*, private val listener: OnIngredientCheckedListener?*/) :
        BaseViewHolder<String>(itemView) {
        override fun bind(item: String, position: Int) {
            itemView.check_box_ingredient.apply {
                text = item
                //setOnCheckedChangeListener { _, isChecked -> listener?.onChecked(position, isChecked) }
            }
        }
    }
    /*interface OnIngredientCheckedListener {
        fun onChecked(position: Int, isChecked: Boolean)
    }*/
}

//data class CheckableIngredient(val ingredient: String, val isChecked: Boolean)