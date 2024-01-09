package io.zoemeow.dutschedule.ui.component.settings.dialog

import android.content.Context
import android.os.Build
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.zoemeow.dutschedule.activity.PermissionRequestActivity
import io.zoemeow.dutschedule.activity.SettingsActivity
import io.zoemeow.dutschedule.model.permissionrequest.PermissionList
import io.zoemeow.dutschedule.model.settings.BackgroundImageOption
import io.zoemeow.dutschedule.ui.component.base.DialogBase
import io.zoemeow.dutschedule.ui.component.base.DialogRadioButton

@Composable
fun SettingsActivity.DialogAppBackgroundSettings(
    context: Context,
    isVisible: Boolean = false,
    value: BackgroundImageOption,
    onDismiss: () -> Unit,
    onValueChanged: (BackgroundImageOption) -> Unit
) {
    DialogBase(
        modifier = Modifier
            .fillMaxWidth()
            .padding(25.dp),
        title = "App background",
        isVisible = isVisible,
        canDismiss = true,
        isTitleCentered = true,
        dismissClicked = {
            onDismiss()
        },
        content = {
            Column(
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.Top,
                modifier = Modifier.fillMaxWidth(),
                content = {
                    DialogRadioButton(
                        title = "None",
                        selected = value == BackgroundImageOption.None,
                        onClick = {
                            onDismiss()
                            onValueChanged(BackgroundImageOption.None)
                        }
                    )
                    DialogRadioButton(
                        title = String.format(
                            "Your current wallpaper%s",
                            when {
                                // TODO: This isn't unavailable for Android 14
                                (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) -> {
                                    "\n(This option is unavailable on Android 14)"
                                }
                                // Permission is not granted.
                                (!PermissionRequestActivity.isPermissionGranted(
                                    PermissionList.PERMISSION_MANAGE_EXTERNAL_STORAGE,
                                    context = context
                                )) -> {
                                    "\n(You need to grant access all file permission)"
                                }
                                // Else, no exception
                                else -> { "" }
                            }
                        ),
                        selected = value == BackgroundImageOption.YourCurrentWallpaper,
                        onClick = {
                            val compSdk = Build.VERSION.SDK_INT < Build.VERSION_CODES.UPSIDE_DOWN_CAKE
                            val compPer = PermissionRequestActivity.isPermissionGranted(
                                PermissionList.PERMISSION_MANAGE_EXTERNAL_STORAGE,
                                context = context
                            )
                            if (compSdk && compPer) {
                                onDismiss()
                                onValueChanged(BackgroundImageOption.YourCurrentWallpaper)
                            }
                        }
                    )
                    DialogRadioButton(
                        title = "Choose a image from media",
                        selected = value == BackgroundImageOption.PickFileFromMedia,
                        onClick = {
                            onDismiss()
                            onValueChanged(BackgroundImageOption.PickFileFromMedia)
                        }
                    )
                }
            )
        },
        actionButtons = {
            TextButton(
                onClick = onDismiss,
                content = { Text("Cancel") },
                modifier = Modifier.padding(start = 8.dp),
            )
        }
    )
}