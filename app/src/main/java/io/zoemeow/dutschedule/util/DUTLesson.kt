package io.zoemeow.dutschedule.util

import java.util.Calendar

data class DUTLesson(
    val hour: Int,
    val minute: Int
) {
    init {
        if (hour < 0 || hour > 23 || minute < 0 || minute > 59) {
            throw Exception("Invalid time format")
        }
    }

    operator fun compareTo(item: DUTLesson): Int {
        return when {
            hour < item.hour -> -1
            hour > item.hour -> 1
            minute < item.minute -> -1
            minute > item.minute -> 1
            else -> 0
        }
    }

    override fun toString(): String {
        return String.format("%2d:%2d", hour, minute)
    }

    // Get current lesson by current time.
    // -3: Unknown
    // -2: Lesson not started
    // -1: Noon break
    // 0: Lesson finished
    fun toDUTLesson(): Int {
        return when {
            this < DUTLesson(7, 0) -> -2
            this < DUTLesson(8, 0) -> 1
            this < DUTLesson(9, 0) -> 2
            this < DUTLesson(10, 0) -> 3
            this < DUTLesson(11, 0) -> 4
            this < DUTLesson(12, 0) -> 5
            this < DUTLesson(12, 30) -> -1
            this < DUTLesson(13, 30) -> 6
            this < DUTLesson(14, 30) -> 7
            this < DUTLesson(15, 30) -> 8
            this < DUTLesson(16, 30) -> 9
            this < DUTLesson(17, 30) -> 10
            this < DUTLesson(18, 15) -> 11
            this < DUTLesson(19, 10) -> 12
            this < DUTLesson(19, 55) -> 13
            this < DUTLesson(20, 30) -> 14
            this >= DUTLesson(20, 30) -> 0
            else -> -3
        }
    }

    companion object {
        fun getCurrentLesson(): DUTLesson {
            val currentDate = Calendar.getInstance()
            return DUTLesson(
                hour = currentDate[Calendar.HOUR_OF_DAY],
                minute = currentDate[Calendar.MINUTE]
            )
        }
    }
}