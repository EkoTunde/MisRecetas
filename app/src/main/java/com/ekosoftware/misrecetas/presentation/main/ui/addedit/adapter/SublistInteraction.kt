package com.ekosoftware.misrecetas.presentation.main.ui.addedit.adapter

interface SublistInteraction {
    fun addLine(position: Int, fromItemCurrentCursorIndex: Int)
    fun onDelete(position: Int)
    fun onMoved(fromPosition: Int, toPosition: Int)
    fun onItemUpdated(position: Int, newText: String)
}