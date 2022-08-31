package io.zoemeow.dutapp.android.view.account

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import io.zoemeow.dutapp.android.utils.dayOfWeekInString
import io.zoemeow.dutapp.android.utils.getCurrentDayOfWeek
import io.zoemeow.dutapp.android.utils.getDateFromCurrentWeek

@Composable
fun AccountSubWeekList(
    onDayAndWeekChanged: ((Int, Int) -> Unit)? = null,
    padding: PaddingValues? = null
) {
    val weekAdjust = remember { mutableStateOf(0) }
    val dayOfWeek = remember { getDateFromCurrentWeek() }
    val currentDayOfWeek = remember { mutableStateOf(getCurrentDayOfWeek()) }

    LaunchedEffect(weekAdjust.value) {
        dayOfWeek.clear()
        dayOfWeek.addAll(getDateFromCurrentWeek(weekAdjust.value))
    }

    LaunchedEffect(currentDayOfWeek.value) {
        if (onDayAndWeekChanged != null)
            onDayAndWeekChanged(weekAdjust.value, currentDayOfWeek.value)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(padding ?: PaddingValues(0.dp)),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            dayOfWeek.forEach { item ->
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(start = 4.dp, end = 4.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .clickable {
                            currentDayOfWeek.value = dayOfWeek.indexOf(item) + 1
                        },
                    color = (
                            if (dayOfWeek.indexOf(item) + 1 == currentDayOfWeek.value)
                                MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.secondaryContainer
                            )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(5.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = dayOfWeekInString(dayOfWeek.indexOf(item) + 1),
                            style = MaterialTheme.typography.bodySmall,
                        )
                        Spacer(modifier = Modifier.size(3.dp))
                        Text(
                            text = "${item.dayOfMonth}",
                            style = MaterialTheme.typography.bodyLarge,
                        )
                        Text(
                            text = "Tg ${item.monthNumber + 1}",
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                }
            }
        }
        Spacer(modifier = Modifier.size(10.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.weight(1f))
            Surface(
                modifier = Modifier
                    .padding(2.dp)
                    .wrapContentWidth()
                    .wrapContentHeight()
                    .clip(RoundedCornerShape(20.dp))
                    .background(MaterialTheme.colorScheme.secondaryContainer)
                    .clickable {

                    },
                color = MaterialTheme.colorScheme.secondaryContainer,
            ) {
                Text(
                    text = "<",
                    modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 10.dp, bottom = 10.dp),
                )
            }
            Spacer(modifier = Modifier.size(5.dp))
            Surface(
                modifier = Modifier
                    .padding(2.dp)
                    .wrapContentWidth()
                    .wrapContentHeight()
                    .clip(RoundedCornerShape(20.dp))
                    .background(MaterialTheme.colorScheme.secondaryContainer)
                    .clickable {
                        weekAdjust.value = 0
                        currentDayOfWeek.value = getCurrentDayOfWeek()
                    },
                color = MaterialTheme.colorScheme.secondaryContainer,
            ) {
                Text(
                    text = "Today",
                    modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 10.dp, bottom = 10.dp),
                )
            }
            Spacer(modifier = Modifier.size(5.dp))
            Surface(
                modifier = Modifier
                    .padding(2.dp)
                    .wrapContentWidth()
                    .wrapContentHeight()
                    .clip(RoundedCornerShape(20.dp))
                    .clickable {

                    },
                color = MaterialTheme.colorScheme.secondaryContainer,
            ) {
                Text(
                    text = ">",
                    modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 10.dp, bottom = 10.dp),
                )
            }
            Spacer(modifier = Modifier.weight(1f))
        }
    }
}