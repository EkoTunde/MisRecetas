package com.ekosoftware.misrecetas.presentation.main.ui.cook

import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.ekosoftware.misrecetas.presentation.main.ui.cook.InstructionFragment.Companion.INSTRUCTION_ARG
import com.ekosoftware.misrecetas.presentation.main.ui.cook.InstructionFragment.Companion.STEP_ARG

class CookViewPager2Adapter(fragmentActivity: CookFragment, private val instructions: List<String>) : FragmentStateAdapter(fragmentActivity) {

    override fun getItemCount(): Int = instructions.size

    override fun createFragment(position: Int): Fragment {
        val fragment = InstructionFragment()
        fragment.arguments = bundleOf(STEP_ARG to position + 1,
            INSTRUCTION_ARG to instructions[position])
        return fragment
    }
}