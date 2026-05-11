package com.example.myquizapp

import com.google.gson.annotations.SerializedName

data class ApiResponse(
    @SerializedName("response_code") val responseCode: Int,
    val results: List<QuestionResult>
)

data class QuestionResult(
    val category: String,
    val type: String,
    val difficulty: String,
    val question: String,
    @SerializedName("correct_answer") val correctAnswer: String,
    @SerializedName("incorrect_answers") val incorrectAnswers: List<String>
)

// Our app's Question data class (updated to include image URL)
data class Question(
    val question: String,
    val options: List<String>,
    val correctAnswer: Int,
    val category: String = "",
    val imageUrl: String? = null  // For bonus: image support
)