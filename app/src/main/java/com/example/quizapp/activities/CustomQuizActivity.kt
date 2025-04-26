package com.example.quizapp.activities

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.ArrayAdapter
import android.widget.SeekBar
import android.widget.SpinnerAdapter
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.quizapp.constants.Constants
import com.example.quizapp.databinding.ActivityCustomQuizBinding

class CustomQuizActivity : AppCompatActivity() {

    private val binding: ActivityCustomQuizBinding by lazy {
        ActivityCustomQuizBinding.inflate(layoutInflater)
    }

    private var amount = 10
    private var category:Int? = null
    private var difficulty:String? = null
    private var type:String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)
        handleSeekbar()
        val categoryList = Constants.getCategoryStringArray()
        binding.categorySpinner.adapter = getSpinnerAdapter(categoryList)
        binding.difficultySpinner.adapter = getSpinnerAdapter(Constants.difficultyList)
        binding.typeSpinner.adapter = getSpinnerAdapter(Constants.typeList)
        handleCategorySpinner()
        handleDifficultySpinner()
        handleTypeSpinner()

        binding.btnCustomBack.setOnClickListener {
            onBackPressed()
        }

        binding.startCustomQuiz.setOnClickListener {
//            QuizClass(this).getQuizList(amount, category, difficulty, type)
            val intent = Intent(this, QuizActivity::class.java)
            intent.putExtra("amount", amount)
            intent.putExtra("category", category)
            intent.putExtra("difficulty", difficulty)
            intent.putExtra("type", type)
            startActivity(intent)
        }
    }

    private fun handleSeekbar(){
        binding.seekBarAmount.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                amount = progress
                val text = "Amount: $amount"
                binding.tvAmount.text = text
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }

    private fun handleCategorySpinner(){
        binding.categorySpinner.onItemSelectedListener = object : OnItemSelectedListener{
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                category = if (position==0){
                    null
                } else {
                    position + 8
                }
                (view as? TextView)?.setTextColor(Color.BLACK)
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {
                //  Nothing to do Here
            }
        }
    }

    private fun handleDifficultySpinner() {
        binding.difficultySpinner.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?,view: View?,position: Int,id: Long) {
                difficulty = when(position){
                    0 -> null
                    1 -> "easy"
                    2 -> "medium"
                    else -> "hard"
                }
                (view as? TextView)?.setTextColor(Color.BLACK)
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {
                //  Nothing to do Here
            }
        }
    }

    private fun handleTypeSpinner(){
        binding.typeSpinner.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?,view: View?,position: Int,id: Long) {
                type = when(position){
                    0 -> null
                    1 -> "multiple"
                    else -> "boolean"
                }
                (view as? TextView)?.setTextColor(Color.BLACK)
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {
                //  Nothing to do Here
            }
        }
    }

    private fun getSpinnerAdapter(list: List<String>):SpinnerAdapter {
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item,list)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        return adapter
    }

}