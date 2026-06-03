package com.example.routinetaskmanager.navigation.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import com.example.routinetaskmanager.featureHome.HomeScreen
import com.example.routinetaskmanager.featureReminder.presentation.all_reminders.AllRemindersRoute
import com.example.routinetaskmanager.featureReminder.presentation.create_reminder.navigation.CreateReminderRoute
import com.example.routinetaskmanager.featureReminder.presentation.reminder_main.navigation.ReminderMainRoute
import com.example.routinetaskmanager.featureReminder.presentation.reminder_main.ui.RemindersDrawerItem
import com.example.routinetaskmanager.featureReminder.presentation.reminder_main.ui.RemindersDrawerScaffold
import com.example.routinetaskmanager.featureReminder.presentation.reminder_main.ui.RemindersMainScreen
import kotlinx.coroutines.launch

private val topRoutes = listOf(Home, Widgets, Reminders, Tasks)

data class AppChrome(
    val topBar: (@Composable () -> Unit)? = null,
    val fab: (@Composable () -> Unit)? = null
)

@Stable
class AppScaffoldState {
    var chrome by mutableStateOf(AppChrome())
        internal set

    fun setChrome(chrome: AppChrome) {
        this.chrome = chrome
    }

    fun clearChrome() {
        chrome = AppChrome()
    }
}

val LocalAppScaffoldState = staticCompositionLocalOf<AppScaffoldState> {
    error("No AppScaffoldState provided")
}

@Composable
fun AppChromeEffect(chrome: AppChrome) {
    val scaffoldState = LocalAppScaffoldState.current

    DisposableEffect(scaffoldState, chrome) {
        onDispose {
            if (scaffoldState.chrome == chrome) {
                scaffoldState.clearChrome()
            }
        }
    }

    SideEffect {
        scaffoldState.setChrome(chrome)
    }
}

@Composable
fun AppNavigation() {
    val branches = remember { BottomBranches<Route>(start = Home) }
    val snackbarHostState = remember { SnackbarHostState() }
    val currentRoute = branches.backStack.last()

    val appScaffoldState = remember { AppScaffoldState() }

    val scope = rememberCoroutineScope()

    val focusManager = LocalFocusManager.current

    CompositionLocalProvider(
        LocalAppScaffoldState provides appScaffoldState
    ) {
        Scaffold(
            modifier = Modifier
                .clickable(
                    onClick = { focusManager.clearFocus() },
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ),
            snackbarHost = {
                SnackbarHost(hostState = snackbarHostState)
            },
            topBar = {
                appScaffoldState.chrome.topBar?.invoke()
            },
            floatingActionButton = {
                appScaffoldState.chrome.fab?.invoke()
            },
            bottomBar = {
                if (currentRoute in topRoutes) {
                    BottomNavigationBar(
                        items = topRoutes,
                        selectedItem = branches.currentTop,
                        onItemClick = { branches.switchTo(it) }
                    )
                }
            }
        ) { paddingValues ->
            Box(
                Modifier
                    .padding(paddingValues)
                    .clickable(
                        onClick = { focusManager.clearFocus() },
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    )
            ) {

                NavDisplay(
                    backStack = branches.backStack,
                    onBack = { branches.pop() },
                    entryProvider = entryProvider {
                        entry<Home> {
                            HomeScreen()
                        }

                        entry<Widgets> {

                        }

                        entry<Reminders> {
                            val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
                            val scope = rememberCoroutineScope()

                            RemindersDrawerScaffold(
                                drawerState = drawerState,
                                scope = scope,
                                selectedItem = RemindersDrawerItem.Main,
                                onMainClick = { },
                                onAllRemindersClick = {
                                    if (branches.backStack.last() != AllReminders) {
                                        branches.push(AllReminders)
                                    }
                                }
                            ) {
                                ReminderMainRoute(
                                    onTopBarIconClick = {
                                        scope.launch {
                                            drawerState.open()
                                        }
                                    }
                                )
                            }
                        }

                        entry<AllReminders> {
                            val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
                            val scope = rememberCoroutineScope()

                            RemindersDrawerScaffold(
                                drawerState = drawerState,
                                scope = scope,
                                selectedItem = RemindersDrawerItem.AllReminders,
                                onMainClick = {
                                    branches.pop()
                                },
                                onAllRemindersClick = { }
                            ) {
                                AllRemindersRoute(
                                    onReminderClick = { reminderId ->
                                        branches.push(ReminderInfo(reminderId))
                                    }
                                )
                            }
                        }

                        entry<CreateReminder> {
                            CreateReminderRoute(
                                showMessage = {
                                    scope.launch {
                                        snackbarHostState.showSnackbar(it)
                                    }
                                },
                                onBackClick = { branches.pop() }
                            )
                        }

                        entry<ReminderInfo> {

                        }

                        entry<Tasks> {

                        }
                    }
                )
            }
        }
    }
}