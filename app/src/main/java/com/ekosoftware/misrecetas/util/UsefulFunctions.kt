package com.ekosoftware.misrecetas.util

import android.app.Activity
import android.content.Context
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.Fragment
import com.ekosoftware.misrecetas.domain.model.Recipe

fun IntArray.contains(integer: Int) : Boolean{
    this.forEach {
        if (it == integer) return true
    }
    return false
}

fun Fragment.hideKeyboard() {
    view?.let { activity?.hideKeyboard(it) }
}

fun Activity.hideKeyboard() {
    hideKeyboard(currentFocus ?: View(this))
}

fun Context.hideKeyboard(view: View) {
    val inputMethodManager = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
    inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
}

fun View.hide() {
    visibility = View.GONE
}

fun View.show() {
    visibility = View.VISIBLE
}

fun Recipe.isNotEqual(recipe: Recipe) : Boolean {
    return this.name != recipe.name
            || this.description != recipe.description
            || this.timeRequired != recipe.timeRequired
            || this.servings != recipe.servings
            || this.imageUrl != recipe.imageUrl
            || this.imageUUID != recipe.imageUUID
            || this.ingredients != recipe.ingredients
            || this.instructions != recipe.instructions
}