package com.sagrd.mentorly.domain.model.content

enum class ApprovalStrategy {
    AUTO,
    PEER_REVIEW,
    ADMIN;

    companion object {
        fun fromApi(value: Int): ApprovalStrategy {
            return when (value) {
                1 -> AUTO
                2 -> PEER_REVIEW
                3 -> ADMIN
                else -> throw IllegalArgumentException(
                    "Estrategia de aprobación no reconocida: $value"
                )
            }
        }
    }
}