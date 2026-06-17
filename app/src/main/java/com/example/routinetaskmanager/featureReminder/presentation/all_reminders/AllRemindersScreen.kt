package com.example.routinetaskmanager.featureReminder.presentation.all_reminders

import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.routinetaskmanager.R
import com.example.routinetaskmanager.core.presentation.model.DropdownMenuItemUi
import com.example.routinetaskmanager.core.presentation.ui.CommonDropdownMenu
import com.example.routinetaskmanager.featureReminder.data.mapper.toRepeatType
import com.example.routinetaskmanager.featureReminder.presentation.all_reminders.mapper.toMiniCardUi
import com.example.routinetaskmanager.featureReminder.presentation.all_reminders.model.AllRemindersIntent
import com.example.routinetaskmanager.featureReminder.presentation.all_reminders.model.AllRemindersUiState
import com.example.routinetaskmanager.featureReminder.presentation.all_reminders.ui.components.ReminderMiniCard
import com.example.routinetaskmanager.featureReminder.presentation.common.ui.components.ReminderCard
import com.example.routinetaskmanager.navigation.ui.AllReminders
import com.example.routinetaskmanager.navigation.ui.AppChrome
import com.example.routinetaskmanager.navigation.ui.AppChromeEffect
import com.example.routinetaskmanager.navigation.ui.CommonAppBarWithMenuButtonAndDropdown
import com.example.routinetaskmanager.navigation.ui.CommonFloatingButton

@Composable
fun AllRemindersScreen(
    uiState : AllRemindersUiState,
    onIntent : (AllRemindersIntent) -> Unit
){
    val reminders = uiState.remindersToShow

    var openedReminderId by remember { mutableStateOf<Long?>(null) }

    AppChromeEffect(
        owner = AllReminders,
        chrome = AppChrome(
            topBar = {
                CommonAppBarWithMenuButtonAndDropdown(
                    title = stringResource(R.string.all_reminders_title),
                    onMenuButtonClick = { onIntent(AllRemindersIntent.OnMenuButtonClick)},
                    onSearchButtonClick = {

                    }
                )
            },
            fab = {
                CommonFloatingButton(
                    onClick = {
                        onIntent(AllRemindersIntent.OnAddFABClick)
                    }
                )
            }
        )
    )

    Column(
        modifier = Modifier
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        RemindersTopFiltersBar(
            onItemClick = {
                onIntent(AllRemindersIntent.OnTypeSelected(
                    typeIndex = it.id
                ))
            },
            selectedId = uiState.reminderFilter.repeatType?.ordinal ?: -1,
            values = uiState.repeatTypeFilterList.map {
                DropdownMenuItemUi(
                    id = it.id,
                    name = stringResource(it.repeatTypeNameRes)
                )
            }
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(
                items = reminders,
                key = { reminder -> reminder.id }
            ) { reminder ->
                Box(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    ReminderMiniCard(
                        modifier = Modifier
                            .combinedClickable(
                                onClick = {onIntent(AllRemindersIntent.OnItemClick(reminder.id))},
                                onLongClick = {
                                    openedReminderId = reminder.id
                                }
                            ),
                        reminder = reminder.toMiniCardUi()
                    )
                    ReminderActionsMenu(
                        expanded = openedReminderId == reminder.id,
                        onDismiss = {
                            openedReminderId = null
                        },
                        values = listOf(
                            DropdownMenuItemUi(id = 0, name = stringResource(R.string.action_open)),
                            DropdownMenuItemUi(id = 1, name = stringResource(R.string.action_edit)),
                            DropdownMenuItemUi(id = 2, name = stringResource(R.string.action_delete))
                        ),
                        onItemClick = { item ->
                            openedReminderId = null

                            when (item.id) {
                                0 -> {
                                    onIntent(AllRemindersIntent.OnOpenClick(reminder.id))
                                }

                                1 -> {
                                    onIntent(AllRemindersIntent.OnEditClick(reminder.id))
                                }

                                2 -> {
                                    onIntent(AllRemindersIntent.OnDeleteClick(reminder.id))
                                }
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun ReminderActionsMenu(
    expanded: Boolean,
    onDismiss: () -> Unit,
    values: List<DropdownMenuItemUi>,
    onItemClick: (DropdownMenuItemUi) -> Unit
) {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismiss
    ) {
        values.forEach { item ->
            DropdownMenuItem(
                text = {
                    Text(
                        text = item.name,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                onClick = {
                    onItemClick(item)
                }
            )
        }
    }
}

@Composable
fun RemindersTopFiltersBar(
    selectedId : Int,
    values : List<DropdownMenuItemUi>,
    onItemClick: (DropdownMenuItemUi) -> Unit
){
    Row(
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically
    ) {
        CommonDropdownMenu(
            onItemClick = onItemClick,
            values = values,
            selectedId = selectedId
        )
    }
}
