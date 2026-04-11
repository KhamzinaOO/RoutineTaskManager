package com.example.routinetaskmanager.core.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp


@Composable
fun CommonButton(
    modifier : Modifier = Modifier,
    onClick : () -> Unit,
    enabled : Boolean = true,
    colors : ButtonColors = ButtonDefaults.buttonColors(
        containerColor = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary
    ),
    shape : Shape = CircleShape,
    text : String
){
    Button(
        modifier = modifier.height(34.dp),
        shape = shape,
        onClick = onClick,
        enabled = enabled,
        colors = colors
    ) {
        Text(
            text = text
        )
    }
}

@Composable
fun SegmentedButton(
    leftText : String,
    rightText : String,
    onLeftButtonClick : () -> Unit,
    onRightButtonClick : () -> Unit
){
    var isLeftButtonPicked by remember { mutableStateOf(false) }

    val leftButtonColor = if(isLeftButtonPicked)
        ButtonDefaults.buttonColors(
        containerColor = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary
    ) else ButtonDefaults.buttonColors(
        containerColor = MaterialTheme.colorScheme.surfaceDim,
        contentColor = MaterialTheme.colorScheme.secondary
    )

    val rightButtonColor = if(!isLeftButtonPicked)
        ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        ) else ButtonDefaults.buttonColors(
        containerColor = MaterialTheme.colorScheme.surfaceDim,
        contentColor = MaterialTheme.colorScheme.secondary
    )
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(32.dp))
            .background(MaterialTheme.colorScheme.surfaceDim)
            .padding(4.dp)
            .width(IntrinsicSize.Min),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        CommonButton(
            modifier = Modifier.weight(1f),
            onClick = {
                onLeftButtonClick()
                isLeftButtonPicked = true
                      },
            colors = leftButtonColor,
            text = leftText)

        CommonButton(
            Modifier.weight(1f),
            onClick = {
                onRightButtonClick()
                isLeftButtonPicked = false
                      },
            colors = rightButtonColor,
            text = rightText
        )
    }
}

@Composable
fun CommonIconButton(
    modifier: Modifier = Modifier,
    icon : ImageVector,
    contentDescription : String,
    tint : Color,
    onClick : () -> Unit
){
    Box(
        modifier = modifier
            .clip(CircleShape)
            .size(40.dp)
            .clickable(
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ){
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = tint
        )
    }
}

@Composable
fun CommonIconButton(
    modifier: Modifier = Modifier,
    icon : Painter,
    contentDescription : String,
    tint : Color,
    onClick : () -> Unit
){
    Box(
        modifier = modifier
            .clip(CircleShape)
            .size(40.dp)
            .clickable(
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ){
        Icon(
            painter = icon,
            contentDescription = contentDescription,
            tint = tint
        )
    }
}

