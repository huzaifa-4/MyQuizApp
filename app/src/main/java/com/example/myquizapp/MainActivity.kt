package com.example.myquizapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.myquizapp.databinding.ActivityMainBinding
import kotlinx.coroutines.*
import retrofit2.Response

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private var questions: List<Question> = emptyList()
    private var currentQuestionIndex = 0
    private val userAnswers = IntArray(10) { -1 }
    private var score = 0

    private val coroutineScope = CoroutineScope(Dispatchers.Main)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        fetchQuestionsFromApi()
        setupClickListeners()
    }

    private fun fetchQuestionsFromApi() {
        binding.progressBar.visibility = ProgressBar.VISIBLE
        binding.tvQuestion.text = "Loading questions..."

        coroutineScope.launch {
            try {
                val response: Response<ApiResponse> = withContext(Dispatchers.IO) {
                    RetrofitClient.apiService.getQuestions(amount = 5)
                }

                if (response.isSuccessful && response.body() != null) {
                    questions = QuestionUtils.processApiResponse(response.body()!!)
                    if (questions.isNotEmpty()) {
                        displayQuestion(currentQuestionIndex)
                        updateNavigationButtons()
                    } else {
                        showError("No questions available")
                        loadStaticQuestions()
                    }
                } else {
                    showError("Failed to load questions")
                    loadStaticQuestions()
                }
            } catch (e: Exception) {
                showError("Network error: ${e.message}")
                loadStaticQuestions()
            } finally {
                binding.progressBar.visibility = ProgressBar.GONE
            }
        }
    }

    private fun loadStaticQuestions() {
        questions = listOf(
            Question(
                "What is the capital of France?",
                listOf("London", "Berlin", "Paris", "Madrid"),
                2,
                "Geography",
                "https://picsum.photos/id/30/200/150"
            ),
            Question(
                "Which planet is known as the Red Planet?",
                listOf("Venus", "Mars", "Jupiter", "Saturn"),
                1,
                "Science",
                "https://picsum.photos/id/10/200/150"
            ),
            Question(
                "What is 2 + 2?",
                listOf("3", "4", "5", "6"),
                1,
                "Mathematics",
                "https://picsum.photos/id/40/200/150"
            ),
            Question(
                "Who painted the Mona Lisa?",
                listOf("Van Gogh", "Picasso", "Da Vinci", "Monet"),
                2,
                "Art",
                "https://picsum.photos/id/50/200/150"
            ),
            Question(
                "What is the largest ocean on Earth?",
                listOf("Atlantic", "Indian", "Arctic", "Pacific"),
                3,
                "Geography",
                "https://picsum.photos/id/30/200/150"
            )
        )
        displayQuestion(currentQuestionIndex)
        updateNavigationButtons()
    }

    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    private fun setupClickListeners() {
        binding.btnPrevious.setOnClickListener {
            if (currentQuestionIndex > 0) {
                saveCurrentAnswer()
                currentQuestionIndex--
                displayQuestion(currentQuestionIndex)
                updateNavigationButtons()
            }
        }

        binding.btnNext.setOnClickListener {
            if (currentQuestionIndex < questions.size - 1) {
                saveCurrentAnswer()
                currentQuestionIndex++
                displayQuestion(currentQuestionIndex)
                updateNavigationButtons()
            }
        }

        binding.btnSubmit.setOnClickListener {
            saveCurrentAnswer()
            calculateScore()
            navigateToResults()
        }

        binding.rgOptions.setOnCheckedChangeListener { _, checkedId ->
            val selectedOption = when (checkedId) {
                R.id.rbOption1 -> 0
                R.id.rbOption2 -> 1
                R.id.rbOption3 -> 2
                R.id.rbOption4 -> 3
                else -> -1
            }
            userAnswers[currentQuestionIndex] = selectedOption
        }
    }

    private fun displayQuestion(index: Int) {
        if (questions.isEmpty()) return

        val question = questions[index]

        // Update question counter
        binding.tvQuestionCounter.text = "Question ${index + 1}/${questions.size}"

        // Update question text
        binding.tvQuestion.text = question.question

        // Update image view
        if (!question.imageUrl.isNullOrEmpty()) {
            binding.ivQuestionImage.visibility = ImageView.VISIBLE
            Glide.with(this)
                .load(question.imageUrl)
                .placeholder(android.R.drawable.ic_menu_gallery)
                .error(android.R.drawable.ic_menu_gallery)
                .into(binding.ivQuestionImage)
        } else {
            binding.ivQuestionImage.visibility = ImageView.GONE
        }

        // Update radio buttons with options
        val radioButtons = listOf(
            binding.rbOption1,
            binding.rbOption2,
            binding.rbOption3,
            binding.rbOption4
        )

        question.options.forEachIndexed { i, option ->
            radioButtons[i].text = option
        }

        // Restore previous selection
        binding.rgOptions.clearCheck()
        if (userAnswers[index] != -1) {
            val radioButtonId = when (userAnswers[index]) {
                0 -> R.id.rbOption1
                1 -> R.id.rbOption2
                2 -> R.id.rbOption3
                3 -> R.id.rbOption4
                else -> -1
            }
            if (radioButtonId != -1) {
                binding.rgOptions.check(radioButtonId)
            }
        }
    }

    private fun saveCurrentAnswer() {
        // Current answer is automatically saved via radio group listener
    }

    private fun updateNavigationButtons() {
        binding.btnPrevious.isEnabled = currentQuestionIndex > 0

        if (currentQuestionIndex == questions.size - 1) {
            binding.btnNext.visibility = Button.GONE
            binding.btnSubmit.visibility = Button.VISIBLE
        } else {
            binding.btnNext.visibility = Button.VISIBLE
            binding.btnSubmit.visibility = Button.GONE
        }
    }

    private fun calculateScore() {
        score = 0
        questions.forEachIndexed { index, question ->
            if (userAnswers[index] == question.correctAnswer) {
                score++
            }
        }
    }

    private fun navigateToResults() {
        val intent = Intent(this, ResultActivity::class.java).apply {
            putExtra("SCORE", score)
            putExtra("TOTAL_QUESTIONS", questions.size)
        }
        startActivity(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        coroutineScope.cancel()
    }
}