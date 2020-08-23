package com.ekosoftware.misrecetas.presentation.main.ui.addedit

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.ekosoftware.misrecetas.databinding.BottomSheetDialogPictureOptionsBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class ImageOptionsBottomSheetDialog(private val bottomSheetListener: BottomSheetListener?) : BottomSheetDialogFragment() {

    private var _binding: BottomSheetDialogPictureOptionsBinding? = null
    private val binding get() = _binding!!

    companion object {
        const val DELETE = 1
        const val EDIT = 2
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = BottomSheetDialogPictureOptionsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.buttonAddEditImage.setOnClickListener {
            bottomSheetListener?.onOptionSelected(EDIT)
            dismiss()
        }
        binding.buttonDeleteImage.setOnClickListener {
            bottomSheetListener?.onOptionSelected(DELETE)
            dismiss()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    interface BottomSheetListener {
        fun onOptionSelected(action: Int)
    }
}