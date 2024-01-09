package io.zoemeow.dutschedule.utils

import android.annotation.SuppressLint
import com.github.marlonlom.utilities.timeago.TimeAgo
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import kotlin.time.DurationUnit
import kotlin.time.toDuration

class CustomDateUtil {
    companion object {
        /**
         * Get current day of week with Sunday as 1 to Saturday as 7.
         */
        fun getCurrentDayOfWeek(): Int {
            return Calendar.getInstance()[Calendar.DAY_OF_WEEK]
        }

        fun dayOfWeekInString(
            value: Int = 1,
            fullString: Boolean = false
        ): String {
            return if (fullString) {
                when (value) {
                    1 -> "Sunday"
                    2 -> "Monday"
                    3 -> "Tuesday"
                    4 -> "Wednesday"
                    5 -> "Thursday"
                    6 -> "Friday"
                    7 -> "Saturday"
                    else -> throw Exception("Invalid value: Must between 1 and 7!")
                }
            } else {
                when (value) {
                    1 -> "Sun"
                    2 -> "Mon"
                    3 -> "Tue"
                    4 -> "Wed"
                    5 -> "Thu"
                    6 -> "Fri"
                    7 -> "Sat"
                    else -> throw Exception("Invalid value: Must between 1 and 7!")
                }
            }
        }

        fun getCurrentDateAndTimeToString(format: String = "yyyy/MM/dd HH:mm:ss"): String {
            return SimpleDateFormat(format, Locale.getDefault()).format(Date())
        }

        fun unixToDuration(unix: Long = System.currentTimeMillis()): String {
            val unixDuration = (System.currentTimeMillis() - unix) / 1000
            val dateDuration: ArrayList<String> = ArrayList()

            val duration = (System.currentTimeMillis() - unix).toDuration(DurationUnit.MILLISECONDS)

            return when (duration.inWholeHours) {
                in 0..23 -> {
                    "Today"
                }
                in 24..47 -> {
                    "Yesterday"
                }
                else -> {
                    TimeAgo.using(unix)
                }
            }

            val date = unixDuration / 60 / 60 / 24
            val day = date % 365
            val month = date % 30
            val year = date / 365

            dateDuration.add("ago")
            if (day > 0) {
                dateDuration.add("$day day${if (day.toInt() != 1 ) "s" else ""}")
            }
            if (month > 0) {
                dateDuration.add("$month month${if (month.toInt() != 1) "s" else ""}")
            }
            if (year > 0) {
                dateDuration.add("$year year${if (year.toInt() != 1) "s" else ""}")
            }

            return if (day.toInt() == 0 && month.toInt() == 0 && year.toInt() == 0) {
                "Today"
            } else {
                dateDuration.reversed().joinToString(separator = " ")
            }

//            return when {
//                // Years
//                unixDuration / 60 / 60 / 24 / 365 > 0 ->
//                    "${unixDuration / 60 / 60 / 24 / 365} year${if (unixDuration / 60 / 60 / 24 / 365 > 1) "s" else ""} ago"
//                // Months
//                unixDuration / 60 / 60 / 24 / 30 > 0 ->
//                    "${unixDuration / 60 / 60 / 24 / 30} month${if (unixDuration / 60 / 60 / 24 / 30 > 1) "s" else ""} ago"
//                // Days
//                unixDuration / 60 / 60 / 24 > 0 ->
//                    "${unixDuration / 60 / 60 / 24} day${if (unixDuration / 60 / 60 / 24 > 1) "s" else ""} ago"
//                // Less than a day
//                else -> "Today"
//            }
        }

        @SuppressLint("SimpleDateFormat")
        fun dateUnixToString(
            date: Long,
            dateFormat: String = "dd/MM/yyyy",
            gmt: String = "UTC"
        ): String {
            // "dd/MM/yyyy"
            // "dd/MM/yyyy HH:mm"

            val simpleDateFormat = SimpleDateFormat(dateFormat)
            simpleDateFormat.timeZone = TimeZone.getTimeZone(gmt)
            return simpleDateFormat.format(Date(date))
        }
    }
}