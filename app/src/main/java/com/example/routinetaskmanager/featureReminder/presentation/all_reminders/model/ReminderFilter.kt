package com.example.routinetaskmanager.featureReminder.presentation.all_reminders.model

import com.example.routinetaskmanager.featureReminder.domain.model.ReminderRepeatType

data class ReminderFilter(
    val searchText : String = "",
    val repeatType: ReminderRepeatType? = null
)

data class ReminderRepeatTypeUi(
    val id : Int,
    val repeatType : ReminderRepeatType?,
    val repeatTypeName : String
)

fun ReminderRepeatType.toRepeatTypeUi(): ReminderRepeatTypeUi {
    return when (this){
        ReminderRepeatType.ON_SCHEDULE_PERIOD -> ReminderRepeatTypeUi(
            id = 0,
            repeatType = this,
            repeatTypeName =  "On schedule (period)"
        )
        ReminderRepeatType.ON_SCHEDULE_CERTAIN -> ReminderRepeatTypeUi(
            id = 1,
            repeatType = this,
            repeatTypeName = "On schedule (certain time)"
        )
        ReminderRepeatType.DURING_SESSION_PERIOD -> ReminderRepeatTypeUi(
            id = 2,
            repeatType = this,
            repeatTypeName = "During session"
        )
        ReminderRepeatType.AFTER_ANOTHER_ACTIVITY -> ReminderRepeatTypeUi(
            id = 3,
            repeatType = this,
            repeatTypeName = "After another activity"
        )
    }
}