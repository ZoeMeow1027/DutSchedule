package io.zoemeow.dutnotify.utils

import android.Manifest
import android.annotation.SuppressLint
import android.app.WallpaperManager
import android.content.Context
import android.graphics.drawable.Drawable
import android.os.Build
import io.zoemeow.dutapi.Utils
import io.zoemeow.dutapi.objects.dutschoolyear.DUTSchoolYearItem
import io.zoemeow.dutnotify.PermissionRequestActivity
import io.zoemeow.dutnotify.model.enums.BackgroundImageType
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class DUTDateUtils {
    companion object {
        fun getDUTSchoolYear(
            unix: Long = System.currentTimeMillis()
        ): DUTSchoolYearItem {
            return Utils.getDUTSchoolYear(unix)
        }

        fun getDUTWeek(
            unix: Long = System.currentTimeMillis()
        ): Int {
            return ((unix - getDUTSchoolYear(unix).start) / 1000 / 60 / 60 / 24 / 7 + 1).toInt()
        }

        /**
         * Get current day of week with Sunday as 0 to Saturday as 7.
         */
        fun getCurrentDayOfWeek(): Int {
            return Calendar.getInstance()[Calendar.DAY_OF_WEEK]
        }

        fun dayOfWeekToString(
            value: Int = 0,
            fullString: Boolean = false
        ): String {
            return if (fullString) {
                when (value) {
                    0 -> "Sunday"
                    1 -> "Monday"
                    2 -> "Tuesday"
                    3 -> "Wednesday"
                    4 -> "Thursday"
                    5 -> "Friday"
                    6 -> "Saturday"
                    else -> throw Exception("Invalid value: Must between 0 and 6!")
                }
            } else {
                when (value) {
                    0 -> "Sun"
                    1 -> "Mon"
                    2 -> "Tue"
                    3 -> "Wed"
                    4 -> "Thu"
                    5 -> "Fri"
                    6 -> "Sat"
                    else -> throw Exception("Invalid value: Must between 0 and 6!")
                }
            }
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


        fun getDateListFromWeek(
            @Suppress("UNUSED_PARAMETER") schoolYear: Int = getDUTSchoolYear().year,
            week: Int = getDUTWeek()
        ): ArrayList<LocalDate> {
            // TODO: schoolYear for reloading older subjects here!
            val arrayList = arrayListOf<LocalDate>()
            // Set to GMT + 7.
            var firstDayOfWeekUnix: Long = getDUTSchoolYear().start + (7 * 60 * 60 * 1000).toLong()
            // Set to first day for 'week' argument.
            // Week always minus 1 (as index).
            firstDayOfWeekUnix += ((week - 1).toLong() * 7 * 24 * 60 * 60 * 1000)
            // LocalDate from Unix epoch days
            val dateTemp = LocalDate.fromEpochDays((firstDayOfWeekUnix / 1000 / 60 / 60 / 24).toInt())

            var count = 0
            while (count < 7) {
                // Add to list
                arrayList.add(dateTemp.plus(count, DateTimeUnit.DAY))
                // Add count by 1
                count += 1
            }

            return arrayList
        }
    }
}