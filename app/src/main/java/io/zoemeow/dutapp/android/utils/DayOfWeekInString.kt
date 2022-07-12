package io.zoemeow.dutapp.android.utils

fun DayOfWeekInString(dayOfWeek: Int): String {
    return when (dayOfWeek) {
        0 -> "Sunday"
        1 -> "Monday"
        2 -> "Tuesday"
        3 -> "Wednesday"
        4 -> "Thursday"
        5 -> "Friday"
        6 -> "Saturday"
        else -> ""
    }
}