package com.ekosoftware.misrecetas.presentation.main.ui.cook

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.ekosoftware.misrecetas.R
import com.ekosoftware.misrecetas.databinding.FragmentInstructionBinding

class InstructionFragment : Fragment() {

    companion object {
        const val STEP_ARG = "step"
        const val INSTRUCTION_ARG = "instruction"
        const val LIST_SIZE = "list size"
    }

    private var _binding: FragmentInstructionBinding? = null
    private val binding get() = _binding!!

    private var step: Int = 0
    private lateinit var instruction: String
    private var listSize = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requireArguments().let {
            step = it.getInt(STEP_ARG)
            instruction = it.getString(INSTRUCTION_ARG, "")
            listSize = it.getInt(LIST_SIZE)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentInstructionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.textStepIndicator.apply {
            text = requireContext().getString(R.string.current_step_placeholder_extended, step)

            // Because step equals fragment's position + 1, first fragment is 1, so first fragment doesn't get left drawable ("<")
            val leftDrawable = if (step != 1) ContextCompat.getDrawable(requireContext(), R.drawable.ic_outline_keyboard_arrow_left_24) else null

            // Because step equals fragment's position + 1, last fragment equals list's size, so last fragment doesn't get right drawable (">")
            val rightDrawable = if (step != listSize) ContextCompat.getDrawable(requireContext(), R.drawable.ic_outline_keyboard_arrow_right_24) else null

            setCompoundDrawablesWithIntrinsicBounds(leftDrawable, null, rightDrawable, null)
        }

        binding.textInstruction.text = instruction
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}