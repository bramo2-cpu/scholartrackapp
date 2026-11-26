package com.scholartrackapp.util

import java.time.LocalDate
import java.time.temporal.ChronoUnit

fun parseDate(text: String): LocalDate? =
    runCatching { LocalDate.parse(text.trim()) }.getOrNull()

fun daysLeft(deadline: String): Long? =
    parseDate(deadline)?.let { ChronoUnit.DAYS.between(LocalDate.now(), it) }

fun deadlineLabel(deadline: String): String {
    val d = daysLeft(deadline) ?: return deadline.ifBlank { "No deadline" }
    return when {
        d < 0 -> "$deadline (passed)"
        d == 0L -> "$deadline (today!)"
        else -> "$deadline ($d days left)"
    }
}

