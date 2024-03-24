package io.zoemeow.dutschedule.model.news

data class NewsGlobalItem(
    var updated: Boolean = false
) : io.dutwrapper.dutwrapper.model.news.NewsGlobalItem() {
    fun update(newsItem: io.dutwrapper.dutwrapper.model.news.NewsGlobalItem) {
        if (this.title == newsItem.title && this.date == newsItem.date) {
            this.updated = true
        }

        this.title = newsItem.title
        this.content = newsItem.content
        this.contentString = newsItem.contentString
        this.links = newsItem.links
        this.date = newsItem.date
    }
}