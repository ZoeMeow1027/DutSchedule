package io.zoemeow.dutapp.android.viewmodel

import android.util.Log
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import io.zoemeow.dutapi.News
import io.zoemeow.dutapi.objects.NewsGlobalItem
import io.zoemeow.dutapi.objects.NewsType
import io.zoemeow.dutapp.android.model.ProcessState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NewsViewModel @Inject constructor() : ViewModel() {
    /**
     * GlobalViewModel
     */
    private val globalViewModel = GlobalViewModel.getInstance()

    /**
     * This will save old item for cache and offline viewing for News Global.
     */
    val newsGlobalList: SnapshotStateList<NewsGlobalItem> = mutableStateListOf()

    /**
     * Check if a progress for get news global is running.
     */
    val newsGlobalState: MutableState<ProcessState> = mutableStateOf(ProcessState.NotRun)

    /**
     * Current News Global page.
     */
    private val newsGlobalPage: MutableState<Int> = mutableStateOf(1)

    /**
     * This will save old item for cache and offline viewing for News Subject.
     */
    val newsSubjectList: SnapshotStateList<NewsGlobalItem> = mutableStateListOf()

    /**
     * Check if a progress for get news subject is running.
     */
    val newsSubjectState: MutableState<ProcessState> = mutableStateOf(ProcessState.NotRun)

    /**
     * Current News Subject page.
     */
    private val newsSubjectPage: MutableState<Int> = mutableStateOf(1)

    @SuppressWarnings("unchecked")
    private fun <T> getNews(
        newsType: NewsType,
        newsState: MutableState<ProcessState>,
        newsList: SnapshotStateList<T>,
        newsPage: MutableState<Int>,
        renewNewsList: Boolean = false
    ) {
        // If another instance is running, will not run this instance.
        if (newsState.value == ProcessState.Running)
            return

        // Set to running to avoid another instance.
        newsState.value = ProcessState.Running

        // Use this instead of viewModelScope.launch {} to avoid freezing UI.
        CoroutineScope(Dispatchers.IO).launch {
            try {
                Log.d("NewsViewModel", "Triggered $newsType load with page ${newsPage.value}")

                val newsTemp = News.getNews(newsType, if (renewNewsList) 1 else newsPage.value)
                if (newsTemp.size <= 0) {
                    Log.w("NewsViewModel", "$newsType list in page ${newsPage.value} is empty! Make sure connected to internet.")
                    throw Exception("$newsType list is empty!")
                }

                //
                if (renewNewsList) newsList.clear()
                for (newsTempItem in newsTemp) {
                    newsList.add(newsTempItem as T)
                }
                newsTemp.clear()

                // If renew, will reset news state to default.
                // Otherwise, increase news page by 1
                newsPage.value = if (renewNewsList) 2 else newsPage.value + 1

                newsState.value = ProcessState.Successful
            }
            // Any exception thrown will be result of failed.
            catch (ex: Exception) {
                globalViewModel.showMessageSnackBar(
                    "We ran into a problem while getting your $newsType. " +
                            "Check your internet connection and try again."
                )
                ex.printStackTrace()
                newsState.value = ProcessState.Failed
            }
        }
    }

    /**
     * Get news from sv.dut.udn.vn (tab Thông báo chung).
     *
     * @param renewNewsList: Mark this function for delete all item in old list and add new one. Otherwise
     * it will append to old one.
     */
    fun getNewsGlobal(renewNewsList: Boolean = false) {
        getNews(
            newsType = NewsType.Global,
            newsState = newsGlobalState,
            newsList = newsGlobalList,
            newsPage = newsGlobalPage,
            renewNewsList = renewNewsList
        )
    }

    /**
     * Get news from sv.dut.udn.vn (tab Thông báo lớp học phần).
     *
     * @param renewNewsList: Mark this function for delete all item in old list and add new one. Otherwise
     * it will append to old one.
     */
    fun getNewsSubject(renewNewsList: Boolean = false) {
        getNews(
            newsType = NewsType.Subject,
            newsState = newsSubjectState,
            newsList = newsSubjectList,
            newsPage = newsSubjectPage,
            renewNewsList = renewNewsList
        )
    }

    val newsGlobalItemChose: MutableState<NewsGlobalItem?> = mutableStateOf(null)
    val newsSubjectItemChose: MutableState<NewsGlobalItem?> = mutableStateOf(null)

    lateinit var lazyListNewsGlobalState: LazyListState
    lateinit var lazyListNewsSubjectState: LazyListState
    lateinit var scope: CoroutineScope

    fun scrollNewsListToTop() {
        if (this::lazyListNewsGlobalState.isInitialized && this::scope.isInitialized) {
            scope.launch {
                if (!lazyListNewsGlobalState.isScrollInProgress)
                    lazyListNewsGlobalState.animateScrollToItem(index = 0)
            }
        }
        if (this::lazyListNewsGlobalState.isInitialized && this::scope.isInitialized) {
            scope.launch {
                if (!lazyListNewsSubjectState.isScrollInProgress)
                    lazyListNewsSubjectState.animateScrollToItem(index = 0)
            }
        }
    }
}