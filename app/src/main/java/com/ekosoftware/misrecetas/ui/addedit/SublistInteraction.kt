package com.ekosoftware.misrecetas.ui.addedit

interface SublistInteraction {
    fun addLine(position: Int, fromItemCurrentCursorIndex: Int)
    fun onDelete(position: Int)
    fun onMoved(fromPosition: Int, toPosition: Int)
    fun onItemUpdated(position: Int, newText: String)
}