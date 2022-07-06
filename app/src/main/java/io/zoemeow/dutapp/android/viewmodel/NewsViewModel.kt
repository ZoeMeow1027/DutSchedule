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

class NewsViewModel : ViewModel() {
    val newsGlobalList: SnapshotStateList<NewsGlobalItem> = mutableStateListOf()
    var newsGlobalState: MutableState<ProcessState> = mutableStateOf(ProcessState.NotRun)
    private var newsGlobalPage: Int = 0
    val newsSubjectList: SnapshotStateList<NewsGlobalItem> = mutableStateListOf()
    var newsSubjectState: MutableState<ProcessState> = mutableStateOf(ProcessState.NotRun)
    private var newsSubjectPage: Int = 0

    fun getNewsGlobal(renew: Boolean = false) {
        // If another instance is running, will not run this instance.
        if (newsGlobalState.value == ProcessState.Running)
            return

        // If renew, will reset news global state to default.
        if (renew) {
            newsGlobalPage = 0
            newsGlobalList.clear()
        }

        // Set to running to avoid another instance.
        newsGlobalState.value = ProcessState.Running
        try {
            // Increase news page by 1
            newsGlobalPage += 1
            newsGlobalList.addAll(News.getNews(NewsType.Global, newsGlobalPage))

            newsGlobalState.value = ProcessState.Successful
        }
        // Any exception thrown will be result of failed.
        catch (ex: Exception) {
            ex.printStackTrace()
            newsGlobalPage -= 1
            newsGlobalState.value = ProcessState.Failed
        }

        Log.d("io.zoemeow.dutapp", "Triggered NewsGlobal load with page $newsGlobalPage")
    }

    fun getNewsSubject(renew: Boolean = false) {
        // If another instance is running, will not run this instance.
        if (newsSubjectState.value == ProcessState.Running)
            return

        // If renew, will reset news global state to default.
        if (renew) {
            newsSubjectPage = 0
            newsSubjectList.clear()
        }

        // Set to running to avoid another instance.
        newsSubjectState.value = ProcessState.Running
        try {
            // Increase news page by 1
            newsSubjectPage += 1
            newsSubjectList.addAll(News.getNews(NewsType.Subject, newsGlobalPage))

            newsSubjectState.value = ProcessState.Successful
        }
        // Any exception thrown will be result of failed.
        catch (ex: Exception) {
            ex.printStackTrace()
            newsSubjectPage -= 1
            newsSubjectState.value = ProcessState.Failed
        }

        Log.d("io.zoemeow.dutapp", "Triggered NewsSubject load with page $newsSubjectPage")
    }
}