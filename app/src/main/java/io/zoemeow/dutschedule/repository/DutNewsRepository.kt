package io.zoemeow.dutschedule.repository

import io.dutwrapper.dutwrapper.News
import io.dutwrapper.dutwrapper.model.enums.NewsSearchType
import io.dutwrapper.dutwrapper.model.news.NewsGlobalGroupByDate
import io.dutwrapper.dutwrapper.model.news.NewsGlobalItem
import io.dutwrapper.dutwrapper.model.news.NewsSubjectGroupByDate
import io.dutwrapper.dutwrapper.model.news.NewsSubjectItem
import io.zoemeow.dutschedule.model.news.NewsGroupByDate

class DutNewsRepository {
    companion object {
        fun getNewsGlobal(
            page: Int = 1,
            searchType: NewsSearchType? = null,
            searchQuery: String? = null
        ): ArrayList<NewsGlobalItem> {
            return try {
                News.getNewsGlobal(page, searchType, searchQuery)
            } catch (ex: Exception) {
                ex.printStackTrace()
                arrayListOf()
            }
        }

        fun getNewsSubject(
            page: Int = 1,
            searchType: NewsSearchType? = null,
            searchQuery: String? = null
        ): ArrayList<NewsSubjectItem> {
            return try {
                News.getNewsSubject(page, searchType, searchQuery)
            } catch (ex: Exception) {
                ex.printStackTrace()
                arrayListOf()
            }
        }

        fun getNewsGlobalGroupByDate(
            page: Int = 1,
            searchType: NewsSearchType? = null,
            searchQuery: String? = null
        ): ArrayList<NewsGlobalGroupByDate> {
            return try {
                News.getNewsGlobalGroupByDate(page, searchType, searchQuery)
            } catch (ex: Exception) {
                ex.printStackTrace()
                arrayListOf()
            }
        }

        fun getNewsSubjectGroupByDate(
            page: Int = 1,
            searchType: NewsSearchType? = null,
            searchQuery: String? = null
        ): ArrayList<NewsSubjectGroupByDate> {
            return try {
                News.getNewsSubjectGroupByDate(page, searchType, searchQuery)
            } catch (ex: Exception) {
                ex.printStackTrace()
                arrayListOf()
            }
        }
    }
}