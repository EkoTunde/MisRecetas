package com.ekosoftware.misrecetas.presentation.main.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.ekosoftware.misrecetas.base.BaseFragment
import com.ekosoftware.misrecetas.data.model.Recipe
import com.ekosoftware.misrecetas.data.network.RecipesDataSource
import com.ekosoftware.misrecetas.data.network.UsersDataSource
import com.ekosoftware.misrecetas.databinding.FragmentHomeBinding
import com.ekosoftware.misrecetas.domain.network.RecipeRepoImpl
import com.ekosoftware.misrecetas.domain.network.UserRepoImpl
import com.ekosoftware.misrecetas.vo.Resource
import com.ekosoftware.misrecetas.vo.VMFactory

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

    private val viewModel by viewModels<HomeViewModel> {
        VMFactory(
            UserRepoImpl(UsersDataSource()),
            RecipeRepoImpl(RecipesDataSource())
        )
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

        setUpRecyclerView()

        Toast.makeText(requireContext(), "Ya estamo' acá", Toast.LENGTH_SHORT).show()

    }

    private fun setUpRecyclerView() {
        binding.rvRecipes.apply {
            layoutManager = LinearLayoutManager(requireContext())
            recipesRecyclerAdapter = RecipesRecyclerAdapter(this@HomeFragment)
            adapter = recipesRecyclerAdapter
        }
    }

    // Asynchronous call for checking if user is signed in (takes ms)
    private fun checkUserIsLoggedIn() {

    }

    private fun fetchData() {
        viewModel.recipes.observe(this, Observer { result ->
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
        val action = HomeFragmentDirections.actionHomeFragmentToRecipeDetailFragment(item)
        findNavController().navigate(action)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // To avoid leaks
        _binding = null
    }
}