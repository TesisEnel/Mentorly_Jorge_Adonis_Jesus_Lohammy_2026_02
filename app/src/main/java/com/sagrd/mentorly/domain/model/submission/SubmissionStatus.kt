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
                1 -> PENDING
                2 -> APPROVED
                3 -> REJECTED
                4 -> ESCALATED
                else -> UNKNOWN
            }
    }
}
