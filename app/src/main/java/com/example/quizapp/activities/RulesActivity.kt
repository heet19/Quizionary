package com.example.quizapp.activities

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.quizapp.databinding.ActivityRulesBinding

class RulesActivity : AppCompatActivity() {

    private val binding: ActivityRulesBinding by lazy {
        ActivityRulesBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)

        binding.btnRuleBackMain.setOnClickListener {
            finish()
        }

    }
}