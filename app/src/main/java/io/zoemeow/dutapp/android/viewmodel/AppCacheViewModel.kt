package io.zoemeow.dutapp.android.viewmodel

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import io.zoemeow.dutapp.android.repository.CacheFileRepository
import javax.inject.Inject

@HiltViewModel
class AppCacheViewModel @Inject constructor(
    private val cacheFileRepository: CacheFileRepository
): ViewModel() {
    companion object {
        private lateinit var instance: AppCacheViewModel

        fun getInstance(): AppCacheViewModel {
            return instance
        }

        fun setInstance(appCacheViewModel: AppCacheViewModel) {
            this.instance = appCacheViewModel
        }
    }

    /**
     * This contains news for News Global.
     */
    val newsGlobalListByDate = cacheFileRepository.newsGlobalListByDate

    // val newsGlobalListByDateFilter: SnapshotStateList<NewsGroupByDate<NewsGlobalItem>> = mutableStateListOf()

    /**
     * This contains news for News Subject.
     */
    val newsSubjectListByDate = cacheFileRepository.newsSubjectListByDate

    // val newsSubjectListByDateFilter: SnapshotStateList<NewsGroupByDate<NewsGlobalItem>> = mutableStateListOf()

    fun requestSaveCache() {
        cacheFileRepository.saveCache()
    }
}