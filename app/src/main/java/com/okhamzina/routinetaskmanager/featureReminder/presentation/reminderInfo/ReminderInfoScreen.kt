package com.okhamzina.routinetaskmanager.featureReminder.presentation.reminderInfo

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.okhamzina.routinetaskmanager.R
import com.okhamzina.routinetaskmanager.core.presentation.ui.TitleText
import com.okhamzina.routinetaskmanager.core.presentation.ui.image.FullscreenImagePagerDialog
import com.okhamzina.routinetaskmanager.core.presentation.ui.image.ImagePagerWithThumbnail
import com.okhamzina.routinetaskmanager.core.utills.formatDateTimeToWeekDayAndTime
import com.okhamzina.routinetaskmanager.featureReminder.domain.model.NotificationMode
import com.okhamzina.routinetaskmanager.featureReminder.domain.model.OnSchedulePeriodDayRepeat
import com.okhamzina.routinetaskmanager.featureReminder.domain.model.Reminder
import com.okhamzina.routinetaskmanager.featureReminder.domain.model.ReminderImage
import com.okhamzina.routinetaskmanager.featureReminder.domain.model.ReminderRepeatRule
import com.okhamzina.routinetaskmanager.featureReminder.domain.model.RepeatInterval
import com.okhamzina.routinetaskmanager.featureReminder.domain.model.RepeatScheduleMode
import com.okhamzina.routinetaskmanager.featureReminder.domain.model.RepeatUnit
import com.okhamzina.routinetaskmanager.featureReminder.domain.model.TimeWindow
import com.okhamzina.routinetaskmanager.featureReminder.domain.model.WeeklyRepeat
import com.okhamzina.routinetaskmanager.featureReminder.presentation.common.ui.components.ReminderActionButtons
import com.okhamzina.routinetaskmanager.featureReminder.presentation.mapper.toRepeatLabel
import com.okhamzina.routinetaskmanager.featureReminder.presentation.mapper.toShortDetailsLabel
import com.okhamzina.routinetaskmanager.featureReminder.presentation.reminderInfo.model.ReminderInfoIntent
import com.okhamzina.routinetaskmanager.featureReminder.presentation.reminderInfo.model.ReminderInfoUiState
import com.okhamzina.routinetaskmanager.navigation.ui.AppChrome
import com.okhamzina.routinetaskmanager.navigation.ui.AppChromeEffect
import com.okhamzina.routinetaskmanager.navigation.ui.AppScaffoldState
import com.okhamzina.routinetaskmanager.navigation.ui.CommonTopAppBarWithActionButtons
import com.okhamzina.routinetaskmanager.navigation.ui.LocalAppScaffoldState
import com.okhamzina.routinetaskmanager.navigation.ui.LocalCurrentRoute
import com.okhamzina.routinetaskmanager.navigation.ui.ReminderInfo
import com.okhamzina.routinetaskmanager.ui.theme.EerieBlack
import com.okhamzina.routinetaskmanager.ui.theme.RoutineTaskManagerTheme
import java.time.DayOfWeek
import java.time.LocalTime

@Composable
fun ReminderInfoScreen(
    uiState: ReminderInfoUiState,
    onIntent : (ReminderInfoIntent) -> Unit
) {
    val imagePaths = uiState.reminder?.images.orEmpty().map { image -> image.imagePath }
    var fullscreenImageIndex by remember(imagePaths) {
        mutableStateOf<Int?>(null)
    }

    AppChromeEffect(
        owner = ReminderInfo(uiState.reminder?.id ?: 0),
        chrome = AppChrome(
            topBar = {
                CommonTopAppBarWithActionButtons(
                    title = uiState.reminder?.name ?: "Error",
                    onBackClick = { onIntent(ReminderInfoIntent.BackClicked) },
                    onEditClick = { onIntent(ReminderInfoIntent.EditClicked) },
                    onDeleteClick = { onIntent(ReminderInfoIntent.DeleteClicked) }
                )
            }
        )
    )
    
    Column(
        modifier = Modifier
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        uiState.reminder?.let { reminder ->
            ReminderStatusBlock(
                reminder = reminder,
                onEnabledChange = { enabled ->
                    onIntent(ReminderInfoIntent.EnabledChanged(enabled))
                }
            )
        }

        uiState.nextOccurrence?.let {
            NextReminderBlock(
                reminderTime = it.scheduledAt.formatDateTimeToWeekDayAndTime(),
                onSkipClick = {onIntent(ReminderInfoIntent.SkipNextClicked)},
                onDoNowClick = {onIntent(ReminderInfoIntent.CompleteNextClicked)},
                onSkipForTodayClick = {onIntent(ReminderInfoIntent.SkipRemainingTodayClicked)}
            )
        } ?: Text(
            text = stringResource(R.string.no_reminders_found_for_this_week)
        )

        if (uiState.reminder?.instructionsText != null || imagePaths.isNotEmpty()){
            InstructionsBlock(
                instructions = uiState.reminder?.instructionsText,
                imagePaths = imagePaths,
                onImageClick = { imageIndex ->
                    fullscreenImageIndex = imageIndex
                }
            )
        }

        uiState.reminder?.repeatRule?.let {
            RepeatBlock(
                repeatRule = it
            )
        }

        uiState.reminder?.let { reminder ->
            NotificationBlock(reminder = reminder)
        }
    }

    fullscreenImageIndex?.let { imageIndex ->
        FullscreenImagePagerDialog(
            imagePaths = imagePaths,
            initialIndex = imageIndex,
            onDismiss = {
                fullscreenImageIndex = null
            }
        )
    }

}


@Preview(
    name = "ReminderInfoScreen"
)
@Composable
private fun ReminderInfoScreenPreview(){
    val reminder = remember {
        Reminder(
            id = 1,
            name = "Drink water",
            instructionsText = " - Keep a glass nearby. \n - Drink water",
            images = listOf(
                ReminderImage(
                    reminderId = 1,
                    imagePath = "android.resource://com.okhamzina.routinetaskmanager/drawable/ic_launcher_foreground",
                    sortOrder = 0
                ),
                ReminderImage(
                    reminderId = 1,
                    imagePath = "android.resource://com.okhamzina.routinetaskmanager/drawable/ic_add_photo",
                    sortOrder = 1
                )
            ),

            repeatRule = ReminderRepeatRule.OnSchedulePeriod(
                schedule = WeeklyRepeat(
                    mode = RepeatScheduleMode.DEFAULT,
                    selectedDays = setOf(DayOfWeek.SATURDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY),
                    defaultValue = OnSchedulePeriodDayRepeat(
                        interval = RepeatInterval(
                            30,
                            RepeatUnit.MINUTES
                        ),
                        timeWindow = TimeWindow(
                            startTime = LocalTime.NOON,
                            endTime = LocalTime.MIDNIGHT,
                            allDayEnabled = false
                        )
                    ),
                    advancedEntries = emptyList(),
                )),
            notificationMode = NotificationMode.SOUND,
            createdAt = 0L,
            updatedAt = 0L
        )
    }
    val route = remember { ReminderInfo(reminder.id) }
    val scaffoldState = remember { AppScaffoldState() }

    RoutineTaskManagerTheme {
        CompositionLocalProvider(
            LocalAppScaffoldState provides scaffoldState,
            LocalCurrentRoute provides route
        ) {
            Scaffold(
                topBar = {
                    scaffoldState.chrome.topBar?.invoke()
                }
            ) { paddingValues ->
                Box(modifier = Modifier.padding(paddingValues)) {
                    ReminderInfoScreen(
                        uiState = ReminderInfoUiState(
                            reminder = reminder
                        ),
                        onIntent = {}
                    )
                }
            }
        }
    }
}

@Composable
fun ReminderStatusBlock(
    reminder: Reminder,
    onEnabledChange: (Boolean) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        TitleText(
            text = stringResource(R.string.field_status)
        )

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        ) {
            Row(
                modifier = Modifier.padding(
                    horizontal = 16.dp,
                    vertical = 12.dp
                ),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    painter = painterResource(
                        if (reminder.isEnabled) {
                            R.drawable.ic_alarm_on
                        } else {
                            R.drawable.ic_alarm_disabled
                        }
                    ),
                    contentDescription = null
                )

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = if (reminder.isEnabled) {
                            stringResource(R.string.status_on)
                        } else {
                            stringResource(R.string.status_off)
                        },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Text(
                        text = if (reminder.isEnabled) {
                            stringResource(R.string.action_disable)
                        } else {
                            stringResource(R.string.action_enable)
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Switch(
                    checked = reminder.isEnabled,
                    onCheckedChange = onEnabledChange
                )
            }
        }
    }
}

@Composable
fun NotificationBlock(
    reminder: Reminder
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        TitleText(
            text = stringResource(R.string.field_notification_sound)
        )

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        ) {
            Row(
                modifier = Modifier.padding(
                    horizontal = 16.dp,
                    vertical = 12.dp
                ),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    painter = painterResource(
                        if (reminder.notificationEnabled) {
                            R.drawable.ic_alarm_on
                        } else {
                            R.drawable.ic_alarm_off
                        }
                    ),
                    contentDescription = null
                )

                Text(
                    modifier = Modifier.weight(1f),
                    text = if (reminder.notificationEnabled) {
                        reminder.notificationMode.toInfoLabel()
                    } else {
                        stringResource(R.string.notifications_off)
                    },
                    style = MaterialTheme.typography.bodyLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun NotificationMode.toInfoLabel(): String {
    return when (this) {
        NotificationMode.SOUND -> stringResource(R.string.notification_mode_sound)
        NotificationMode.VIBRATION -> stringResource(R.string.notification_mode_vibration)
        NotificationMode.MUTE -> stringResource(R.string.notification_mode_silent)
    }
}

@Composable
fun RepeatBlock(
    repeatRule: ReminderRepeatRule
){
    TitleText(
        text = stringResource(R.string.field_repeat_type)
    )

    Row(
        modifier = Modifier.padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ){
        Icon(
            painter = painterResource(R.drawable.ic_schedule),
            "Schedule"
        )

        Text(
            text = repeatRule.toRepeatLabel()
        )
    }

    TitleText(
        text = stringResource(R.string.field_repeat_time)
    )

    repeatRule.toShortDetailsLabel()?.let {
        Text(
            modifier = Modifier.padding(horizontal = 16.dp),
            text = it
        )
    }

}
@Composable
fun InstructionsBlock(
    instructions: String?,
    imagePaths: List<String>,
    onImageClick: (Int) -> Unit
){
    Column(

    ) {
        TitleText(
            text = stringResource(R.string.field_instructions)
        )

        Column(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            instructions?.let{
                Text(text = instructions)
            }

            if (imagePaths.isNotEmpty()) {
                ImagePagerWithThumbnail(
                    modifier = Modifier.fillMaxWidth(),
                    imagePaths = imagePaths,
                    onClick = onImageClick
                )
            }
        }

    }
}

@Composable
fun NextReminderBlock(
    reminderTime : String,
    onSkipClick: () ->  Unit,
    onDoNowClick: () -> Unit,
    onSkipForTodayClick: () -> Unit
){
    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        TitleText(
            text = stringResource(R.string.next_reminder)
        )
        NextReminderInfoCard(
            reminderTime = reminderTime,
            onOutlinedButtonClick = onSkipClick,
            onFilledButtonClick = onDoNowClick
        )

        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = onSkipForTodayClick,
            contentPadding = PaddingValues(vertical = 0.dp, horizontal = 4.dp)
        ){
            Text(stringResource(R.string.skip_for_today))
        }
    }
}

@Composable
fun NextReminderInfoCard(
    reminderTime : String,
    onOutlinedButtonClick : () -> Unit,
    onFilledButtonClick : () -> Unit
){
    val contentColor = EerieBlack

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.tertiary)
    ){
        Column(
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = reminderTime,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = contentColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                ReminderActionButtons(
                    skipText = stringResource(R.string.skip),
                    onSkipClick = onOutlinedButtonClick,
                    doNowText = stringResource(R.string.do_now),
                    onDoNowClick = onFilledButtonClick,
                    contentColor = contentColor
                )
            }
        }
    }
}
