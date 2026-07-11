package com.okhamzina.routinetaskmanager.core.presentation.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
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
    text : String,
    contentPadding : PaddingValues = PaddingValues(horizontal = 12.dp)
){
    Button(
        modifier = modifier.height(34.dp),
        shape = shape,
        onClick = onClick,
        enabled = enabled,
        colors = colors,
        contentPadding = contentPadding
    ) {
        Text(
            text = text,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            softWrap = false
        )
    }
}

@Composable
fun CommonButtonWithLeadingIcon(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    enabled: Boolean = true,
    colors: ButtonColors = ButtonDefaults.buttonColors(
        containerColor = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary
    ),
    shape: Shape = CircleShape,
    text: String,
    contentPadding: PaddingValues = PaddingValues(horizontal = 10.dp),
    icon: (@Composable RowScope.() -> Unit)? = null
) {
    Button(
        modifier = modifier.height(34.dp),
        shape = shape,
        onClick = onClick,
        enabled = enabled,
        colors = colors,
        contentPadding = contentPadding
    ) {
        icon?.invoke(this)

        if (icon != null) {
            Spacer(modifier = Modifier.width(4.dp))
        }

        Text(
            text = text,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            softWrap = false
        )
    }
}

@Composable
fun CommonOutlinedButton(
    modifier : Modifier = Modifier,
    onClick : () -> Unit,
    enabled : Boolean = true,
    colors : ButtonColors = ButtonDefaults.outlinedButtonColors(
        containerColor = Color.Transparent,
        contentColor = MaterialTheme.colorScheme.onTertiary,
    ),
    shape : Shape = CircleShape,
    text : String,
    borderColor: Color = MaterialTheme.colorScheme.onTertiary,
    contentPadding : PaddingValues = PaddingValues(horizontal = 12.dp)
){
    OutlinedButton(
        modifier = modifier.height(34.dp),
        shape = shape,
        onClick = onClick,
        enabled = enabled,
        colors = colors,
        contentPadding = contentPadding,
        border = BorderStroke(width = 1.dp, borderColor)
    ) {
        Text(
            text = text,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            softWrap = false
        )
    }
}

@Composable
fun SegmentedButton(
    modifier: Modifier = Modifier,
    isLeftButtonPicked : Boolean,
    leftText: String,
    rightText: String,
    onLeftButtonClick: () -> Unit,
    onRightButtonClick: () -> Unit,
    leftLeadingIcon: Painter? = null,
    rightLeadingIcon: Painter? = null,
) {

    val leftSelected = isLeftButtonPicked
    val rightSelected = !isLeftButtonPicked

    val leftButtonColor = if (leftSelected) {
        ButtonDefaults.buttonColors(
        containerColor = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary
    )} else ButtonDefaults.buttonColors(
        containerColor = MaterialTheme.colorScheme.surfaceDim,
        contentColor = MaterialTheme.colorScheme.secondary
    )

    val rightButtonColor = if (rightSelected) {
        ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        )} else ButtonDefaults.buttonColors(
        containerColor = MaterialTheme.colorScheme.surfaceDim,
        contentColor = MaterialTheme.colorScheme.secondary
        )

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(32.dp))
            .background(MaterialTheme.colorScheme.surfaceDim)
            .padding(4.dp)
            .width(IntrinsicSize.Min),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        CommonButtonWithLeadingIcon(
            modifier = Modifier.weight(1f),
            onClick = {
                onLeftButtonClick()
            },
            colors = leftButtonColor,
            text = leftText,
            contentPadding = PaddingValues(horizontal = 8.dp),
            icon = {
                leftLeadingIcon?.let {
                    Icon(
                        painter = it,
                        contentDescription = null
                    )
                }
            }
        )

        CommonButtonWithLeadingIcon(
            modifier = Modifier.weight(1f),
            onClick = {
                onRightButtonClick()
            },
            colors = rightButtonColor,
            text = rightText,
            contentPadding = PaddingValues(horizontal = 8.dp),
            icon = {
                rightLeadingIcon?.let {
                    Icon(
                        painter = it,
                        contentDescription = null
                    )
                }
            }
        )
    }
}
@Composable
fun CommonIconButton(
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.surface,
    icon : ImageVector,
    contentDescription : String,
    tint : Color  = MaterialTheme.colorScheme.onSurface,
    onClick : () -> Unit
){
    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(color)
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
    color: Color = MaterialTheme.colorScheme.surface,
    icon : Painter,
    contentDescription : String,
    tint : Color = MaterialTheme.colorScheme.secondary,
    onClick : () -> Unit
){
    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(color)
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
