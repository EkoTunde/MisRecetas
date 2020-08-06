package com.ekosoftware.misrecetas.util

interface ItemTouchHelperAdapter {

    fun onItemMoved(fromPosition: Int, toPosition: Int)
    fun onItemSwiped(position: Int)

}