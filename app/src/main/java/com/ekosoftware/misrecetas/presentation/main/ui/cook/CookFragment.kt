package com.ekosoftware.misrecetas.presentation.main.ui.cook

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ekosoftware.misrecetas.R
import com.ekosoftware.misrecetas.databinding.FragmentCookBinding
import com.ekosoftware.misrecetas.domain.model.Ingredient
import com.ekosoftware.misrecetas.domain.model.Recipe
import com.ekosoftware.misrecetas.presentation.main.ui.cook.adapters.BottomSheetIngredientsRecyclerAdapter
import com.ekosoftware.misrecetas.presentation.main.ui.cook.adapters.BottomSheetIngredientsTitleAdapter
import com.ekosoftware.misrecetas.presentation.main.ui.cook.adapters.CookViewPager2Adapter
import com.google.android.material.bottomsheet.BottomSheetBehavior

class CookFragment : Fragment() {

    companion object {
        const val RECIPE_ARG = "recipe"
    }

    private var _binding: FragmentCookBinding? = null
    private val binding get() = _binding!!

    private lateinit var recipe: Recipe

    private lateinit var concatAdapter: ConcatAdapter
    private lateinit var ingredientsAdapter: BottomSheetIngredientsRecyclerAdapter
    private lateinit var titleAdapter: BottomSheetIngredientsTitleAdapter
    private lateinit var pager2Adapter: CookViewPager2Adapter

    private lateinit var bottomSheet: RecyclerView
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<RecyclerView>

    private lateinit var ingredients: MutableList<Ingredient>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            recipe = it.getParcelable(RECIPE_ARG)!!
            ingredients = recipe.ingredients?.mapIndexed { index, name ->
                Ingredient(index, name, false)
            }?.toMutableList() ?: mutableListOf()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentCookBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initToolbar()
        initViewPager2()
        initRecyclerView()
        initBottomSheetListener()
    }

    private fun initToolbar() {
        return binding.toolbar.setupWithNavController(
            findNavController(),
            AppBarConfiguration(findNavController().graph)
        )
    }

    private fun initViewPager2() = binding.pager.apply {
        pager2Adapter = CookViewPager2Adapter(this@CookFragment, recipe.instructions!!)
        adapter = pager2Adapter
    }

    private fun initRecyclerView() = binding.bottomSheetRvIngredients.apply {
        layoutManager = LinearLayoutManager(this@CookFragment.requireContext())
        titleAdapter = BottomSheetIngredientsTitleAdapter(
            requireContext(),
            requireContext().getString(R.string.ingredients),
            onStateImagePressedListener
        )
        ingredientsAdapter = BottomSheetIngredientsRecyclerAdapter(requireContext(), ingredientsCheckedListener)
        concatAdapter = ConcatAdapter(titleAdapter, ingredientsAdapter)
        adapter = concatAdapter
        ingredientsAdapter.submitList(ingredients)
        ingredientsAdapter.notifyDataSetChanged()
    }

    private fun initBottomSheetListener() {
        bottomSheet = binding.bottomSheetRvIngredients
        bottomSheetBehavior = BottomSheetBehavior.from(binding.bottomSheetRvIngredients)
        bottomSheetBehavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                newState.takeIf { it == BottomSheetBehavior.STATE_COLLAPSED || it == BottomSheetBehavior.STATE_EXPANDED }?.let {
                    titleAdapter.setStateIndicator(newState)
                }
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) = Unit
        })
    }

    private val ingredientsCheckedListener = object : BottomSheetIngredientsRecyclerAdapter.OnIngredientCheckedListener {
        override fun onChecked(position: Int, isChecked: Boolean) {
            ingredients[position].isChecked = isChecked
            ingredientsAdapter.submitList(ingredients)
        }
    }

    private val onStateImagePressedListener = object : BottomSheetIngredientsTitleAdapter.OnStateImagePressedListener {
        override fun onPressed() {
            if (bottomSheetBehavior.state == BottomSheetBehavior.STATE_EXPANDED) {
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
                return
            }
            if (bottomSheetBehavior.state == BottomSheetBehavior.STATE_COLLAPSED) bottomSheetBehavior.state =
                BottomSheetBehavior.STATE_EXPANDED
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

}