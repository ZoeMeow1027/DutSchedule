package io.zoemeow.dutschedule.repository

import io.dutwrapper.dutwrapper.News
import io.dutwrapper.dutwrapper.model.enums.NewsSearchType
import io.dutwrapper.dutwrapper.model.news.NewsGlobalItem
import io.dutwrapper.dutwrapper.model.news.NewsSubjectItem
import io.zoemeow.dutschedule.model.news.NewsGroupByDate

class DutNewsRepository {
    companion object {
        fun getNewsGlobal(
            page: Int = 1,
            searchType: NewsSearchType? = null,
            searchQuery: String? = null
        ): ArrayList<NewsGlobalItem> {
            return try {
                News.getNewsGlobal(page, searchType, searchQuery)
            } catch (ex: Exception) {
                ex.printStackTrace()
                arrayListOf()
            }
        }

        fun getNewsSubject(
            page: Int = 1,
            searchType: NewsSearchType? = null,
            searchQuery: String? = null
        ): ArrayList<NewsSubjectItem> {
            return try {
                News.getNewsSubject(page, searchType, searchQuery)
            } catch (ex: Exception) {
                ex.printStackTrace()
                arrayListOf()
            }
        }

        fun getNewsGlobalDiff(
            source: List<NewsGroupByDate<NewsGlobalItem>>,
            target: ArrayList<NewsGlobalItem>
        ): ArrayList<NewsGlobalItem> {
            val result: ArrayList<NewsGlobalItem> = arrayListOf()

            // Three conditions:
            // - If news group with date not exist in targetItem date.
            // - If news group with date exist but no news found with targetItem.
            // - If news group with date exist, news exist but different.
            target.forEach { targetItem ->
                val newsGroupFound =
                    source.firstOrNull { newsGroupItem ->
                        newsGroupItem.date == targetItem.date &&
                                newsGroupItem.itemList.any {
                                    it.title == targetItem.title &&
                                            it.date == targetItem.date &&
                                            it.contentString == targetItem.contentString
                                }
                    }

                if (newsGroupFound == null)
                    result.add(targetItem)
                else if (newsGroupFound.itemList.firstOrNull { newsItem ->
                        newsItem.title == targetItem.title &&
                                newsItem.date == targetItem.date &&
                                newsItem.content == newsItem.content
                    } == null) {
                    result.add(targetItem)
                }
            }

            return result
        }

        fun getNewsSubjectDiff(
            source: ArrayList<NewsGroupByDate<NewsSubjectItem>>,
            target: ArrayList<NewsSubjectItem>
        ): ArrayList<NewsSubjectItem> {
            val result: ArrayList<NewsSubjectItem> = arrayListOf()

            // Three conditions:
            // - If news group with date not exist in targetItem date.
            // - If news group with date exist but no news found with targetItem.
            // - If news group with date exist, news exist but different.
            target.forEach { targetItem ->
                val newsGroupFound =
                    source.firstOrNull { newsGroupItem -> newsGroupItem.date == targetItem.date }
                if (newsGroupFound == null)
                    result.add(targetItem)
                else if (newsGroupFound.itemList.firstOrNull { newsItem ->
                        newsItem.title == targetItem.title &&
                                newsItem.date == targetItem.date &&
                                newsItem.content == newsItem.content
                    } == null) {
                    result.add(targetItem)
                }
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
                if (source.firstOrNull { filter -> filter.date == item.date } == null) {
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
                        } else {
                            source.first { itemFilter -> itemFilter.date == item.date }.itemList.add(
                                item
                            )
                        }
                    }
                }
            }

            if (sortGroupByDescending)
                source.sortByDescending { it.date }
            else source.sortBy { it.date }
        }

        fun addAndCheckDuplicateNewsSubject(
            source: ArrayList<NewsGroupByDate<NewsSubjectItem>>,
            target: ArrayList<NewsSubjectItem>,
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
                if (source.firstOrNull { filter -> filter.date == item.date } == null) {
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
                        if (!timestampCount.any { it.date == item.date })
                            timestampCount.add(DateCount(item.date, 0))

                        if (addItemToTop) {
                            source.first { itemFilter -> itemFilter.date == item.date }.itemList.add(
                                timestampCount.first { it.date == item.date }.count, item
                            )
                        } else {
                            source.first { itemFilter -> itemFilter.date == item.date }.itemList.add(
                                item
                            )
                        }

                        timestampCount.first { it.date == item.date }.count += 1
                    }
                }
            }

            if (sortGroupByDescending)
                source.sortByDescending { it.date }
            else source.sortBy { it.date }
        }
    }
}