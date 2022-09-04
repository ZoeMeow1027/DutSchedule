package io.zoemeow.dutnotify.util

import android.annotation.SuppressLint
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import java.text.SimpleDateFormat
import java.util.*

@SuppressLint("SimpleDateFormat")
fun dateToString(date: Long, dateFormat: String, gmt: String = "UTC"): String {
    // "dd/MM/yyyy"
    // "dd/MM/yyyy HH:mm"

    val simpleDateFormat = SimpleDateFormat(dateFormat)
    simpleDateFormat.timeZone = TimeZone.getTimeZone(gmt)
    return simpleDateFormat.format(Date(date))
}

fun getDayOfWeekToString(dayOfWeek: Int): String {
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

fun calculateDayAgoFromNews(unix: Long): String {
    val unixDuration = (System.currentTimeMillis() - unix) / 1000

    return when {
        // Years
        unixDuration / 60 / 60 / 24 / 365 > 0 ->
            "${unixDuration / 60 / 60 / 24 / 365} year${if (unixDuration / 60 / 60 / 24 / 365 > 1) "s" else ""} ago"
        // Months
        unixDuration / 60 / 60 / 24 / 30 > 0 ->
            "${unixDuration / 60 / 60 / 24 / 30} month${if (unixDuration / 60 / 60 / 24 / 30 > 1) "s" else ""} ago"
        // Days
        unixDuration / 60 / 60 / 24 > 0 ->
            "${unixDuration / 60 / 60 / 24} day${if (unixDuration / 60 / 60 / 24 > 1) "s" else ""} ago"
        // Less than a day
        else -> "Today"
    }
}

fun getCurrentDayOfWeek(): Int {
    var calendar = Calendar.getInstance()[Calendar.DAY_OF_WEEK]
    if (calendar - 1 > 0) calendar -= 1 else calendar = 7
    return calendar
}

fun getDateFromCurrentWeek(weekAdjust: Int = 0): ArrayList<kotlinx.datetime.LocalDate> {
    val arrayList: ArrayList<kotlinx.datetime.LocalDate> = arrayListOf()

    val calendar = Calendar.getInstance()
    // day-of-week: Sunday to saturday: 1 -> 7
    // Adjust to Monday is 1 and Saturday is 7
    val currentDayOfWeek = if (calendar[Calendar.DAY_OF_WEEK] - 1 > 0)
        calendar[Calendar.DAY_OF_WEEK] - 1 else 7

    val currentDate = kotlinx.datetime.LocalDate(
        calendar[Calendar.YEAR],
        calendar[Calendar.MONTH],
        calendar[Calendar.DAY_OF_MONTH],
    ).plus(weekAdjust, DateTimeUnit.WEEK)

    // Get date before currentDate.
    var tempBefore = currentDayOfWeek
    var minusCount = 0
    while (tempBefore - 1 > 0) {
        tempBefore -= 1

        minusCount += 1
        arrayList.add(currentDate.minus(minusCount, DateTimeUnit.DAY))
    }

    // Add current date
    arrayList.add(currentDate)

    // Get date after currentDate.
    var tempAfter = currentDayOfWeek
    var plusCount = 0
    while (tempAfter + 1 < 8) {
        tempAfter += 1

        plusCount += 1
        arrayList.add(currentDate.plus(plusCount, DateTimeUnit.DAY))
    }

    arrayList.sortWith(compareBy({ it.month }, { it.dayOfMonth }))

    return arrayList
}

fun dayOfWeekInString(value: Int = 0): String {
    return when (value) {
        1 -> "Mon"
        2 -> "Tue"
        3 -> "Wed"
        4 -> "Thu"
        5 -> "Fri"
        6 -> "Sat"
        7 -> "Sun"
        else -> throw Exception("Invalid value: Must between 1 and 7!")
    }
}