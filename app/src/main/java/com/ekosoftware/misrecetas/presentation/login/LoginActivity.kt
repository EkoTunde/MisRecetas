package com.ekosoftware.misrecetas.presentation.login

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.findNavController
import com.ekosoftware.misrecetas.R
import com.ekosoftware.misrecetas.presentation.main.MainActivity
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        FirebaseAuth.getInstance().currentUser?.let {
            startActivity(Intent(this@LoginActivity, MainActivity::class.java))
        }

        navController = findNavController(R.id.nav_host_login_fragment)
    }
}