package com.sagrd.mentorly.domain.model.submission

enum class SubmissionStatus {
    PENDING,
    APPROVED,
    REJECTED,
    ESCALATED;

    companion object {
        fun fromApi(value: Int): SubmissionStatus {
            return when (value) {
                1 -> PENDING
                2 -> APPROVED
                3 -> REJECTED
                4 -> ESCALATED
                else -> throw IllegalArgumentException(
                    "Estado de entrega no reconocido: $value"
                )
            }
        }
    }
}
