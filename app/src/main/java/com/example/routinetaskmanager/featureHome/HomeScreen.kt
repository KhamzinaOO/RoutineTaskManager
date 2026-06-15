package com.example.routinetaskmanager.featureHome

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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.routinetaskmanager.core.presentation.ui.rememberExactAlarmAccessRequest
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
import java.time.format.DateTimeFormatter
import org.koin.androidx.compose.koinViewModel

@Composable
fun HomeScreen(
    viewModel: HomeViewModel = koinViewModel(),
    showMessage: (String) -> Unit
){
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val requestExactAlarmAccess = rememberExactAlarmAccessRequest(
        onGranted = {
            viewModel.onSessionButtonClick()
        },
        onDenied = {
            viewModel.onExactAlarmAccessDenied()
            viewModel.onSessionButtonClick()
        }
    )
    val requestNotificationPermission = rememberNotificationPermissionRequest(
        onGranted = {
            requestExactAlarmAccess()
        },
        onDenied = {
            viewModel.onNotificationPermissionDenied()
        }
    )

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is HomeEffect.ShowMessage -> showMessage(effect.message)
            }
        }
    }

    AppChromeEffect(
        owner = Home,
        chrome = AppChrome(
            topBar = {
                HomeTopBar(
                    greeting = uiState.greetingText,
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
                onEndClick = { viewModel.onEndSessionButtonClick() },
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
                    label = reminder.scheduledAt.format(DateTimeFormatter.ofPattern("EEEE HH:mm")),
                    outlinedButtonText = "Skip",
                    onOutlinedButtonClick = {},
                    filledButtonText = "Do now",
                    onFilledButtonClick = {}
                )
            }

            var isLeftButtonPicked by remember { mutableStateOf(true) }
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
                        text = "task",
                        status = true,
                        time = "10:00",
                        duration = "All day",
                        description = "description"
                    ),
                    TaskCardUi(
                        text = "task",
                        status = true,
                        time = "11:00",
                        duration = "11:00-15:00",
                        description = "descriptiondescriptiondescriptiondescriptiondescriptiondescriptiondescriptiondescriptiondescriptiondescriptiondescriptiondescription"
                    ),
                    TaskCardUi(
                        text = "task",
                        status = false,
                        time = "12:00",
                        duration = "All day",
                        description = "description"
                    ),TaskCardUi(
                        text = "task",
                        status = false,
                        time = "13:00",
                        duration = "All day",
                        description = "description"
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
                    title = "Drink water",
                    progressText = "0.2/2L",
                    progress = 0.1f,
                    textFieldHint = "ml",
                    onAddButtonClicked = {}
                )
                TimerTracker(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .height(170.dp),
                    title = "Pomodoro focus",
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
