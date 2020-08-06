package com.ekosoftware.misrecetas.presentation.main.ui.detail.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ekosoftware.misrecetas.R
import com.ekosoftware.misrecetas.base.BaseViewHolder
import kotlinx.android.synthetic.main.item_detail_instructions.view.*

class InstructionsAdapter(private val context: Context, private val instructions: List<String>) :

    RecyclerView.Adapter<BaseViewHolder<*>>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<*> {
        return InstructionsViewHolder(
            LayoutInflater.from(context).inflate(R.layout.item_detail_instructions, parent, false)
        )
    }

    override fun getItemCount(): Int = instructions.size

    override fun onBindViewHolder(holder: BaseViewHolder<*>, position: Int) {
        when (holder) {
            is InstructionsViewHolder -> holder.bind(instructions[position], position)
            else -> throw IllegalArgumentException("${holder.javaClass.name} isn't a valid ViewHolder for ${this.javaClass.name}")
        }
    }

    inner class InstructionsViewHolder(itemView: View) : BaseViewHolder<String>(itemView) {
        override fun bind(item: String, position: Int) {
            itemView.txt_step_num.text = position.toString()
            itemView.txt_step_description.text = item
        }
    }
}