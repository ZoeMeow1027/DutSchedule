package io.zoemeow.dutapp.android.viewmodel

import android.Manifest
import android.app.Activity
import android.app.WallpaperManager
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.core.content.ContextCompat
import io.zoemeow.dutapi.objects.AccountInformation
import io.zoemeow.dutapi.objects.NewsGlobalItem
import io.zoemeow.dutapi.objects.SubjectFeeItem
import io.zoemeow.dutapi.objects.SubjectScheduleItem
import io.zoemeow.dutapp.android.model.ProcessState
import io.zoemeow.dutapp.android.model.enums.BackgroundImageType
import io.zoemeow.dutapp.android.model.enums.LoginState
import io.zoemeow.dutapp.android.model.news.NewsGroupByDate
import io.zoemeow.dutapp.android.utils.getCurrentDayOfWeek
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class UIStatus {
    // Main Activity
    val pMainActivity: MutableState<Activity?> = mutableStateOf(null)

    // Main Activity's SnackBar State.
    private lateinit var pMainActivitySnackBarHostState: SnackbarHostState

    // Drawable and painter for background image
    val mainActivityBackgroundDrawable: MutableState<Drawable?> = mutableStateOf(null)

    fun setActivity(activity: Activity) {
        pMainActivity.value = activity
    }

    fun getSnackBarState(): SnackbarHostState {
        return pMainActivitySnackBarHostState
    }

    fun setSnackBarState(value: SnackbarHostState) {
        pMainActivitySnackBarHostState = value
    }

    /**
     * Show message in snack bar in MainActivity Scaffold.
     *
     * @param msg Message to show
     */
    fun showSnackBarMessage(msg: String, forceCloseOld: Boolean = false) {
        if (!this::pMainActivitySnackBarHostState.isInitialized)
            return

        CoroutineScope(Dispatchers.Main).launch {
            if (forceCloseOld)
                pMainActivitySnackBarHostState.currentSnackbarData?.dismiss()
            pMainActivitySnackBarHostState.showSnackbar(msg)
        }
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
                } else throw Exception("Missing permission: READ_EXTERNAL_STORAGE")
            }
            // Otherwise set to null
            else mainActivityBackgroundDrawable.value = null
        } catch (ex: Exception) {
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
     * 4: Account Information
     */
    val accountCurrentPage: MutableState<Int> = mutableStateOf(0)

    val accountCurrentDayOfWeek: MutableState<Int> = mutableStateOf(getCurrentDayOfWeek())

    lateinit var scope: CoroutineScope

    // News UI area ================================================================================
    lateinit var newsLazyListGlobalState: LazyListState
    lateinit var newsLazyListSubjectState: LazyListState

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

    val newsItemChosenGlobal: MutableState<NewsGlobalItem?> = mutableStateOf(null)
    val newsItemChosenSubject: MutableState<NewsGlobalItem?> = mutableStateOf(null)

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
    // =============================================================================================


    // News Data area ==============================================================================
    /**
     * Check if a progress for get news global is running.
     */
    val procNewsGlobal: MutableState<ProcessState> = mutableStateOf(ProcessState.NotRanYet)

    /**
     * Current News Global page.
     */
    val newsGlobalPageCurrent: MutableState<Int> = mutableStateOf(1)

    /**
     * Current News Global list.
     */
    val listNewsGlobalByDate: SnapshotStateList<NewsGroupByDate<NewsGlobalItem>> = mutableStateListOf()

    /**
     * Check if a progress for get news subject is running.
     */
    val procNewsSubject: MutableState<ProcessState> = mutableStateOf(ProcessState.NotRanYet)

    /**
     * Current News Subject page.
     */
    val newsSubjectPageCurrent: MutableState<Int> = mutableStateOf(1)

    /**
     * Current News Subject list.
     */
    val listNewsSubjectByDate: SnapshotStateList<NewsGroupByDate<NewsGlobalItem>> = mutableStateListOf()
    // =============================================================================================


    // Account UI area =============================================================================
    /**
     * Gets or sets your current username (get from account information).
     */
    val username: MutableState<String> = mutableStateOf("")

    /**
     * Gets or sets if your account is logged in.
     */
    val loginState: MutableState<LoginState> = mutableStateOf(LoginState.NotLoggedIn)

    /**
     * Gets or sets if you are logging in.
     */
    val procAccLogin: MutableState<ProcessState> = mutableStateOf(ProcessState.NotRanYet)

    /**
     * Gets or sets if subject schedule process are running.
     */
    val procAccSubSch: MutableState<ProcessState> = mutableStateOf(ProcessState.NotRanYet)

    /**
     * Gets or sets if subject fee process are running.
     */
    val procAccSubFee: MutableState<ProcessState> = mutableStateOf(ProcessState.NotRanYet)

    /**
     * Gets or sets if account information process are running.
     */
    val procAccInfo: MutableState<ProcessState> = mutableStateOf(ProcessState.NotRanYet)

    /**
     * Gets or sets your current subject schedule list.
     */
    val subjectSchedule: SnapshotStateList<SubjectScheduleItem> = mutableStateListOf()

    /**
     * Gets or sets your current subject schedule list by day you specified.
     */
    val subjectScheduleByDay: SnapshotStateList<SubjectScheduleItem> = mutableStateListOf()

    /**
     * Gets or sets your current subject fee list.
     */
    val subjectFee: SnapshotStateList<SubjectFeeItem> = mutableStateListOf()

    /**
     * Gets or sets your current account information.
     */
    val accountInformation: MutableState<AccountInformation?> = mutableStateOf(null)
    // =============================================================================================
}