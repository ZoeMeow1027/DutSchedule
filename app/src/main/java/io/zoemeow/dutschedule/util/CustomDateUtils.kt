package io.zoemeow.dutschedule.util

import android.annotation.SuppressLint
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class CustomDateUtils {
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
                    else -> throw Exception("Invalid value: Must between 0 and 6!")
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
                    else -> throw Exception("Invalid value: Must between 0 and 6!")
                }
            }
        }

        fun getCurrentDateAndTimeToString(format: String = "yyyy-MM-dd HH:mm:ss"): String {
            return SimpleDateFormat(format, Locale.getDefault()).format(Date())
        }

        fun unixToDuration(
            unix: Long = System.currentTimeMillis()
        ): String {
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

        @SuppressLint("SimpleDateFormat")
        fun dateToString(
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