package io.zoemeow.dutapp.android.viewmodel

import android.Manifest
import android.app.Activity
import android.app.PendingIntent
import android.app.WallpaperManager
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.core.app.ActivityCompat
import androidx.core.app.ComponentActivity
import androidx.core.content.ContextCompat
import dagger.hilt.android.AndroidEntryPoint
import io.zoemeow.dutapi.objects.NewsGlobalItem
import io.zoemeow.dutapp.android.MainActivity
import io.zoemeow.dutapp.android.model.enums.BackgroundImageType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.coroutines.suspendCoroutine

class UIStatus @Inject constructor() {
    companion object {
        private lateinit var instance: UIStatus

        fun getInstance(): UIStatus {
            if (!this::instance.isInitialized)
                instance = UIStatus()

            return instance
        }
    }

    // Main Activity
    private val pMainActivity: MutableState<Activity?> = mutableStateOf(null)

    // Main Activity's SnackBar State.
    private lateinit var pMainActivitySnackBarHostState: SnackbarHostState

    // Drawable and painter for background image
    val mainActivityBackgroundDrawable: MutableState<Drawable?> = mutableStateOf(null)

    fun setMainActivity(activity: Activity) {
        pMainActivity.value = activity
    }

    /**
     *
     */
    fun mainActivityGetSnackBarState(): SnackbarHostState {
        return pMainActivitySnackBarHostState
    }

    /**
     *
     */
    fun mainActivitySetSnackBarState(value: SnackbarHostState) {
        pMainActivitySnackBarHostState = value
    }

    /**
     * Show message in snack bar in MainActivity Scaffold.
     *
     * @param msg Message to show
     */
    fun showSnackBarMessage(msg: String) {
        if (!this::pMainActivitySnackBarHostState.isInitialized)
            return

        scope.launch {
            pMainActivitySnackBarHostState.showSnackbar(msg)
        }
    }

    fun checkPermissionAndReloadAppBackground(
        type: BackgroundImageType,
        onSuccessful: (() -> Unit)? = null,
        onRequested: (() -> Unit)? = null,
    ) {
        if (ContextCompat.checkSelfPermission(pMainActivity.value!!, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            reloadAppBackground(type)
            if (onSuccessful != null) onSuccessful()
        }
        else {
            if (onRequested != null) onRequested()
        }
    }

    fun requestPermissionAppBackground() {
        Log.d("RequestPermission", "Triggered")
        ActivityCompat.requestPermissions(
            pMainActivity.value!!,
            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
            0
        )
    }

    /**
     * Get current drawable for background image. Image loaded will save to backgroundPainter.
     */
    fun reloadAppBackground(
        type: BackgroundImageType,
    ) {
        try {
            // This will get background wallpaper from launcher.
            if (type == BackgroundImageType.FromWallpaper) {
                if (
                    ContextCompat.checkSelfPermission(
                        pMainActivity.value!!,
                        Manifest.permission.READ_EXTERNAL_STORAGE
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    val wallpaperManager = WallpaperManager.getInstance(pMainActivity.value)
                    mainActivityBackgroundDrawable.value = wallpaperManager.drawable
                }
                else throw Exception("Missing permission: READ_EXTERNAL_STORAGE")
            }
            // Otherwise set to null
            else mainActivityBackgroundDrawable.value = null
        }
        catch (ex: Exception) {
            ex.printStackTrace()
        }
    }


    /**
     * Main Activity: Check if current theme is dark mode.
     */
    val mainActivityIsDarkTheme: MutableState<Boolean> = mutableStateOf(false)

    /**
     * Account: Current page of account nav. route.
     *
     * 0: Not logged in,
     * 1: Dashboard,
     * 2: Subject schedule,
     * 3: Subject fee
     */
    val accountCurrentPage: MutableState<Int> = mutableStateOf(0)


    val newsItemChosenGlobal: MutableState<NewsGlobalItem?> = mutableStateOf(null)
    val newsItemChosenSubject: MutableState<NewsGlobalItem?> = mutableStateOf(null)

    lateinit var newsLazyListGlobalState: LazyListState
    lateinit var newsLazyListSubjectState: LazyListState
    lateinit var scope: CoroutineScope

    fun newsDetectItemChosen(needClear: Boolean = false): Boolean {
        var exist = false

        if (newsItemChosenGlobal.value != null) {
            if (needClear) newsItemChosenGlobal.value = null
            exist = true
        }

        if (newsItemChosenSubject.value != null) {
            if (needClear) newsItemChosenSubject.value = null
            exist = true
        }

        return exist
    }

    fun newsScrollListToTop() {
        if (this::scope.isInitialized) {
            if (this::newsLazyListGlobalState.isInitialized) {
                scope.launch {
                    if (!newsLazyListGlobalState.isScrollInProgress)
                        newsLazyListGlobalState.animateScrollToItem(index = 0)
                }
            }
            if (this::newsLazyListSubjectState.isInitialized) {
                scope.launch {
                    if (!newsLazyListSubjectState.isScrollInProgress)
                        newsLazyListSubjectState.animateScrollToItem(index = 0)
                }
            }
        }
    }

    // Just trigger for UI update. This doesn't do anything.
    val triggerUpdateComposeUI: MutableState<Boolean> = mutableStateOf(false)
    fun updateComposeUI() {
        triggerUpdateComposeUI.value = !triggerUpdateComposeUI.value
    }
}