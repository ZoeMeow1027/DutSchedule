package io.zoemeow.dutapp.android.repository

import android.os.Build
import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.google.gson.reflect.TypeToken
import io.zoemeow.dutapp.android.model.SchoolYearItem
import io.zoemeow.dutapp.android.model.appsettings.BackgroundImage
import io.zoemeow.dutapp.android.model.enums.AppTheme
import io.zoemeow.dutapp.android.model.enums.OpenLinkType
import java.io.BufferedReader
import java.io.File
import java.io.Serializable
import javax.inject.Inject
import kotlin.properties.Delegates

class SettingsFileRepository @Inject constructor(private val file: File): Serializable {

    @SerializedName("background_image")
    private var pBackgroundImage: BackgroundImage = BackgroundImage()

    // Android 12 and up can control dynamic color.
    @SerializedName("dynamic_color_enabled")
    private var pDynamicColorEnabled: Boolean = (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)

    // Android 9 and up can control dark mode follow system.
    @SerializedName("app_theme")
    private var pAppTheme: AppTheme = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) AppTheme.FollowSystem else AppTheme.LightMode

    @SerializedName("school_year")
    private var pSchoolYear: SchoolYearItem = SchoolYearItem()

    @SerializedName("black_theme")
    private var pBlackTheme: Boolean = false

    @SerializedName("open_link_type")
    private var pOpenLinkType: OpenLinkType = OpenLinkType.InBuiltIn

    // App background image
    @Transient
    val backgroundImage: MutableState<BackgroundImage> = mutableStateOf(BackgroundImage())

    // App dynamic color
    @Transient
    val dynamicColorEnabled: MutableState<Boolean> = mutableStateOf(true)

    // App mode layout
    @Transient
    val appTheme: MutableState<AppTheme> = mutableStateOf(AppTheme.FollowSystem)

    // School year settings
    @Transient
    val schoolYear: MutableState<SchoolYearItem> = mutableStateOf(SchoolYearItem())

    // App black theme
    @Transient
    val blackTheme: MutableState<Boolean> = mutableStateOf(false)

    // Open link in
    @Transient
    var openLinkType: MutableState<OpenLinkType> = mutableStateOf(OpenLinkType.InBuiltIn)


    @Transient
    private var readyToSave: Boolean = true

    private fun loadSettings() {
        try {
            Log.d("SettingsRead", "Triggered settings reading...")
            readyToSave = false

            val buffer: BufferedReader = file.bufferedReader()
            val inputStr = buffer.use { it.readText() }
            buffer.close()

            val itemType = object : TypeToken<SettingsFileRepository>() {}.type
            val variableItemTemp = Gson().fromJson<SettingsFileRepository>(inputStr, itemType)

            backgroundImage.value = variableItemTemp.pBackgroundImage
            blackTheme.value = variableItemTemp.pBlackTheme
            dynamicColorEnabled.value = variableItemTemp.pDynamicColorEnabled
            appTheme.value = variableItemTemp.pAppTheme
            schoolYear.value = variableItemTemp.pSchoolYear
            openLinkType.value = variableItemTemp.pOpenLinkType
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

            pBackgroundImage = backgroundImage.value
            pBlackTheme = blackTheme.value
            pDynamicColorEnabled = dynamicColorEnabled.value
            pAppTheme = appTheme.value
            pSchoolYear = schoolYear.value
            pOpenLinkType = openLinkType.value

            try {
                val str = Gson().toJson(this)
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