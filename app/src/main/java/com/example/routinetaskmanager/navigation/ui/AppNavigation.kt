package com.example.routinetaskmanager.navigation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.example.routinetaskmanager.featureHome.HomeRoute
import com.example.routinetaskmanager.featureReminder.presentation.all_reminders.AllRemindersRoute
import com.example.routinetaskmanager.featureReminder.presentation.create_edit_reminder.CreateEditReminderRoute
import com.example.routinetaskmanager.featureReminder.presentation.reminderInfo.ReminderInfoRoute
import com.example.routinetaskmanager.featureReminder.presentation.reminder_main.navigation.ReminderMainRoute
import com.example.routinetaskmanager.featureReminder.presentation.reminder_main.ui.RemindersDrawerItem
import com.example.routinetaskmanager.featureReminder.presentation.reminder_main.ui.RemindersDrawerScaffold
import kotlinx.coroutines.launch

private val topRoutes = listOf(Home, Widgets, Reminders, Tasks)

private val bottomBranches = listOf(Home, Widgets, Reminders, Tasks, AllReminders)

data class AppChrome(
    val topBar: (@Composable () -> Unit)? = null,
    val fab: (@Composable () -> Unit)? = null
)

@Stable
class AppScaffoldState {
    var chrome by mutableStateOf(AppChrome())
        private set

    private var chromeOwner: Route? = null

    fun setChrome(owner: Route, chrome: AppChrome) {
        chromeOwner = owner
        this.chrome = chrome
    }
}

val LocalAppScaffoldState = staticCompositionLocalOf<AppScaffoldState> {
    error("No AppScaffoldState provided")
}

val LocalCurrentRoute = staticCompositionLocalOf<Route> {
    error("No current route provided")
}
@Composable
fun AppChromeEffect(
    owner: Route,
    chrome: AppChrome
) {
    val scaffoldState = LocalAppScaffoldState.current
    val currentRoute = LocalCurrentRoute.current

    SideEffect {
        if (owner == currentRoute) {
            scaffoldState.setChrome(owner, chrome)
        }
    }
}

@Composable
fun AppNavigation() {
    val branches = rememberSaveable(
        saver = BottomBranches.Saver
    ) {
        BottomBranches(start = Home)
    }

    val snackbarHostState = remember { SnackbarHostState() }

    val currentRoute by remember {
        derivedStateOf { branches.backStack.last() }
    }

    val appScaffoldState = remember { AppScaffoldState() }

    val scope = rememberCoroutineScope()

    val focusManager = LocalFocusManager.current

    fun showActionSnackbar(
        message: String,
        actionLabel: String,
        onAction: () -> Unit
    ) {
        scope.launch {
            val result = snackbarHostState.showSnackbar(
                message = message,
                actionLabel = actionLabel
            )

            if (result == SnackbarResult.ActionPerformed) {
                onAction()
            }
        }
    }

    CompositionLocalProvider(
        LocalAppScaffoldState provides appScaffoldState,
        LocalCurrentRoute provides currentRoute
    ) {
        Scaffold(
            modifier = Modifier
                .fillMaxSize()
                .clickable(
                    onClick = { focusManager.clearFocus() },
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ),
            containerColor = MaterialTheme.colorScheme.background,
            contentColor = MaterialTheme.colorScheme.onBackground,
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
                if (currentRoute in bottomBranches) {
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
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
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
                    entryDecorators = listOf(
                        rememberSaveableStateHolderNavEntryDecorator(),
                        rememberViewModelStoreNavEntryDecorator()
                    ),
                    entryProvider = entryProvider {
                        entry<Home> {
                            HomeRoute(
                                showMessage = { message ->
                                    scope.launch {
                                        snackbarHostState.showSnackbar(message)
                                    }
                                },
                                showActionMessage = ::showActionSnackbar,
                                onAddReminderClick = {
                                    branches.push(CreateReminder)
                                },
                                onTasksClick = {
                                    branches.switchTo(Tasks)
                                }
                            )
                        }

                        entry<Widgets> {
                            AppChromeEffect(
                                owner = Widgets,
                                chrome = AppChrome{

                                }
                            )
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
                                        if (!drawerState.isOpen){
                                            scope.launch {
                                                drawerState.open()
                                            }
                                        }else{
                                            scope.launch {
                                                drawerState.close()
                                            }
                                        }
                                    },
                                    onFABClicked = {branches.push(CreateReminder)},
                                    showMessage = { message ->
                                        scope.launch {
                                            snackbarHostState.showSnackbar(message)
                                        }
                                    },
                                    showActionMessage = ::showActionSnackbar
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
                                    },
                                    onMenuClick = {
                                        if (!drawerState.isOpen) {
                                            scope.launch {
                                                drawerState.open()
                                            }
                                        } else {
                                            scope.launch {
                                                drawerState.close()
                                            }
                                        }
                                    },
                                    onFABClicked = {
                                        branches.push(CreateReminder)
                                    },
                                    onEditClick = {
                                        branches.push(route = EditReminder(it))
                                    },
                                    showSnackBar = {
                                        scope.launch {
                                            snackbarHostState.showSnackbar(it)
                                        }
                                    }
                                )
                            }
                        }

                        entry<EditReminder>{ args ->
                            CreateEditReminderRoute(
                                id = args.reminderId,
                                showMessage = {
                                    scope.launch {
                                        snackbarHostState.showSnackbar(it)
                                    }
                                },
                                onBackClick = { branches.pop() }
                            )
                        }

                        entry<CreateReminder> {
                            CreateEditReminderRoute(
                                id = null,
                                showMessage = {
                                    scope.launch {
                                        snackbarHostState.showSnackbar(it)
                                    }
                                },
                                onBackClick = { branches.pop() }
                            )
                        }

                        entry<ReminderInfo> { args ->
                            ReminderInfoRoute(
                                reminderId = args.reminderId,
                                showMessage = {
                                    scope.launch {
                                        snackbarHostState.showSnackbar(it)
                                    }
                                },
                                onBackClick = {branches.pop()},
                                onEditClick = {branches.push(route = EditReminder(it))}
                            )
                        }

                        entry<Tasks> {
                            AppChromeEffect(
                                owner = Tasks,
                                chrome = AppChrome{

                                }
                            )
                        }
                    }
                )
            }
        }
    }
}
