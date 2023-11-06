package io.zoemeow.dutschedule.model.settings

import java.io.Serializable

data class SubjectCode(
    val studentYearId: String,
    val classId: String
): Serializable {
    fun isEquals(item: SubjectCode): Boolean {
        return studentYearId == item.studentYearId &&
                classId == item.classId
    }

    override fun toString(): String {
        return "${studentYearId}.${classId}"
    }
}