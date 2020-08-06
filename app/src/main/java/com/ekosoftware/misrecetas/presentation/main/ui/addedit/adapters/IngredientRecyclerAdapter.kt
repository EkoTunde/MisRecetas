package com.ekosoftware.misrecetas.presentation.main.ui.addedit.adapters

import android.text.Editable
import android.text.TextWatcher
import android.view.*
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.ekosoftware.misrecetas.R
import com.ekosoftware.misrecetas.util.ItemTouchHelperAdapter
import kotlinx.android.synthetic.main.item_ingredient.view.*

class IngredientRecyclerAdapter(
    private val interaction: Interaction? = null
) : RecyclerView.Adapter<RecyclerView.ViewHolder>(), ItemTouchHelperAdapter {

    private lateinit var touchHelper: ItemTouchHelper

    private val diffCallback = object : DiffUtil.ItemCallback<String>() {

        override fun areItemsTheSame(oldItem: String, newItem: String): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: String, newItem: String): Boolean {
            return oldItem == newItem
        }
    }

    private val differ = AsyncListDiffer(this, diffCallback)

    inner class IngredientViewHolder(
        itemView: View,
        private val interaction: Interaction?
    ) : RecyclerView.ViewHolder(itemView), View.OnTouchListener, GestureDetector.OnGestureListener {

        private val gestureDetector by lazy { GestureDetector(itemView.context, this@IngredientViewHolder) }

        fun bind(item: String) {
            itemView.apply {
                setOnTouchListener(this@IngredientViewHolder)
                text_input_ingredient.apply {
                    this.setText(item)
                    this.setOnEditorActionListener(object : TextView.OnEditorActionListener {
                        override fun onEditorAction(v: TextView?, actionId: Int, event: KeyEvent?): Boolean {
                            if (actionId == EditorInfo.IME_ACTION_NEXT) {
                                interaction?.addLine(absoluteAdapterPosition)
                                return true
                            }
                            return false
                        }
                    })
                    this.setOnClickListener {
                        interaction?.onFocus(absoluteAdapterPosition)
                    }
                    this.onFocusChangeListener = View.OnFocusChangeListener { _, hasFocus ->
                        if (hasFocus) {
                            interaction?.onFocus(absoluteAdapterPosition)
                        }
                    }
                    this.addTextChangedListener(object : TextWatcher{
                        override fun afterTextChanged(s: Editable?) {
                            interaction?.onItemUpdated(absoluteAdapterPosition, s.toString())
                        }
                        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
                        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit
                    })
                }

                button_delete.setOnClickListener {
                    interaction?.onDelete(absoluteAdapterPosition)
                }

                button_drag.setOnTouchListener(this@IngredientViewHolder)
            }
        }

        override fun onShowPress(e: MotionEvent?) = Unit

        override fun onSingleTapUp(e: MotionEvent?): Boolean {
            itemView.text_input_ingredient.requestFocus()
            return false // Return true if not working
        }

        override fun onDown(e: MotionEvent?): Boolean = false
        override fun onFling(e1: MotionEvent?, e2: MotionEvent?, velocityX: Float, velocityY: Float): Boolean =
            false // return true if not working

        override fun onScroll(e1: MotionEvent?, e2: MotionEvent?, distanceX: Float, distanceY: Float): Boolean = false
        override fun onLongPress(e: MotionEvent?) = touchHelper.startDrag(this@IngredientViewHolder)

        override fun onTouch(v: View?, event: MotionEvent?): Boolean {
            gestureDetector.onTouchEvent(event)
            event?.let {
                when (it.action) {
                    MotionEvent.ACTION_DOWN -> Unit
                    MotionEvent.ACTION_UP -> v?.performClick()
                    else -> Unit
                }
            }

            // True to consume event
            return true
        }
    }

    interface Interaction {
        fun addLine(position: Int)
        fun onDelete(position: Int)
        fun onFocus(position: Int)
        fun onMoved(fromPosition: Int, toPosition: Int)
        fun onItemUpdated(position: Int, newText: String)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IngredientViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.item_ingredient, parent, false)
        return IngredientViewHolder(view, interaction)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is IngredientViewHolder -> {
                holder.bind(differ.currentList[position])
            }
        }
    }

    override fun getItemCount(): Int = differ.currentList.size

    override fun onViewAttachedToWindow(holder: RecyclerView.ViewHolder) {
        super.onViewAttachedToWindow(holder)
        holder.itemView.text_input_ingredient.requestFocus()
    }

    fun submitList(list: List<String>) {
        differ.submitList(list)
    }

    fun setTouchHelper(touchHelper: ItemTouchHelper) {
        this.touchHelper = touchHelper
    }

    override fun onItemMoved(fromPosition: Int, toPosition: Int) {
        interaction?.onMoved(fromPosition, toPosition)
    }

    override fun onItemSwiped(position: Int) {
        interaction?.onDelete(position)
    }
}