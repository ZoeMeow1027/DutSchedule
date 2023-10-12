package io.zoemeow.dutschedule.ui.component.account

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp

@Composable
fun AccountInfoBanner(
    padding: PaddingValues,
    isLoading: Boolean = false,
    username: String? = null,
    schoolClass: String? = null,
    trainingProgramPlan: String? = null,
) {
    Surface(
        modifier = Modifier
            .padding(padding)
            .clip(RoundedCornerShape(7.dp)),
        color = MaterialTheme.colorScheme.secondaryContainer,
        content = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .padding(10.dp),
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.Top,
                content = {
                    if (isLoading) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .wrapContentHeight(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically,
                            content = {
                                CircularProgressIndicator(
                                    modifier = Modifier.padding(vertical = 30.dp)
                                )
                            }
                        )
                    }
                    else {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .wrapContentHeight(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.Top,
                            content = {
                                Text(
                                    text = "Basic account information",
                                    style = MaterialTheme.typography.titleLarge,
                                    modifier = Modifier.padding(bottom = 10.dp),
                                )
                            }
                        )
                        OutlinedTextBoxInfo(
                            title = "Username",
                            value = username ?: "(unknown)",
                            modifier = Modifier.fillMaxWidth().padding(bottom = 5.dp)
                        )
                        OutlinedTextBoxInfo(
                            title = "Class",
                            value = schoolClass ?: "(unknown)",
                            modifier = Modifier.fillMaxWidth().padding(bottom = 5.dp)
                        )
                        OutlinedTextBoxInfo(
                            title = "Training program plan",
                            value = trainingProgramPlan ?: "(unknown)",
                            modifier = Modifier.fillMaxWidth().padding(bottom = 5.dp)
                        )
                    }
                }
            )
        },
    )
}

@Composable
private fun OutlinedTextBoxInfo(
    title: String,
    value: String,
    modifier: Modifier,
) {
    OutlinedTextField(
        modifier = modifier,
        value = value,
        readOnly = true,
        onValueChange = { },
        label = { Text(title) }
    )
}