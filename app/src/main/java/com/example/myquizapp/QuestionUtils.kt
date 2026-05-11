package com.example.myquizapp

import android.text.Html
import java.util.*

object QuestionUtils {

    // Decode HTML entities from API response
    fun decodeHtml(text: String): String {
        return Html.fromHtml(text, Html.FROM_HTML_MODE_LEGACY).toString()
    }

    // Process API response and convert to our Question format
    fun processApiResponse(apiResponse: ApiResponse): List<Question> {
        return apiResponse.results.map { result ->
            val decodedQuestion = decodeHtml(result.question)
            val decodedCorrectAnswer = decodeHtml(result.correctAnswer)
            val decodedIncorrectAnswers = result.incorrectAnswers.map { decodeHtml(it) }

            // Combine and shuffle options
            val allOptions = (decodedIncorrectAnswers + decodedCorrectAnswer).shuffled()
            val correctAnswerIndex = allOptions.indexOf(decodedCorrectAnswer)

            // Get image URL based on category (placeholder images from picsum.photos)
            val imageUrl = getImageUrlForCategory(result.category)

            Question(
                question = decodedQuestion,
                options = allOptions,
                correctAnswer = correctAnswerIndex,
                category = result.category,
                imageUrl = imageUrl
            )
        }
    }

    // Get placeholder images based on category
    private fun getImageUrlForCategory(category: String): String {
        val imageId = when {
            category.contains("Science", ignoreCase = true) -> 10
            category.contains("History", ignoreCase = true) -> 20
            category.contains("Geography", ignoreCase = true) -> 30
            category.contains("Mathematics", ignoreCase = true) -> 40
            category.contains("Art", ignoreCase = true) -> 50
            else -> Random().nextInt(100)
        }
        return "https://picsum.photos/id/$imageId/200/150"
    }
}