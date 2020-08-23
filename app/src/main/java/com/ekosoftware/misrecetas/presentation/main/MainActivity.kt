package com.ekosoftware.misrecetas.presentation.main

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.findNavController
import com.ekosoftware.misrecetas.R
import com.ekosoftware.misrecetas.data.network.RecipesDataSource
import com.ekosoftware.misrecetas.databinding.ActivityMainBinding
import com.ekosoftware.misrecetas.domain.network.RecipeRepoImpl
import com.ekosoftware.misrecetas.presentation.main.ui.viewmodel.*
import com.ekosoftware.misrecetas.util.FirebaseError
import com.ekosoftware.misrecetas.util.hideKeyboard
import com.google.android.material.snackbar.Snackbar

class MainActivity : AppCompatActivity() {

    private lateinit var navController: NavController
    private lateinit var binding: ActivityMainBinding

    private val viewModel: MainViewModel by viewModels {
        MainVMFactory(
            application,
            RecipeRepoImpl(RecipesDataSource())
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        navController = findNavController(R.id.nav_host_fragment)
        subscribeObservers()
    }

    override fun onSupportNavigateUp(): Boolean {
        hideKeyboard()
        return navController.navigateUp() || super.onSupportNavigateUp()
    }

    private fun subscribeObservers() {
        viewModel.eventResultReceiver.observe(this, { result ->
            when (result.result) {
                Result.LOADING -> {
                    val loadingMsg = if (result.event == Event.ADD || result.event == Event.UPDATE)
                        R.string.saving else R.string.deleting
                    getString(loadingMsg).publish(Snackbar.LENGTH_SHORT)
                }
                Result.SUCCESS -> {
                    getString(
                        R.string.success_msg_placeholder,
                        result.event.getString(),
                        result.recipe.name
                    ).publish(Snackbar.LENGTH_LONG)
                }
                Result.FAILURE -> {
                    if (result.failureMsg == FirebaseError.ERROR_MISSING_PERMISSIONS)
                        getString(
                            R.string.failure_msg_placeholder,
                            result.event.getString(),
                            result.recipe.name
                        ).publish(Snackbar.LENGTH_LONG)
                    else getString(
                        R.string.failure_msg_placeholder,
                        result.event.getString(),
                        result.recipe.name
                    ).publish(Snackbar.LENGTH_LONG, result)
                }
            }
        })
    }

    private fun String.publish(duration: Int, eventResult: EventResult? = null) {
        val snack = Snackbar.make(binding.parentLayout, this, duration)
        eventResult?.let {
            snack.setAction(getString(R.string.retry)) {
                viewModel.startNetworkOperation(eventResult.event, eventResult.recipe)
            }
        }
        snack.show()
    }

    private fun Event.getString() = when (this) {
        Event.ADD -> getString(R.string.add)
        Event.UPDATE -> getString(R.string.update)
        Event.DELETE -> getString(R.string.delete)
    }
}