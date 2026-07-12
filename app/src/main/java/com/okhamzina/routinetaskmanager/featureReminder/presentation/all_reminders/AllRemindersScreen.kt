package com.okhamzina.routinetaskmanager.featureReminder.presentation.all_reminders

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
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.okhamzina.routinetaskmanager.R
import com.okhamzina.routinetaskmanager.core.presentation.model.DropdownMenuItemUi
import com.okhamzina.routinetaskmanager.core.presentation.ui.CommonDropdownMenu
import com.okhamzina.routinetaskmanager.featureReminder.presentation.mapper.toMiniCardUi
import com.okhamzina.routinetaskmanager.featureReminder.presentation.all_reminders.model.AllRemindersIntent
import com.okhamzina.routinetaskmanager.featureReminder.presentation.all_reminders.model.AllRemindersUiState
import com.okhamzina.routinetaskmanager.featureReminder.presentation.all_reminders.ui.components.ReminderMiniCard
import com.okhamzina.routinetaskmanager.navigation.ui.AllReminders
import com.okhamzina.routinetaskmanager.navigation.ui.AppChrome
import com.okhamzina.routinetaskmanager.navigation.ui.AppChromeEffect
import com.okhamzina.routinetaskmanager.navigation.ui.CommonAppBarWithMenuButtonAndDropdown
import com.okhamzina.routinetaskmanager.navigation.ui.CommonFloatingButton

@Composable
fun AllRemindersScreen(
    uiState : AllRemindersUiState,
    onIntent : (AllRemindersIntent) -> Unit
){
    val reminders = uiState.visibleReminders

    var openedReminderId by remember { mutableStateOf<Long?>(null) }

    AppChromeEffect(
        owner = AllReminders,
        chrome = AppChrome(
            topBar = {
                CommonAppBarWithMenuButtonAndDropdown(
                    title = stringResource(R.string.all_reminders_title),
                    onMenuButtonClick = { onIntent(AllRemindersIntent.MenuButtonClicked)},
                    onSearchButtonClick = {
                        onIntent(AllRemindersIntent.SearchButtonClicked)
                    }
                )
            },
            fab = {
                CommonFloatingButton(
                    onClick = {
                        onIntent(AllRemindersIntent.AddReminderClicked)
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
                onIntent(AllRemindersIntent.TypeFilterSelected(
                    typeId = it.id
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
                            .clip(RoundedCornerShape(24.dp))
                            .combinedClickable(
                                onClick = {onIntent(AllRemindersIntent.ReminderClicked(reminder.id))},
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
                                    onIntent(AllRemindersIntent.ReminderClicked(reminder.id))
                                }

                                1 -> {
                                    onIntent(AllRemindersIntent.EditReminderClicked(reminder.id))
                                }

                                2 -> {
                                    onIntent(AllRemindersIntent.DeleteReminderClicked(reminder.id))
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
