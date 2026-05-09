package com.example.routinetaskmanager.navigation.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import com.example.routinetaskmanager.featureHome.HomeScreen
import com.example.routinetaskmanager.featureReminder.CreateReminderScreen
import com.example.routinetaskmanager.featureReminder.RemindersMainScreen

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

    CompositionLocalProvider(
        LocalAppScaffoldState provides appScaffoldState
    ) {
        Scaffold(
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
            Box(Modifier.padding(paddingValues)) {
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
                            RemindersMainScreen(
                                onFloatingButtonClick = {branches.push(CreateReminder(reminderId = null))}
                            )
                        }

                        entry<CreateReminder> {
                            CreateReminderScreen(
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