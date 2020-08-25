package com.ekosoftware.misrecetas.presentation.main.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.ekosoftware.misrecetas.R
import com.ekosoftware.misrecetas.databinding.FragmentHomeBinding
import com.ekosoftware.misrecetas.domain.model.Recipe
import com.ekosoftware.misrecetas.presentation.main.ui.viewmodel.MainViewModel
import com.ekosoftware.misrecetas.vo.Resource
import kotlinx.android.synthetic.main.fragment_home.*

class HomeFragment : Fragment(), RecipesRecyclerAdapter.Interaction {

    companion object {
        const val RC_SIGN_IN = 1
    }

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var recipesRecyclerAdapter: RecipesRecyclerAdapter

    private val viewModel by activityViewModels<MainViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpNavigation()
        setUpRecyclerView()

        fetchData()
    }

    private fun setUpNavigation() {
        val navController = findNavController()
        val appBarConfiguration = AppBarConfiguration(navController.graph)
        binding.toolbarAddEdit.setupWithNavController(navController, appBarConfiguration)
    }

    private fun setUpRecyclerView() = recycler_view_recipes_list.apply {
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

    private fun fetchData() {
        viewModel.fetchRecipes.observe(requireActivity(), { result ->
            when (result) {
                is Resource.Loading -> {
                    showProgress()
                }
                is Resource.Success -> {
                    hideProgress()
                    recipesRecyclerAdapter.submitList(result.data)
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
        //viewModel.selectRecipe(item)
        findNavController().navigate(action)
    }

    private fun showProgress() {
        if (progressBar != null) {
            progressBar.visibility = View.VISIBLE
        }
    }

    private fun hideProgress() {
        if (progressBar != null) {
            progressBar.visibility = View.GONE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // To avoid leaks
        _binding = null
    }
}