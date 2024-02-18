package io.zoemeow.dutschedule.model

data class DUTLessons(
    val index: Int,
    val name: String,
    val lesson: Double
) {
    companion object {
        val unknown = DUTLessons(index = -1, name = "(Unknown)", lesson = -99.9)
        val notStartedYet = DUTLessons(index = 0, name = "Not started yet", lesson = 0.0)
        val lesson1 = DUTLessons(index = 1, name = "Lesson 1", lesson = 1.0)
        val lesson2 = DUTLessons(index = 2, name = "Lesson 2", lesson = 2.0)
        val lesson3 = DUTLessons(index = 3, name = "Lesson 3", lesson = 3.0)
        val lesson4 = DUTLessons(index = 4, name = "Lesson 4", lesson = 4.0)
        val lesson5 = DUTLessons(index = 5, name = "Lesson 5", lesson = 5.0)
        val noonBreak = DUTLessons(index = 6, name = "Noon break", lesson = 5.5)
        val lesson6 = DUTLessons(index = 7, name = "Lesson 6", lesson = 6.0)
        val lesson7 = DUTLessons(index = 8, name = "Lesson 7", lesson = 7.0)
        val lesson8 = DUTLessons(index = 9, name = "Lesson 8", lesson = 8.0)
        val lesson9 = DUTLessons(index = 10, name = "Lesson 9", lesson = 9.0)
        val lesson10 = DUTLessons(index = 11, name = "Lesson 10", lesson = 10.0)
        val lesson11 = DUTLessons(index = 12, name = "Lesson 11", lesson = 11.0)
        val lesson12 = DUTLessons(index = 13, name = "Lesson 12", lesson = 12.0)
        val lesson13 = DUTLessons(index = 14, name = "Lesson 13", lesson = 13.0)
        val lesson14 = DUTLessons(index = 15, name = "Lesson 14", lesson = 14.0)
        val doneToday = DUTLessons(index = 16, name = "Done today", lesson = 99.9)
    }
}