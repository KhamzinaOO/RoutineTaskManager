package com.example.routinetaskmanager.core.presentation.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.routinetaskmanager.core.presentation.model.DropdownMenuItemUi

@Composable
fun CommonDropdownMenu(
    values: List<DropdownMenuItemUi>,
    selectedId: Int?,
    onItemClick: (DropdownMenuItemUi) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    CommonDropdownMenuBase(
        values = values,
        selectedId = selectedId,
        onItemClick = onItemClick,
        modifier = modifier,
        enabled = enabled,
        height = 34.dp,
        fillMaxWidth = false,
        leadingIcon = null,
        leadingIconContentDescription = null,
        buttonColors = ButtonDefaults.buttonColors()
    )
}

@Composable
fun CommonDropdownMenuLarge(
    values: List<DropdownMenuItemUi>,
    selectedId: Int?,
    onItemClick: (DropdownMenuItemUi) -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    contentDescription: String? = null,
    enabled: Boolean = true
) {
    CommonDropdownMenuBase(
        values = values,
        selectedId = selectedId,
        onItemClick = onItemClick,
        modifier = modifier.fillMaxWidth(),
        enabled = enabled,
        height = 48.dp,
        fillMaxWidth = true,
        leadingIcon = icon,
        leadingIconContentDescription = contentDescription,
        buttonColors = ButtonDefaults.buttonColors(
            contentColor = MaterialTheme.colorScheme.onSurface,
            containerColor = MaterialTheme.colorScheme.surface
        )
    )
}

@Composable
private fun CommonDropdownMenuBase(
    values: List<DropdownMenuItemUi>,
    selectedId: Int?,
    onItemClick: (DropdownMenuItemUi) -> Unit,
    modifier: Modifier,
    enabled: Boolean,
    height: Dp,
    fillMaxWidth: Boolean,
    leadingIcon: ImageVector?,
    leadingIconContentDescription: String?,
    buttonColors: ButtonColors
) {
    var expanded by remember { mutableStateOf(false) }

    val selectedItem = values.firstOrNull { it.id == selectedId }
    val isEnabled = enabled && values.isNotEmpty()

    Box(modifier = modifier) {
        Button(
            modifier = Modifier
                .then(
                    if (fillMaxWidth) Modifier.fillMaxWidth()
                    else Modifier.widthIn(min = 72.dp)
                )
                .height(height),
            enabled = isEnabled,
            shape = RoundedCornerShape(32.dp),
            colors = buttonColors,
            contentPadding = PaddingValues(start = 12.dp, end = 8.dp),
            onClick = { expanded = true }
        ) {
            DropdownButtonContent(
                text = selectedItem?.name.orEmpty(),
                fillMaxWidth = fillMaxWidth,
                leadingIcon = leadingIcon,
                leadingIconContentDescription = leadingIconContentDescription
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = {
                expanded = false
            }
        ) {
            values.forEach { item ->
                val selected = item.id == selectedId

                DropdownMenuItem(
                    text = {
                        Text(
                            text = item.name,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    trailingIcon = {
                        if (selected) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null
                            )
                        }
                    },
                    onClick = {
                        expanded = false
                        onItemClick(item)
                    }
                )
            }
        }
    }
}

@Composable
private fun DropdownButtonContent(
    text: String,
    fillMaxWidth: Boolean,
    leadingIcon: ImageVector?,
    leadingIconContentDescription: String?
) {
    if (fillMaxWidth) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (leadingIcon != null) {
                    Icon(
                        imageVector = leadingIcon,
                        contentDescription = leadingIconContentDescription
                    )
                } else {
                    Spacer(modifier = Modifier.size(24.dp))
                }

                Text(
                    text = text,
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    softWrap = false
                )
            }

            Icon(
                imageVector = Icons.Default.ArrowDropDown,
                contentDescription = null
            )
        }
    } else {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = text,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                softWrap = false
            )

            Icon(
                imageVector = Icons.Default.ArrowDropDown,
                contentDescription = null
            )
        }
    }
}
