package io.zoemeow.dutapp.android.viewmodel

import android.content.Context
import android.content.Intent
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
import io.zoemeow.dutapp.android.view.news.NewsDetailsActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class NewsViewModel : ViewModel() {
    companion object {
        private val instance: MutableState<NewsViewModel> = mutableStateOf(NewsViewModel())

        fun getInstance(): NewsViewModel {
            return instance.value
        }

        fun setInstance(accViewModel: NewsViewModel) {
            this.instance.value = accViewModel
        }
    }

    val newsGlobalList: SnapshotStateList<NewsGlobalItem> = mutableStateListOf()
    val newsGlobalState: MutableState<ProcessState> = mutableStateOf(ProcessState.NotRun)
    private var newsGlobalPage: Int = 1
    val newsSubjectList: SnapshotStateList<NewsGlobalItem> = mutableStateListOf()
    var newsSubjectState: MutableState<ProcessState> = mutableStateOf(ProcessState.NotRun)
    private var newsSubjectPage: Int = 1

    // https://www.youtube.com/watch?v=ksstsMCDEmk

    fun getNewsGlobal(renew: Boolean = false) {
        // If another instance is running, will not run this instance.
        if (newsGlobalState.value == ProcessState.Running)
            return

        // Set to running to avoid another instance.
        newsGlobalState.value = ProcessState.Running

        CoroutineScope(Dispatchers.IO).launch {
            try {
                Log.d("io.zoemeow.dutapp", "Triggered NewsGlobal load with page $newsGlobalPage")

                val newsTemp = News.getNews(NewsType.Global, if (renew) 1 else newsGlobalPage)
                if (newsTemp.size <= 0)
                    throw Exception("News list is empty!")

                //
                if (renew) newsGlobalList.clear()
                newsGlobalList.addAll(newsTemp)
                newsTemp.clear()

                // If renew, will reset news global state to default.
                if (renew) newsGlobalPage = 2
                // Otherwise, increase news page by 1
                else newsGlobalPage += 1

                newsGlobalState.value = ProcessState.Successful
            }
            // Any exception thrown will be result of failed.
            catch (ex: Exception) {
                ex.printStackTrace()
                newsGlobalState.value = ProcessState.Failed
            }
        }
    }

    fun getNewsSubject(renew: Boolean = false) {
        // If another instance is running, will not run this instance.
        if (newsSubjectState.value == ProcessState.Running)
            return

        // Set to running to avoid another instance.
        newsSubjectState.value = ProcessState.Running

        CoroutineScope(Dispatchers.IO).launch {
            try {
                Log.d("io.zoemeow.dutapp", "Triggered NewsSubject load with page $newsSubjectPage")

                val newsTemp = News.getNews(NewsType.Subject, if (renew) 1 else newsSubjectPage)
                if (newsTemp.size <= 0)
                    throw Exception("News list is empty!")

                //
                if (renew) newsSubjectList.clear()
                newsSubjectList.addAll(newsTemp)
                newsTemp.clear()

                // If renew, will reset news subject state to default.
                if (renew) newsSubjectPage = 2
                // Otherwise, increase news page by 1
                else newsSubjectPage += 1

                newsSubjectState.value = ProcessState.Successful
            }
            // Any exception thrown will be result of failed.
            catch (ex: Exception) {
                ex.printStackTrace()
                newsSubjectState.value = ProcessState.Failed
            }
        }
    }

    private var context: MutableState<Context?> = mutableStateOf(null)
    fun setContext(context: Context) {
        this.context.value = context
    }

    fun openNewsDetailsGlobalActivity(news: NewsGlobalItem) {
        val intent = Intent(context.value, NewsDetailsActivity::class.java)
        intent.putExtra("isNewsGlobal", true)
        intent.putExtra("newsItem", news)
        context.value?.startActivity(intent)
    }

    fun openNewsDetailsSubjectActivity(news: NewsGlobalItem) {
        val intent = Intent(context.value, NewsDetailsActivity::class.java)
        intent.putExtra("isNewsGlobal", false)
        intent.putExtra("newsItem", news)
        context.value?.startActivity(intent)
    }
}