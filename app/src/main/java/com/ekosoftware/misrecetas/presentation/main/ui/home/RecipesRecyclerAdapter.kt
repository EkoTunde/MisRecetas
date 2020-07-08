package com.ekosoftware.misrecetas.presentation.main.ui.home

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.ekosoftware.misrecetas.R
import com.ekosoftware.misrecetas.data.model.Difficulty
import com.ekosoftware.misrecetas.data.model.Difficulty.*
import com.ekosoftware.misrecetas.data.model.Recipe
import kotlinx.android.synthetic.main.item_recipe.view.*

class RecipesRecyclerAdapter(private val interaction: Interaction? = null) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val diffCallback = object : DiffUtil.ItemCallback<Recipe>() {

        override fun areItemsTheSame(oldItem: Recipe, newItem: Recipe): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Recipe, newItem: Recipe): Boolean {
            return oldItem == newItem
        }
    }

    private val differ = AsyncListDiffer(this, diffCallback)

    fun submitList(list: List<Recipe>) {
        differ.submitList(list)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecipesViewHolderViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_recipe, parent, false)
        return RecipesViewHolderViewHolder(view, interaction)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is RecipesViewHolderViewHolder -> {
                holder.bind(differ.currentList[position])
            }
        }
    }

    override fun getItemCount(): Int = differ.currentList.size

    inner class RecipesViewHolderViewHolder(
        itemView: View,
        private val interaction: Interaction?
    ) : RecyclerView.ViewHolder(itemView) {
        fun bind(item: Recipe) {
            itemView.setOnClickListener {
                interaction?.onRecipeSelected(item)
            }
            // Set image
            Glide.with(itemView.context).load(item.imageUrl).centerCrop().into(itemView.recipe_image)
            itemView.txt_name.text = item.name
            itemView.txt_creator.text =
                item.creator?.displayName ?: itemView.context.getString(R.string.anonymous)
            val timeRequired = "${item.timeRequired}'"
            itemView.txt_time_required.text = timeRequired
            itemView.txt_servings.text = "${item.servings}"
            item.difficulty?.let {
                val difficulty = getDifficultyLabel(item.difficulty!!, itemView.context)
                itemView.txt_difficulty.text = difficulty
            }
            itemView.ratingBar.rating = item.rating?.toFloat() ?: 0.0F

        }

        private fun getDifficultyLabel(difficulty: Difficulty, context: Context): String {
            return when (difficulty) {
                HARD -> context.getString(R.string.hard)
                NORMAL -> context.getString(R.string.normal)
                EASY -> context.getString(R.string.easy)
            }
        }
    }

    interface Interaction {
        fun onRecipeSelected(item: Recipe)
    }
}