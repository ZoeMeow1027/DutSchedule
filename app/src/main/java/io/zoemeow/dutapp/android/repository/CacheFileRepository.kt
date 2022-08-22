package io.zoemeow.dutapp.android.repository

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.zoemeow.dutapi.objects.NewsGlobalItem
import io.zoemeow.dutapp.android.model.news.NewsGroupByDate
import java.io.File
import javax.inject.Inject

class CacheFileRepository @Inject constructor(
    @Transient private val fileNewsCacheGlobal: File,
    @Transient private val fileNewsCacheSubject: File,
) {
    @Transient
    val newsGlobalListByDate: SnapshotStateList<NewsGroupByDate<NewsGlobalItem>> = mutableStateListOf()

    @Transient
    val newsSubjectListByDate: SnapshotStateList<NewsGroupByDate<NewsGlobalItem>> = mutableStateListOf()

    fun loadCache() {
        runCatching {
            fileNewsCacheGlobal.bufferedReader().apply {
                val text = this.use { it.readText() }
                val arrayList = Gson().fromJson<ArrayList<NewsGroupByDate<NewsGlobalItem>>>(text, (object : TypeToken<ArrayList<NewsGroupByDate<NewsGlobalItem>>>() {}.type))
                newsGlobalListByDate.clear()
                newsGlobalListByDate.addAll(arrayList)
                newsGlobalListByDate.sortByDescending { it.date }
                arrayList.clear()
                this.close()
            }

            fileNewsCacheSubject.bufferedReader().apply {
                val text = this.use { it.readText() }
                val arrayList = Gson().fromJson<ArrayList<NewsGroupByDate<NewsGlobalItem>>>(text, (object : TypeToken<ArrayList<NewsGroupByDate<NewsGlobalItem>>>() {}.type))
                newsSubjectListByDate.clear()
                newsSubjectListByDate.addAll(arrayList)
                newsSubjectListByDate.sortByDescending { it.date }
                arrayList.clear()
                this.close()
            }
        }.onFailure {
            it.printStackTrace()
        }
    }

    fun saveCache() {
        runCatching {
            val jsonGlobal = Gson().toJson(newsGlobalListByDate)
            fileNewsCacheGlobal.writeText(jsonGlobal)
            val jsonSubject = Gson().toJson(newsSubjectListByDate)
            fileNewsCacheSubject.writeText(jsonSubject)
        }.onFailure {
            it.printStackTrace()
        }
    }
}