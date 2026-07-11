package com.okhamzina.routinetaskmanager.featureReminder.presentation.all_reminders.model

import androidx.annotation.StringRes
import com.okhamzina.routinetaskmanager.R
import com.okhamzina.routinetaskmanager.featureReminder.domain.model.ReminderRepeatType

data class ReminderFilter(
    val searchText : String = "",
    val repeatType: ReminderRepeatType? = null
)

data class ReminderRepeatTypeUi(
    val id : Int,
    val repeatType : ReminderRepeatType?,
    @StringRes val repeatTypeNameRes : Int
)

fun ReminderRepeatType.toRepeatTypeUi(): ReminderRepeatTypeUi {
    return when (this){
        ReminderRepeatType.ON_SCHEDULE_PERIOD -> ReminderRepeatTypeUi(
            id = 0,
            repeatType = this,
            repeatTypeNameRes = R.string.repeat_type_on_schedule_period
        )
        ReminderRepeatType.ON_SCHEDULE_CERTAIN -> ReminderRepeatTypeUi(
            id = 1,
            repeatType = this,
            repeatTypeNameRes = R.string.repeat_type_on_schedule_certain
        )
        ReminderRepeatType.DURING_SESSION_PERIOD -> ReminderRepeatTypeUi(
            id = 2,
            repeatType = this,
            repeatTypeNameRes = R.string.repeat_type_during_session
        )
    }
}
