package com.example.routinetaskmanager.navigation.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.routinetaskmanager.R

@Composable
fun BottomNavigationBar(
    items : List<TopRoute>,
    selectedItem : Route,
    onItemClick : (TopRoute) -> Unit
){

    val selectedContainerColor = MaterialTheme.colorScheme.primaryContainer
    val selectedContentColor = MaterialTheme.colorScheme.onPrimaryContainer

    val unselectedContainerColor = MaterialTheme.colorScheme.surface
    val unselectedContentColor = MaterialTheme.colorScheme.onSurface

    BottomAppBar(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RectangleShape),
        containerColor = MaterialTheme.colorScheme.surface
    ){
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            items.forEach { item ->
                BottomAppBarItem(
                    icon = painterResource(item.icon),
                    contentDescription = item.label,
                    containerColor = if(selectedItem == item) selectedContainerColor else unselectedContainerColor,
                    contentColor = if(selectedItem == item) selectedContentColor else unselectedContentColor,
                    text = item.label,
                    onClick = { onItemClick(item) }
                )
            }
        }
    }
}

@Composable
fun BottomAppBarItem(
    icon : Painter,
    contentDescription : String,
    containerColor: Color,
    contentColor : Color,
    text : String,
    onClick : () -> Unit
){
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val animatedContainerColor by animateColorAsState(
        targetValue = if (isPressed) {
            containerColor.darken(0.15f)
        } else {
            containerColor
        },
        label = "BottomAppBarItemContainerColor"
    )

    Column(
        modifier = Modifier.clickable(
            interactionSource = interactionSource,
            indication = null,
            onClick = onClick
        ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .clip(CircleShape)
                .background(animatedContainerColor),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                modifier = Modifier.padding(
                    horizontal = 16.dp,
                    vertical = 4.dp
                ),
                painter = icon,
                contentDescription = contentDescription,
                tint = contentColor
            )
        }
        Text(
            text = text,
            color = contentColor,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold
        )
    }
}

private fun Color.darken(fraction: Float): Color {
    return Color(
        red = red * (1f - fraction),
        green = green * (1f - fraction),
        blue = blue * (1f - fraction),
        alpha = alpha
    )
}