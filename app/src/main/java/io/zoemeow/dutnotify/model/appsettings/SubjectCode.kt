package io.zoemeow.dutnotify.model.appsettings

data class SubjectCode(
    val studentYearId: String,
    val classId: String,
    val name: String,
) {
    fun isEquals(item: SubjectCode): Boolean {
        return studentYearId == item.studentYearId &&
                classId == item.classId &&
                name == item.name
    }

    override fun toString(): String {
        return "${studentYearId}.${classId} - ${this.name}"
    }
}