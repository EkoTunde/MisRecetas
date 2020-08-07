package com.ekosoftware.misrecetas.util

fun IntArray.contains(integer: Int) : Boolean{
    this.forEach {
        if (it == integer) return true
    }
    return false
}