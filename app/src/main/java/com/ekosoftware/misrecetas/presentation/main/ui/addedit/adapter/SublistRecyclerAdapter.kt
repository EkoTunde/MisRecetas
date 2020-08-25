package com.ekosoftware.misrecetas.presentation.main.ui.addedit.adapter

import android.content.Context
import android.view.*
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.ekosoftware.misrecetas.R
import com.ekosoftware.misrecetas.util.ItemTouchHelperAdapter
import kotlinx.android.synthetic.main.item_sublist.view.*

enum class Type {
    INGREDIENTS, INSTRUCTIONS
}

class SublistRecyclerAdapter(
    private val context: Context, private val type: Type, private val interaction: SublistInteraction? = null
) : RecyclerView.Adapter<RecyclerView.ViewHolder>(), ItemTouchHelperAdapter {

    private lateinit var touchHelper: ItemTouchHelper

    private val diffCallback = object : DiffUtil.ItemCallback<String>() {
        override fun areItemsTheSame(oldItem: String, newItem: String): Boolean = oldItem == newItem
        override fun areContentsTheSame(oldItem: String, newItem: String): Boolean = oldItem == newItem
    }

    private val differ = AsyncListDiffer(this, diffCallback)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_sublist, parent, false)
        return SublistViewHolder(view, interaction)
    }

    override fun getItemCount(): Int = differ.currentList.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as SublistViewHolder).bind(differ.currentList[position])
    }

    inner class SublistViewHolder(
        itemView: View,
        private val interaction: SublistInteraction?
    ) : RecyclerView.ViewHolder(itemView), View.OnTouchListener, GestureDetector.OnGestureListener {

        private val gestureDetector by lazy { GestureDetector(itemView.context, this@SublistViewHolder) }

        fun bind(item: String) {

            initViewForType()
            updateView()
            itemView.setOnTouchListener(this@SublistViewHolder)
            itemView.text_sublist_item.apply {
                if (type == Type.INGREDIENTS) hint = this@SublistRecyclerAdapter.context.getString(R.string.ingredient_suggestion)
                setText(item)
                itemView.text_input_layout.isEndIconVisible = false
                setOnEditorActionListener(object : TextView.OnEditorActionListener {
                    override fun onEditorAction(v: TextView?, actionId: Int, event: KeyEvent?): Boolean {
                        if (actionId == EditorInfo.IME_ACTION_NEXT) {
                            // Send cursor's position so fragment can take cursor's following text
                            // to create the new line. So, if the cursor is text's last index, the new line
                            // item's value will be an empty String
                            val cursorPosition = itemView.text_sublist_item.selectionStart
                            interaction?.addLine(absoluteAdapterPosition, cursorPosition)
                            return true
                        }
                        return false
                    }
                })
                addTextChangedListener { interaction?.onItemUpdated(absoluteAdapterPosition, it.toString()) }
            }

            itemView.button_delete.setOnClickListener { interaction?.onDelete(absoluteAdapterPosition) }
            itemView.button_drag.setOnTouchListener(this@SublistViewHolder)
        }

        private fun initViewForType() = itemView.run {
            val hintAndDescription = when (type) {
                Type.INGREDIENTS -> Pair(false, this@SublistRecyclerAdapter.context.getString(R.string.ingredient))
                Type.INSTRUCTIONS -> Pair(true, this@SublistRecyclerAdapter.context.getString(R.string.instruction))
            }
            text_input_layout.isHintEnabled = hintAndDescription.first
            text_input_layout.contentDescription = hintAndDescription.second
        }

        fun updateView() {
            if (type != Type.INGREDIENTS) {
                itemView.text_input_layout.hint =
                    context.getString(R.string.current_step_placeholder, absoluteAdapterPosition + 1)
            }
        }

        override fun onShowPress(e: MotionEvent?) = Unit

        override fun onSingleTapUp(e: MotionEvent?): Boolean {
            itemView.text_sublist_item.requestFocus()
            return false // Return true if not working
        }

        override fun onDown(e: MotionEvent?): Boolean = false
        override fun onFling(e1: MotionEvent?, e2: MotionEvent?, velocityX: Float, velocityY: Float): Boolean =
            false // return true if not working

        override fun onScroll(e1: MotionEvent?, e2: MotionEvent?, distanceX: Float, distanceY: Float): Boolean = false
        override fun onLongPress(e: MotionEvent?) = touchHelper.startDrag(this@SublistViewHolder)

        override fun onTouch(v: View?, event: MotionEvent?): Boolean {
            gestureDetector.onTouchEvent(event)
            event?.let {
                when (it.action) {
                    MotionEvent.ACTION_DOWN -> Unit
                    MotionEvent.ACTION_UP -> v?.performClick()
                    else -> Unit
                }
            }
            return true // True to consume event
        }
    }

    // Holds an array of items positions which are allowed to gain focus when a notify#Changed occurs
    private var focusableItems: IntArray = intArrayOf()

    // Set the items which can gain focus when notify#Changed
    fun setFocusableItems(vararg positions: Int) {
        focusableItems = positions
    }

    override fun onViewAttachedToWindow(holder: RecyclerView.ViewHolder) {
        super.onViewAttachedToWindow(holder)
        (holder as SublistViewHolder).updateView()
        if (focusableItems.contains(holder.absoluteAdapterPosition)) holder.itemView.text_sublist_item.requestFocus()
    }

    fun submitList(list: List<String>) = differ.submitList(list)

    fun setTouchHelper(touchHelper: ItemTouchHelper) {
        this.touchHelper = touchHelper
    }

    override fun onItemMoved(fromPosition: Int, toPosition: Int) {
        interaction?.onMoved(fromPosition, toPosition)
    }

    override fun onItemSwiped(position: Int) {
        interaction?.onDelete(position)
    }

    fun notifyAddLine(list: List<String>, position: Int, itemCount: Int) {
        submitList(list)
        notifyItemChanged(position) // Notify adapter "From item" has changed
        notifyItemInserted(position + 1) // Notify adapter of new item

        // Forces following items to update so they accurately indicate the step (a.k.a. adapterPosition + 1) by the hint
        notifyItemRangeChanged(position + 1, itemCount)
    }

    fun notifyOnDelete(list: List<String>, position: Int) {
        submitList(list) // Update adapter's list
        notifyItemRemoved(position)

        // Forces following items to update so they accurately indicate the step
        // (a.k.a. adapterPosition + 1) by the hint
        notifyItemRangeChanged(position, list.size)
    }

    fun notifyItemsMoved(sublist: MutableList<String>, fromPosition: Int, toPosition: Int) {
        submitList(sublist)
        if (type == Type.INSTRUCTIONS) {
            // Notifies to the adapter a range changed so steps (a.k.a. adapterPosition + 1) indicated by the hint are updated
            if (fromPosition <= toPosition) notifyItemRangeChanged(fromPosition, toPosition + 1 - fromPosition)
            else notifyItemRangeChanged(toPosition, fromPosition + 1 - toPosition)
        }
    }
}