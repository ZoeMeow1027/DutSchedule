package io.zoemeow.dutapp.android.model.news

import com.google.gson.annotations.SerializedName
import io.zoemeow.dutapi.objects.NewsGlobalItem
import java.io.Serializable

data class NewsCacheGlobal(
    @SerializedName("news_list")
    val newsListByDate: ArrayList<NewsGroupByDate<NewsGlobalItem>> = arrayListOf(),
    @SerializedName("page_current")
    var pageCurrent: Int = 1,
): Serializable