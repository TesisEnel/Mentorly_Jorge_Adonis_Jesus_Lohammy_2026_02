package com.sagrd.mentorly.domain.model.submission

enum class EvidenceType(val apiValue: Int) {
    URL(1),
    TEXT(2);

    companion object {
        fun fromApi(value: Int): EvidenceType = entries.firstOrNull {
            it.apiValue == value
        } ?: URL
    }
}
