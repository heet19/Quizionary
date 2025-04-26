package com.example.quizapp.activities

import android.os.Bundle
import android.os.Looper
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.quizapp.R
import com.example.quizapp.adapter.QuizSummeryAdapter
import com.example.quizapp.databinding.ActivityResultBinding
import com.example.quizapp.models.ResultModel

class ResultActivity : AppCompatActivity() {

    private val binding: ActivityResultBinding by lazy {
        ActivityResultBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)

        val resultList:ArrayList<ResultModel> = intent.getSerializableExtra("resultList") as ArrayList<ResultModel>

        binding.rvSummery.layoutManager = LinearLayoutManager(this)
        val adapter = QuizSummeryAdapter(resultList)
        binding.rvSummery.adapter = adapter

        binding.tvTotalScore.text = "Your Score: ${getFinalScore(resultList)}"

        val percentage = getPercentage(getFinalScore(resultList), resultList.size)
        val percentageDouble = percentage.toDoubleOrNull() ?: 0.0

        val animation = when {
            percentageDouble >= 90 -> R.raw.activity_anim
            percentageDouble >= 70 -> R.raw.activity_anim
            percentageDouble >= 30 -> R.raw.passed_text
            else -> R.raw.failed_text
        }

        binding.lavResult1.setAnimation(animation)
        binding.lavResult1.playAnimation()

        // Stop and hide the animation after 3 seconds
        android.os.Handler(Looper.getMainLooper()).postDelayed({
            if (binding.lavResult1.isAnimating) {
                // Stop the animation
                binding.lavResult1.cancelAnimation()
            }
            // Hide the view after 3 seconds
            binding.lavResult1.visibility = View.GONE
        }, 3000)

        binding.btnHome.setOnClickListener {
            finish()
        }

    }

    private fun getPercentage(score: Double, totalQuestions: Int): String {
        val percent = (score / totalQuestions) * 100
        return String.format("%.2f", percent)
    }

    private fun getFinalScore(list:ArrayList<ResultModel>):Double{
        var result = 0.0
        for (i in list)
            result+= i.score

        return String.format("%.2f",result).toDouble()
    }
}