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