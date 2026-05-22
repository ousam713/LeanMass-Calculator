package com.leanmass.calculator.auth

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.leanmass.calculator.calculator.CalculatorActivity
import com.leanmass.calculator.database.DatabaseHelper
import com.leanmass.calculator.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var dbHelper: DatabaseHelper
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ViewBinding
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        dbHelper = DatabaseHelper(this)
        sharedPreferences = getSharedPreferences("LeanMassPrefs", MODE_PRIVATE)

        // Si déjà connecté → aller directement au calculateur
        if (sharedPreferences.getInt("userId", -1) != -1) {
            goToCalculator()
            return
        }

        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Remplis tous les champs", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val userId = dbHelper.loginUser(email, password)
            if (userId != -1) {
                // Sauvegarde userId en session
                sharedPreferences.edit().putInt("userId", userId).apply()
                goToCalculator()
            } else {
                Toast.makeText(this, "Email ou mot de passe incorrect", Toast.LENGTH_SHORT).show()
            }
        }

        binding.tvRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    private fun goToCalculator() {
        startActivity(Intent(this, CalculatorActivity::class.java))
        finish()
    }
}