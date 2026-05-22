package com.leanmass.calculator.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.leanmass.calculator.R
import com.leanmass.calculator.database.DatabaseHelper
import com.leanmass.calculator.databinding.ActivityRegisterBinding

class RegisterActivity : AppCompatActivity() {

    // Sans ViewBinding (pour montrer les 2 variantes )
    private lateinit var dbHelper: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        dbHelper = DatabaseHelper(this)

        val etName = findViewById<android.widget.EditText>(R.id.etName)
        val etEmail = findViewById<android.widget.EditText>(R.id.etEmail)
        val etPassword = findViewById<android.widget.EditText>(R.id.etPassword)
        val btnRegister = findViewById<android.widget.Button>(R.id.btnRegister)
        val tvLogin = findViewById<android.widget.TextView>(R.id.tvLogin)

        btnRegister.setOnClickListener {
            val name = etName.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Remplis tous les champs", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password.length < 6) {
                Toast.makeText(this, "Mot de passe trop court (min 6 caractères)", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val success = dbHelper.registerUser(name, email, password)
            if (success) {
                Toast.makeText(this, "Compte créé ! Connecte-toi", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            } else {
                Toast.makeText(this, "Email déjà utilisé", Toast.LENGTH_SHORT).show()
            }
        }

        tvLogin.setOnClickListener {
            finish()
        }
    }
}