package com.ekosoftware.misrecetas.presentation.main.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.observe
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.ekosoftware.misrecetas.R
import com.ekosoftware.misrecetas.base.BaseFragment
import com.ekosoftware.misrecetas.domain.model.Recipe
import com.ekosoftware.misrecetas.data.network.RecipesDataSource
import com.ekosoftware.misrecetas.databinding.FragmentHomeBinding
import com.ekosoftware.misrecetas.domain.network.RecipeRepoImpl
import com.ekosoftware.misrecetas.presentation.main.ui.viewmodel.MainVMFactory
import com.ekosoftware.misrecetas.presentation.main.ui.viewmodel.MainViewModel
import com.ekosoftware.misrecetas.vo.Resource

class HomeFragment : BaseFragment(), RecipesRecyclerAdapter.Interaction {

    private lateinit var recipesRecyclerAdapter: RecipesRecyclerAdapter

    override val rootLayout: ViewGroup? get() = binding.root
    override val mainLayouts: List<View> get() = listOf(binding.progressBar, binding.rvRecipes)
    override val progressBar: View get() = binding.progressBar

    companion object {
        const val RC_SIGN_IN = 1
    }

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val viewModel by activityViewModels<MainViewModel> {
        MainVMFactory(RecipeRepoImpl(RecipesDataSource()))
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setUpNavigation()
        setUpRecyclerView()

        binding.btnAddRecipe.setOnClickListener {
            val action = HomeFragmentDirections.actionHomeFragmentToAddEditRecipeFragment(
                null,
                getString(R.string.add_recipe)
            )
            findNavController().navigate(action)
        }

        fetchData()
    }

    private fun setUpNavigation() {
        val navController = findNavController()
        val appBarConfiguration = AppBarConfiguration(navController.graph)
        binding.toolbar.toolbarAddEdit.setupWithNavController(navController, appBarConfiguration)
    }

    private fun setUpRecyclerView() {
        binding.rvRecipes.apply {
            layoutManager = LinearLayoutManager(requireContext())
            addItemDecoration(
                DividerItemDecoration(
                    requireContext(),
                    DividerItemDecoration.VERTICAL
                )
            )
            recipesRecyclerAdapter = RecipesRecyclerAdapter(this@HomeFragment)
            adapter = recipesRecyclerAdapter
        }
    }

    private fun fetchData() {
        viewModel.fetchRecipes.observe(requireActivity(), Observer { result ->
            when (result) {
                is Resource.Loading -> {
                    showProgress()
                }
                is Resource.Success -> {
                    recipesRecyclerAdapter.submitList(result.data)
                    hideProgress()
                }
                is Resource.Failure -> {
                    hideProgress()
                }
            }
        })
    }

    override fun onRecipeSelected(item: Recipe) {
        val action =
            HomeFragmentDirections.actionHomeFragmentToRecipeDetailFragment(item, item.name!!)
        findNavController().navigate(action)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // To avoid leaks
        _binding = null
    }
}