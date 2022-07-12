package io.zoemeow.dutapp.android.viewmodel

import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.lifecycle.ViewModel
import io.zoemeow.dutapi.News
import io.zoemeow.dutapi.objects.NewsGlobalItem
import io.zoemeow.dutapi.objects.NewsType
import io.zoemeow.dutapp.android.model.ProcessState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class NewsViewModel : ViewModel() {
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
    private var newsGlobalPage: MutableState<Int> = mutableStateOf(1)

    /**
     * Get news from sv.dut.udn.vn (tab Thông báo chung).
     *
     * @param renew: Mark this function for delete all item in old list and add new one. Otherwise
     * it will append to old one.
     */
    fun getNewsGlobal(renew: Boolean = false) {
        // If another instance is running, will not run this instance.
        if (newsGlobalState.value == ProcessState.Running)
            return

        // Set to running to avoid another instance.
        newsGlobalState.value = ProcessState.Running

        // Use this instead of viewModelScope.launch {} to avoid freezing UI.
        CoroutineScope(Dispatchers.IO).launch {
            try {
                Log.d("io.zoemeow.dutapp", "Triggered NewsGlobal load with page ${newsGlobalPage.value}")

                val newsTemp = News.getNews(NewsType.Global, if (renew) 1 else newsGlobalPage.value)
                if (newsTemp.size <= 0)
                    throw Exception("News list is empty!")

                //
                if (renew) newsGlobalList.clear()
                newsGlobalList.addAll(newsTemp)
                newsTemp.clear()

                // If renew, will reset news global state to default.
                // Otherwise, increase news page by 1
                newsGlobalPage.value = if (renew) 2 else newsGlobalPage.value + 1

                newsGlobalState.value = ProcessState.Successful
            }
            // Any exception thrown will be result of failed.
            catch (ex: Exception) {
                ex.printStackTrace()
                newsGlobalState.value = ProcessState.Failed
            }
        }
    }

    /**
     * This will save old item for cache and offline viewing for News Subject.
     */
    val newsSubjectList: SnapshotStateList<NewsGlobalItem> = mutableStateListOf()

    /**
     * Check if a progress for get news subject is running.
     */
    var newsSubjectState: MutableState<ProcessState> = mutableStateOf(ProcessState.NotRun)

    /**
     * Current News Subject page.
     */
    private var newsSubjectPage: MutableState<Int> = mutableStateOf(1)

    /**
     * Get news from sv.dut.udn.vn (tab Thông báo lớp học phần).
     *
     * @param renew: Mark this function for delete all item in old list and add new one. Otherwise
     * it will append to old one.
     */
    fun getNewsSubject(renew: Boolean = false) {
        // If another instance is running, will not run this instance.
        if (newsSubjectState.value == ProcessState.Running)
            return

        // Set to running to avoid another instance.
        newsSubjectState.value = ProcessState.Running

        // Use this instead of viewModelScope.launch {} to avoid freezing UI.
        CoroutineScope(Dispatchers.IO).launch {
            try {
                Log.d("io.zoemeow.dutapp", "Triggered NewsSubject load with page ${newsSubjectPage.value}")

                val newsTemp = News.getNews(NewsType.Subject, if (renew) 1 else newsSubjectPage.value)
                if (newsTemp.size <= 0)
                    throw Exception("News list is empty!")

                //
                if (renew) newsSubjectList.clear()
                newsSubjectList.addAll(newsTemp)
                newsTemp.clear()

                // If renew, will reset news subject state to default.
                // Otherwise, increase news page by 1
                newsSubjectPage.value = if (renew) 2 else newsGlobalPage.value + 1

                newsSubjectState.value = ProcessState.Successful
            }
            // Any exception thrown will be result of failed.
            catch (ex: Exception) {
                ex.printStackTrace()
                newsSubjectState.value = ProcessState.Failed
            }
        }
    }

    val newsGlobalItemChose: MutableState<NewsGlobalItem?> = mutableStateOf(null)
    val newsSubjectItemChose: MutableState<NewsGlobalItem?> = mutableStateOf(null)
}