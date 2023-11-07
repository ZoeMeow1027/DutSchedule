package io.zoemeow.dutschedule.ui.component.account

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun LoginBannerNotLoggedIn(
    padding: PaddingValues = PaddingValues(0.dp),
    clicked: (() -> Unit)? = null,
    loggedIn: Boolean = false,
    studentName: String? = null,
    studentId: String? = null,
    schoolYear: Int? = null,
) {
    Surface(
        modifier = Modifier.padding(padding),
        color = MaterialTheme.colorScheme.secondaryContainer,
        shape = RoundedCornerShape(7.dp),
        content = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .padding(10.dp)
                    .clickable {
                        clicked?.let { it() }
                    },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start,
                content = {
                    Icon(
                        Icons.Outlined.AccountCircle,
                        "",
                        modifier = Modifier.size(65.dp),
                    )
                    Spacer(modifier = Modifier.size(10.dp))
                    Column(
                        content = {
                            if (!loggedIn) {
                                Text(
                                    "Not logged in",
                                    fontSize = 22.sp,
                                    modifier = Modifier.padding(bottom = 3.dp),
                                )
                                Text("Tap here to sign in")
                            } else {
                                Text(
                                    studentName ?: "(unknown)",
                                    fontSize = 22.sp,
                                    modifier = Modifier.padding(bottom = 3.dp),
                                )
                                Text(
                                    studentId ?: "(unknown)",
                                    modifier = Modifier.padding(bottom = 3.dp),
                                )
                                Text("School year ${schoolYear ?: "(unknown)"}")
                            }
                        }
                    )
                }
            )
        },
    )
}