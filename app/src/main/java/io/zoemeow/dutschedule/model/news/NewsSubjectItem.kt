package io.zoemeow.dutschedule.model.news

data class NewsSubjectItem(
    var updated: Boolean = false
) : io.dutwrapper.dutwrapper.model.news.NewsSubjectItem() {
    fun update(newsItem: io.dutwrapper.dutwrapper.model.news.NewsSubjectItem) {
        this.title = newsItem.title
        this.content = newsItem.content
        this.contentString = newsItem.contentString
        this.links = newsItem.links
        this.date = newsItem.date

        this.affectedClass = newsItem.affectedClass
        this.affectedDate = newsItem.affectedDate
        this.lessonStatus = newsItem.lessonStatus
        this.affectedLesson = newsItem.affectedLesson
        this.affectedRoom = newsItem.affectedRoom
        this.lecturerName = newsItem.lecturerName
        this.lecturerGender = newsItem.lecturerGender

        this.updated = true
    }
}
