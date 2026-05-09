package com.example.routinetaskmanager.core.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.example.routinetaskmanager.core.model.DropdownMenuItemUi

@Composable
fun CommonDropdownMenu(
    onDismiss: (Int) -> Unit,
    values: List<DropdownMenuItemUi>?,
    selectedId: Int = values?.firstOrNull()?.id ?: 0
) {
    var isDropdownActive by remember { mutableStateOf(false) }
    var currentSelectedId by remember(selectedId, values) {
        mutableIntStateOf(selectedId)
    }
    val selectedName = values
        ?.firstOrNull { it.id == currentSelectedId }
        ?.name
        ?: values?.firstOrNull()?.name

    Column {
        Button(
            modifier = Modifier.height(34.dp),
            shape = RoundedCornerShape(32.dp),
            contentPadding = PaddingValues(start = 16.dp, end = 8.dp),
            onClick = { isDropdownActive = true }
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                selectedName?.let { Text(text = it) }
                Icon(
                    Icons.Default.ArrowDropDown,
                    contentDescription = "down"
                )
            }
        }

        DropdownMenu(
            expanded = isDropdownActive,
            onDismissRequest = {
                isDropdownActive = false
                onDismiss(currentSelectedId)
            }
        ) {
            values?.forEach { item ->
                DropdownMenuItem(
                    text = { Text(text = item.name) },
                    onClick = {
                        currentSelectedId = item.id
                        isDropdownActive = false
                        onDismiss(item.id)
                    }
                )
            }
        }
    }
}

@Composable
fun CommonDropdownMenuLarge(
    onDismiss: (Int) -> Unit,
    values: List<DropdownMenuItemUi>?,
    icon: ImageVector? = null,
    contentDescription: String? = null,
    selectedId: Int = values?.firstOrNull()?.id ?: 0
) {
    var isDropdownActive by remember { mutableStateOf(false) }
    var currentSelectedId by remember(selectedId, values) {
        mutableIntStateOf(selectedId)
    }
    val selectedName = values
        ?.firstOrNull { it.id == currentSelectedId }
        ?.name
        ?: values?.firstOrNull()?.name

    Column {
        Button(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            colors = ButtonDefaults.buttonColors(
                contentColor = MaterialTheme.colorScheme.onSurface,
                containerColor = MaterialTheme.colorScheme.surface
            ),
            shape = RoundedCornerShape(32.dp),
            contentPadding = PaddingValues(start = 16.dp, end = 8.dp),
            onClick = { isDropdownActive = true }
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    if (icon != null) {
                        Icon(
                            icon,
                            contentDescription = contentDescription
                        )
                    } else {
                        Box(modifier = Modifier.size(24.dp))
                    }
                    selectedName?.let { Text(text = it) }
                }
                Icon(
                    Icons.Default.ArrowDropDown,
                    contentDescription = "down"
                )
            }
        }

        DropdownMenu(
            expanded = isDropdownActive,
            onDismissRequest = {
                isDropdownActive = false
                onDismiss(currentSelectedId)
            }
        ) {
            values?.forEach { item ->
                DropdownMenuItem(
                    text = { Text(text = item.name) },
                    onClick = {
                        currentSelectedId = item.id
                        isDropdownActive = false
                        onDismiss(item.id)
                    }
                )
            }
        }
    }
}
