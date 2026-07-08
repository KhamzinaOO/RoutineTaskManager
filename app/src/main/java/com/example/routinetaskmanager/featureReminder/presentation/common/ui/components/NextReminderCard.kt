package com.example.routinetaskmanager.featureReminder.presentation.common.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.routinetaskmanager.R
import com.example.routinetaskmanager.core.presentation.ui.CommonButton
import com.example.routinetaskmanager.core.presentation.ui.CommonOutlinedButton
import com.example.routinetaskmanager.ui.theme.EerieBlack
import com.example.routinetaskmanager.ui.theme.White

@Composable
fun NextReminderCard(
    time : String? = null,
    label : String,
    reminderTime : String? = null,
    outlinedButtonText : String,
    onOutlinedButtonClick : () -> Unit,
    filledButtonText : String,
    onFilledButtonClick : () -> Unit
){
    val contentColor = EerieBlack

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.tertiary)
    ){
        Column(
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = stringResource(R.string.next_reminder_label),
                    style = MaterialTheme.typography.bodySmall,
                    color = contentColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                time?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodySmall,
                        color = contentColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        softWrap = false
                    )
                }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.bodyMedium,
                        textDecoration = TextDecoration.Underline,
                        fontWeight = FontWeight.SemiBold,
                        color = contentColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (reminderTime != null) {
                        Text(
                            text = reminderTime,
                            style = MaterialTheme.typography.bodySmall,
                            color = contentColor,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                ReminderActionButtons(
                    skipText = outlinedButtonText,
                    onSkipClick = onOutlinedButtonClick,
                    doNowText = filledButtonText,
                    onDoNowClick = onFilledButtonClick,
                    contentColor = contentColor
                )
            }
        }
    }
}

@Composable
fun ReminderActionButtons(
    skipText: String,
    onSkipClick: () -> Unit,
    doNowText: String,
    onDoNowClick: () -> Unit,
    contentColor: Color = MaterialTheme.colorScheme.onTertiary,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .width(IntrinsicSize.Max),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        CommonOutlinedButton(
            modifier = Modifier.weight(1f),
            onClick = onSkipClick,
            text = skipText,
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = Color.Transparent,
                contentColor = contentColor
            ),
            borderColor = contentColor,
            contentPadding = PaddingValues(8.dp)
        )

        CommonButton(
            modifier = Modifier.weight(1f),
            onClick = onDoNowClick,
            text = doNowText,
            colors = ButtonDefaults.buttonColors(
                containerColor = contentColor,
                contentColor = White
            ),
            contentPadding = PaddingValues(8.dp)
        )
    }
}
