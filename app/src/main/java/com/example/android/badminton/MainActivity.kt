package com.example.android.badminton

import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import com.example.android.badminton.data.PlayerRoomDatabase

class MainActivity : AppCompatActivity() {
    private var passwordEntered = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        // Restore the password state on orientation change
        passwordEntered = savedInstanceState?.getBoolean("PASSWORD_ENTERED", false) ?: false

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

        val dialog = AlertDialog.Builder(this)
            .setTitle("Admin Password")
            .setMessage("Enter the admin password to unlock full features")
            .setView(passwordInput)  // Directly set the EditText here
            .setPositiveButton("OK") { _, _ ->
                val enteredPassword = passwordInput.text.toString()
                UserSession.setAdminStatus(enteredPassword == "123") // Use the real password condition
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}
