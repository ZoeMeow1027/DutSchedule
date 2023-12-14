package io.zoemeow.dutschedule.viewmodel

import android.util.Log
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import io.dutwrapper.dutwrapper.model.enums.NewsSearchType
import io.dutwrapper.dutwrapper.model.enums.NewsType
import io.dutwrapper.dutwrapper.model.news.NewsGlobalItem
import io.zoemeow.dutschedule.model.ProcessState
import io.zoemeow.dutschedule.repository.DutNewsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NewsSearchViewModel @Inject constructor(): ViewModel() {
    //<editor-fold desc="Config variables">
    // Search query
    val query = mutableStateOf("")

    // Search method
    val method = mutableStateOf(NewsSearchType.ByTitle)

    // News search type
    val type = mutableStateOf(NewsType.Global)
    //</editor-fold>

    //<editor-fold desc="Runtime variables - Don't modify these variables to avoid issues">
    // News progress - If is running, other execute won't be executed.
    val progress = mutableStateOf(ProcessState.NotRunYet)

    // News result
    val newsList = mutableStateListOf<NewsGlobalItem>()

    // News page
    private val newsPage = mutableIntStateOf(1)
    //</editor-fold>

    //<editor-fold desc="UI variables - Don't modify these variables to avoid issues">
    // Search method option (by title/by content)
    val searchMethodOptionVisible = mutableStateOf(false)

    // Search type option (news global/news subject)
    val newsSearchType = mutableStateOf(false)
    //</editor-fold>


    // Functions
    fun invokeSearch(startOver: Boolean = false) {
        Log.d("DutSchedule", "Invoking search")
        CoroutineScope(Dispatchers.IO).launch {
            if (progress.value == ProcessState.Running) {
                return@launch
            }
            if (query.value.isEmpty()) {
                progress.value = ProcessState.NotRunYet
                return@launch
            } else {
                progress.value = ProcessState.Running
            }

            try {
                if (startOver) {
                    newsList.clear()
                }
                if (type.value == NewsType.Subject) {
                    newsList.addAll(
                        DutNewsRepository.getNewsSubject(
                            if (startOver) 1 else newsPage.value,
                            searchType = method.value,
                            searchQuery = query.value
                        )
                    )
                } else {
                    newsList.addAll(
                        DutNewsRepository.getNewsGlobal(
                            if (startOver) 1 else newsPage.value,
                            searchType = method.value,
                            searchQuery = query.value
                        )
                    )
                }
                if (startOver) {
                    newsPage.value = 2
                } else {
                    newsPage.value += 1
                }

                progress.value = ProcessState.Successful
            } catch (_: Exception) {
                progress.value = ProcessState.Failed
            }
        }
    }
}