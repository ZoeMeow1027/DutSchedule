package io.zoemeow.dutschedule.service

import android.content.Intent
import com.google.gson.Gson
import io.dutwrapperlib.dutwrapper.objects.enums.LessonStatus
import io.dutwrapperlib.dutwrapper.objects.news.NewsGlobalItem
import io.dutwrapperlib.dutwrapper.objects.news.NewsSubjectItem
import io.zoemeow.dutschedule.activity.PermissionRequestActivity
import io.zoemeow.dutschedule.model.ProcessState
import io.zoemeow.dutschedule.model.news.NewsGroupByDate
import io.zoemeow.dutschedule.model.permissionrequest.PermissionList
import io.zoemeow.dutschedule.model.settings.AppSettings
import io.zoemeow.dutschedule.model.settings.SubjectCode
import io.zoemeow.dutschedule.repository.DutNewsRepository
import io.zoemeow.dutschedule.repository.FileModuleRepository
import io.zoemeow.dutschedule.util.AppUtils
import io.zoemeow.dutschedule.util.CustomDateUtils
import io.zoemeow.dutschedule.util.NotificationsUtils

class NewsService : BaseService(
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
        // - 2: Get specific page by page number (in page) and keep cache and current page number.
        // - 3: Clear cache, reset and fetch page 1.
        // Apply for fetchglobal and fetchsubject.
        val fetchType = intent?.getIntExtra("news.service.variable.fetchtype", 0)
        // Page to fetch news.
        // Apply for fetchglobal and fetchsubject with fetch action 1, 2.
        val page = intent?.getIntExtra("news.service.variable.page", 1)
        // Fetch full news?
        // Apply for fetchglobal, fetchsubject, fetchall and fetchallinbackground.
        val fetchFullNews = intent?.getBooleanExtra("news.service.variable.fetchfullnews", false)

        when (intent?.action) {
            "news.service.action.getnewscache" -> {
                getNewsCache()
            }
            "news.service.action.fetchglobal" -> {
                fetchNewsGlobal(
                    newsPageType = fetchType ?: 0,
                    page = page ?: 1
                )
            }
            "news.service.action.fetchsubject" -> {
                fetchNewsSubject(
                    newsPageType = fetchType ?: 0,
                    page = page ?: 1
                )
            }
            "news.service.action.fetchall" -> {
                fetchNewsGlobal()
                fetchNewsSubject()
            }
            "news.service.action.fetchallinbackground" -> {
                fetchNewsGlobal(notify = true)
                fetchNewsSubject(notify = true)
            }
            else -> {}
        }
    }

    private fun getNewsCache() {
        try {
            sendBroadcast(
                processState = ProcessState.Successful,
                dataType = "news.global",
                data = file.getCacheNewsGlobal().newsListByDate,
                pageSet = file.getCacheNewsGlobal().pageCurrent
            )
            sendBroadcast(
                processState = ProcessState.Successful,
                dataType = "news.subject",
                data = file.getCacheNewsSubject().newsListByDate,
                pageSet = file.getCacheNewsSubject().pageCurrent
            )
        } catch (_: Exception) {
            sendBroadcast<ArrayList<NewsGroupByDate<NewsGlobalItem>>>(
                processState = ProcessState.Failed
            )
            sendBroadcast<ArrayList<NewsGroupByDate<NewsSubjectItem>>>(
                processState = ProcessState.Failed
            )
        }
    }

    private fun fetchNewsGlobal(
        notify: Boolean = false,
        newsPageType: Int = 0,
        page: Int = 1
    ) {
        try {
            sendBroadcast<ArrayList<NewsGroupByDate<NewsGlobalItem>>>(
                processState = ProcessState.Running,
                dataType = "news.global",
                data = null
            )

            // Get news cache
            val newsCache = file.getCacheNewsGlobal()

            // Get news from internet
            val newsFromInternet = DutNewsRepository.getNewsGlobal(
                page = when (newsPageType) {
                    0 -> newsCache.pageCurrent
                    2 -> page
                    1, 3 -> 1
                    else -> 1
                }
            )

            // If requested, clear cache
            if (newsPageType == 3) {
                newsCache.newsListByDate.clear()
            }

            val newsDiff = DutNewsRepository.getNewsGlobalDiff(
                source = newsCache.newsListByDate,
                target = newsFromInternet,
            )

            DutNewsRepository.addAndCheckDuplicateNewsGlobal(
                source = newsCache.newsListByDate,
                target = newsDiff,
                addItemToTop = newsPageType != 0
            )

            when (newsPageType) {
                0 -> {
                    newsCache.pageCurrent += 1
                }
                1 -> {
                    if (newsCache.pageCurrent <= 1)
                        newsCache.pageCurrent += 1
                }
                3 -> {
                    newsCache.pageCurrent = 2
                }
            }
            file.saveCacheNewsGlobal(newsCache)

            // Notify
            if (newsDiff.isEmpty() || !notify) {
                return
            }

            if (!PermissionRequestActivity.isPermissionGranted(PermissionList.PERMISSION_NOTIFICATION, this)) {
                return
            }

            newsDiff.forEach { newsItem ->
                NotificationsUtils.showNewsNotification(
                    context = this,
                    channel_id = "notification.id.news.global",
                    news_md5 = AppUtils.getMD5("${newsItem.date}_${newsItem.title}"),
                    news_title = "News Global",
                    news_description = newsItem.title,
                    data = newsItem
                )
            }

            sendBroadcast<ArrayList<NewsGroupByDate<NewsGlobalItem>>>(
                processState = ProcessState.Successful,
                dataType = "news.global",
                data = null
            )
        } catch (_: Exception) {
            sendBroadcast<ArrayList<NewsGroupByDate<NewsGlobalItem>>>(
                processState = ProcessState.Failed,
                dataType = "news.global",
                data = null
            )
        }
    }

    private fun fetchNewsSubject(
        notify: Boolean = false,
        newsPageType: Int = 0,
        page: Int = 1
    ) {
        try {
            sendBroadcast<ArrayList<NewsGroupByDate<NewsSubjectItem>>>(
                processState = ProcessState.Running,
                dataType = "news.subject",
                data = null
            )

            // Get news cache
            val newsCache = file.getCacheNewsSubject()

            // Get news from internet
            val newsFromInternet = DutNewsRepository.getNewsSubject(
                page = when (newsPageType) {
                    0 -> newsCache.pageCurrent
                    2 -> page
                    1, 3 -> 1
                    else -> 1
                }
            )

            // If requested, clear cache
            if (newsPageType == 3) {
                newsCache.newsListByDate.clear()
            }

            val newsDiff = DutNewsRepository.getNewsSubjectDiff(
                source = newsCache.newsListByDate,
                target = newsFromInternet,
            )

            DutNewsRepository.addAndCheckDuplicateNewsSubject(
                source = newsCache.newsListByDate,
                target = newsDiff,
                addItemToTop = newsPageType != 0
            )

            when (newsPageType) {
                0 -> {
                    newsCache.pageCurrent += 1
                }
                1 -> {
                    if (newsCache.pageCurrent <= 1)
                        newsCache.pageCurrent += 1
                }
                3 -> {
                    newsCache.pageCurrent = 2
                }
            }
            file.saveCacheNewsSubject(newsCache)

            // Notify
            if (newsDiff.isEmpty() || !notify) {
                return
            }

            if (!PermissionRequestActivity.isPermissionGranted(PermissionList.PERMISSION_NOTIFICATION, this)) {
                return
            }

            newsDiff.forEach { newsItem ->
                // Default value is false.
                var notify = false
                // If enabled news filter, do following.

                // If filter was empty -> Not set -> All news -> Enable notify.
                if (settings.newsFilterList.isEmpty()) {
                    notify = true
                }
                // If a news in filter list -> Enable notify.
                else if (settings.newsFilterList.any { source ->
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
                ) notify = true

                // If no notify, continue with return@forEach.
                if (!notify)
                    return@forEach

                val notifyTitle = when (newsItem.lessonStatus) {
                    LessonStatus.Leaving -> {
                        String.format(
                            "New %s notification from %s",
                            "Leaving",
                            newsItem.lecturerName
                        )
                    }
                    LessonStatus.MakeUp -> {
                        String.format(
                            "New %s notification from %s",
                            "Make up",
                            newsItem.lecturerName
                        )
                    }
                    else -> {
                        String.format(
                            "New notification from %s",
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
                    "${
                        String.format(
                            "Applied to: %s",
                            affectedClassrooms
                        )
                    }\n"
                )
                // Date and lessons
                if (
                    newsItem.lessonStatus == LessonStatus.Leaving ||
                    newsItem.lessonStatus == LessonStatus.MakeUp
                ) {
                    // Date
                    notifyContentList.add(
                        String.format(
                            "On date: %s",
                            CustomDateUtils.dateToString(newsItem.affectedDate, "dd/MM/yyyy")
                        )
                    )
                    // Lessons
                    notifyContentList.add(
                        String.format(
                            "Lesson that will %s: %s",
                            when (newsItem.lessonStatus) {
                                LessonStatus.Leaving -> "Leaving"
                                LessonStatus.MakeUp -> "Make up"
                                else -> ""
                            },
                            if (newsItem.affectedLesson != null) newsItem.affectedLesson.toString() else "(unknown)",
                        )
                    )
                    // Make-up room
                    if (newsItem.lessonStatus == LessonStatus.MakeUp) {
                        // Make up in room
                        notifyContentList.add(
                            String.format(
                                "In Room: %s",
                                newsItem.affectedRoom
                            )
                        )
                    }
                } else {
                    notifyContentList.add(newsItem.contentString)
                }

                NotificationsUtils.showNewsNotification(
                    context = this,
                    channel_id = "notification.id.news.subject",
                    news_md5 = AppUtils.getMD5("${newsItem.date}_${newsItem.title}"),
                    news_title = notifyTitle,
                    news_description = notifyContentList.joinToString("\n"),
                    data = newsItem
                )
            }

            sendBroadcast<ArrayList<NewsGroupByDate<NewsSubjectItem>>>(
                processState = ProcessState.Successful,
                dataType = "news.subject",
                data = null
            )
        } catch (_: Exception) {
            sendBroadcast<ArrayList<NewsGroupByDate<NewsSubjectItem>>>(
                processState = ProcessState.Failed,
                dataType = "news.subject",
                data = null
            )
        }
    }

    private fun <T> sendBroadcast(
        processState: ProcessState,
        dataType: String? = null,
        data: T? = null,
        pageSet: Int = 1
    ) {
        Intent("news.receiver").apply {
            this.putExtra("news.receiver.status", processState.value)
            this.putExtra("news.receiver.datatype", dataType)
            this.putExtra(
                "news.receiver.data",
                data?.let { Gson().toJson(it) } ?: (null as String?)
            )
            this.putExtra("news.receiver.pageset", pageSet)
        }.also {

        }
    }

    override fun onCompleted(result: ProcessState) {
        stopSelf()
    }

    override fun onDestroying() {

    }
}