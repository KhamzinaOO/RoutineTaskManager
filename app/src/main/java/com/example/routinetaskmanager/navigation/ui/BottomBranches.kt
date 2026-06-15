package com.example.routinetaskmanager.navigation.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import com.example.routinetaskmanager.navigation.ui.mapper.toRouteOrNull
import com.example.routinetaskmanager.navigation.ui.mapper.toSavedString

class BottomBranches private constructor(
    private val stacks: LinkedHashMap<Route, SnapshotStateList<Route>>,
    currentTop: Route
) {

    constructor(start: Route) : this(
        stacks = linkedMapOf(
            start to mutableStateListOf(start)
        ),
        currentTop = start
    )

    var currentTop by mutableStateOf(currentTop)
        private set

    val backStack = mutableStateListOf<Route>().apply {
        addAll(stacks.values.flatten())
    }

    fun switchTo(top: Route) {
        val existing = stacks.remove(top)

        stacks[top] = existing ?: mutableStateListOf(top)
        currentTop = top

        rebuildBackStack()
    }

    fun push(route: Route) {
        stacks.getValue(currentTop).add(route)
        rebuildBackStack()
    }

    fun pop() {
        val currentStack = stacks.getValue(currentTop)

        if (currentStack.size > 1) {
            currentStack.removeAt(currentStack.lastIndex)
        } else if (stacks.size > 1) {
            stacks.remove(currentTop)
            currentTop = stacks.keys.last()
        }

        rebuildBackStack()
    }

    private fun rebuildBackStack() {
        backStack.clear()
        backStack.addAll(stacks.values.flatten())
    }

    private fun toSavedList(): List<String> {
        val result = mutableListOf<String>()

        result += currentTop.toSavedString()
        result += stacks.size.toString()

        stacks.values.forEach { stack ->
            result += stack.size.toString()

            stack.forEach { route ->
                result += route.toSavedString()
            }
        }

        return result
    }

    companion object {
        val Saver = listSaver<BottomBranches, String>(
            save = { branches ->
                branches.toSavedList()
            },
            restore = { saved ->
                restoreFromSavedList(saved)
            }
        )

        private fun restoreFromSavedList(saved: List<String>): BottomBranches {
            if (saved.size < 2) {
                return BottomBranches(Home)
            }

            val restoredCurrentTop = saved[0].toRouteOrNull() ?: Home
            val stackCount = saved[1].toIntOrNull() ?: 1

            var index = 2
            val restoredStacks = linkedMapOf<Route, SnapshotStateList<Route>>()

            repeat(stackCount) {
                val stackSize = saved.getOrNull(index)?.toIntOrNull() ?: 0
                index++

                val stack = mutableStateListOf<Route>()

                repeat(stackSize) {
                    val route = saved.getOrNull(index)?.toRouteOrNull()
                    index++

                    if (route != null) {
                        stack.add(route)
                    }
                }

                if (stack.isNotEmpty()) {
                    restoredStacks[stack.first()] = stack
                }
            }

            val safeStacks: LinkedHashMap<Route, SnapshotStateList<Route>> =
                if (restoredStacks.isNotEmpty()) {
                    restoredStacks
                } else {
                    linkedMapOf<Route, SnapshotStateList<Route>>(
                        Home to mutableStateListOf<Route>().apply {
                            add(Home)
                        }
                    )
                }

            val safeCurrentTop = if (safeStacks.containsKey(restoredCurrentTop)) {
                restoredCurrentTop
            } else {
                safeStacks.keys.last()
            }

            return BottomBranches(
                stacks = safeStacks,
                currentTop = safeCurrentTop
            )
        }
    }
}