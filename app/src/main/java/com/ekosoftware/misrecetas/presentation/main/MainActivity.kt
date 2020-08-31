package com.ekosoftware.misrecetas.presentation.main

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.findNavController
import com.ekosoftware.misrecetas.R
import com.ekosoftware.misrecetas.databinding.ActivityMainBinding
import com.ekosoftware.misrecetas.domain.constants.Event
import com.ekosoftware.misrecetas.domain.constants.FirebaseError
import com.ekosoftware.misrecetas.domain.constants.Result
import com.ekosoftware.misrecetas.domain.model.EventResult
import com.ekosoftware.misrecetas.presentation.login.LoginActivity
import com.ekosoftware.misrecetas.presentation.main.ui.home.HomeFragmentDirections
import com.ekosoftware.misrecetas.presentation.main.ui.viewmodel.MainViewModel
import com.ekosoftware.misrecetas.presentation.main.ui.viewmodel.UserViewModel
import com.ekosoftware.misrecetas.util.hideKeyboard
import com.ekosoftware.misrecetas.vo.Resource
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var navController: NavController
    private lateinit var binding: ActivityMainBinding

    private val mainViewModel: MainViewModel by viewModels()
    private val userViewModel: UserViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        userViewModel.setUser()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        navController = findNavController(R.id.nav_host_fragment)
        navController.addOnDestinationChangedListener { _, destination, _ ->
            if (destination.id == R.id.homeFragment) {
                binding.btnAddRecipe.visibility = View.VISIBLE
            } else {
                binding.btnAddRecipe.visibility = View.GONE
            }
        }
        binding.btnAddRecipe.setOnClickListener { addRecipe() }
        subscribeEventResultObserver()
        subscribeAuthOperationsObserver()
        subscribeProfileUpdateObserver()
    }

    private fun addRecipe() {
        val action = HomeFragmentDirections.actionHomeFragmentToAddEditRecipeFragment(null)
        navController.navigate(action)
    }

    override fun onSupportNavigateUp(): Boolean {
        hideKeyboard()
        return navController.navigateUp() || super.onSupportNavigateUp()
    }

    private fun subscribeEventResultObserver() = mainViewModel.eventResultReceiver.observe(this, { result ->
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

    private val TAG = "MainActivity"
    private fun subscribeAuthOperationsObserver() = userViewModel.authOperationLiveData.observe(this, { result ->
        when (result) {
            is Resource.Success -> {
                val intent = Intent(this, LoginActivity::class.java)
                getString(R.string.successfully_updated_profile)
                startActivity(intent)
                finish()
            }
            is Resource.Failure -> {
                Log.d(TAG, "subscribeAuthOperationsObserver: error -> ${result.exception}")
                getString(R.string.couldnt_complete_operation).publish(Snackbar.LENGTH_LONG)
            }
        }
    })

    private fun subscribeProfileUpdateObserver() =
        userViewModel.updateProfileLiveData.observe(this, { it.publish(Snackbar.LENGTH_SHORT) })

    private fun String.publish(duration: Int, eventResult: EventResult? = null) {
        val snack = Snackbar.make(binding.parentLayout, this, duration)
        eventResult?.let {
            snack.setAction(getString(R.string.retry)) {
                mainViewModel.startNetworkOperation(eventResult.event, eventResult.recipe)
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