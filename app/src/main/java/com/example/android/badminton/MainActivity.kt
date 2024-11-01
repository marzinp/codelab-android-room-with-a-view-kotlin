package com.example.android.badminton

import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController

class MainActivity : AppCompatActivity() {
    private var passwordEntered = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Restore the password state and set admin status accordingly
        passwordEntered = savedInstanceState?.getBoolean("PASSWORD_ENTERED", false) ?: false
        UserSession.setAdminStatus(passwordEntered) // Update UserSession based on saved status

        if (!passwordEntered) {
            showPasswordDialog()
        }

        // Set up Navigation
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController
        val appBarConfiguration = AppBarConfiguration(navController.graph)
        setupActionBarWithNavController(navController, appBarConfiguration)
    }

    private fun showPasswordDialog() {
        val passwordInput = EditText(this).apply {
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        }

        AlertDialog.Builder(this)
            .setTitle("Admin Password")
            .setMessage("Enter the admin password to unlock full features")
            .setView(passwordInput)
            .setPositiveButton("OK") { _, _ ->
                val enteredPassword = passwordInput.text.toString()
                passwordEntered = enteredPassword == "mayavi" // Set based on entered password
                UserSession.setAdminStatus(passwordEntered)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    // Save the password state to avoid re-triggering on configuration change
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean("PASSWORD_ENTERED", passwordEntered)
    }
}
