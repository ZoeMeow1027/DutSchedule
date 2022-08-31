package io.zoemeow.dutapp.android.model.appsettings

import java.io.Serializable
import java.text.DecimalFormat

data class CustomClock(
    var hour: Int,
    var minute: Int,
): Serializable {
    override fun toString(): String {
        val numberFormat = DecimalFormat("00")
        return "${numberFormat.format(this.hour)}:${numberFormat.format(this.minute)}"
    }

    // 0 if a = b, < 0 of a < b, > 0 if a > b
    operator fun compareTo(value: CustomClock): Int {
        return when {
            (this.hour > value.hour) -> 1
            (this.hour == value.hour && this.minute > value.minute) -> 1
            (this.hour < value.hour) -> -1
            (this.hour == value.hour && this.minute < value.minute) -> -1
            else -> 0
        }
    }

    fun isInRange(
        valueLeft: CustomClock,
        valueRight: CustomClock
    ): Boolean {
        return if (valueLeft < valueRight) {
            valueLeft < this && this < valueRight
        }
        else if (valueLeft > valueRight) {
            (valueLeft > this && valueRight > this) ||
                    (valueLeft < this && valueRight < this)
        }
        else {
            valueLeft == this && valueRight == this
        }
    }
}