package com.ekosoftware.misrecetas.presentation.main.ui.detail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import com.ekosoftware.misrecetas.R
import com.ekosoftware.misrecetas.databinding.FragmentRecipeDetailBinding
import com.ekosoftware.misrecetas.domain.model.Recipe
import com.ekosoftware.misrecetas.domain.model.User
import com.ekosoftware.misrecetas.presentation.main.ui.detail.adapters.IngredientsAdapter
import com.ekosoftware.misrecetas.presentation.main.ui.detail.adapters.InstructionsAdapter
import com.ekosoftware.misrecetas.presentation.main.ui.detail.adapters.MainContentAdapter
import com.ekosoftware.misrecetas.presentation.main.ui.detail.adapters.TitleAdapter

class RecipeDetailFragment : Fragment() {

    private var _binding: FragmentRecipeDetailBinding? = null
    private val binding get() = _binding!!

    private lateinit var recipe: Recipe
    private lateinit var currentUser: User

    private lateinit var concatAdapter: ConcatAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requireArguments().let {
            recipe = it.getParcelable("recipe")!!
            currentUser = it.getParcelable("user")!!
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentRecipeDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initRecyclerView()

        /*
        val layout = findViewById<CollapsingToolbarLayout>(R.id.collapsing_toolbar_layout)
    val toolbar = findViewById<Toolbar>(R.id.toolbar)
    val navController = findNavController(R.id.nav_host_fragment)
    val appBarConfiguration = AppBarConfiguration(navController.graph)
    layout.setupWithNavController(toolbar, navController, appBarConfiguration)
        * */
    }

    private fun initRecyclerView() {

        binding.recipeContentRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            concatAdapter = setUpAdapters()
            adapter = concatAdapter
        }
    }

    private fun setUpAdapters() : ConcatAdapter {
        val mainContentAdapter = MainContentAdapter(requireContext(), recipe)
        val ingredientsTitleAdapter = TitleAdapter(
            requireContext(), requireContext().getString(
                R.string.ingredients_title
            )
        )
        val ingredientsAdapter = IngredientsAdapter(requireContext(), recipe.ingredients!!)
        val instructionsTitleAdapter = TitleAdapter(
            requireContext(), requireContext().getString(
                R.string.instructions_title
            )
        )
        val instructionsAdapter = InstructionsAdapter(requireContext(), recipe.instructions!!)

        return ConcatAdapter(
            mainContentAdapter,
            ingredientsTitleAdapter,
            ingredientsAdapter,
            instructionsTitleAdapter,
            instructionsAdapter
        )
    }

    private fun editRecipe() {
        val action = RecipeDetailFragmentDirections.actionRecipeDetailFragmentToAddEditRecipeFragment()
        findNavController().navigate(action)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}