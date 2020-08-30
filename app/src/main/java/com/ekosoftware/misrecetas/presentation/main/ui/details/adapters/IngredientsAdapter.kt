package com.ekosoftware.misrecetas.presentation.main.ui.details.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.ekosoftware.misrecetas.R
import com.ekosoftware.misrecetas.base.BaseViewHolder
import kotlinx.android.synthetic.main.item_detail_ingredients.view.*

class IngredientsAdapter(private val context: Context) :
    RecyclerView.Adapter<BaseViewHolder<*>>() {

    fun submitList(list: List<String>) {
        differ.submitList(list)
        notifyDataSetChanged()
    }

    private val diffCallback = object : DiffUtil.ItemCallback<String>() {
        override fun areItemsTheSame(oldItem: String, newItem: String): Boolean = oldItem == newItem
        override fun areContentsTheSame(oldItem: String, newItem: String): Boolean = oldItem == newItem
    }

    private val differ = AsyncListDiffer(this, diffCallback)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<*> {
        return IngredientsViewHolder(
            LayoutInflater.from(context).inflate(R.layout.item_detail_ingredients, parent, false)
        )
    }

    override fun getItemCount(): Int = differ.currentList.size

    override fun onBindViewHolder(holder: BaseViewHolder<*>, position: Int) {
        when(holder){
            is IngredientsViewHolder -> holder.bind(differ.currentList[position], position)
            else -> throw IllegalArgumentException("${holder.javaClass.name} isn't a valid ViewHolder for ${this.javaClass.name}")
        }
    }

    class IngredientsViewHolder(itemView: View) : BaseViewHolder<String>(itemView) {
        override fun bind(item: String, position: Int) {
            itemView.txt_ingredient.text = item
        }
    }
}