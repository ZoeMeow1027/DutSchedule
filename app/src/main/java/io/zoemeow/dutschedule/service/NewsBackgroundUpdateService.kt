package io.zoemeow.dutschedule.service

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.google.gson.Gson
import io.dutwrapper.dutwrapper.model.enums.LessonStatus
import io.dutwrapper.dutwrapper.model.enums.NewsType
import io.dutwrapper.dutwrapper.model.news.NewsGlobalItem
import io.dutwrapper.dutwrapper.model.news.NewsSubjectItem
import io.zoemeow.dutschedule.R
import io.zoemeow.dutschedule.activity.PermissionRequestActivity
import io.zoemeow.dutschedule.model.NotificationHistory
import io.zoemeow.dutschedule.model.ProcessState
import io.zoemeow.dutschedule.model.news.NewsFetchType
import io.zoemeow.dutschedule.model.news.NewsGroupByDate
import io.zoemeow.dutschedule.model.settings.AppSettings
import io.zoemeow.dutschedule.model.settings.SubjectCode
import io.zoemeow.dutschedule.repository.DutRequestRepository
import io.zoemeow.dutschedule.repository.FileModuleRepository
import io.zoemeow.dutschedule.utils.CustomDateUtil
import io.zoemeow.dutschedule.utils.CustomDateUtil.Companion.dateUnixToString
import io.zoemeow.dutschedule.utils.CustomDateUtil.Companion.getCurrentDateAndTimeToString
import io.zoemeow.dutschedule.utils.NotificationsUtil
import io.zoemeow.dutschedule.utils.calcMD5

class NewsBackgroundUpdateService : BaseService(
    nNotifyId = "notification.id.service",
    nTitle = "News service is running",
    nContent = "A task is running to get news list from sv.dut.udn.vn. This might take a few minutes..."
) {
    private lateinit var file: FileModuleRepository
    private lateinit var dutRequestRepository: DutRequestRepository
    private lateinit var settings: AppSettings

    override fun onInitialize() {
        file = FileModuleRepository(this)
        settings = file.getAppSettings()
        dutRequestRepository = DutRequestRepository()
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

        // Notify?
        // 0: All, 1: News global only, 2: News subject only, 3: News global and news subject with filter.
        val nofityType = intent?.getIntExtra("news.service.variable.notifytype", 0) ?: 0

        when (intent?.action) {
            "news.service.action.fetchglobal" -> {
                fetchNewsGlobal(
                    notify = nofityType,
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
                    notify = nofityType,
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
                    notify = nofityType,
                    fetchType = NewsFetchType.FirstPage
                )
                fetchNewsSubject(
                    notify = nofityType,
                    fetchType = NewsFetchType.FirstPage
                )

                // Schedule next run
                if (schedule) {
                    scheduleNextRun()
                }
            }
            "news.service.action.fetchallpage1background.skipfirst" -> {
                // Do nothing

                // Schedule next run
                if (schedule) {
                    scheduleNextRun()
                }
            }
            else -> {}
        }
    }

    private fun fetchNewsGlobal(
        notify: Int = 0,
        fetchType: NewsFetchType = NewsFetchType.NextPage
    ) {
        try {
            // Get news cache
            val newsCache = file.getCacheNewsGlobal()

            if (newsCache.lastModifiedDate + (settings.newsBackgroundDuration * 60 * 1000) > System.currentTimeMillis()) {
                throw Exception("Request too fast. Try again later.")
            }

            // Get news from internet
            val newsFromInternet = dutRequestRepository.getNewsGlobal(
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

            newsCache.lastModifiedDate = System.currentTimeMillis()

            file.saveCacheNewsGlobal(newsCache)

            // Check if any news need to be notify here using newsFiltered!
            // If no notification permission, aborting...
            if (!PermissionRequestActivity.checkPermissionNotification(this).isGranted) {
                return
            }

            // TODO: Notify by notify variable...

            // Processing news global notifications for notify here!
            newsFiltered.forEach { newsGroup ->
                newsGroup.itemList.forEach { newsItem ->
                    notifyNewsGlobal(this, newsItem)
                }
            }
            Log.d("NewsBackgroundService", "Done executing function in news global.")
        } catch (ex: Exception) {
            Log.w("NewsBackgroundService", "An error was occurred when executing function in news global.")
            ex.printStackTrace()
        }
    }

    private fun fetchNewsSubject(
        notify: Int = 0,
        fetchType: NewsFetchType = NewsFetchType.NextPage
    ) {
        try {
            // Get news cache
            val newsCache = file.getCacheNewsSubject()

            if (newsCache.lastModifiedDate + (settings.newsBackgroundDuration * 60 * 1000) > System.currentTimeMillis()) {
                throw Exception("Request too fast. Try again later.")
            }

            // Get news from internet
            val newsFromInternet = dutRequestRepository.getNewsSubject(
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

            newsCache.lastModifiedDate = System.currentTimeMillis()

            file.saveCacheNewsSubject(newsCache)

            // Check if any news need to be notify here using newsFiltered!
            // If no notification permission, aborting...
            if (!PermissionRequestActivity.checkPermissionNotification(this).isGranted) {
                return
            }

            // TODO: Notify by notify variable...

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

                    // TODO: If no notify/notify settings is off, continue with return@forEach.
                    // notifyRequired and notify variable

                    if (notifyRequired) {
                        notifyNewsSubject(this, newsItem)
                    }
                }
            }
            Log.d("NewsBackgroundService", "Done executing function in news subject.")
        } catch (ex: Exception) {
            Log.w("NewsBackgroundService", "An error was occurred when executing function in news subject.")
            ex.printStackTrace()
        }
    }

    private fun notifyNewsGlobal(
        context: Context,
        newsItem: NewsGlobalItem
    ) {
        // Add to notification list
        addToNotificationList(
            title = newsItem.title,
            description = newsItem.contentString,
            newsDate = System.currentTimeMillis(),
            type = NewsType.Global,
            jsonData = Gson().toJson(newsItem)
        )

        // Notify here
        NotificationsUtil.showNewsNotification(
            context = context,
            channelId = "notification.id.news.global",
            newsMD5 = "${newsItem.date}_${newsItem.title}".calcMD5(),
            newsTitle = context.getString(R.string.service_newsbackgroundservice_newsglobal_title),
            newsDescription = newsItem.title,
            jsonData = Gson().toJson(newsItem)
        )
    }

    private fun notifyNewsSubject(
        context: Context,
        newsItem: NewsSubjectItem
    ) {
        if (settings.newsBackgroundParseNewsSubject) {
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

            // Title will make announcement about lecturer and subjects
            val notifyTitle = when (newsItem.lessonStatus) {
                LessonStatus.Leaving -> {
                    String.format(
                        context.getString(R.string.service_newsbackgroundservice_newssubject_title_noannouncement),
                        context.getString(R.string.service_newsbackgroundservice_newssubject_title_noannouncement_leaving),
                        newsItem.lecturerName,
                        affectedClassrooms
                    )
                }
                LessonStatus.MakeUp -> {
                    String.format(
                        context.getString(R.string.service_newsbackgroundservice_newssubject_title_noannouncement),
                        context.getString(R.string.service_newsbackgroundservice_newssubject_title_noannouncement_makeup),
                        newsItem.lecturerName,
                        affectedClassrooms
                    )
                }
                else -> {
                    String.format(
                        context.getString(R.string.service_newsbackgroundservice_newssubject_title_announcement),
                        newsItem.lecturerName,
                        affectedClassrooms
                    )
                }
            }

            val notifyContentList = arrayListOf<String>()
            // Date and lessons
            if (
                newsItem.lessonStatus == LessonStatus.Leaving ||
                newsItem.lessonStatus == LessonStatus.MakeUp
            ) {
                // Date & lessons
                notifyContentList.add(
                    String.format(
                        context.getString(R.string.service_newsbackgroundservice_newssubject_date),
                        CustomDateUtil.dateUnixToString(newsItem.affectedDate, "dd/MM/yyyy"),
                        if (newsItem.affectedLesson != null) newsItem.affectedLesson.toString() else "(unknown)"
                    )
                )
                // Make-up room
                if (newsItem.lessonStatus == LessonStatus.MakeUp) {
                    // Make up in room
                    notifyContentList.add(
                        String.format(
                            context.getString(R.string.service_newsbackgroundservice_newssubject_room),
                            newsItem.affectedRoom
                        )
                    )
                }
            } else {
                notifyContentList.add(newsItem.contentString)
            }

            // TODO: Add to notification list - Disabled due to not excluding here
//            addToNotificationList(
//                title = notifyTitle,
//                description = notifyContentList.joinToString("\n"),
//                newsDate = System.currentTimeMillis(),
//                type = NewsType.Subject,
//                jsonData = Gson().toJson(newsItem)
//            )

            // Notify here
            NotificationsUtil.showNewsNotification(
                context = context,
                channelId = "notification.id.news.subject",
                newsMD5 = "${newsItem.date}_${newsItem.title}".calcMD5(),
                newsTitle = notifyTitle,
                newsDescription = notifyContentList.joinToString("\n"),
                jsonData = Gson().toJson(newsItem)
            )
        } else {
            // TODO: Add to notification list - Disabled due to not excluding here
//            addToNotificationList(
//                title = newsItem.title,
//                description = newsItem.contentString,
//                newsDate = System.currentTimeMillis(),
//                type = NewsType.Subject,
//                jsonData = Gson().toJson(newsItem)
//            )

            // Notify here
            NotificationsUtil.showNewsNotification(
                context = context,
                channelId = "notification.id.news.subject",
                newsMD5 = "${newsItem.date}_${newsItem.title}".calcMD5(),
                newsTitle = newsItem.title,
                newsDescription = newsItem.contentString,
                jsonData = Gson().toJson(newsItem)
            )
        }
    }

    private fun addToNotificationList(
        title: String,
        description: String,
        newsDate: Long,
        type: NewsType,
        jsonData: String
    ) {
        // Load notification history
        val cache = file.getNotificationHistory()

        // Create and add to list
        val item = NotificationHistory(
            title = title,
            description = description,
            tag = when (type) {
                NewsType.Global -> 1
                NewsType.Subject -> 2
                else -> 0
            },
            timestamp = newsDate,
            parameters = mapOf(
                "type" to when (type) {
                    NewsType.Global -> "news_global"
                    NewsType.Subject -> "news_subject"
                    else -> ""
                },
                "data" to jsonData
            )
        )
        cache.add(item)

        // Save notification history after add
        file.saveNotificationHistory(cache)

        // Optimal: Clear list
        cache.clear()
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
            pendingIntent = pendingIntent,
            onDone = {
                Log.d(
                    "NewsBackgroundService",
                    String.format(
                        "Scheduled service to run at %s. Next run: %s.",
                        getCurrentDateAndTimeToString("dd/MM/yyyy HH:mm:ss"),
                        dateUnixToString(
                            System.currentTimeMillis() + settings.newsBackgroundDuration * 60 * 1000,
                            "dd/MM/yyyy HH:mm:ss",
                            "GMT+7"
                        )
                    )
                )
            }
        )
    }

    companion object {
        fun getPendingIntentForBackground(context: Context): PendingIntent {
            val intent = Intent(context, NewsBackgroundUpdateService::class.java).also {
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
                    PendingIntent.FLAG_UPDATE_CURRENT
                )
            }

            return pendingIntent
        }

        fun cancelSchedule(
            context: Context,
            onDone: (() -> Unit)? = null
        ) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            alarmManager.cancel(getPendingIntentForBackground(context))
            onDone?.let { it() }
        }
    }
}