package com.ekosoftware.misrecetas.ui.details

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import com.ekosoftware.misrecetas.R
import com.ekosoftware.misrecetas.databinding.FragmentRecipeDetailsBinding
import com.ekosoftware.misrecetas.domain.constants.Event
import com.ekosoftware.misrecetas.domain.model.Recipe
import com.ekosoftware.misrecetas.ui.details.adapters.IngredientsAdapter
import com.ekosoftware.misrecetas.ui.details.adapters.InstructionsAdapter
import com.ekosoftware.misrecetas.ui.details.adapters.MainContentAdapter
import com.ekosoftware.misrecetas.ui.details.adapters.TitleAdapter
import com.ekosoftware.misrecetas.ui.viewmodel.MainViewModel
import com.ekosoftware.misrecetas.util.GlideApp
import com.google.firebase.storage.FirebaseStorage

class RecipeDetailsFragment : Fragment() {

    private var _binding: FragmentRecipeDetailsBinding? = null
    private val binding get() = _binding!!

    private lateinit var concatAdapter: ConcatAdapter
    private lateinit var mainContentAdapter: MainContentAdapter
    private lateinit var ingredientsAdapter: IngredientsAdapter
    private lateinit var instructionsAdapter: InstructionsAdapter

    private lateinit var currentRecipe: Recipe

    private val mainViewModel by activityViewModels<MainViewModel>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentRecipeDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initToolbar()
        initRecyclerView()
        mainViewModel.detailRecipe.observe(viewLifecycleOwner, recipeObserver())
    }

    private fun initToolbar() {
        val navController = findNavController()
        val appBarConfiguration = AppBarConfiguration(navController.graph)
        binding.collapsingToolbarLayout.setupWithNavController(binding.toolbar, navController, appBarConfiguration)
    }

    private fun recipeObserver(): Observer<Recipe?> = Observer { recipe ->
        currentRecipe = recipe!!
        binding.toolbar.title = recipe.name
        binding.collapsingToolbarLayout.title = recipe.name
        binding.headerImage.setImage(recipe)
        mainContentAdapter.submitRecipe(recipe)
        recipe.ingredients?.let { ingredientsAdapter.submitList(recipe.ingredients!!) }
        recipe.instructions?.let { instructionsAdapter.submitList(recipe.instructions!!) }
        setMenuItemsInToolbar()
        binding.btnEditRecipe.setOnClickListener { editRecipe() }
    }

    private fun setMenuItemsInToolbar() = binding.toolbar.setOnMenuItemClickListener {
        when (it.itemId) {
         /*   R.id.menu_item_edit -> {
                editRecipe()
                true
            }*/
            R.id.menu_item_delete -> {
                mainViewModel.startNetworkOperation(Event.DELETE, currentRecipe)
                findNavController().navigateUp()
                true
            }
            else -> false
        }
    }

    private fun initRecyclerView() = binding.recipeContentRecyclerView.apply {
        layoutManager = LinearLayoutManager(requireContext())
        concatAdapter = setUpAdapters()
        adapter = concatAdapter
    }

    private fun setUpAdapters(): ConcatAdapter {
        mainContentAdapter = MainContentAdapter(requireContext())
        ingredientsAdapter = IngredientsAdapter(requireContext())
        instructionsAdapter = InstructionsAdapter(requireContext())

        return ConcatAdapter(
            mainContentAdapter,
            TitleAdapter(requireContext(), requireContext().getString(R.string.ingredients_title)),
            ingredientsAdapter,
            TitleAdapter(requireContext(), requireContext().getString(R.string.instructions_title)),
            instructionsAdapter
        )
    }

    private fun ImageView.setImage(item: Recipe) {
        item.imageUrl?.let {
            GlideApp.with(requireContext()).load(it).centerCrop().into(this)
            return
        }
        item.imageUUID?.let {
            val image = FirebaseStorage.getInstance().reference.child("${item.imageUUID}")
            GlideApp.with(requireContext()).load(image).centerCrop().into(this)
            return
        }
    }

    private fun editRecipe() {
        //mainViewModel.selectRecipe(currentRecipe)
        val action = RecipeDetailsFragmentDirections.actionRecipeDetailFragmentToAddEditRecipeFragment(currentRecipe)
        findNavController().navigate(action)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}