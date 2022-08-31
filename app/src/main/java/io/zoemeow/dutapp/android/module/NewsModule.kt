package io.zoemeow.dutapp.android.module

import io.zoemeow.dutapi.News
import io.zoemeow.dutapi.objects.NewsGlobalItem
import io.zoemeow.dutapi.objects.NewsType
import io.zoemeow.dutapp.android.model.news.NewsGroupByDate

class NewsModule {
    companion object {
        fun getNewsGlobal(
            page: Int = 1
        ): ArrayList<NewsGlobalItem> {
            return try {
                News.getNews(NewsType.Global, page)
            } catch (ex: Exception) {
                ex.printStackTrace()
                arrayListOf()
            }
        }

        fun getNewsSubject(
            page: Int = 1
        ): ArrayList<NewsGlobalItem> {
            return try {
                News.getNews(NewsType.Subject, page)
            } catch (ex: Exception) {
                ex.printStackTrace()
                arrayListOf()
            }
        }

        fun getNewsGlobalDiff(
            source: ArrayList<NewsGroupByDate<NewsGlobalItem>>,
            target: ArrayList<NewsGlobalItem>
        ): ArrayList<NewsGlobalItem> {
            val result: ArrayList<NewsGlobalItem> = arrayListOf()

            // Three conditions:
            // - If news group with date not exist in targetItem date.
            // - If news group with date exist but no news found with targetItem.
            // - If news group with date exist, news exist but different.
            target.forEach { targetItem ->
                if (
                    source.firstOrNull { newsGroupItem ->
                        // 1
                        newsGroupItem.date == targetItem.date &&
                                // 2 + 3
                                newsGroupItem.itemList.firstOrNull { newsItem ->
                                    newsItem.title == targetItem.title &&
                                            newsItem.date == targetItem.date &&
                                            newsItem.content == targetItem.content
                                } != null
                    } == null
                ) result.add(targetItem)
            }

            return result
        }

        fun getNewsSubjectDiff(
            source: ArrayList<NewsGroupByDate<NewsGlobalItem>>,
            target: ArrayList<NewsGlobalItem>
        ): ArrayList<NewsGlobalItem> {
            val result: ArrayList<NewsGlobalItem> = arrayListOf()

            // Three conditions:
            // - If news group with date not exist in targetItem date.
            // - If news group with date exist but no news found with targetItem.
            // - If news group with date exist, news exist but different.
            target.forEach { targetItem ->
                if (
                    source.firstOrNull { newsGroupItem ->
                        // 1
                        newsGroupItem.date == targetItem.date &&
                                // 2 + 3
                                newsGroupItem.itemList.firstOrNull { newsItem ->
                                    newsItem.title == targetItem.title &&
                                            newsItem.date == targetItem.date &&
                                            newsItem.content == targetItem.content
                                } != null
                    } == null
                ) result.add(targetItem)
            }

            return result
        }

        fun addAndCheckDuplicateNewsGlobal(
            source: ArrayList<NewsGroupByDate<NewsGlobalItem>>,
            target: ArrayList<NewsGlobalItem>,
            addItemToTop: Boolean = true,
            sortGroupByDescending: Boolean = true,
        ) {
            data class DateCount(
                val date: Long,
                var count: Int = 0,
            )
            val timestampCount: ArrayList<DateCount> = arrayListOf()
            
            target.forEach { item ->
                // If no date found -> Always new news -> Add
                if (source.firstOrNull { filter -> filter.date == item.date} == null) {
                    source.add(
                        NewsGroupByDate(
                            itemList = arrayListOf(item),
                            date = item.date
                        )
                    )
                } else {
                    // Check if a news is exist in cache
                    val findItem = source.firstOrNull { itemFilter ->
                        itemFilter.itemList.firstOrNull { itemChild ->
                            itemChild.date == item.date &&
                                    itemChild.title == item.title &&
                                    itemChild.content == item.content
                        } != null
                    }

                    // If not exist: Add news and add newsNeedToNotify for notify user.
                    // Otherwise ignore
                    if (findItem == null) {
                        if (addItemToTop) {
                            if (!timestampCount.any { it.date == item.date })
                                timestampCount.add(DateCount(item.date, 0))

                            source.first { itemFilter -> itemFilter.date == item.date }.itemList.add(
                                timestampCount.first { it.date == item.date }.count, item
                            )
                            timestampCount.first { it.date == item.date }.count += 1
                        }
                        else {
                            source.first { itemFilter -> itemFilter.date == item.date }.itemList.add(item)
                        }
                    }
                }
            }

            if (sortGroupByDescending)
                source.sortByDescending { it.date }
            else source.sortBy { it.date }
        }
    }
}