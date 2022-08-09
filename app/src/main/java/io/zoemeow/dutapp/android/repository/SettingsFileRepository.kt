package io.zoemeow.dutapp.android.repository

import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.zoemeow.dutapp.android.model.account.SchoolYearItem
import io.zoemeow.dutapp.android.model.appsettings.AppSettings
import io.zoemeow.dutapp.android.model.appsettings.BackgroundImage
import io.zoemeow.dutapp.android.model.enums.AppTheme
import io.zoemeow.dutapp.android.model.enums.OpenLinkType
import java.io.BufferedReader
import java.io.File
import java.io.Serializable
import javax.inject.Inject

class SettingsFileRepository @Inject constructor(
    @Transient private val file: File
): Serializable {
    // App mode layout
    @Transient
    val appTheme: MutableState<AppTheme> = mutableStateOf(AppTheme.FollowSystem)

    // App background image
    @Transient
    val backgroundImage: MutableState<BackgroundImage> = mutableStateOf(BackgroundImage())

    // App dynamic color
    @Transient
    val dynamicColorEnabled: MutableState<Boolean> = mutableStateOf(true)

    // School year settings
    @Transient
    val schoolYear: MutableState<SchoolYearItem> = mutableStateOf(SchoolYearItem())

    // App black theme
    @Transient
    val blackTheme: MutableState<Boolean> = mutableStateOf(false)

    // Open link in
    @Transient
    var openLinkType: MutableState<OpenLinkType> = mutableStateOf(OpenLinkType.InCustomTabs)

    @Transient
    private var readyToSave: Boolean = false

    fun loadSettings() {
        try {
            Log.d("SettingsRead", "Triggered settings reading...")
            readyToSave = false

            val buffer: BufferedReader = file.bufferedReader()
            val inputStr = buffer.use { it.readText() }
            buffer.close()

            val dataFromFile = Gson().fromJson<AppSettings>(
                inputStr,
                (object: TypeToken<AppSettings>() {}.type)
            )

            appTheme.value = dataFromFile.appTheme
            backgroundImage.value = dataFromFile.backgroundImage
            blackTheme.value = dataFromFile.blackThemeEnabled
            dynamicColorEnabled.value = dataFromFile.dynamicColorEnabled
            schoolYear.value = dataFromFile.schoolYear
            openLinkType.value = dataFromFile.builtInBrowserOpenLinkType
        }
        catch (ex: Exception) {
            ex.printStackTrace()
        }
        finally {
            readyToSave = true
            saveSettings()
        }
    }

    fun saveSettings() {
        if (readyToSave) {
            Log.d("SettingsWrite", "Triggered settings writing...")

            try {
                val data = AppSettings(
                    appTheme = appTheme.value,
                    backgroundImage = backgroundImage.value,
                    blackThemeEnabled = blackTheme.value,
                    dynamicColorEnabled = dynamicColorEnabled.value,
                    schoolYear = schoolYear.value,
                    builtInBrowserOpenLinkType = openLinkType.value
                )

                val str = Gson().toJson(data)
                file.writeText(str)
            }
            catch (ex: Exception) {
                ex.printStackTrace()
            }
        }
    }

    init {
        loadSettings()
    }
}