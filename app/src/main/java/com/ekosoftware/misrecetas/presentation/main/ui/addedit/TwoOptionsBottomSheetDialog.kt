package com.ekosoftware.misrecetas.presentation.main.ui.addedit

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import com.ekosoftware.misrecetas.databinding.BottomSheetDialogTwoOptionsBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class TwoOptionsBottomSheetDialog (
    private val dialogDetails: DialogDetails/*,
    private val bottomSheetListener: BottomSheetListener?*/
) :
    BottomSheetDialogFragment() {

    private var _binding: BottomSheetDialogTwoOptionsBinding? = null
    private val binding get() = _binding!!

    companion object {
        const val OPTION_1 = 1
        const val OPTION_2 = 2
    }

    private var bottomSheetListener: BottomSheetListener? = null

    fun setOnBottomSheetListener(bottomSheetListener: BottomSheetListener) {
        this.bottomSheetListener = bottomSheetListener
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = BottomSheetDialogTwoOptionsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setTitle()
        setButtonOne()
        setButtonTwo()
    }

    private fun setTitle() {
        if (dialogDetails.title.isNullOrEmpty()) binding.title.visibility = View.GONE
        else binding.title.text = dialogDetails.title
    }

    private fun setButtonOne() = binding.buttonOptionOne.apply {
        text = dialogDetails.optionOne
        setOnClickListener {
            this@TwoOptionsBottomSheetDialog.bottomSheetListener?.onOptionSelected(OPTION_1)
            dismiss()
        }
        dialogDetails.optionOneResId?.let { resId ->
            this@TwoOptionsBottomSheetDialog.context?.let { context -> ContextCompat.getDrawable(context, resId) }
        }.also { drawable -> icon = drawable }
    }

    private fun setButtonTwo() = binding.buttonOptionTwo.apply {
        text = dialogDetails.optionTwo
        setOnClickListener {
            this@TwoOptionsBottomSheetDialog.bottomSheetListener?.onOptionSelected(OPTION_2)
            dismiss()
        }
        dialogDetails.optionTwoResId?.let { resId ->
            this@TwoOptionsBottomSheetDialog.context?.let { context -> ContextCompat.getDrawable(context, resId) }
        }.also { drawable -> icon = drawable }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    interface BottomSheetListener {
        fun onOptionSelected(action: Int)
    }
}

data class DialogDetails(
    val title: String? = null,
    val optionOne: String,
    val optionOneResId: Int? = null,
    val optionTwo: String,
    val optionTwoResId: Int? = null
)