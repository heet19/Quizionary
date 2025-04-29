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
import com.example.quizapp.models.QuizResponse
import com.example.quizapp.models.ResultModel
import com.example.quizapp.retrofit.QuizService
import com.example.quizapp.utils.Utils
import com.facebook.shimmer.ShimmerFrameLayout
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class QuizActivity : AppCompatActivity() {

    private val binding: ActivityQuizBinding by lazy {
        ActivityQuizBinding.inflate(layoutInflater)
    }

    private lateinit var shimmerFrameLayout: ShimmerFrameLayout

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

        // Initialize the shimmer layout
        shimmerFrameLayout = binding.shimmerEffectFrameQuiz
        startShimmer()

        val sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        val isMusicEnabled = sharedPreferences.getBoolean(MUSIC_QUIZ_ONLY_KEY, true)

        if (isMusicEnabled) {
            mediaPlayer = MediaPlayer.create(this, R.raw.quizmusic).apply {
                isLooping = true
                start()
            }
        }

        val amount = intent.getIntExtra("amount", 10)
        val category = intent.getIntExtra("category", 0)
        val difficulty = intent.getStringExtra("difficulty")
        val type = intent.getStringExtra("type")

        fetchQuizQuestions(amount, category, difficulty, type)

//        questionList = intent.getSerializableExtra("questionList") as ArrayList<QuizQuestion>
//
//        binding.pbProgress.max = questionList.size
//        setQuestion()
//        setOptions()
//        startTimer()
//        binding.tvProgress.text = "1/${questionList.size}"
//

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

    private fun fetchQuizQuestions(amount: Int, category: Int, difficulty: String?, type: String?) {
         startShimmer()

            val retrofit = Retrofit.Builder()
                .baseUrl("https://opentdb.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build()

            val service = retrofit.create(QuizService::class.java)
            val call = service.getQuiz(amount, category, difficulty, type)

            call.enqueue(object : Callback<QuizResponse> {
                override fun onResponse(call: Call<QuizResponse>,response: Response<QuizResponse>) {
                    if (response.isSuccessful && response.body() != null) {
                        val quizResponse = response.body()!!
                        questionList = ArrayList(quizResponse.results)

                        if (questionList.isNotEmpty()) {
                            binding.pbProgress.max = questionList.size
                            setQuestion()
                            setOptions()
                            stopShimmer()
                            startTimer()
                            binding.tvProgress.text = "1/${questionList.size}"
                        } else {
                            Utils.showToast(this@QuizActivity, "No questions available for this selection")
                            finish()
                        }

                    } else {
                        Utils.showToast(this@QuizActivity, "Failed to fetch questions")
                        finish()
                    }
                }

                override fun onFailure(call: Call<QuizResponse>, t: Throwable) {
                    Utils.showToast(this@QuizActivity, "Error: ${t.message}")
                    finish()
                }
            })
    }

    private fun startShimmer() {
        shimmerFrameLayout.startShimmer()
        shimmerFrameLayout.visibility = View.VISIBLE
        binding.cardView2.visibility = View.GONE
        binding.constraintLayout2.visibility = View.GONE
        binding.clCircularProgressBar.visibility = View.GONE
        binding.constraintLayout2.visibility = View.GONE
        binding.linearLayout.visibility = View.GONE
        binding.btnNext.visibility = View.GONE
    }

    private fun stopShimmer() {
        shimmerFrameLayout.stopShimmer()
        shimmerFrameLayout.visibility = View.GONE
        binding.cardView2.visibility = View.VISIBLE
        binding.constraintLayout2.visibility = View.VISIBLE
        binding.clCircularProgressBar.visibility = View.VISIBLE
        binding.linearLayout.visibility = View.VISIBLE
        binding.btnNext.visibility = View.VISIBLE
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

//    private fun startTimer() {
//        binding.circularProgressBar.max = 20
//        binding.circularProgressBar.progress = 20
//
//        timer = object : CountDownTimer(20000, 1000) {
//            override fun onTick(remaining: Long) {
//                binding.circularProgressBar.incrementProgressBy(-1)
//                binding.tvTimer.text = (remaining/1000).toString()
//                timeLeft = (remaining/1000).toInt()
//            }
//            override fun onFinish() {
//                showCorrectAnswer()
//                allowPlaying = false
//            }
//        }.start()
//    }

    private fun startTimer() {
        val totalTime = 20000L // 20 seconds in milliseconds
        val interval = 16L // ~60 FPS, smooth animation

        binding.circularProgressBar.max = totalTime.toInt()
        binding.circularProgressBar.progress = totalTime.toInt()

        timer?.cancel()
        timer = object : CountDownTimer(totalTime, interval) {
            override fun onTick(remaining: Long) {
                binding.circularProgressBar.progress = remaining.toInt()

                val secondsLeft = (remaining / 1000).toInt()
                binding.tvTimer.text = secondsLeft.toString()
                timeLeft = secondsLeft

                updateProgressBarColor(remaining)
            }

            override fun onFinish() {
                binding.circularProgressBar.progress = 0
                binding.tvTimer.text = "0"
                allowPlaying = false
                showCorrectAnswer()
            }
        }.start()
    }

    private fun updateProgressBarColor(remaining: Long) {
        val fraction = remaining / 20000f

        val color = when {
            fraction > 0.5f -> blendColors(0xFF47FF33.toInt(), 0xFF00BFFF.toInt(), (1f - fraction) * 2f) // Green to Yellow
            fraction > 0.25f -> blendColors(0xFF00BFFF.toInt(), 0xFFFFFF00.toInt(), (0.5f - fraction) * 4f) // Yellow to Orange
            else -> blendColors(0xFFFFFF00.toInt(), 0xFFFF3729.toInt(), (0.25f - fraction) * 4f) // Orange to Red
        }

        binding.circularProgressBar.progressDrawable.setTint(color)
    }

    private fun blendColors(from: Int, to: Int, ratio: Float): Int {
        val inverseRatio = 1f - ratio
        val r = ((from shr 16 and 0xFF) * inverseRatio + (to shr 16 and 0xFF) * ratio).toInt()
        val g = ((from shr 8 and 0xFF) * inverseRatio + (to shr 8 and 0xFF) * ratio).toInt()
        val b = ((from and 0xFF) * inverseRatio + (to and 0xFF) * ratio).toInt()
        return 0xFF shl 24 or (r shl 16) or (g shl 8) or b
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