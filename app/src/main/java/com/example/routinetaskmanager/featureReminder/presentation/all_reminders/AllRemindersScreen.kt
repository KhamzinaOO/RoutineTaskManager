package com.example.routinetaskmanager.featureReminder.presentation.all_reminders

import androidx.compose.foundation.layout.Box
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.routinetaskmanager.featureHome.ScheduleRow
import com.example.routinetaskmanager.featureReminder.presentation.all_reminders.viewModel.AllRemindersUiState
import com.example.routinetaskmanager.featureReminder.presentation.common.ui.components.ReminderCard

@Composable
fun AllRemindersScreen(
    uiState : AllRemindersUiState
){
    val reminders = uiState.reminders

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