package com.ekosoftware.misrecetas.presentation.main.ui.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.ekosoftware.misrecetas.R
import com.ekosoftware.misrecetas.domain.model.Recipe
import com.ekosoftware.misrecetas.util.GlideApp
import com.google.firebase.storage.FirebaseStorage
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
        notifyDataSetChanged()
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

    inner class RecipesViewHolderViewHolder(itemView: View, private val interaction: Interaction?) :
        RecyclerView.ViewHolder(itemView) {
        fun bind(item: Recipe) {
            itemView.apply {
                setOnClickListener {
                    interaction?.onRecipeSelected(item)
                }
                recipe_image.setImage(item)
                txt_name.text = item.name
                //txt_creator.text = item.creator?.displayName ?: itemView.context.getString(R.string.anonymous)
                val timeRequired = "${item.timeRequired}'"
                txt_time_required.text = timeRequired
                txt_servings.text = "${item.servings}"
            }
        }

        private fun ImageView.setImage(item: Recipe) {

            // If url is available
            item.imageUrl?.takeIf { it.isNotEmpty() }?.let {
                GlideApp.with(itemView.context).load(it).centerCrop().into(this)
                return
            }

            // If not, check if FirebaseStorage has an image for this recipe
            item.imageUUID?.takeIf { it.isNotEmpty() }?.let {
                val image = FirebaseStorage.getInstance().reference.child("${item.imageUUID}.jpg")
                GlideApp.with(itemView.context).load(image)
                    .error(
                        GlideApp.with(itemView.context).load(ContextCompat.getDrawable(itemView.context, R.drawable.chef_hat))
                    )
                    .centerCrop()
                    .into(this)
                return
            }
            GlideApp.with(itemView.context).load(ContextCompat.getDrawable(itemView.context, R.drawable.chef_hat)).into(this)
        }
    }

    interface Interaction {
        fun onRecipeSelected(item: Recipe)
    }
}