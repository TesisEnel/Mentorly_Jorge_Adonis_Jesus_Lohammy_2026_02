package com.sagrd.mentorly.domain.model.content

enum class ActivityType {
    EXERCISE,
    QUIZ;

    companion object {
        fun fromApi(value: Int): ActivityType {
            return when (value) {
                1 -> EXERCISE
                2 -> QUIZ
                else -> throw IllegalArgumentException(
                    "Tipo de actividad no reconocido: $value"
                )
            }
        }
    }
}