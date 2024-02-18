package io.zoemeow.dutschedule.model

import java.io.Serializable
import java.util.Calendar

class CustomClock : Serializable {
    val hour: Int
    val minute: Int

    constructor(calendar: Calendar) {
        this.hour = calendar.get(Calendar.HOUR_OF_DAY)
        this.minute = calendar.get(Calendar.MINUTE)
    }

    constructor(hour: Int, minute: Int) {
        if (hour < 0 || hour > 23 || minute < 0 || minute > 59) {
            throw Exception("Invalid time format")
        }

        this.hour = hour
        this.minute = minute
    }

    override fun toString(): String {
//        val numberFormat = DecimalFormat("00")
//        return "${numberFormat.format(this.hour)}:${numberFormat.format(this.minute)}"
        return String.format("%2d:%2d", hour, minute)
    }

    // 0 if a = b, < 0 of a < b, > 0 if a > b
    operator fun compareTo(item: CustomClock): Int {
        return when {
            hour < item.hour -> -1
            hour > item.hour -> 1
            minute < item.minute -> -1
            minute > item.minute -> 1
            else -> 0
        }
    }


    fun isInRange(
        valueLeft: CustomClock,
        valueRight: CustomClock
    ): Boolean {
        return if (valueLeft < valueRight) {
            valueLeft < this && this < valueRight
        } else if (valueLeft > valueRight) {
            (valueLeft > this && valueRight > this) ||
                    (valueLeft < this && valueRight < this)
        } else {
            valueLeft == this && valueRight == this
        }
    }

    // Get current lesson by current time.
    fun toDUTLesson2(): DUTLessons {
        return when {
            this < CustomClock(7, 0) -> DUTLessons.notStartedYet
            this < CustomClock(8, 0) -> DUTLessons.lesson1
            this < CustomClock(9, 0) -> DUTLessons.lesson2
            this < CustomClock(10, 0) -> DUTLessons.lesson3
            this < CustomClock(11, 0) -> DUTLessons.lesson4
            this < CustomClock(12, 0) -> DUTLessons.lesson5
            this < CustomClock(12, 30) -> DUTLessons.noonBreak
            this < CustomClock(13, 30) -> DUTLessons.lesson6
            this < CustomClock(14, 30) -> DUTLessons.lesson7
            this < CustomClock(15, 30) -> DUTLessons.lesson8
            this < CustomClock(16, 30) -> DUTLessons.lesson9
            this < CustomClock(17, 30) -> DUTLessons.lesson10
            this < CustomClock(18, 15) -> DUTLessons.lesson11
            this < CustomClock(19, 10) -> DUTLessons.lesson12
            this < CustomClock(19, 55) -> DUTLessons.lesson13
            this < CustomClock(20, 30) -> DUTLessons.lesson14
            this >= CustomClock(20, 30) -> DUTLessons.doneToday
            else -> DUTLessons.unknown
        }
    }

    companion object {
        fun getCurrent(): CustomClock {
            return CustomClock(Calendar.getInstance())
        }
    }
}