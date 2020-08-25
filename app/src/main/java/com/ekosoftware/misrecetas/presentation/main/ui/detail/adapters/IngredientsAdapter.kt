package com.ekosoftware.misrecetas.presentation.main.ui.detail.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ekosoftware.misrecetas.R
import com.ekosoftware.misrecetas.base.BaseViewHolder
import kotlinx.android.synthetic.main.item_detail_ingredients.view.*

class IngredientsAdapter(private val context: Context, private val ingredients: List<String>) :
    RecyclerView.Adapter<BaseViewHolder<*>>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<*> {
        return IngredientsViewHolder(
            LayoutInflater.from(context).inflate(R.layout.item_detail_ingredients, parent, false)
        )
    }

    override fun getItemCount(): Int = ingredients.size

    override fun onBindViewHolder(holder: BaseViewHolder<*>, position: Int) {
        when(holder){
            is IngredientsViewHolder -> holder.bind(ingredients[position], position)
            else -> throw IllegalArgumentException("${holder.javaClass.name} isn't a valid ViewHolder for ${this.javaClass.name}")
        }
    }

    class IngredientsViewHolder(itemView: View) : BaseViewHolder<String>(itemView) {
        override fun bind(item: String, position: Int) {
            itemView.txt_ingredient.text = item
        }
    }
}