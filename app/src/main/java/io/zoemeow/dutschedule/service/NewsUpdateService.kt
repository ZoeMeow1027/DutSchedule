package io.zoemeow.dutschedule.service

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.google.gson.Gson
import io.dutwrapper.dutwrapper.model.enums.LessonStatus
import io.dutwrapper.dutwrapper.model.news.NewsGlobalItem
import io.dutwrapper.dutwrapper.model.news.NewsSubjectItem
import io.zoemeow.dutschedule.activity.PermissionRequestActivity
import io.zoemeow.dutschedule.model.ProcessState
import io.zoemeow.dutschedule.model.news.NewsFetchType
import io.zoemeow.dutschedule.model.news.NewsGroupByDate
import io.zoemeow.dutschedule.model.permissionrequest.PermissionList
import io.zoemeow.dutschedule.model.settings.AppSettings
import io.zoemeow.dutschedule.model.settings.SubjectCode
import io.zoemeow.dutschedule.repository.DutNewsRepository
import io.zoemeow.dutschedule.repository.FileModuleRepository
import io.zoemeow.dutschedule.util.CustomDateUtils
import io.zoemeow.dutschedule.util.NotificationsUtils
import io.zoemeow.dutschedule.util.calcMD5

class NewsUpdateService : BaseService(
    nNotifyId = "notification.id.service",
    nTitle = "News service is running",
    nContent = "A task is running to get news list from sv.dut.udn.vn..."
) {
    private lateinit var file: FileModuleRepository
    private lateinit var settings: AppSettings

    override fun onInitialize() {
        file = FileModuleRepository(this)
        settings = file.getAppSettings()
    }

    override fun doWorkBackground(intent: Intent?) {
        // Fetch action:
        // - 0: Fetch current news by page number and plus 1.
        // - 1: Get page 1 but keep cache and current page number.
        // - 2: Clear cache, reset and fetch page 1.
        // Apply for fetchglobal and fetchsubject.
        val fetchType = intent?.getIntExtra("news.service.variable.fetchtype", 0)
        // Page to fetch news.
        // Apply for fetchglobal and fetchsubject with fetch action 1, 2.
        // val page = intent?.getIntExtra("news.service.variable.page", 1)
        // Fetch full news?
        // Apply for fetchglobal, fetchsubject, fetchall and fetchallinbackground.
        // val fetchFullNews = intent?.getBooleanExtra("news.service.variable.fetchfullnews", false)

        // Schedule for next run
        // Apply for fetchglobal and fetchsubject with fetch action 1
        // val schedule = intent?.getBooleanExtra("news.service.variable.schedulenextrun", false) ?: false
        val schedule = settings.newsBackgroundDuration > 0

        when (intent?.action) {
            "news.service.action.fetchglobal" -> {
                fetchNewsGlobal(
                    notify = true,
                    fetchType = when (fetchType) {
                        0 -> NewsFetchType.NextPage
                        1 -> NewsFetchType.FirstPage
                        2 -> NewsFetchType.ClearAndFirstPage
                        else -> NewsFetchType.NextPage
                    }
                )
            }
            "news.service.action.fetchsubject" -> {
                fetchNewsSubject(
                    notify = true,
                    fetchType = when (fetchType) {
                        0 -> NewsFetchType.NextPage
                        1 -> NewsFetchType.FirstPage
                        2 -> NewsFetchType.ClearAndFirstPage
                        else -> NewsFetchType.NextPage
                    }
                )
            }
            "news.service.action.fetchall" -> {
                fetchNewsGlobal(
                    fetchType = when (fetchType) {
                        0 -> NewsFetchType.NextPage
                        1 -> NewsFetchType.FirstPage
                        2 -> NewsFetchType.ClearAndFirstPage
                        else -> NewsFetchType.NextPage
                    }
                )
                fetchNewsSubject(
                    fetchType = when (fetchType) {
                        0 -> NewsFetchType.NextPage
                        1 -> NewsFetchType.FirstPage
                        2 -> NewsFetchType.ClearAndFirstPage
                        else -> NewsFetchType.NextPage
                    }
                )
            }
            "news.service.action.fetchallpage1background" -> {
                fetchNewsGlobal(
                    notify = true,
                    fetchType = NewsFetchType.FirstPage
                )
                fetchNewsSubject(
                    notify = true,
                    fetchType = NewsFetchType.FirstPage
                )
            }
            else -> {}
        }
        if (schedule) {
            Log.d("NewsService", "Triggered next run")
            scheduleNextRun()
        }
    }

    private fun fetchNewsGlobal(
        notify: Boolean = false,
        fetchType: NewsFetchType = NewsFetchType.NextPage
    ) {
        try {
            // Get news cache
            val newsCache = file.getCacheNewsGlobal()

            // Get news from internet
            val newsFromInternet = DutNewsRepository.getNewsGlobal(
                page = when (fetchType) {
                    NewsFetchType.NextPage -> newsCache.pageCurrent
                    NewsFetchType.FirstPage -> 1
                    NewsFetchType.ClearAndFirstPage -> 1
                }
            )

            // If requested, clear cache
            if (fetchType == NewsFetchType.ClearAndFirstPage) {
                newsCache.newsListByDate.clear()
            }

            // Remove duplicate news to new list
            val newsFiltered = arrayListOf<NewsGroupByDate<NewsGlobalItem>>()
            newsFromInternet.forEach { newsItem ->
                val anyMatch = newsCache.newsListByDate.any { newsSourceGroup ->
                    newsSourceGroup.itemList.any { newsSourceItem ->
                        newsSourceItem.date == newsItem.date
                                && newsSourceItem.title == newsItem.title
                                && newsSourceItem.contentString == newsItem.contentString
                    }
                }

                if (!anyMatch) {
                    // Check if date group exist
                    val groupExist =
                        newsFiltered.any { newsGroupTarget -> newsGroupTarget.date == newsItem.date }
                    if (!groupExist) {
                        val newsGroupNew = NewsGroupByDate(
                            date = newsItem.date,
                            itemList = arrayListOf(newsItem)
                        )
                        newsFiltered.add(newsGroupNew)
                    } else {
                        newsFiltered.first { newsGroupTarget -> newsGroupTarget.date == newsItem.date }
                            .add(newsItem)
                    }
                }
            }

            // Add to current cache
            newsFiltered.forEach { newsGroup ->
                var itemIndex = 0
                newsGroup.itemList.forEach { newsItem ->
                    if (newsCache.newsListByDate.any { group -> group.date == newsItem.date }) {
                        if (fetchType == NewsFetchType.FirstPage) {
                            newsCache.newsListByDate.first { group -> group.date == newsItem.date }
                                .itemList.add(itemIndex, newsItem)
                            itemIndex += 1
                        } else {
                            newsCache.newsListByDate.first { group -> group.date == newsItem.date }
                                .itemList.add(newsItem)
                        }
                    } else {
                        val newsGroupNew = NewsGroupByDate(
                            date = newsItem.date,
                            itemList = arrayListOf(newsItem)
                        )
                        newsCache.newsListByDate.add(newsGroupNew)
                    }
                }
            }
            newsCache.newsListByDate.sortByDescending { group -> group.date }

            when (fetchType) {
                NewsFetchType.NextPage -> {
                    newsCache.pageCurrent += 1
                }
                NewsFetchType.FirstPage -> {
                    if (newsCache.pageCurrent <= 1)
                        newsCache.pageCurrent += 1
                }
                NewsFetchType.ClearAndFirstPage -> {
                    newsCache.pageCurrent = 2
                }
            }
            file.saveCacheNewsGlobal(newsCache)

            // Check if any news need to be notify here using newsFiltered!
            // If no notification permission, aborting...
            if (!PermissionRequestActivity.isPermissionGranted(PermissionList.PERMISSION_NOTIFICATION, this)) {
                return
            }

            // If no need to notify, aborting...
            if (!notify) {
                return
            }

            // Processing news global notifications for notify here!
            newsFiltered.forEach { newsGroup ->
                newsGroup.itemList.forEach { newsItem ->
                    NotificationsUtils.showNewsNotification(
                        context = this,
                        channelId = "notification.id.news.global",
                        newsMD5 = "${newsItem.date}_${newsItem.title}".calcMD5(),
                        newsTitle = "News Global",
                        newsDescription = newsItem.title,
                        jsonData = Gson().toJson(newsItem)
                    )
                }
            }
        } catch (_: Exception) {
        }
    }

    private fun fetchNewsSubject(
        notify: Boolean = false,
        fetchType: NewsFetchType = NewsFetchType.NextPage
    ) {
        try {
            // Get news cache
            val newsCache = file.getCacheNewsSubject()

            // Get news from internet
            val newsFromInternet = DutNewsRepository.getNewsSubject(
                page = when (fetchType) {
                    NewsFetchType.NextPage -> newsCache.pageCurrent
                    NewsFetchType.FirstPage -> 1
                    NewsFetchType.ClearAndFirstPage -> 1
                }
            )

            // If requested, clear cache
            if (fetchType == NewsFetchType.ClearAndFirstPage) {
                newsCache.newsListByDate.clear()
            }

            // Remove duplicate news to new list
            val newsFiltered = arrayListOf<NewsGroupByDate<NewsSubjectItem>>()
            newsFromInternet.forEach { newsItem ->
                val anyMatch = newsCache.newsListByDate.any { newsSourceGroup ->
                    newsSourceGroup.itemList.any { newsSourceItem ->
                        newsSourceItem.date == newsItem.date
                                && newsSourceItem.title == newsItem.title
                                && newsSourceItem.contentString == newsItem.contentString
                    }
                }

                if (!anyMatch) {
                    // Check if date group exist
                    val groupExist =
                        newsFiltered.any { newsGroupTarget -> newsGroupTarget.date == newsItem.date }
                    if (!groupExist) {
                        val newsGroupNew = NewsGroupByDate(
                            date = newsItem.date,
                            itemList = arrayListOf(newsItem)
                        )
                        newsFiltered.add(newsGroupNew)
                    } else {
                        newsFiltered.first { newsGroupTarget -> newsGroupTarget.date == newsItem.date }
                            .add(newsItem)
                    }
                }
            }

            // Add to current cache
            newsFiltered.forEach { newsGroup ->
                var itemIndex = 0
                newsGroup.itemList.forEach { newsItem ->
                    if (newsCache.newsListByDate.any { group -> group.date == newsItem.date }) {
                        if (fetchType == NewsFetchType.FirstPage) {
                            newsCache.newsListByDate.first { group -> group.date == newsItem.date }
                                .itemList.add(itemIndex, newsItem)
                            itemIndex += 1
                        } else {
                            newsCache.newsListByDate.first { group -> group.date == newsItem.date }
                                .itemList.add(newsItem)
                        }
                    } else {
                        val newsGroupNew = NewsGroupByDate(
                            date = newsItem.date,
                            itemList = arrayListOf(newsItem)
                        )
                        newsCache.newsListByDate.add(newsGroupNew)
                    }
                }
            }
            newsCache.newsListByDate.sortByDescending { group -> group.date }

            when (fetchType) {
                NewsFetchType.NextPage -> {
                    newsCache.pageCurrent += 1
                }
                NewsFetchType.FirstPage -> {
                    if (newsCache.pageCurrent <= 1)
                        newsCache.pageCurrent += 1
                }
                NewsFetchType.ClearAndFirstPage -> {
                    newsCache.pageCurrent = 2
                }
            }
            file.saveCacheNewsSubject(newsCache)

            // Check if any news need to be notify here using newsFiltered!
            // If no notification permission, aborting...
            if (!PermissionRequestActivity.isPermissionGranted(PermissionList.PERMISSION_NOTIFICATION, this)) {
                return
            }

            // If no need to notify, aborting...
            if (!notify) {
                return
            }

            // TODO: Processing news subject notifications for notify here!
            newsFiltered.forEach newsGroupForEach@ { newsGroup ->
                newsGroup.itemList.forEach newsItemForEach@ { newsItem ->
                    // Default value is false.
                    var notifyRequired = false
                    // If enabled news filter, do following.

                    // If filter was empty -> Not set -> All news -> Enable notify.
                    if (settings.newsBackgroundFilterList.isEmpty()) {
                        notifyRequired = true
                    }
                    // If a news in filter list -> Enable notify.
                    else if (settings.newsBackgroundFilterList.any { source ->
                            newsItem.affectedClass.any { targetGroup ->
                                targetGroup.codeList.any { target ->
                                    source.isEquals(
                                        SubjectCode(
                                            target.studentYearId,
                                            target.classId,
                                            targetGroup.subjectName
                                        )
                                    )
                                }
                            }
                        }
                    ) notifyRequired = true

                    // If no notify/notify settings is off, continue with return@forEach.
                    if (!notifyRequired || !notify) {
                        return@newsItemForEach
                    }

                    val notifyTitle = when (newsItem.lessonStatus) {
                        LessonStatus.Leaving -> {
                            String.format(
                                "New %s announcement from %s",
                                "Leaving",
                                newsItem.lecturerName
                            )
                        }
                        LessonStatus.MakeUp -> {
                            String.format(
                                "New %s announcement from %s",
                                "Make up",
                                newsItem.lecturerName
                            )
                        }
                        else -> {
                            String.format(
                                "New announcement from %s",
                                newsItem.lecturerName
                            )
                        }
                    }

                    // Affected classrooms
                    var affectedClassrooms = ""
                    newsItem.affectedClass.forEach { className ->
                        if (affectedClassrooms.isEmpty()) {
                            affectedClassrooms = className.subjectName
                        } else {
                            affectedClassrooms += ", ${className.subjectName}"
                        }
                        var first = true
                        for (item in className.codeList) {
                            if (first) {
                                affectedClassrooms += " ("
                                first = false
                            } else {
                                affectedClassrooms += ", "
                            }
                            affectedClassrooms += "${item.studentYearId}.${item.classId}"
                        }
                        affectedClassrooms += ")"
                    }

                    val notifyContentList = arrayListOf<String>()
                    // Affected classrooms
                    notifyContentList.add(
                        String.format(
                            "Subject(s) affected: %s",
                            affectedClassrooms
                        )
                    )
                    // Date and lessons
                    if (
                        newsItem.lessonStatus == LessonStatus.Leaving ||
                        newsItem.lessonStatus == LessonStatus.MakeUp
                    ) {
                        // Date
                        notifyContentList.add(
                            String.format(
                                "Date affected: %s",
                                CustomDateUtils.dateToString(newsItem.affectedDate, "dd/MM/yyyy")
                            )
                        )
                        // Lessons
                        notifyContentList.add(
                            String.format(
                                "Lesson(s) affected: %s",
                                if (newsItem.affectedLesson != null) newsItem.affectedLesson.toString() else "(unknown)",
                            )
                        )
                        // Make-up room
                        if (newsItem.lessonStatus == LessonStatus.MakeUp) {
                            // Make up in room
                            notifyContentList.add(
                                String.format(
                                    "Room will make up: %s",
                                    newsItem.affectedRoom
                                )
                            )
                        }
                    } else {
                        notifyContentList.add(newsItem.contentString)
                    }

                    NotificationsUtils.showNewsNotification(
                        context = this,
                        channelId = "notification.id.news.subject",
                        newsMD5 = "${newsItem.date}_${newsItem.title}".calcMD5(),
                        newsTitle = notifyTitle,
                        newsDescription = when (settings.newsBackgroundParseNewsSubject) {
                            true -> notifyContentList.joinToString("\n")
                            false -> newsItem.contentString
                        },
                        jsonData = Gson().toJson(newsItem)
                    )
                }
            }
        } catch (_: Exception) {
        }
    }

    override fun onCompleted(result: ProcessState) {
        stopSelf()
    }

    override fun onDestroying() { }

    private fun scheduleNextRun() {
        val pendingIntent = getPendingIntentForBackground(this)
        super.scheduleNextRun(
            intervalInMinute = settings.newsBackgroundDuration,
            scheduleStart = null,
            scheduleEnd = null,
            pendingIntent = pendingIntent
        )
    }

    companion object {
        fun getPendingIntentForBackground(context: Context): PendingIntent {
            val intent = Intent(context, NewsUpdateService::class.java).also {
                it.action = "news.service.action.fetchallpage1background"
            }
            val pendingIntent: PendingIntent
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                pendingIntent = PendingIntent.getForegroundService(
                    context,
                    1234,
                    intent,
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        PendingIntent.FLAG_IMMUTABLE
                    }
                    else PendingIntent.FLAG_UPDATE_CURRENT
                )
            } else {
                pendingIntent = PendingIntent.getService(
                    context,
                    1234,
                    intent,
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        PendingIntent.FLAG_IMMUTABLE
                    }
                    else PendingIntent.FLAG_UPDATE_CURRENT
                )
            }

            return pendingIntent
        }

        fun cancelSchedule(
            context: Context
        ) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            alarmManager.cancel(getPendingIntentForBackground(context))
            Log.d("NewsService", "Cancelled run")
        }
    }
}