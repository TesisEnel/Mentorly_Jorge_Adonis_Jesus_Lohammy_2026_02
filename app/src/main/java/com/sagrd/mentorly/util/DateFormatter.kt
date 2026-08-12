package com.sagrd.mentorly.util

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

object DateFormatter {

    private val locale = Locale.forLanguageTag("es-DO")
    private val dateFormatter = DateTimeFormatter.ofPattern(
        "d 'de' MMMM 'de' yyyy",
        locale
    )
    private val dateTimeFormatter = DateTimeFormatter.ofPattern(
        "d 'de' MMMM 'de' yyyy, HH:mm",
        locale
    )

    fun format(value: String): String {
        val dateValue = value.trim()
        if (dateValue.isBlank()) return dateValue

        return runCatching {
            if ('T' !in dateValue) {
                LocalDate.parse(dateValue).format(dateFormatter)
            } else {
                parseDateTime(dateValue).format(dateTimeFormatter)
            }
        }.getOrElse { value }
    }

    private fun parseDateTime(value: String): LocalDateTime {
        return runCatching { OffsetDateTime.parse(value).toLocalDateTime() }
            .getOrElse { LocalDateTime.parse(value) }
    }
}
