package com.example.quizapp.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.example.quizapp.R
import com.example.quizapp.adapter.GridAdapter
import com.example.quizapp.constants.Constants
import com.example.quizapp.constants.QuizClass
import com.example.quizapp.databinding.ActivityMainBinding
import com.example.quizapp.models.Category
import com.facebook.shimmer.ShimmerFrameLayout
import com.google.android.material.appbar.MaterialToolbar

class MainActivity : AppCompatActivity() {

    private val binding: ActivityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    private lateinit var shimmerFrameLayout: ShimmerFrameLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)

        // Initialize the shimmer layout
        shimmerFrameLayout = binding.shimmerEffectFrameMain
        startShimmer()

        val sharedPreferences = getSharedPreferences("QuizPrefs", MODE_PRIVATE)
        val isMusicGlobal = sharedPreferences.getBoolean("music_global", false)

        if (isMusicGlobal) {
            val musicIntent = Intent(this, com.example.quizapp.music.MusicService::class.java)
            musicIntent.putExtra("ACTION", "PLAY")
            startService(musicIntent)
        }

        // Reference the MaterialToolbar inside AppBarLayout
        val toolbar: MaterialToolbar = findViewById(R.id.toolbarDetail)
        setSupportActionBar(toolbar) // Set up the MaterialToolbar as the app bar

        val quizClass = QuizClass(this)

        val rvCategoryList = binding.rvCategoryList
        rvCategoryList?.layoutManager = GridLayoutManager(this,2)

        quizClass.getQuestionStatsList(object : QuizClass.QuestionStatCallback {
            override fun onQuestionStatFetched(map: Map<String, Category>) {
                val adapter = GridAdapter(Constants.getCategoryItemList(),map)
                rvCategoryList?.adapter = adapter

                adapter.setOnTouchResponse(object :GridAdapter.OnTouchResponse{
                    override fun onClick(id: Int) {
//                        quizClass.getQuizList(10,id,null,null)
                        val intent = Intent(this@MainActivity, QuizActivity::class.java)
                        intent.putExtra("amount", 10)
                        intent.putExtra("category", id)
                        intent.putExtra("difficulty", null as String?)
                        intent.putExtra("type", null as String?)
                        startActivity(intent)
                    }
                })
                stopShimmer()
            }
        })

        binding.btnRandomQuiz.setOnClickListener {
//            quizClass.getQuizList(10, null, null, null)
            val intent = Intent(this, QuizActivity::class.java)
            intent.putExtra("amount", 10)
            intent.putExtra("category", null as Int?)
            intent.putExtra("difficulty", null as String?)
            intent.putExtra("type", null as String?)
            startActivity(intent)
        }

        binding.btnCustomQuiz.setOnClickListener {
            startActivity(Intent(this, CustomQuizActivity::class.java))
        }
    }

    private fun startShimmer() {
        shimmerFrameLayout.startShimmer()
        shimmerFrameLayout.visibility = View.VISIBLE
        binding.rvCategoryList.visibility = View.GONE
        binding.linearLayoutButton.visibility = View.GONE
    }

    private fun stopShimmer() {
        shimmerFrameLayout.stopShimmer()
        shimmerFrameLayout.visibility = View.GONE
        binding.rvCategoryList.visibility = View.VISIBLE
        binding.linearLayoutButton.visibility = View.VISIBLE
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        Log.d("onOptionsItemSelected", "Selected Item: ${item.title}")
        return when (item.itemId) {
            R.id.btnRulesShow -> {
                startActivity(Intent(this@MainActivity, RulesActivity::class.java))
                true
            }
            R.id.setting -> {
                startActivity(Intent(this@MainActivity, SettingActivity::class.java))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}