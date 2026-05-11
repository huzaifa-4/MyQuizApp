package com.example.myquizapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.myquizapp.databinding.ActivityResultBinding

class ResultActivity : AppCompatActivity() {

    private lateinit var binding: ActivityResultBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityResultBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Get data from intent
        val score = intent.getIntExtra("SCORE", 0)
        val totalQuestions = intent.getIntExtra("TOTAL_QUESTIONS", 0)

        displayResults(score, totalQuestions)
        setupRestartButton()
    }

    private fun displayResults(score: Int, totalQuestions: Int) {
        val percentage = if (totalQuestions > 0) {
            (score.toFloat() / totalQuestions) * 100
        } else {
            0f
        }

        // Update score text using string resource with parameters
        binding.tvScore.text = getString(R.string.your_score, score, totalQuestions)

        // Update percentage using string resource with parameter
        binding.tvPercentage.text = getString(R.string.score_percentage, percentage.toInt())

        // ✅ KEEP YOUR EMOJI PRAISE SYSTEM EXACTLY AS YOU LIKE IT!
        val performanceMessage = when {
            percentage >= 80 -> "Excellent! 🎉"
            percentage >= 60 -> "Good job! 👍"
            percentage >= 40 -> "Not bad! 👏"
            else -> "Keep practicing! 💪"
        }
        binding.tvPerformance.text = performanceMessage

        // Show API info using string resource
        binding.tvApiInfo.text = getString(R.string.api_info)
    }

    private fun setupRestartButton() {
        binding.btnRestart.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish() // Close result activity
        }
    }
}