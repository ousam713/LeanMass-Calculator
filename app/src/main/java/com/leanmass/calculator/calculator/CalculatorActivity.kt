package com.leanmass.calculator.calculator

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.leanmass.calculator.database.DatabaseHelper
import com.leanmass.calculator.databinding.ActivityCalculatorBinding
import com.leanmass.calculator.history.HistoryActivity

class CalculatorActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCalculatorBinding
    private lateinit var dbHelper: DatabaseHelper
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ViewBinding
        binding = ActivityCalculatorBinding.inflate(layoutInflater)
        setContentView(binding.root)

        dbHelper = DatabaseHelper(this)
        sharedPreferences = getSharedPreferences("LeanMassPrefs", MODE_PRIVATE)

        binding.btnCalculate.setOnClickListener {
            calculate()
        }

        binding.btnHistory.setOnClickListener {
            startActivity(Intent(this, HistoryActivity::class.java))
        }
    }

    private fun calculate() {
        val weightStr = binding.etWeight.text.toString().trim()
        val heightStr = binding.etHeight.text.toString().trim()

        if (weightStr.isEmpty() || heightStr.isEmpty()) {
            Toast.makeText(this, "Remplis tous les champs", Toast.LENGTH_SHORT).show()
            return
        }

        val weight = weightStr.toDoubleOrNull()
        val height = heightStr.toDoubleOrNull()

        if (weight == null || height == null || weight <= 0 || height <= 0) {
            Toast.makeText(this, "Valeurs invalides", Toast.LENGTH_SHORT).show()
            return
        }

        val isMale = binding.rgGender.checkedRadioButtonId == binding.rbMale.id
        val gender = if (isMale) "Homme" else "Femme"

        // Formule de Boer
        val lbm = if (isMale) {
            (0.407 * weight) + (0.267 * height) - 19.2
        } else {
            (0.252 * weight) + (0.473 * height) - 48.3
        }

        // Norme
        val isSatisfying = if (isMale) {
            lbm >= DatabaseHelper.LBM_MIN_MALE
        } else {
            lbm >= DatabaseHelper.LBM_MIN_FEMALE
        }

        // Affichage résultat
        binding.layoutResult.visibility = View.VISIBLE
        binding.tvLbmResult.text = "LBM : %.2f kg".format(lbm)

        if (isSatisfying) {
            binding.tvResultIcon.text = "😊"
            binding.tvResultLabel.text = "Résultat satisfaisant"
            binding.tvResultLabel.setTextColor(getColor(android.R.color.holo_green_dark))
        } else {
            binding.tvResultIcon.text = "😟"
            binding.tvResultLabel.text = "Résultat à surveiller"
            binding.tvResultLabel.setTextColor(getColor(android.R.color.holo_red_dark))
        }

        // Sauvegarde dans SQLite
        val userId = sharedPreferences.getInt("userId", -1)
        if (userId != -1) {
            dbHelper.saveCalculation(userId, weight, height, gender, lbm)
        }
    }
}