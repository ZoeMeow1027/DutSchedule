package io.zoemeow.dutschedule.model.news

import io.dutwrapper.dutwrapper.model.enums.NewsSearchType
import io.dutwrapper.dutwrapper.model.enums.NewsType

data class NewsSearchHistory(
    val query: String, val newsMethod: NewsSearchType, val newsType: NewsType
) {
    fun isEqual(item: NewsSearchHistory, caseSensitive: Boolean = false): Boolean {
        return (if (caseSensitive) item.query == this.query else item.query.lowercase() == this.query.lowercase()) && item.newsType == this.newsType && item.newsMethod == this.newsMethod
    }
}