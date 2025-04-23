package com.example.quizapp.activities

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.quizapp.databinding.ActivitySettingBinding
import com.example.quizapp.music.MusicService

class SettingActivity : AppCompatActivity() {

    private val binding: ActivitySettingBinding by lazy {
        ActivitySettingBinding.inflate(layoutInflater)
    }

    private val PREFS_NAME = "QuizPrefs"
    private val MUSIC_QUIZ_ONLY_KEY = "music_quiz_only"
    private val MUSIC_GLOBAL_KEY = "music_global"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)

        binding.btnSettingBackMain.setOnClickListener {
            finish()
        }

        val sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)

        binding.musicPlay.isChecked = sharedPreferences.getBoolean(MUSIC_QUIZ_ONLY_KEY, true)
        binding.musicAllTime.isChecked = sharedPreferences.getBoolean(MUSIC_GLOBAL_KEY, false)

        // "Only while Playing" switch
        binding.musicPlay.setOnCheckedChangeListener { _, isChecked ->
            // Optionally disable the global music switch if this one is on
            if (isChecked) binding.musicAllTime.isChecked = false
            sharedPreferences.edit().putBoolean(MUSIC_QUIZ_ONLY_KEY, isChecked).apply()
        }

        // "All the time" switch
        binding.musicAllTime.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) binding.musicPlay.isChecked = false
            sharedPreferences.edit().putBoolean(MUSIC_GLOBAL_KEY, isChecked).apply()

            val musicIntent = Intent(this, MusicService::class.java)
            musicIntent.putExtra("ACTION", if (isChecked) "PLAY" else "PAUSE")
            startService(musicIntent)
        }

        binding.ruleLayout.setOnClickListener {
            startActivity(Intent(this, RulesActivity::class.java))
        }

        binding.shareLayout.setOnClickListener {
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_SUBJECT, "Check out this awesome app!")
                putExtra(Intent.EXTRA_TEXT, "https://play.google.com/store/apps/details?id=com.yourpackage.name")
            }
            startActivity(Intent.createChooser(shareIntent, "Share App via"))
        }

        binding.ratingLayout.setOnClickListener {
            val appPackageName = "com.example.quizapp" // Or hardcode: "com.yourpackage.name"
            try {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$appPackageName"))
                intent.addFlags(
                    Intent.FLAG_ACTIVITY_NO_HISTORY or
                            Intent.FLAG_ACTIVITY_NEW_DOCUMENT or
                            Intent.FLAG_ACTIVITY_MULTIPLE_TASK
                )
                startActivity(intent)
            } catch (e: ActivityNotFoundException) {
                // Play Store app not found, open in browser
                val webIntent = Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://play.google.com/store/apps/details?id=$appPackageName")
                )
                startActivity(webIntent)
            }
        }


    }
}