package com.ekosoftware.misrecetas.presentation.main.ui.detail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.ekosoftware.misrecetas.R
import com.ekosoftware.misrecetas.databinding.FragmentRecipeDetailBinding
import com.ekosoftware.misrecetas.domain.model.Recipe
import com.ekosoftware.misrecetas.presentation.main.ui.detail.adapters.IngredientsAdapter
import com.ekosoftware.misrecetas.presentation.main.ui.detail.adapters.InstructionsAdapter
import com.ekosoftware.misrecetas.presentation.main.ui.detail.adapters.MainContentAdapter
import com.ekosoftware.misrecetas.presentation.main.ui.detail.adapters.TitleAdapter
import com.ekosoftware.misrecetas.presentation.main.ui.viewmodel.MainViewModel
import com.google.firebase.storage.FirebaseStorage

class RecipeDetailFragment : Fragment() {

    private var _binding: FragmentRecipeDetailBinding? = null
    private val binding get() = _binding!!

    private lateinit var recipe: Recipe

    private lateinit var concatAdapter: ConcatAdapter

    private val mainViewModel by activityViewModels<MainViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requireArguments().let {
            recipe = it.getParcelable("recipe")!!
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentRecipeDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        /*val appBarConfiguration = AppBarConfiguration(findNavController().graph)
        binding.toolbar.setupWithNavController(findNavController(), appBarConfiguration)*/

        val navController = findNavController()
        val appBarConfiguration = AppBarConfiguration(navController.graph)
        binding.collapsingToolbarLayout.setupWithNavController(binding.toolbar, navController, appBarConfiguration)
        binding.headerImage.setImage(recipe)
        binding.btnEditRecipe.setOnClickListener { editRecipe() }
        initRecyclerView()
    }

    private fun initRecyclerView() {

        binding.recipeContentRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            concatAdapter = setUpAdapters()
            adapter = concatAdapter
        }
    }

    private fun setUpAdapters(): ConcatAdapter {
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

    private fun ImageView.setImage(item: Recipe) {
        item.imageUrl?.let {
            /*GlideApp*/Glide.with(requireContext()).load(it).centerCrop().into(this)
            return
        }
        item.imageUUID?.let {
            val image = FirebaseStorage.getInstance().reference.child("${item.imageUUID}")
            /*GlideApp*/Glide.with(requireContext()).load(image).centerCrop().into(this)
            return
        }
    }

    private fun editRecipe() {
        mainViewModel.selectRecipe(recipe)
        val action = RecipeDetailFragmentDirections.actionRecipeDetailFragmentToAddEditRecipeFragment()
        findNavController().navigate(action)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}