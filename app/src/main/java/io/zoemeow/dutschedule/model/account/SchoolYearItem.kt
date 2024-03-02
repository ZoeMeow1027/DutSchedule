package io.zoemeow.dutschedule.model.account

import java.io.Serializable

data class SchoolYearItem(
    // School year (ex. 21 is for 2021-2022).
    var year: Int = 23,
    // School semester (in range 1-3, ex. 1 for semester 1, 3 for semester in summer).
    var semester: Int = 1
): Serializable {
    fun clone(
        year: Int? = null,
        semester: Int? = null,
    ): SchoolYearItem {
        return SchoolYearItem(
            year = year ?: this.year,
            semester = semester ?: this.semester
        )
    }

    override fun toString(): String {
        return String.format(
            "School year: 20%2d-20%2d - Semester: %s",
            year,
            year + 1,
            if (semester == 3) "Summer semester" else semester.toString()
        )
    }
}
