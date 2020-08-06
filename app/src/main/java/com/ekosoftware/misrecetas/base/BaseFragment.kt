package com.ekosoftware.misrecetas.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.fragment.app.Fragment

abstract class BaseFragment : Fragment(){

    abstract val mainLayouts: List<View>?
    abstract val rootLayout: ViewGroup?
    abstract val progressBar: View?

    fun showProgress() {
        rootLayout?.visibility = GONE
        mainLayouts?.forEach {
            it.visibility = GONE
        }
        progressBar?.visibility = VISIBLE
    }

    fun hideProgress() {
        rootLayout?.visibility = VISIBLE
        mainLayouts?.forEach {
            it.visibility = VISIBLE
        }
        progressBar?.visibility = GONE
    }

}