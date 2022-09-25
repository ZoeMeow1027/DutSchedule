package io.zoemeow.subjectnotifier.model.account

import io.zoemeow.dutapi.objects.accounts.AccountInformation
import io.zoemeow.dutapi.objects.accounts.SubjectFeeItem
import io.zoemeow.dutapi.objects.accounts.SubjectScheduleItem
import java.io.Serializable

data class AccountCache(
    val subjectScheduleList: ArrayList<SubjectScheduleItem> = arrayListOf(),
    val subjectFeeList: ArrayList<SubjectFeeItem> = arrayListOf(),
    var accountInformation: AccountInformation? = null,
) : Serializable