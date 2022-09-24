package io.zoemeow.dutnotify.view.news

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.unit.dp
import io.zoemeow.dutapi.objects.enums.LessonStatus
import io.zoemeow.dutapi.objects.news.NewsSubjectItem
import io.zoemeow.dutnotify.R
import io.zoemeow.dutnotify.ui.custom.NewsScreenCore
import io.zoemeow.dutnotify.utils.DUTDateUtils.Companion.dateToString
import io.zoemeow.dutnotify.utils.DUTDateUtils.Companion.unixToDuration

@Composable
fun NewsDetailsSubject(
    isDarkMode: Boolean = false,
    padding: PaddingValues,
    news: NewsSubjectItem,
    linkClicked: (String) -> Unit
) {
    @Composable
    fun CustomDivider() {
        Spacer(modifier = Modifier.size(10.dp))
        Divider(
            color = Color.Gray,
            thickness = 1.dp
        )
        Spacer(modifier = Modifier.size(10.dp))
    }

    NewsScreenCore {
        val optionsScrollState = rememberScrollState()
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(optionsScrollState)
        ) {
            Column(
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.Top,
                modifier = Modifier.padding(10.dp)
            ) {
                Text(
                    text = String.format(
                        stringResource(id = R.string.newsdetails_subject_title),
                        news.lecturerName
                    ),
                    style = MaterialTheme.typography.headlineMedium
                )
                Spacer(modifier = Modifier.size(10.dp))
                Text(
                    text = "⏱ ${
                        dateToString(
                            news.date,
                            "dd/MM/yyyy",
                            "UTC"
                        )
                    } (${unixToDuration(news.date)})",
                    style = MaterialTheme.typography.titleLarge,
                )
                // Affecting classrooms.
                Spacer(modifier = Modifier.size(10.dp))
                var affectedClassrooms = ""
                news.affectedClass.forEach { className ->
                    if (affectedClassrooms.isEmpty()) {
                        affectedClassrooms = "\n- ${className.subjectName}"
                    } else {
                        affectedClassrooms += "\n- ${className.subjectName}"
                    }
                    var first = true
                    for (item in className.codeList) {
                        if (first) {
                            affectedClassrooms += " ["
                            first = false
                        } else {
                            affectedClassrooms += ", "
                        }
                        affectedClassrooms += "${item.studentYearId.lowercase()}.${item.classId.uppercase()}"
                    }
                    affectedClassrooms += "]"
                }
                Text(
                    text = String.format(
                        stringResource(id = R.string.notification_newssubject_appliedto),
                        affectedClassrooms
                    ),
                    style = MaterialTheme.typography.titleLarge,
                )
                CustomDivider()
                // Affecting lessons, hour, room.
                if (arrayListOf(
                        LessonStatus.Leaving,
                        LessonStatus.MakeUp
                    ).contains(news.lessonStatus)
                ) {
                    Text(
                        text =
                        String.format(
                            stringResource(id = R.string.newsdetails_subject_status),
                            stringResource(
                                id = when (news.lessonStatus) {
                                    LessonStatus.Leaving -> R.string.newsdetails_subject_statusleaving
                                    LessonStatus.MakeUp -> R.string.newsdetails_subject_statusmakeup
                                    else -> R.string.newsdetails_subject_statusleaving
                                }
                            )
                        ),
                        style = MaterialTheme.typography.bodyLarge,
                    )
                    Spacer(modifier = Modifier.size(5.dp))
                    Text(
                        text = String.format(
                            stringResource(
                                when (news.lessonStatus) {
                                    LessonStatus.Leaving -> R.string.newsdetails_subject_lessonleaving
                                    LessonStatus.MakeUp -> R.string.newsdetails_subject_lessonmakeup
                                    else -> R.string.newsdetails_subject_lessonleaving
                                }
                            ),
                            news.affectedLesson
                        ),
                        style = MaterialTheme.typography.bodyLarge,
                    )
                    Spacer(modifier = Modifier.size(5.dp))
                    Text(
                        text = String.format(
                            stringResource(id = R.string.newsdetails_subject_date),
                            dateToString(news.affectedDate, "dd/MM/yyyy", "UTC")
                        ),
                        style = MaterialTheme.typography.bodyLarge,
                    )
                    if (news.lessonStatus == LessonStatus.MakeUp) {
                        Spacer(modifier = Modifier.size(5.dp))
                        Text(
                            text =
                            String.format(
                                stringResource(id = R.string.newsdetails_subject_room),
                                news.affectedRoom
                            ),
                            style = MaterialTheme.typography.bodyLarge,
                        )
                    }
                } else {
                    val annotatedString = buildAnnotatedString {
                        if (news.contentString != null) {
                            // Parse all string to annotated string.
                            append(news.contentString)
                            // Adjust color for annotated string to follow system mode.
                            addStyle(
                                style = SpanStyle(color = if (isDarkMode) Color.White else Color.Black),
                                start = 0,
                                end = news.contentString.length
                            )
                            // Adjust for detected link.
                            news.links?.forEach {
                                addStringAnnotation(
                                    tag = it.position!!.toString(),
                                    annotation = it.url!!,
                                    start = it.position,
                                    end = it.position + it.text!!.length
                                )
                                addStyle(
                                    style = SpanStyle(color = Color(0xff64B5F6)),
                                    start = it.position,
                                    end = it.position + it.text.length
                                )
                            }
                        }
                    }
                    SelectionContainer {
                        ClickableText(
                            text = annotatedString,
                            style = MaterialTheme.typography.bodyLarge,
                            onClick = {
                                try {
                                    news.links?.forEach { item ->
                                        annotatedString
                                            .getStringAnnotations(
                                                item.position!!.toString(),
                                                it,
                                                it
                                            )
                                            .firstOrNull()
                                            ?.let { url ->
                                                var urlTemp = url.toString()
                                                urlTemp =
                                                    urlTemp.replace(
                                                        "http://",
                                                        "http://",
                                                        ignoreCase = true
                                                    )
                                                urlTemp = urlTemp.replace(
                                                    "https://",
                                                    "https://",
                                                    ignoreCase = true
                                                )
                                                linkClicked(urlTemp)
                                            }
                                    }
                                } catch (_: Exception) {
                                    // TODO: Exception for can't open link here!
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}