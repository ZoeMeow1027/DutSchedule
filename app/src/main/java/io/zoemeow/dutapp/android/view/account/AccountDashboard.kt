package io.zoemeow.dutapp.android.view.account

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import io.zoemeow.dutapp.android.R
import io.zoemeow.dutapp.android.viewmodel.AccountViewModel
import io.zoemeow.dutapp.android.viewmodel.UIStatus

@Composable
fun AccountDashboard(
    accountViewModel: AccountViewModel,
    uiStatus: UIStatus,
    padding: PaddingValues,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top,
                modifier = Modifier
                    .wrapContentWidth()
                    .wrapContentHeight()
                    .clip(CircleShape)
                    .border(1.dp, if (uiStatus.mainActivityIsDarkTheme.value) Color.White else Color.Black, CircleShape)
                    .align(Alignment.Center)
            ) {
                Icon(
                    imageVector = ImageVector.vectorResource(id = R.drawable.ic_baseline_person_24),
                    contentDescription = "Account Avatar",
                    modifier = Modifier
                        .size(64.dp)
                        .align(Alignment.CenterHorizontally)
                        .padding(5.dp),
                    tint = if (uiStatus.mainActivityIsDarkTheme.value) Color.White else Color.Black
                )
            }
        }
        Spacer(modifier = Modifier.size(15.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .padding(top = 5.dp, bottom = 5.dp, start = 10.dp, end = 10.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.Start,
            ) {
                Text(
                    text = "Username: ${accountViewModel.username.value.ifEmpty { "(unknown)" }}",
                    modifier = Modifier.padding(top = 3.dp, bottom = 3.dp)
                )
                Text(
                    text = "Class: ${accountViewModel.accountInformation.value?.schoolClass ?: "(unknown)"}",
                    modifier = Modifier.padding(top = 3.dp, bottom = 3.dp)
                )
                Text(
                    text = "Training Program plan: ${accountViewModel.accountInformation.value?.trainingProgramPlan ?: "(unknown)"}",
                    modifier = Modifier.padding(top = 3.dp, bottom = 3.dp)
                )
            }
        }
        Spacer(modifier = Modifier.size(15.dp))
        Box(
            modifier = Modifier
                .padding(top = 5.dp, bottom = 5.dp, start = 10.dp, end = 10.dp)
                .fillMaxWidth()
                .wrapContentHeight()
                .clip(RoundedCornerShape(15.dp))
                .background(MaterialTheme.colorScheme.secondaryContainer)
                .clickable {
                    uiStatus.accountCurrentPage.value = 4
                },
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "View Account Information",
                modifier = Modifier.padding(15.dp),
            )
        }
        Spacer(modifier = Modifier.size(5.dp))
        Box(
            modifier = Modifier
                .padding(top = 5.dp, bottom = 5.dp, start = 10.dp, end = 10.dp)
                .fillMaxWidth()
                .wrapContentHeight()
                .clip(RoundedCornerShape(15.dp))
                .background(MaterialTheme.colorScheme.secondaryContainer)
                .clickable {
                    uiStatus.accountCurrentPage.value = 2
                },
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "View Subject Schedule",
                modifier = Modifier.padding(15.dp),
            )
        }
        Spacer(modifier = Modifier.size(5.dp))
        Box(
            modifier = Modifier
                .padding(top = 5.dp, bottom = 5.dp, start = 10.dp, end = 10.dp)
                .fillMaxWidth()
                .wrapContentHeight()
                .clip(RoundedCornerShape(15.dp))
                .background(MaterialTheme.colorScheme.secondaryContainer)
                .clickable {
                    uiStatus.accountCurrentPage.value = 3
                },
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "View Subject Fee",
                modifier = Modifier.padding(15.dp),
            )
        }
    }
}