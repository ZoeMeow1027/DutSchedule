package io.zoemeow.dutschedule.model.news

import io.dutwrapper.dutwrapper.model.news.NewsGlobalItem
import io.dutwrapper.dutwrapper.model.news.NewsSubjectItem
import io.zoemeow.dutschedule.model.VariableListState
import io.zoemeow.dutschedule.repository.DutRequestRepository

/**
 * @param onEventSent Event when done:
 * 1: News global
 * 2: News subject
 */
class DUTNews(
    private val dutRequestRepository: DutRequestRepository,
    private val onEventSent: ((Int) -> Unit)? = null
) {
    companion object {
        fun <T> VariableListState<T>.getPage(): Int {
            return try {
                this.parameters["page"]?.toInt() ?: 1
            } catch (_: Exception) {
                1
            }
        }

        fun <T> VariableListState<T>.setPage(page: Int = 1) {
            if (page < 1) {
                throw Exception("")
            }
            this.parameters["page"] = page.toString()
        }
    }

    val newsGlobal: VariableListState<NewsGlobalItem> = VariableListState()
    val newsSubject: VariableListState<NewsSubjectItem> = VariableListState()

    
}