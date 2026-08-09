package com.sagrd.mentorly.domain.model.student

enum class StudentRole {
    STUDENT,
    ADMIN;

    companion object {
        fun fromApi(value: Int): StudentRole {
            return when (value) {
                1 -> STUDENT
                2 -> ADMIN
                else -> throw IllegalArgumentException(
                    "Rol de estudiante no reconocido: $value"
                )
            }
        }
    }
}