package com.okhamzina.routinetaskmanager.featureReminder.presentation.reminder_main.ui

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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.okhamzina.routinetaskmanager.R
import com.okhamzina.routinetaskmanager.core.presentation.ui.dateTime.WeekCarousel
import com.okhamzina.routinetaskmanager.featureHome.ScheduleRow
import com.okhamzina.routinetaskmanager.featureReminder.domain.model.ReminderOccurrenceStatus
import com.okhamzina.routinetaskmanager.featureReminder.presentation.common.ui.components.ReminderCard
import com.okhamzina.routinetaskmanager.featureReminder.presentation.common.ui.components.WorkSessionControl
import com.okhamzina.routinetaskmanager.featureReminder.presentation.reminder_main.model.ReminderMainIntent
import com.okhamzina.routinetaskmanager.featureReminder.presentation.reminder_main.model.ReminderMainUiState
import com.okhamzina.routinetaskmanager.navigation.ui.AppChrome
import com.okhamzina.routinetaskmanager.navigation.ui.AppChromeEffect
import com.okhamzina.routinetaskmanager.navigation.ui.CommonCalendarAppBar
import com.okhamzina.routinetaskmanager.navigation.ui.CommonFloatingButton
import com.okhamzina.routinetaskmanager.navigation.ui.Reminders
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle

@Composable
fun RemindersMainScreen(
    uiState : ReminderMainUiState,
    onIntent : (ReminderMainIntent) -> Unit,
    showActionMessage: (message: String, actionLabel: String, onAction: () -> Unit) -> Unit
){
    val reminders = uiState.reminders

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

        Box(
            modifier = Modifier
                .fillMaxWidth(),
            contentAlignment = Alignment.CenterEnd
        ) {
            WorkSessionControl(
                remindersCount = uiState.sessionReminderCount,
                isActive = uiState.isSessionActive,
                startedAtMillis = uiState.sessionStartedAtMillis,
                isLoading = uiState.isSessionActionInProgress,
                showActionMessage = showActionMessage,
                onStartSession = { onIntent(ReminderMainIntent.SessionButtonClick) },
                onEndSession = { onIntent(ReminderMainIntent.SessionButtonClick) },
                onNotificationPermissionDenied = {
                    onIntent(ReminderMainIntent.NotificationPermissionDenied)
                }
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
