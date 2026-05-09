package com.example.routinetaskmanager.navigation.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList

class BottomBranches<T : Any>(start: T) {

    private val stacks = linkedMapOf<T, SnapshotStateList<T>>(
        start to mutableStateListOf(start)
    )

    var currentTop by mutableStateOf(start)
        private set

    val backStack = mutableStateListOf(start)

    fun switchTo(top: T) {
        val existing = stacks.remove(top)

        stacks[top] = existing ?: mutableStateListOf(top)
        currentTop = top

        rebuildBackStack()
    }

    fun push(route: T) {
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

    fun rebuildBackStack() {
        backStack.clear()
        backStack.addAll(stacks.values.flatten())
    }
}