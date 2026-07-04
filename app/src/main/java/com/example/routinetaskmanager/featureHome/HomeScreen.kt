package com.example.routinetaskmanager.featureHome

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.routinetaskmanager.R
import com.example.routinetaskmanager.core.presentation.model.asString
import com.example.routinetaskmanager.core.presentation.ui.PermissionDeniedAction
import com.example.routinetaskmanager.core.presentation.ui.openAppNotificationSettings
import com.example.routinetaskmanager.core.presentation.ui.rememberNotificationPermissionRequest
import com.example.routinetaskmanager.core.utills.formatTime
import com.example.routinetaskmanager.featureReminder.domain.model.ReminderOccurrenceStatus
import com.example.routinetaskmanager.featureReminder.presentation.common.ui.components.NextReminderCard
import com.example.routinetaskmanager.featureReminder.presentation.common.ui.components.WorkSessionButton
import com.example.routinetaskmanager.featureTask.ui.TaskCardUi
import com.example.routinetaskmanager.featureWidgets.AmountTracker
import com.example.routinetaskmanager.featureWidgets.TimerTracker
import com.example.routinetaskmanager.navigation.ui.AppChrome
import com.example.routinetaskmanager.navigation.ui.AppChromeEffect
import com.example.routinetaskmanager.navigation.ui.Home
import com.example.routinetaskmanager.navigation.ui.HomeTopBar
import kotlinx.coroutines.delay
import java.time.Duration
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@SuppressLint("LocalContextGetResourceValueCall")
@Composable
fun HomeScreen(
    uiState : HomeUiState,
    onIntent: (HomeUiIntent) -> Unit,
    showActionMessage: (message: String, actionLabel: String, onAction: () -> Unit) -> Unit
){
    val context = LocalContext.current
    val notificationPermissionRequestRef = remember { arrayOf<(() -> Unit)?>(null) }

    val requestNotificationPermission = rememberNotificationPermissionRequest(
        onGranted = {
            onIntent(HomeUiIntent.OnSessionButtonClick)
        },
        onDenied = {
            onIntent(HomeUiIntent.NotificationPermissionDenied)
        },
        onDeniedWithAction = { action ->
            val actionLabel = when (action) {
                PermissionDeniedAction.RetryRequest -> context.getString(R.string.action_retry)
                PermissionDeniedAction.OpenSettings -> context.getString(R.string.action_settings)
            }

            showActionMessage(
                context.getString(R.string.notifications_required_for_session),
                actionLabel
            ) {
                when (action) {
                    PermissionDeniedAction.RetryRequest -> notificationPermissionRequestRef[0]?.invoke()
                    PermissionDeniedAction.OpenSettings -> openAppNotificationSettings(context)
                }
            }
        }
    )
    notificationPermissionRequestRef[0] = requestNotificationPermission

    AppChromeEffect(
        owner = Home,
        chrome = AppChrome(
            topBar = {
                HomeTopBar(
                    greeting = uiState.greetingText.asString(context),
                    date = uiState.dateText,
                    onSettingClick = {

                    }
                )
            }
        )
    )

    Column(
        modifier = Modifier.verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        var elapsedTimeMillis by remember { mutableLongStateOf(0L) }

        LaunchedEffect(uiState.isSessionActive, uiState.sessionStartedAtMillis) {
            val startedAtMillis = uiState.sessionStartedAtMillis

            if (!uiState.isSessionActive || startedAtMillis == null) {
                elapsedTimeMillis = 0L
                return@LaunchedEffect
            }

            while (true) {
                elapsedTimeMillis = System.currentTimeMillis() - startedAtMillis
                delay(1000L)
            }
        }

        val timerText = formatTime(elapsedTimeMillis)


        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.CenterEnd
        ){
            WorkSessionButton(
                remindersCount = uiState.sessionReminderCount,
                timer = timerText,
                isActive = uiState.isSessionActive,
                isLoading = uiState.isSessionActionInProgress,
                onEndClick = { onIntent(HomeUiIntent.OnSessionButtonClick) },
                onStartClick = { requestNotificationPermission() }
            )
        }

        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(horizontal = 16.dp)
        ) {
            uiState.reminders.firstOrNull { reminder ->
                reminder.status == ReminderOccurrenceStatus.PLANNED
            }?.let { reminder ->
                NextReminderCard(
                    time = reminder.scheduledAt.format(DateTimeFormatter.ofPattern("EEEE HH:mm")),
                    label = reminder.reminderName,
                    reminderTime = formatStartsIn(LocalDateTime.now(), reminder.scheduledAt),
                    outlinedButtonText = stringResource(R.string.action_skip),
                    onOutlinedButtonClick = {},
                    filledButtonText = stringResource(R.string.action_do_now),
                    onFilledButtonClick = {}
                )
            }

            var isLeftButtonPicked by remember { mutableStateOf(true) }
            val sampleTask = stringResource(R.string.home_sample_task)
            val sampleDescription = stringResource(R.string.home_sample_description)
            val sampleLongDescription = stringResource(R.string.home_sample_long_description)
            val allDay = stringResource(R.string.time_all_day)
            ScheduleItemsCard(
                reminders = uiState.reminders.map { reminder ->
                    ReminderCardUi(
                        time = reminder.scheduledAt.format(DateTimeFormatter.ofPattern("HH:mm")),
                        status = reminder.status == ReminderOccurrenceStatus.COMPLETED,
                        text = reminder.reminderName
                    )
                },
                tasks = listOf(
                    TaskCardUi(
                        text = sampleTask,
                        status = true,
                        time = "10:00",
                        duration = allDay,
                        description = sampleDescription
                    ),
                    TaskCardUi(
                        text = sampleTask,
                        status = true,
                        time = "11:00",
                        duration = "11:00-15:00",
                        description = sampleLongDescription
                    ),
                    TaskCardUi(
                        text = sampleTask,
                        status = false,
                        time = "12:00",
                        duration = allDay,
                        description = sampleDescription
                    ),TaskCardUi(
                        text = sampleTask,
                        status = false,
                        time = "13:00",
                        duration = allDay,
                        description = sampleDescription
                    ),

                    ),
                isLeftButtonPicked,
                {isLeftButtonPicked = true},
                {isLeftButtonPicked = false},
                {}
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AmountTracker(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .height(170.dp),
                    title = stringResource(R.string.home_drink_water),
                    progressText = stringResource(R.string.home_water_progress),
                    progress = 0.1f,
                    textFieldHint = stringResource(R.string.home_water_unit_ml),
                    onAddButtonClicked = {}
                )
                TimerTracker(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .height(170.dp),
                    title = stringResource(R.string.home_pomodoro_focus),
                    progressText = "00:00",
                    progress = 0f,
                    onStartClick = {},
                    onEndClick = {},
                    onRepeatClick = {}
                )
            }
        }
    }
}

//TODO: localization
private fun formatStartsIn(
    now: LocalDateTime,
    scheduledAt: LocalDateTime
): String {
    val duration = Duration.between(now, scheduledAt)

    if (duration.isNegative || duration.isZero) {
        return "Started"
    }

    val days = if(duration.toDays()>0) "${duration.toDays()}d" else ""
    val hours = if(duration.toHours() % 24 >0) "${duration.toHours() % 24}h" else ""
    val minutes = if(duration.toMinutes() % 60 >0) "${duration.toMinutes() % 60}m" else ""

    return "Starts in $days $hours $minutes"
}
