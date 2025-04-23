package com.example.quizapp.activities

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.quizapp.R
import com.example.quizapp.adapter.QuizSummeryAdapter
import com.example.quizapp.databinding.ActivityQuizBinding
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

        binding.tvTotalScore.text = "Your Score: ${getFinalScore(resultList).toString()}"

        binding.btnHome.setOnClickListener {
            finish()
        }

    }

    private fun getFinalScore(list:ArrayList<ResultModel>):Double{
        var result = 0.0
        for (i in list)
            result+= i.score

        return String.format("%.2f",result).toDouble()
    }
}