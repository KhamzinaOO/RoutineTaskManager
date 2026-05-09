package com.example.routinetaskmanager.featureReminder

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
import androidx.compose.ui.unit.dp
import com.example.routinetaskmanager.core.ui.WeekCarousel
import com.example.routinetaskmanager.core.utills.formatTime
import com.example.routinetaskmanager.featureHome.ReminderCardUi
import com.example.routinetaskmanager.featureHome.ScheduleRow
import com.example.routinetaskmanager.navigation.ui.AppChrome
import com.example.routinetaskmanager.navigation.ui.AppChromeEffect
import com.example.routinetaskmanager.navigation.ui.CommonCalendarAppBar
import com.example.routinetaskmanager.navigation.ui.CommonFloatingButton
import com.example.routinetaskmanager.navigation.ui.Home
import com.example.routinetaskmanager.navigation.ui.Reminders
import kotlinx.coroutines.delay
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale
import java.util.logging.Formatter

@Composable
fun RemindersMainScreen(
    onFloatingButtonClick : () -> Unit
){

    AppChromeEffect(
        chrome = AppChrome(
            topBar = {
                CommonCalendarAppBar(
                    title = LocalDate.now().month.getDisplayName(
                        TextStyle.FULL_STANDALONE,
                        Locale.getDefault()
                    ),
                    onMenuButtonClick = {},
                    onSearchButtonClick = {},
                    onCalendarButtonClick = {}
                )
            },
            fab = {
                CommonFloatingButton {
                    onFloatingButtonClick()
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

        val reminders = listOf(
            ReminderCardUi(time = "11:00", status = true, text = "Use eye drops"),
            ReminderCardUi(time = "12:00", status = true, text = "Use eye drops"),
            ReminderCardUi(time = "13:00", status = false, text = "Use eye drops"),
            ReminderCardUi(time = "14:00", status = false, text = "Use eye drops"),
        )

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
                            time = reminder.time,
                            isDone = reminder.status,
                            content = {
                                ReminderCard(
                                    modifier = Modifier.padding(vertical = 4.dp),
                                    text = reminder.text
                                )
                            }
                        )
                    }
                }
            }
        }
    }
}