package io.zoemeow.dutschedule.model.news

enum class NewsFetchType(val value: Int) {
    NextPage(0),
    FirstPage(1),
    ClearAndFirstPage(2);

    companion object {
        fun fromValue(value: Int): NewsFetchType {
            return when (value) {
                0 -> NextPage
                1 -> FirstPage
                2 -> ClearAndFirstPage
                else -> throw Exception("Invalid value!")
            }
        }
    }
}