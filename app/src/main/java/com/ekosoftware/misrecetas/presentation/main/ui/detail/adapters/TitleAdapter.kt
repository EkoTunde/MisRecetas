package com.ekosoftware.misrecetas.presentation.main.ui.detail.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ekosoftware.misrecetas.R
import com.ekosoftware.misrecetas.base.BaseViewHolder
import kotlinx.android.synthetic.main.item_detail_title.view.*

class TitleAdapter(private val context: Context, private val title: String) : RecyclerView.Adapter<BaseViewHolder<*>>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<*> {
        return TitleViewHolder((LayoutInflater.from(context).inflate(R.layout.item_detail_title, parent, false)))
    }

    override fun getItemCount(): Int = 1

    override fun onBindViewHolder(holder: BaseViewHolder<*>, position: Int) {
        when (holder) {
            is TitleViewHolder -> holder.bind(title, position)
            else -> throw IllegalArgumentException("${holder.javaClass.name} isn't a valid ViewHolder for ${this.javaClass.name}")
        }
    }

    class TitleViewHolder(itemView: View) : BaseViewHolder<String>(itemView) {
        override fun bind(item: String, position: Int) {
            itemView.txt_ingredients_title.text = item
        }
    }
}