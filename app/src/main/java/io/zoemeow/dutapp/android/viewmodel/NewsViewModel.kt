package io.zoemeow.dutapp.android.viewmodel

import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import io.zoemeow.dutapi.News
import io.zoemeow.dutapi.objects.NewsGlobalItem
import io.zoemeow.dutapi.objects.NewsType
import io.zoemeow.dutapp.android.model.news.NewsGroupByDate
import io.zoemeow.dutapp.android.model.ProcessState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NewsViewModel @Inject constructor() : ViewModel() {
    /**
     * UIStatus
     */
    private val uiStatus: UIStatus = UIStatus.getInstance()

    /**
     * This will save old item for cache and offline viewing for News Global.
     */
    val newsGlobalListByDate: SnapshotStateList<NewsGroupByDate<NewsGlobalItem>> = mutableStateListOf()

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
    val newsSubjectListByDate: SnapshotStateList<NewsGroupByDate<NewsGlobalItem>> = mutableStateListOf()

    /**
     * Check if a progress for get news subject is running.
     */
    val newsSubjectState: MutableState<ProcessState> = mutableStateOf(ProcessState.NotRun)

    /**
     * Current News Subject page.
     */
    private val newsSubjectPage: MutableState<Int> = mutableStateOf(1)

    /**
     * Get news from sv.dut.udn.vn (tab Thông báo chung).
     *
     * @param renewNewsList: Mark this function for delete all item in old list and add new one. Otherwise
     * it will append to old one.
     */
    fun getNewsGlobal(renewNewsList: Boolean = false) {
        val newsType = NewsType.Global
        val newsState = newsGlobalState
        val newsListByDate = newsGlobalListByDate
        val newsPage = newsGlobalPage

        // If another instance is running, will not run this instance.
        if (newsState.value == ProcessState.Running)
            return

        // Set to running to avoid another instance.
        newsState.value = ProcessState.Running

        // Use this instead of viewModelScope.launch {} to avoid freezing UI.
        CoroutineScope(Dispatchers.IO).launch {
            try {
                Log.d("NewsViewModel", "Triggered $newsType load with ${newsPage.value}")

                val newsTemp = News.getNews(newsType, if (renewNewsList) 1 else newsPage.value)
                if (newsTemp.size <= 0) {
                    Log.w("NewsViewModel", "$newsType list in page ${newsPage.value} is empty! Make sure connected to internet.")
                    throw Exception("$newsType list is empty!")
                }

                //
                if (renewNewsList) {
                    newsListByDate.clear()
                }
                newsTemp.forEach { item ->
                    if (newsListByDate.firstOrNull { it.date == item.date } == null) {
                        newsListByDate.add(
                            NewsGroupByDate(
                                itemList = arrayListOf(item),
                                date = item.date
                            )
                        )
                    }
                    else {
                        newsListByDate.first { it.date == item.date }.itemList.add(item)
                    }
                }
                newsListByDate.sortByDescending { it.date }
                newsTemp.clear()

                // If renew, will reset news state to default.
                // Otherwise, increase news page by 1
                newsPage.value = if (renewNewsList) 2 else newsPage.value + 1

                newsState.value = ProcessState.Successful
            }
            // Any exception thrown will be result of failed.
            catch (ex: Exception) {
                uiStatus.showSnackBarMessage(
                    "We ran into a problem while getting your $newsType. " +
                            "Check your internet connection and try again."
                )
                ex.printStackTrace()
                newsState.value = ProcessState.Failed
            }
        }
    }

    /**
     * Get news from sv.dut.udn.vn (tab Thông báo lớp học phần).
     *
     * @param renewNewsList: Mark this function for delete all item in old list and add new one. Otherwise
     * it will append to old one.
     */
    fun getNewsSubject(renewNewsList: Boolean = false) {
        val newsType = NewsType.Subject
        val newsState = newsSubjectState
        val newsListByDate = newsSubjectListByDate
        val newsPage = newsSubjectPage

        // If another instance is running, will not run this instance.
        if (newsState.value == ProcessState.Running)
            return

        // Set to running to avoid another instance.
        newsState.value = ProcessState.Running

        // Use this instead of viewModelScope.launch {} to avoid freezing UI.
        CoroutineScope(Dispatchers.IO).launch {
            try {
                Log.d("NewsViewModel", "Triggered $newsType load with ${newsPage.value}")

                val newsTemp = News.getNews(newsType, if (renewNewsList) 1 else newsPage.value)
                if (newsTemp.size <= 0) {
                    Log.w("NewsViewModel", "$newsType list in page ${newsPage.value} is empty! Make sure connected to internet.")
                    throw Exception("$newsType list is empty!")
                }

                //
                if (renewNewsList) {
                    newsListByDate.clear()
                }
                newsTemp.forEach { item ->
                    if (newsListByDate.firstOrNull { it.date == item.date } == null) {
                        newsListByDate.add(
                            NewsGroupByDate(
                                itemList = arrayListOf(item),
                                date = item.date
                            )
                        )
                    }
                    else {
                        newsListByDate.first { it.date == item.date }.itemList.add(item)
                    }
                }
                newsListByDate.sortByDescending { it.date }
                newsTemp.clear()

                // If renew, will reset news state to default.
                // Otherwise, increase news page by 1
                newsPage.value = if (renewNewsList) 2 else newsPage.value + 1

                newsState.value = ProcessState.Successful
            }
            // Any exception thrown will be result of failed.
            catch (ex: Exception) {
                uiStatus.showSnackBarMessage(
                    "We ran into a problem while getting your $newsType. " +
                            "Check your internet connection and try again."
                )
                ex.printStackTrace()
                newsState.value = ProcessState.Failed
            }
        }
    }
}