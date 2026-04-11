package com.example.routinetaskmanager.core.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp

@Composable
fun NotificationSegmentedButton(
    onButtonClick : (Int) -> Unit
){
    var isSoundButtonActive by remember { mutableStateOf(true) }
    var isVibrationButtonActive by remember { mutableStateOf(false) }
    var isMuteButtonActive by remember { mutableStateOf(false) }

    val activeButtonContainerColor = MaterialTheme.colorScheme.primaryContainer
    val activeButtonContentColor = MaterialTheme.colorScheme.onPrimaryContainer

    val inactiveButtonContainerColor = MaterialTheme.colorScheme.surface
    val inactiveButtonContentColor = MaterialTheme.colorScheme.onSurface

    Row(
        modifier = Modifier
            .height(40.dp)
            .fillMaxWidth()
    ) {
        NotificationButton(
            modifier = Modifier.weight(1f),
            isSelected = isSoundButtonActive,
            text = "Sound",
            contentColor = if(isSoundButtonActive) activeButtonContentColor else inactiveButtonContentColor,
            containerColor = if(isSoundButtonActive) activeButtonContainerColor else inactiveButtonContainerColor,
            shape = RoundedCornerShape(topStart = 32.dp, topEnd = 0.dp, bottomStart = 32.dp, bottomEnd = 0.dp)
        ) {
            onButtonClick(0)
            isSoundButtonActive = true
            isVibrationButtonActive = false
            isMuteButtonActive = false
        }

        NotificationButton(
            modifier = Modifier.weight(1f),
            isSelected = isVibrationButtonActive,
            text = "Vibration",
            contentColor = if(isVibrationButtonActive) activeButtonContentColor else inactiveButtonContentColor,
            containerColor = if(isVibrationButtonActive) activeButtonContainerColor else inactiveButtonContainerColor,
            shape = RectangleShape
        ) {
            onButtonClick(1)
            isSoundButtonActive = false
            isVibrationButtonActive = true
            isMuteButtonActive = false
        }

        NotificationButton(
            modifier = Modifier.weight(1f),
            isSelected = isMuteButtonActive,
            text = "Mute",
            contentColor = if(isMuteButtonActive) activeButtonContentColor else inactiveButtonContentColor,
            containerColor = if(isMuteButtonActive) activeButtonContainerColor else inactiveButtonContainerColor,
            shape = RoundedCornerShape(topStart = 0.dp, topEnd = 32.dp, bottomStart = 0.dp, bottomEnd = 32.dp)
        ) {
            onButtonClick(2)
            isSoundButtonActive = false
            isVibrationButtonActive = false
            isMuteButtonActive = true
        }
    }
}

@Composable
fun NotificationButton(
    modifier : Modifier,
    isSelected : Boolean,
    text : String,
    contentColor : Color,
    containerColor : Color,
    shape : Shape,
    onClick : () -> Unit
){
    Card(
        modifier = modifier
            .fillMaxSize()
            .clip(shape)
            .clickable(
            onClick = onClick
        ),
        shape = shape,
        colors = CardDefaults.cardColors(
            contentColor = contentColor,
            containerColor = containerColor
        ),
        border = BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outline
        )
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)
        ) {
            if(isSelected) {
                Icon(
                    modifier = Modifier.size(18.dp),
                    imageVector = Icons.Default.Check,
                    contentDescription = "check"
                )
            }
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge
            )
        }
    }
}