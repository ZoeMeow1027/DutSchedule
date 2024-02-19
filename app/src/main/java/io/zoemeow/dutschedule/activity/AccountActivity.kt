package io.zoemeow.dutschedule.activity

import android.content.Context
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import dagger.hilt.android.AndroidEntryPoint
import io.zoemeow.dutschedule.ui.view.account.AccountInformation
import io.zoemeow.dutschedule.ui.view.account.MainView
import io.zoemeow.dutschedule.ui.view.account.SubjectFee
import io.zoemeow.dutschedule.ui.view.account.SubjectInformation
import io.zoemeow.dutschedule.ui.view.account.TrainingResult
import io.zoemeow.dutschedule.ui.view.account.TrainingSubjectResult

@AndroidEntryPoint
class AccountActivity: BaseActivity() {
    @Composable
    override fun OnPreloadOnce() {

    }

    @Composable
    override fun OnMainView(
        context: Context,
        snackBarHostState: SnackbarHostState,
        containerColor: Color,
        contentColor: Color
    ) {
        when (intent.action) {
            "subject_information" -> {
                SubjectInformation(
                    snackBarHostState = snackBarHostState,
                    containerColor = containerColor,
                    contentColor = contentColor
                )
            }
            "subject_fee" -> {
                SubjectFee(
                    snackBarHostState = snackBarHostState,
                    containerColor = containerColor,
                    contentColor = contentColor
                )
            }
            "acc_info" -> {
                AccountInformation(
                    snackBarHostState = snackBarHostState,
                    containerColor = containerColor,
                    contentColor = contentColor
                )
            }
            "acc_training_result" -> {
                TrainingResult(
                    context = context,
                    snackBarHostState = snackBarHostState,
                    containerColor = containerColor,
                    contentColor = contentColor
                )
            }
            "acc_training_result_subjectresult" -> {
                TrainingSubjectResult(
                    snackBarHostState = snackBarHostState,
                    containerColor = containerColor,
                    contentColor = contentColor
                )
            }
            else -> {
                MainView(
                    context = context,
                    snackBarHostState = snackBarHostState,
                    containerColor = containerColor,
                    contentColor = contentColor
                )
            }
        }
    }
}