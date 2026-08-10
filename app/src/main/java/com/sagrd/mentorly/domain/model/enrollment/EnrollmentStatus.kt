package com.sagrd.mentorly.domain.model.enrollment

enum class EnrollmentStatus {
    ACTIVE,
    COMPLETED,
    EXPIRED;

    companion object {
        fun fromApi(value: Int): EnrollmentStatus {
            return when (value) {
                1 -> ACTIVE
                2 -> COMPLETED
                3 -> EXPIRED
                else -> throw IllegalArgumentException(
                    "Estado de inscripción no reconocido: $value"
                )
            }
        }
    }
}
