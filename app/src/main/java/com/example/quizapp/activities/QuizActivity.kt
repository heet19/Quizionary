package com.example.quizapp.activities

import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import android.view.View.OnClickListener
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.quizapp.R
import com.example.quizapp.constants.Constants
import com.example.quizapp.databinding.ActivityQuizBinding
import com.example.quizapp.models.QuizQuestion
import com.example.quizapp.models.ResultModel

class QuizActivity : AppCompatActivity() {

    private val binding: ActivityQuizBinding by lazy {
        ActivityQuizBinding.inflate(layoutInflater)
    }

    private var mediaPlayer: MediaPlayer? = null
    private val PREFS_NAME = "QuizPrefs"
    private val MUSIC_QUIZ_ONLY_KEY = "music_quiz_only"

    private lateinit var questionList:ArrayList<QuizQuestion>
    private var position = 0
    private var timer:CountDownTimer? = null
    private var score = 0.0
    private var timeLeft = 0
    private var allowPlaying = true

    private var resultList = ArrayList<ResultModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)

        val sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        val isMusicEnabled = sharedPreferences.getBoolean(MUSIC_QUIZ_ONLY_KEY, true)

        if (isMusicEnabled) {
            mediaPlayer = MediaPlayer.create(this, R.raw.quizmusic).apply {
                isLooping = true
                start()
            }
        }

        questionList = intent.getSerializableExtra("questionList") as ArrayList<QuizQuestion>

        binding.pbProgress.max = questionList.size
        setQuestion()
        setOptions()
        startTimer()
        binding.tvProgress.text = "1/${questionList.size}"

        binding.btnNext.setOnClickListener {
            onNext()
        }

        var redBg = ContextCompat.getDrawable(this, R.drawable.red_button_bg)
        val optionClickListener = OnClickListener { view->
            if (allowPlaying) {
                timer?.cancel()
                view.background = redBg
                showCorrectAnswer()
                setScore(view as Button?)
                allowPlaying = false
            }
        }

        binding.option1.setOnClickListener(optionClickListener)
        binding.option2.setOnClickListener(optionClickListener)
        binding.option3.setOnClickListener(optionClickListener)
        binding.option4.setOnClickListener(optionClickListener)

    }

    private fun setQuestion() {
        val decodedQuestion = Constants.decodeHtmlString(questionList[position].question)
        binding.tvQuestion.text = decodedQuestion
        binding.tvQuestionNo.text = "Question no.${position + 1}"
    }

    private lateinit var correctAnswer:String
    private lateinit var optionList: List<String>
    private fun setOptions() {
        val question = questionList[position]
        val temp = Constants.getRandomOptions(question.correct_answer, question.incorrect_answers)
        optionList = temp.second
        correctAnswer = temp.first

        binding.option1.text = "A. ${optionList[0]}"
        binding.option2.text = "B. ${optionList[1]}"

        if (question.type == "multiple") {
            binding.option3.visibility = View.VISIBLE
            binding.option4.visibility = View.VISIBLE

            binding.option3.text = "C. ${optionList[2]}"
            binding.option4.text = "D. ${optionList[3]}"

        } else {
            binding.option3.visibility = View.GONE
            binding.option4.visibility = View.GONE
        }
    }

    private fun onNext() {
        if (allowPlaying) {
            Toast.makeText(this, "Please select an option.", Toast.LENGTH_SHORT).show()
            return
        }

        val resultModel = ResultModel(
            20-timeLeft,
            questionList[position].type,
            questionList[position].difficulty,
            score)

        resultList.add(resultModel)
        score = 0.0

        if (position<questionList.size-1) {
            timer?.cancel()
            position++
            setQuestion()
            setOptions()
            binding.pbProgress.progress = position+1
            binding.tvProgress.text = "${position+1}/${questionList.size}"
            resetButtonBackground()
            allowPlaying = true
            startTimer()
        } else {
            endGame()
        }
    }

    private fun startTimer() {
        binding.circularProgressBar.max = 20
        binding.circularProgressBar.progress = 20

        timer = object : CountDownTimer(20000, 1000) {
            override fun onTick(remaining: Long) {
                binding.circularProgressBar.incrementProgressBy(-1)
                binding.tvTimer.text = (remaining/1000).toString()
                timeLeft = (remaining/1000).toInt()
            }
            override fun onFinish() {
                showCorrectAnswer()
                allowPlaying = false
            }
        }.start()
    }

    private fun setScore(button: Button?) {
        val selectedAnswer = button?.text.toString().substringAfter(". ").trim()
        if (correctAnswer == selectedAnswer) {
            score = getScore()
        }
    }

    private fun getScore():Double {
        val score1 = when(questionList[position].type) {
            "boolean" -> 1.0
            else -> 1.0
        }
//        val score1 = when(questionList[position].type) {
//            "boolean" -> 0.5
//            else -> 1.0
//        }

//        val score2: Double = (timeLeft.toDouble())/(20).toDouble()

//        val score3 = when(questionList[position].difficulty) {
//            "easy" -> 1.0
//            "medium" -> 2.0
//            else -> 3.0
//        }
        return score1
    }

    private fun showCorrectAnswer() {
        val blueBg = ContextCompat.getDrawable(this, R.drawable.green_button_bg)
        when(true){
            (correctAnswer == optionList[0]) -> binding.option1.background = blueBg
            (correctAnswer == optionList[1]) -> binding.option2.background = blueBg
            (correctAnswer == optionList[2]) -> binding.option3.background = blueBg
            else -> binding.option4.background = blueBg
        }
    }

    private fun resetButtonBackground() {
        val grayBg = ContextCompat.getDrawable(this, R.drawable.gray_button_bg)
        binding.option1.background = grayBg
        binding.option2.background = grayBg
        binding.option3.background = grayBg
        binding.option4.background = grayBg
    }

    private fun endGame() {
        val intent = Intent(this, ResultActivity::class.java)
        intent.putExtra("resultList", resultList)
        startActivity(intent)
        finish()
    }

    override fun onPause() {
        super.onPause()
        mediaPlayer?.pause()
    }

    override fun onResume() {
        super.onResume()
        val isMusicEnabled = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
            .getBoolean(MUSIC_QUIZ_ONLY_KEY, true)
        if (isMusicEnabled) {
            mediaPlayer?.start()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.release()
        mediaPlayer = null
    }

}