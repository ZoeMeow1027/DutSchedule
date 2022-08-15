package io.zoemeow.dutapp.android.viewmodel

import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import io.zoemeow.dutapi.News
import io.zoemeow.dutapi.objects.NewsGlobalItem
import io.zoemeow.dutapi.objects.NewsType
import io.zoemeow.dutapp.android.model.ProcessState
import io.zoemeow.dutapp.android.model.news.NewsGroupByDate
import io.zoemeow.dutapp.android.utils.getMD5
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NewsViewModel @Inject constructor() : ViewModel() {
    /**
     * UIStatus instance
     */
    private val uiStatus: UIStatus = UIStatus.getInstance()

    /**
     * AppCache instance
     */
    private val appCacheViewModel: AppCacheViewModel = AppCacheViewModel.getInstance()

    /**
     * Check if a progress for get news global is running.
     */
    val newsGlobalState: MutableState<ProcessState> = mutableStateOf(ProcessState.NotRanYet)

    /**
     * Current News Global page.
     */
    private val newsGlobalPage: MutableState<Int> = mutableStateOf(1)

    /**
     * Check if a progress for get news subject is running.
     */
    val newsSubjectState: MutableState<ProcessState> = mutableStateOf(ProcessState.NotRanYet)

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
        // If another instance is running, will not run this instance.
        if (newsGlobalState.value == ProcessState.Running)
            return

        // Set to running to avoid another instance.
        newsGlobalState.value = ProcessState.Running

        val pageValue = if (renewNewsList) 1 else newsGlobalPage.value
        val arrayList = arrayListOf<NewsGlobalItem>()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                Log.d("NewsGlobal", "Triggered page number $pageValue")
                arrayList.addAll(News.getNews(NewsType.Global, pageValue))
            }
            catch (ex: Exception) {
                ex.printStackTrace()
            }
        }.invokeOnCompletion {
            if (it != null || arrayList.size == 0) {
                it?.printStackTrace()
                newsGlobalState.value = ProcessState.Failed
                uiStatus.showSnackBarMessage(
                    "We ran into a problem while getting your ${NewsType.Global}. " +
                            "Check your internet connection and try again."
                )
            }
            else {
                if (renewNewsList)
                    appCacheViewModel.newsGlobalListByDate.clear()
                arrayList.forEach { item ->
                    // If no date found -> Always new news -> Add
                    if (appCacheViewModel.newsGlobalListByDate.firstOrNull { itemFilter -> itemFilter.date == item.date } == null) {
                        appCacheViewModel.newsGlobalListByDate.add(
                            NewsGroupByDate(
                                itemList = arrayListOf(item),
                                date = item.date,
                            )
                        )
                    }
                    else {
                        // Check if a news is exist in cache
                        val findItem = appCacheViewModel.newsGlobalListByDate.firstOrNull { itemFilter ->
                            itemFilter.itemList.firstOrNull { itemChild ->
                                getMD5("${itemChild.date}___${itemChild.title}") == getMD5("${item.date}___${item.title}")
                            } != null
                        }
                        // If not exist -> Add news. Otherwise ignore.
                        if (findItem == null) appCacheViewModel.newsGlobalListByDate.first { itemFilter -> itemFilter.date == item.date }.itemList.add(item)
                    }
                }
                appCacheViewModel.newsGlobalListByDate.sortByDescending { item -> item.date }
                arrayList.clear()

                var itemCount = 0
                appCacheViewModel.newsGlobalListByDate.forEach { newsGroup ->
                    itemCount += newsGroup.itemList.size
                }

                newsGlobalPage.value = if (renewNewsList) 2 else (itemCount / 30 + 1)
                newsGlobalState.value = ProcessState.Successful
            }
            appCacheViewModel.requestSaveCache()
        }
    }

    /**
     * Get news from sv.dut.udn.vn (tab Thông báo lớp học phần).
     *
     * @param renewNewsList: Mark this function for delete all item in old list and add new one. Otherwise
     * it will append to old one.
     */
    fun getNewsSubject(renewNewsList: Boolean = false) {
        // If another instance is running, will not run this instance.
        if (newsSubjectState.value == ProcessState.Running)
            return

        // Set to running to avoid another instance.
        newsSubjectState.value = ProcessState.Running

        val pageValue = if (renewNewsList) 1 else newsSubjectPage.value
        val arrayList = arrayListOf<NewsGlobalItem>()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                Log.d("NewsSubject", "Triggered page number $pageValue")
                arrayList.addAll(News.getNews(NewsType.Subject, pageValue))
            }
            catch (ex: Exception) {
                ex.printStackTrace()
            }
        }.invokeOnCompletion {
            if (it != null || arrayList.size == 0) {
                it?.printStackTrace()
                newsSubjectState.value = ProcessState.Failed
                uiStatus.showSnackBarMessage(
                    "We ran into a problem while getting your ${NewsType.Subject}. " +
                            "Check your internet connection and try again."
                )
            }
            else {
                if (renewNewsList)
                    appCacheViewModel.newsSubjectListByDate.clear()
                arrayList.forEach { item ->
                    if (appCacheViewModel.newsSubjectListByDate.firstOrNull { itemFilter -> itemFilter.date == item.date } == null) {
                        appCacheViewModel.newsSubjectListByDate.add(
                            NewsGroupByDate(
                                itemList = arrayListOf(item),
                                date = item.date,
                            )
                        )
                    }
                    else {
                        // Check if a news is exist in cache
                        val findItem = appCacheViewModel.newsSubjectListByDate.firstOrNull { itemFilter ->
                            itemFilter.itemList.firstOrNull { itemChild ->
                                getMD5("${itemChild.date}___${itemChild.title}") == getMD5("${item.date}___${item.title}")
                            } != null
                        }
                        // If not exist:
                        // - Add news.
                        // - Notify here.
                        // Otherwise ignore.
                        if (findItem == null) {
                            appCacheViewModel.newsSubjectListByDate.first { itemFilter -> itemFilter.date == item.date }.itemList.add(item)
                        }
                    }
                }
                appCacheViewModel.newsSubjectListByDate.sortByDescending { item -> item.date }
                arrayList.clear()

                var itemCount = 0
                appCacheViewModel.newsSubjectListByDate.forEach { newsGroup ->
                    itemCount += newsGroup.itemList.size
                }

                newsSubjectPage.value = if (renewNewsList) 2 else (itemCount / 30 + 1)
                newsSubjectState.value = ProcessState.Successful
            }
        }
        appCacheViewModel.requestSaveCache()
    }

    init {
        getNewsGlobal()
        getNewsSubject()
    }
}