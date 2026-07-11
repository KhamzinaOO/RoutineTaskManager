package com.okhamzina.routinetaskmanager.featureReminder.presentation.reminder_main.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.okhamzina.routinetaskmanager.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

enum class RemindersDrawerItem {
    Main,
    AllReminders
}

@Composable
fun RemindersDrawerScaffold(
    drawerState : DrawerState,
    scope : CoroutineScope,
    selectedItem: RemindersDrawerItem,
    onMainClick: () -> Unit,
    onAllRemindersClick: () -> Unit,
    content: @Composable () -> Unit
) {
    fun closeDrawerAndNavigate(action: () -> Unit) {
        scope.launch {
            drawerState.close()
            action()
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            Column(
                modifier = Modifier
                    .fillMaxHeight(0.9f)
                    .fillMaxWidth(0.8f)
                    .clip(RoundedCornerShape(bottomEnd = 16.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                NavigationDrawerItem(
                    label = {
                        Text(
                            text = stringResource(R.string.drawer_main),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    selected = selectedItem == RemindersDrawerItem.Main,
                    onClick = {
                        closeDrawerAndNavigate(onMainClick)
                    }
                )

                NavigationDrawerItem(
                    label = {
                        Text(
                            text = stringResource(R.string.drawer_all_reminders),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    selected = selectedItem == RemindersDrawerItem.AllReminders,
                    onClick = {
                        closeDrawerAndNavigate(onAllRemindersClick)
                    }
                )
            }
        }
    ) {
        content()
    }
}
