package com.okhamzina.routinetaskmanager.core.presentation.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.okhamzina.routinetaskmanager.R
import com.okhamzina.routinetaskmanager.featureReminder.domain.model.NotificationMode

@Composable
fun NotificationSegmentedButton(
    selectedMode: NotificationMode,
    onButtonClick : (NotificationMode) -> Unit
){
    val isSoundButtonActive = selectedMode == NotificationMode.SOUND
    val isVibrationButtonActive = selectedMode == NotificationMode.VIBRATION
    val isMuteButtonActive = selectedMode == NotificationMode.MUTE

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
            text = stringResource(R.string.notification_mode_sound),
            contentColor = if(isSoundButtonActive) activeButtonContentColor else inactiveButtonContentColor,
            containerColor = if(isSoundButtonActive) activeButtonContainerColor else inactiveButtonContainerColor,
            shape = RoundedCornerShape(topStart = 32.dp, topEnd = 0.dp, bottomStart = 32.dp, bottomEnd = 0.dp)
        ) {
            onButtonClick(NotificationMode.SOUND)
        }

        NotificationButton(
            modifier = Modifier.weight(1f),
            isSelected = isVibrationButtonActive,
            text = stringResource(R.string.notification_mode_vibration),
            contentColor = if(isVibrationButtonActive) activeButtonContentColor else inactiveButtonContentColor,
            containerColor = if(isVibrationButtonActive) activeButtonContainerColor else inactiveButtonContainerColor,
            shape = RectangleShape
        ) {
            onButtonClick(NotificationMode.VIBRATION)
        }

        NotificationButton(
            modifier = Modifier.weight(1f),
            isSelected = isMuteButtonActive,
            text = stringResource(R.string.notification_mode_mute),
            contentColor = if(isMuteButtonActive) activeButtonContentColor else inactiveButtonContentColor,
            containerColor = if(isMuteButtonActive) activeButtonContainerColor else inactiveButtonContainerColor,
            shape = RoundedCornerShape(topStart = 0.dp, topEnd = 32.dp, bottomStart = 0.dp, bottomEnd = 32.dp)
        ) {
            onButtonClick(NotificationMode.MUTE)
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
                    contentDescription = stringResource(R.string.action_check)
                )
            }
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                softWrap = false
            )
        }
    }
}
