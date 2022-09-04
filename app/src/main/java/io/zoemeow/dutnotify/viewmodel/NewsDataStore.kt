package io.zoemeow.dutnotify.viewmodel

import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import io.zoemeow.dutapi.objects.NewsGlobalItem
import io.zoemeow.dutapi.objects.NewsType
import io.zoemeow.dutnotify.model.enums.ProcessState
import io.zoemeow.dutnotify.model.enums.NewsPageType
import io.zoemeow.dutnotify.model.news.NewsGroupByDate
import io.zoemeow.dutnotify.module.NewsModule
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class NewsDataStore(
    private val mainViewModel: MainViewModel
) {
    // News UI area ================================================================================
    /**
     * Check if a progress for get news global is running.
     */
    val procNewsGlobal: MutableState<ProcessState> = mutableStateOf(ProcessState.NotRanYet)

    /**
     * Check if a progress for get news subject is running.
     */
    val procNewsSubject: MutableState<ProcessState> = mutableStateOf(ProcessState.NotRanYet)

    // News Data area ==============================================================================
    /**
     * Current News Global page.
     */
    val newsGlobalPageCurrent: MutableState<Int> = mutableStateOf(1)

    /**
     * Current News Global list.
     */
    val listNewsGlobalByDate: SnapshotStateList<NewsGroupByDate<NewsGlobalItem>> =
        mutableStateListOf()

    /**
     * Current News Subject page.
     */
    val newsSubjectPageCurrent: MutableState<Int> = mutableStateOf(1)

    /**
     * Current News Subject list.
     */
    val listNewsSubjectByDate: SnapshotStateList<NewsGroupByDate<NewsGlobalItem>> =
        mutableStateListOf()
    // =============================================================================================

    fun fetchNewsGlobal(
        newsPageType: NewsPageType = NewsPageType.NextPage,
    ) {
        // If another instance is running, immediately stop this thread now.
        if (procNewsGlobal.value == ProcessState.Running)
            return

        // Set to running to avoid another instance.
        procNewsGlobal.value = ProcessState.Running

        // Temporary variables
        val newsFromInternet = arrayListOf<NewsGlobalItem>()

        Log.d("NewsGlobal", "Triggered getting news")

        CoroutineScope(Dispatchers.Main).launch {
            withContext(Dispatchers.IO) {
                try {
                    newsFromInternet.addAll(
                        NewsModule.getNewsGlobal(
                            when (newsPageType) {
                                NewsPageType.NextPage -> newsGlobalPageCurrent.value
                                else -> 1
                            }
                        )
                    )
                } catch (ex: Exception) {
                    ex.printStackTrace()
                }
            }
        }.invokeOnCompletion {
            it?.printStackTrace()

            if (newsFromInternet.size == 0) {
                procNewsGlobal.value = ProcessState.Failed
                newsFromInternet.clear()
                mainViewModel.showSnackBarMessage(
                    "We ran into a problem while getting your News ${NewsType.Global}. " +
                            "Check your internet connection and try again."
                )
                return@invokeOnCompletion
            }

            if (newsPageType == NewsPageType.ResetToPage1)
                listNewsGlobalByDate.clear()

            val newsTemp = arrayListOf<NewsGroupByDate<NewsGlobalItem>>().apply {
                addAll(listNewsGlobalByDate)
            }
            val newsDiff = NewsModule.getNewsGlobalDiff(
                source = newsTemp,
                target = newsFromInternet,
            )
            NewsModule.addAndCheckDuplicateNewsGlobal(
                source = newsTemp,
                target = newsDiff,
                addItemToTop = newsPageType != NewsPageType.NextPage
            )
            listNewsGlobalByDate.swapList(newsTemp)
            newsTemp.clear()

            when (newsPageType) {
                NewsPageType.NextPage -> {
                    newsGlobalPageCurrent.value += 1
                }
                NewsPageType.ResetToPage1 -> {
                    newsGlobalPageCurrent.value = 2
                }
                else -> {}
            }

            procNewsGlobal.value = ProcessState.Successful
            mainViewModel.requestSaveCache()
        }
    }

    fun fetchNewsSubject(
        newsPageType: NewsPageType = NewsPageType.NextPage,
    ) {
        // If another instance is running, immediately stop this thread now.
        if (procNewsSubject.value == ProcessState.Running)
            return

        // Set to running to avoid another instance.
        procNewsSubject.value = ProcessState.Running

        // Temporary variables
        val newsFromInternet = arrayListOf<NewsGlobalItem>()

        Log.d("NewsSubject", "Triggered getting news")

        CoroutineScope(Dispatchers.Main).launch {
            withContext(Dispatchers.IO) {
                try {
                    newsFromInternet.addAll(
                        NewsModule.getNewsSubject(
                            when (newsPageType) {
                                NewsPageType.NextPage -> newsSubjectPageCurrent.value
                                else -> 1
                            }
                        )
                    )
                } catch (ex: Exception) {
                    ex.printStackTrace()
                }
            }
        }.invokeOnCompletion {
            it?.printStackTrace()

            if (newsFromInternet.size == 0) {
                procNewsSubject.value = ProcessState.Failed
                mainViewModel.showSnackBarMessage(
                    "We ran into a problem while getting your News ${NewsType.Global}. " +
                            "Check your internet connection and try again."
                )
                newsFromInternet.clear()
                return@invokeOnCompletion
            }

            if (newsPageType == NewsPageType.ResetToPage1)
                listNewsSubjectByDate.clear()

            val newsTemp = arrayListOf<NewsGroupByDate<NewsGlobalItem>>().apply {
                addAll(listNewsSubjectByDate)
            }
            val newsDiff = NewsModule.getNewsGlobalDiff(
                source = newsTemp,
                target = newsFromInternet,
            )
            NewsModule.addAndCheckDuplicateNewsGlobal(
                source = newsTemp,
                target = newsDiff,
                addItemToTop = newsPageType != NewsPageType.NextPage
            )
            listNewsSubjectByDate.swapList(newsTemp)
            newsTemp.clear()

            when (newsPageType) {
                NewsPageType.NextPage -> {
                    newsSubjectPageCurrent.value += 1
                }
                NewsPageType.ResetToPage1 -> {
                    newsSubjectPageCurrent.value = 2
                }
                else -> {}
            }

            procNewsSubject.value = ProcessState.Successful
            mainViewModel.requestSaveCache()
        }
    }
}