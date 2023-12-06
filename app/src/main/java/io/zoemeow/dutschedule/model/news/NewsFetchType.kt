package io.zoemeow.dutschedule.model.news

enum class NewsFetchType(value: Int) {
    NextPage(0),
    FirstPage(1),
    ClearAndFirstPage(2)
}