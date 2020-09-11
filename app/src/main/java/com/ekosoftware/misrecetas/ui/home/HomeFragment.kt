package com.ekosoftware.misrecetas.ui.home

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
import com.ekosoftware.misrecetas.ui.viewmodel.MainViewModel
import com.ekosoftware.misrecetas.ui.viewmodel.UserViewModel
import com.ekosoftware.misrecetas.util.hide
import com.ekosoftware.misrecetas.util.hideKeyboard
import com.ekosoftware.misrecetas.util.show
import com.ekosoftware.misrecetas.vo.Resource
import kotlinx.android.synthetic.main.fragment_home.*

class HomeFragment : Fragment(), RecipesRecyclerAdapter.Interaction {

    companion object {
        const val RC_SIGN_IN = 1
    }

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var recipesRecyclerAdapter: RecipesRecyclerAdapter

    private val mainViewModel by activityViewModels<MainViewModel>()
    private val userViewModel by activityViewModels<UserViewModel>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        hideKeyboard()

        if (isUserLoggedIn()) { // If user is logged in, "normal flow is launched"
            setUpNavigation()
            setUpRecyclerView()
            hideSplashScreen()
            fetchRecipesData()
        } else { // If not, go to login
            val action = HomeFragmentDirections.actionHomeFragmentToLoginFragment()
            findNavController().navigate(action)
        }
    }

    private fun isUserLoggedIn() = userViewModel.isUserLoggedIn()

    private fun setUpNavigation() {
        val navController = findNavController()
        val appBarConfiguration = AppBarConfiguration(navController.graph)
        binding.toolbarHome.setupWithNavController(navController, appBarConfiguration)
        binding.toolbarHome.setOnMenuItemClickListener {
            println("ItemID is ${it.itemId}, while item account id is -> ${R.id.menu_item_account}")
            when (it.itemId) {
                R.id.menu_item_account -> {
                    val action = HomeFragmentDirections.actionHomeFragmentToProfileFragment()
                    navController.navigate(action)
                    true
                }
                else -> false
            }
        }
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

    private fun hideSplashScreen() = binding.apply {
        arrayOf(toolbarHome, recyclerViewRecipesList, progressBar).forEach { it.visibility = View.VISIBLE }
        splashImage.visibility = View.GONE
    }

    private fun fetchRecipesData() {
        mainViewModel.fetchRecipes.observe(requireActivity(), { result ->
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
        mainViewModel.showRecipeDetails(item)
        val action = HomeFragmentDirections.actionHomeFragmentToRecipeDetailFragment(item.name!!)
        findNavController().navigate(action)
    }

    private fun showProgress() = progressBar?.let {
        /*if (progressBar != null) {*/
            progressBar.show()/*visibility = View.VISIBLE*/
        /*}*/
    }

    private fun hideProgress() = progressBar?.let {
            progressBar.hide()/*visibility = View.GONE*/
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // To avoid leaks
        _binding = null
    }
}