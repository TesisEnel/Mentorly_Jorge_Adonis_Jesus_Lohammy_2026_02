package com.sagrd.mentorly.domain.model.submission

enum class SubmissionStatus {
    PENDING,
    APPROVED,
    REJECTED,
    ESCALATED,
    UNKNOWN;

    companion object {
        fun fromApi(value: Int): SubmissionStatus =
            when (value) {
                0 -> PENDING
                1 -> APPROVED
                2 -> REJECTED
                3 -> ESCALATED
                else -> UNKNOWN
            }
    }
}