package com.example.routinetaskmanager.featureReminder.presentation.reminder_main.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.routinetaskmanager.R
import com.example.routinetaskmanager.core.presentation.ui.PermissionDeniedAction
import com.example.routinetaskmanager.core.presentation.ui.dateTime.WeekCarousel
import com.example.routinetaskmanager.core.presentation.ui.openAppNotificationSettings
import com.example.routinetaskmanager.core.presentation.ui.rememberNotificationPermissionRequest
import com.example.routinetaskmanager.core.utills.formatTime
import com.example.routinetaskmanager.featureHome.ScheduleRow
import com.example.routinetaskmanager.featureReminder.domain.model.ReminderOccurrenceStatus
import com.example.routinetaskmanager.featureReminder.presentation.common.ui.components.ReminderCard
import com.example.routinetaskmanager.featureReminder.presentation.common.ui.components.WorkSessionButton
import com.example.routinetaskmanager.featureReminder.presentation.reminder_main.model.ReminderMainIntent
import com.example.routinetaskmanager.featureReminder.presentation.reminder_main.model.ReminderMainUiState
import com.example.routinetaskmanager.navigation.ui.AppChrome
import com.example.routinetaskmanager.navigation.ui.AppChromeEffect
import com.example.routinetaskmanager.navigation.ui.CommonCalendarAppBar
import com.example.routinetaskmanager.navigation.ui.CommonFloatingButton
import com.example.routinetaskmanager.navigation.ui.Reminders
import kotlinx.coroutines.delay
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle

@Composable
fun RemindersMainScreen(
    uiState : ReminderMainUiState,
    onIntent : (ReminderMainIntent) -> Unit,
    showActionMessage: (message: String, actionLabel: String, onAction: () -> Unit) -> Unit
){
    val reminders = uiState.reminders
    val context = LocalContext.current
    val notificationPermissionRequestRef = remember { arrayOf<(() -> Unit)?>(null) }

    val requestNotificationPermission = rememberNotificationPermissionRequest(
        onGranted = {
            onIntent(ReminderMainIntent.SessionButtonClick)
        },
        onDenied = {
            onIntent(ReminderMainIntent.NotificationPermissionDenied)
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
        owner = Reminders,
        chrome = AppChrome(
            topBar = {
                CommonCalendarAppBar(
                    title = uiState.selectedDate.month.getDisplayName(
                        TextStyle.FULL_STANDALONE,
                        LocalLocale.current.platformLocale
                    ),
                    onMenuButtonClick = {
                        onIntent(ReminderMainIntent.MenuButtonClick)
                    },
                    onSearchButtonClick = {
                        onIntent(ReminderMainIntent.SearchButtonClick)
                    },
                    onCalendarButtonClick = {
                        onIntent(ReminderMainIntent.CalendarButtonClick)
                    }
                )
            },
            fab = {
                CommonFloatingButton {
                    onIntent(ReminderMainIntent.AddFABClick)
                }
            }
        )
    )

    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .padding(horizontal = 16.dp)
        ) {
            WeekCarousel(
                onDaySelected = { date ->
                    onIntent(ReminderMainIntent.DateClick(date))
                }
            )
        }

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
            modifier = Modifier
                .fillMaxWidth(),
            contentAlignment = Alignment.CenterEnd
        ) {
            WorkSessionButton(
                remindersCount = uiState.sessionReminderCount,
                timer = timerText,
                isActive = uiState.isSessionActive,
                isLoading = uiState.isSessionActionInProgress,
                onEndClick = { onIntent(ReminderMainIntent.EndSessionButtonClick) },
                onStartClick = { requestNotificationPermission() }
            )
        }

        if(reminders.isNotEmpty()){
            Box(
                modifier = Modifier
                    .padding(bottom = 8.dp)
                    .padding(horizontal = 16.dp)
            ) {
                Card(
                    Modifier
                        .fillMaxWidth(),
                    shape = RoundedCornerShape(25.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    LazyColumn(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxHeight()
                            .fillMaxWidth()
                    ) {
                        itemsIndexed(
                            items = reminders,
                            key = { index, reminder ->
                                "${reminder.reminderId}-${reminder.scheduledAt}-$index"
                            }
                        ) { _, reminder ->
                            ScheduleRow(
                                time = reminder.scheduledAt.format(DateTimeFormatter.ofPattern("HH:mm") ),
                                isDone = reminder.status == ReminderOccurrenceStatus.COMPLETED,
                                content = {
                                    ReminderCard(
                                        modifier = Modifier
                                            .padding(vertical = 4.dp),
                                        text = reminder.reminderName
                                    )
                                }
                            )
                        }
                    }
                }
            }
        }else{
            Text(
                modifier = Modifier
                    .padding(top = 16.dp)
                    .fillMaxWidth(),
                text = stringResource(R.string.empty_reminders),
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.outlineVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
