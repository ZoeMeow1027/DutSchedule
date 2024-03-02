package io.zoemeow.dutschedule.ui.component.main.notification

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.zoemeow.dutschedule.model.NotificationHistory
import io.zoemeow.dutschedule.utils.CustomDateUtil
import io.zoemeow.dutschedule.utils.getRandomString

@Composable
fun NotificationItem(
    modifier: Modifier = Modifier,
    item: NotificationHistory,
    showDate: Boolean = false,
    onClear: (() -> Unit)? = null,
    opacity: Float = 1f
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(5.dp),
        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = opacity),
        content = {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 6.dp, vertical = 3.dp),
                verticalAlignment = Alignment.CenterVertically,
                content = {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 5.dp)
                            .weight(0.9f),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.Start
                    ) {
                        if (showDate) {
                            Text(
                                CustomDateUtil.unixToDuration(item.timestamp),
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.padding(bottom = 5.dp)
                            )
                        }
                        Text(
                            item.title,
                            style = MaterialTheme.typography.titleSmall,
                            overflow = TextOverflow.Ellipsis,
                            maxLines = 2
                        )
                        Spacer(modifier = Modifier.size(3.dp))
                        Text(
                            item.description,
                            style = MaterialTheme.typography.bodySmall,
                            overflow = TextOverflow.Ellipsis,
                            maxLines = 3
                        )
                    }
                    Column(
                        modifier = Modifier.weight(0.10f),
                        content = {
                            IconButton(
                                modifier = Modifier.size(36.dp),
                                onClick = { onClear?.let { it() } }
                            ) {
                                Icon(Icons.Default.Clear, "")
                            }
                        }
                    )
                }
            )
        }
    )
}

@Preview
@Composable
private fun Preview1() {
    val notificationHistory = NotificationHistory(
        id = getRandomString(32),
        title = "News global",
        description = "V/v Xét giao Đồ án tốt nghiệp học kỳ 2/23-24",
        tag = 1,
        timestamp = 1708534800000,
        parameters = mapOf(),
        isRead = false
    )
    NotificationItem(
        item = notificationHistory
    )
}

@Preview
@Composable
private fun Preview2() {
    val notificationHistory = NotificationHistory(
        id = getRandomString(32),
        title = "Thầy Lê Kim Hùng thông báo đến lớp: Phương pháp luận nghiên cứu khoa học [20.Nh29]",
        description = "Chiều mai (thứ sáu, 23/2) thầy Hùng bận việc từ 16.00 nên ngày mai ta nghỉ tiết 9-10 (HP PPNCKH). Ta còn nhiều tuần để bù (báo các em biết).",
        tag = 1,
        timestamp = 1708534800000,
        parameters = mapOf(),
        isRead = false
    )
    NotificationItem(
        item = notificationHistory
    )
}