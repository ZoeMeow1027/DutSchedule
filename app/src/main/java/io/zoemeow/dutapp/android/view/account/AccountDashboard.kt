package io.zoemeow.dutapp.android.view.account

import android.content.Intent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import io.zoemeow.dutapp.android.R

@Composable
fun AccountDashboard(
    padding: PaddingValues,
    logoutRequested: () -> Unit
) {
    val dialogLogoutEnabled = remember { mutableStateOf(false) }

    AccountLogoutDialog(
        enabled = dialogLogoutEnabled,
        logoutRequest = { logoutRequested() }
    )
    Column(
        modifier = Modifier.fillMaxSize().padding(padding),
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
                    .fillMaxWidth()
                    .wrapContentHeight()
            ) {
                Image(
                    imageVector = ImageVector.vectorResource(id = R.drawable.ic_baseline_person_24),
                    contentDescription = "Account Avatar",
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .border(1.dp, Color.Gray, CircleShape)
                        .align(Alignment.CenterHorizontally),
                )
            }
        }
        Spacer(modifier = Modifier.size(15.dp))
        val context = LocalContext.current
        Box(
            modifier = Modifier
                .padding(top = 5.dp, bottom = 5.dp, start = 10.dp, end = 10.dp)
                .fillMaxWidth()
                .wrapContentHeight()
                .clip(RoundedCornerShape(15.dp))
                .background(MaterialTheme.colorScheme.primaryContainer)
                .clickable {

                },
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "View account information",
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
                .background(MaterialTheme.colorScheme.primaryContainer)
                .clickable {
                    val intent = Intent(context, AccountSubjectScheduleActivity::class.java)
                    context.startActivity(intent)
                },
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "View subject schedules",
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
                .background(MaterialTheme.colorScheme.primaryContainer)
                .clickable {
                    val intent = Intent(context, AccountSubjectFeeActivity::class.java)
                    context.startActivity(intent)
                },
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "View subject fees",
                modifier = Modifier.padding(15.dp),
            )
        }
        Spacer(modifier = Modifier.size(15.dp))
        Button(
            onClick = {
                dialogLogoutEnabled.value = true
            },
            content = {
                Text(
                    text = "Logout"
                )
            }
        )
    }
}