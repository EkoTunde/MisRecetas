package com.ekosoftware.misrecetas.presentation.main.ui.cook

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.ekosoftware.misrecetas.R
import com.ekosoftware.misrecetas.databinding.FragmentInstructionBinding

class InstructionFragment : Fragment() {

    companion object {
        const val STEP_ARG = "step"
        const val INSTRUCTION_ARG = "ingredient"
    }

    private var _binding: FragmentInstructionBinding? = null
    private val binding get() = _binding!!

    private var step: Int = 0
    private lateinit var instruction: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            step = it.getInt(STEP_ARG)
            instruction = it.getString(INSTRUCTION_ARG, "")
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentInstructionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.textStepIndicator.text = requireContext().getString(R.string.current_step_placeholder_extended, step)
        binding.textInstruction.text = instruction
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}