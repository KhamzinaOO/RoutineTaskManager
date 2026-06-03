package com.example.routinetaskmanager.featureReminder.presentation.reminder_main.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.unit.dp
import com.example.routinetaskmanager.core.ui.WeekCarousel
import com.example.routinetaskmanager.core.utills.formatTime
import com.example.routinetaskmanager.featureHome.ScheduleRow
import com.example.routinetaskmanager.featureReminder.presentation.common.ui.components.ReminderCard
import com.example.routinetaskmanager.featureReminder.presentation.common.ui.components.WorkSessionButton
import com.example.routinetaskmanager.featureReminder.presentation.reminder_main.viewModel.ReminderMainIntent
import com.example.routinetaskmanager.featureReminder.presentation.reminder_main.viewModel.ReminderMainUiState
import com.example.routinetaskmanager.navigation.ui.AppChrome
import com.example.routinetaskmanager.navigation.ui.AppChromeEffect
import com.example.routinetaskmanager.navigation.ui.CommonCalendarAppBar
import com.example.routinetaskmanager.navigation.ui.CommonFloatingButton
import kotlinx.coroutines.delay
import java.time.LocalDate
import java.time.format.TextStyle

@Composable
fun RemindersMainScreen(
    uiState : ReminderMainUiState,
    onIntent : (ReminderMainIntent) -> Unit
){
    val reminders = uiState.reminders

    AppChromeEffect(
        chrome = AppChrome(
            topBar = {
                CommonCalendarAppBar(
                    title = LocalDate.now().month.getDisplayName(
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
            modifier = Modifier.padding(horizontal = 16.dp)
        ) {
            WeekCarousel(
                onDaySelected = {}
            )
        }

        var isRunning by remember { mutableStateOf(false) }
        var elapsedTimeMillis by remember { mutableLongStateOf(0L) }

        LaunchedEffect(isRunning) {
            while (isRunning) {
                delay(1000L)
                elapsedTimeMillis += 1000L
            }
        }

        val timerText = formatTime(elapsedTimeMillis)


        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.CenterEnd
        ) {
            WorkSessionButton(
                0,
                timer = timerText,
                onEndClick = { isRunning = false },
                onStartClick = { isRunning = true }
            )
        }

        Box(
            modifier = Modifier.padding(horizontal = 16.dp)
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
                        .height(300.dp)
                        .fillMaxWidth()
                ) {
                    items(reminders) { reminder ->
                        ScheduleRow(
                            time = "ndy",
                            isDone = false,
                            content = {
                                ReminderCard(
                                    modifier = Modifier.padding(vertical = 4.dp),
                                    text = reminder.name
                                )
                            }
                        )
                    }
                }
            }
        }
    }
}