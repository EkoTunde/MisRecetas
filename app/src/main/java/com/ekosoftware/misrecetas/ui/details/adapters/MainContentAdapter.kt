package com.ekosoftware.misrecetas.ui.details.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ekosoftware.misrecetas.R
import com.ekosoftware.misrecetas.core.BaseViewHolder
import com.ekosoftware.misrecetas.domain.model.Recipe
import kotlinx.android.synthetic.main.item_detail_main_content.view.*

class MainContentAdapter(private val context: Context) :
    RecyclerView.Adapter<BaseViewHolder<*>>() {

    private var recipe: Recipe = Recipe()

    fun submitRecipe(recipe: Recipe) {
        this.recipe = recipe
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<*> {
        return MainContentViewHolder(
            LayoutInflater.from(context).inflate(R.layout.item_detail_main_content, parent, false)
        )
    }

    override fun getItemCount(): Int = 1

    override fun onBindViewHolder(holder: BaseViewHolder<*>, position: Int) {
        when (holder) {
            is MainContentViewHolder -> holder.bind(recipe, position)
            else -> throw IllegalArgumentException("${holder.javaClass.name} isn't a valid ViewHolder for ${this.javaClass.name}")
        }
    }

    inner class MainContentViewHolder(itemView: View) : BaseViewHolder<Recipe>(itemView) {
        override fun bind(item: Recipe, position: Int) {
            // Set timeRequired txt
            recipe.timeRequired.let { time ->
                itemView.txt_time_required.let { view ->
                    if (recipe.timeRequired != null) {
                        view.text = time.toString()
                    } else {
                        view.visibility = View.GONE
                    }
                }
            }

            // Set servings
            itemView.txt_servings.text = item.servings.toString()

            // Set description
            if (item.description != null) {
                itemView.txt_description.text = item.description.toString()
            } else {
                itemView.layout_description.visibility = View.GONE
            }
        }
    }
}