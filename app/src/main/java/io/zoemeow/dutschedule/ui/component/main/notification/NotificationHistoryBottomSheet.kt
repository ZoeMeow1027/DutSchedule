package io.zoemeow.dutschedule.ui.component.main.notification

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.zoemeow.dutschedule.R
import io.zoemeow.dutschedule.activity.MainActivity
import io.zoemeow.dutschedule.model.NotificationHistory
import io.zoemeow.dutschedule.utils.CustomDateUtil
import io.zoemeow.dutschedule.utils.getRandomString

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainActivity.NotificationHistoryBottomSheet(
    snackbarHost: (@Composable () -> Unit)? = null,
    visible: Boolean = false,
    sheetState: SheetState,
    itemList: List<NotificationHistory> = listOf(),
    onDismiss: () -> Unit,
    onClick: ((NotificationHistory) -> Unit)? = null,
    onClear: ((NotificationHistory) -> Unit)? = null,
    onClearAll: (() -> Unit)? = null,
    opacity: Float = 1f
) {
    if (visible) {
        ModalBottomSheet(
            onDismissRequest = onDismiss,
            sheetState = sheetState,
        ) {
            MainView(
                snackbarHost = snackbarHost,
                itemList = itemList,
                opacity = opacity,
                onClick = onClick,
                onClear = onClear,
                onClearAll = onClearAll
            )
        }
    }
}

@Composable
private fun MainView(
    itemList: List<NotificationHistory>,
    snackbarHost: (@Composable () -> Unit)? = null,
    onClick: ((NotificationHistory) -> Unit)? = null,
    onClear: ((NotificationHistory) -> Unit)? = null,
    onClearAll: (() -> Unit)? = null,
    opacity: Float = 1f
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.7f)
            .padding(horizontal = 15.dp)
            .verticalScroll(rememberScrollState()),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 3.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Notifications",
                style = MaterialTheme.typography.headlineSmall,
            )
            if (itemList.isNotEmpty()) {
                IconButton(
                    onClick = {
                        onClearAll?.let { it() }
                    },
                    content = {
                        Icon(ImageVector.vectorResource(id = R.drawable.ic_baseline_clear_all_24), "")
                    }
                )
            }
        }
        if (itemList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 15.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("No notifications")
            }
        } else {
            itemList.groupBy { p -> p.timestamp }
                .toSortedMap(compareByDescending { it })
                .forEach(action = { group ->
                    Text(
                        CustomDateUtil.unixToDuration(group.key),
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(top = 5.dp, bottom = 4.dp)
                    )
                    group.value.forEach { item ->
                        NotificationItem(
                            modifier = Modifier.padding(bottom = 5.dp),
                            opacity = opacity,
                            onClick = { onClick?.let { it(item) } },
                            onClear = { onClear?.let { it(item) } },
                            item = item
                        )
                    }
                })
        }
        Spacer(modifier = Modifier.size(9.dp))
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.7f),
        verticalArrangement = Arrangement.Bottom
    ) {
        snackbarHost?.let { it() }
    }
}

@Preview()
@Composable
private fun Preview() {
    MainView(
        opacity = 0.7f,
        itemList = listOf(
            NotificationHistory(
                id = getRandomString(32),
                title = "Thầy ___ thông báo đến lớp: Phương pháp luận nghiên cứu khoa học [20.Nh29]",
                description = "Chiều mai (thứ sáu, 23/2) thầy Hùng bận việc từ 16.00 nên ngày mai ta nghỉ tiết 9-10 (HP PPNCKH). Ta còn nhiều tuần để bù (báo các em biết).",
                tag = 1,
                timestamp = 1708534800000,
                parameters = mapOf(),
                isRead = false
            ),
            NotificationHistory(
                id = getRandomString(32),
                title = "News global",
                description = "V/v Xét giao Đồ án tốt nghiệp học kỳ 2/23-24",
                tag = 1,
                timestamp = 1708534800000,
                parameters = mapOf(),
                isRead = false
            )
        )
    )
}