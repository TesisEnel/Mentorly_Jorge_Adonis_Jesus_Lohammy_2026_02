package com.sagrd.mentorly.util

fun formatDate(dateStr: String?): String {
    if (dateStr.isNullOrBlank()) return ""

    val cleanDate = dateStr.substringBefore("T").trim()
    val parts = cleanDate.split("-")
    if (parts.size != 3) return dateStr

    val months = listOf(
        "enero", "febrero", "marzo", "abril", "mayo", "junio",
        "julio", "agosto", "septiembre", "octubre", "noviembre", "diciembre"
    )

    val days = parts[2].toIntOrNull() ?: return dateStr
    val monthIndex = (parts[1].toIntOrNull() ?: return dateStr) - 1
    val year = parts[0]

    return if (monthIndex in months.indices) {
        "$days de ${months[monthIndex]} de $year"
    } else {
        "$days/${parts[1]}/$year"
    }
}
