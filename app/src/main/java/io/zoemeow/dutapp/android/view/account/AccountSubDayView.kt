package io.zoemeow.dutapp.android.view.account

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import io.zoemeow.dutapp.android.viewmodel.MainViewModel

@Composable
fun AccountSubDayView(
    mainViewModel: MainViewModel,
    currentDayOfWeek: Int,
    padding: PaddingValues? = null
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(padding ?: PaddingValues(0.dp)),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.Start
    ) {
        if (mainViewModel.uiStatus.subjectScheduleByDay.size > 0) {
            Text(
                text = "Subject schedule",
                modifier = Modifier.padding(top = 5.dp, bottom = 5.dp)
            )
            mainViewModel.uiStatus.subjectScheduleByDay.forEach { item ->
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                        .padding(top = 5.dp, bottom = 5.dp)
                        .clip(RoundedCornerShape(20.dp)),
                    color = MaterialTheme.colorScheme.secondaryContainer,
                ) {
                    Column(
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.Start,
                        modifier = Modifier.fillMaxWidth()
                            .wrapContentHeight()
                            .padding(10.dp),
                    ) {
                        Text(
                            text = item.name,
                            style = MaterialTheme.typography.titleLarge
                        )
                        item.subjectStudy.scheduleList.filter { schItem -> schItem.dayOfWeek == currentDayOfWeek }.forEach {
                            Text(
                                text = "Lesson: ${it.lesson.start}-${it.lesson.end}, Room: ${it.room}",
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                }
            }
        }
        else {
            Text(
                text = "You don't have any lessons today.",
                modifier = Modifier.padding(top = 5.dp, bottom = 5.dp)
            )
        }
    }
//    LazyColumn(
//        modifier = Modifier
//            .fillMaxWidth()
//            .wrapContentHeight()
//            .padding(padding ?: PaddingValues(0.dp)),
//        verticalArrangement = Arrangement.Center,
//        horizontalAlignment = Alignment.Start
//    ) {
//        if (accountViewModel.subjectScheduleList.firstOrNull { it.subjectExam.date > 0 } != null) {
//            item {
//                Spacer(modifier = Modifier.size(5.dp))
//                Text("Subject examination")
//                Spacer(modifier = Modifier.size(5.dp))
//            }
//            items(accountViewModel.subjectScheduleList.filter { it.subjectExam.date > 0 }) { item ->
//                Column(
//                    verticalArrangement = Arrangement.Center,
//                    horizontalAlignment = Alignment.Start,
//                    modifier = Modifier.fillMaxWidth()
//                        .wrapContentHeight()
//                        .padding(10.dp),
//                ) {
//                    Text(
//                        text = item.name,
//                        style = MaterialTheme.typography.titleLarge
//                    )
//                    Text(
//                        text = dateToString(item.subjectExam.date, "dd/MM/yyyy HH:mm"),
//                        style = MaterialTheme.typography.bodyMedium
//                    )
//                }
//            }
//        }
//    }
}