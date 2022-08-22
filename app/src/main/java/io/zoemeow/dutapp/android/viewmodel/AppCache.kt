package io.zoemeow.dutapp.android.viewmodel

import io.zoemeow.dutapp.android.repository.CacheFileRepository

class AppCache(
    private val cacheFileRepository: CacheFileRepository
) {
    /**
     * This contains news for News Global.
     */
    val newsGlobalListByDate = cacheFileRepository.newsGlobalListByDate

    /**
     * This contains news for News Subject.
     */
    val newsSubjectListByDate = cacheFileRepository.newsSubjectListByDate

    fun saveCache() {
        cacheFileRepository.saveCache()
    }

    fun loadCache() {
        cacheFileRepository.loadCache()
    }
}