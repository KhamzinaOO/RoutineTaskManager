package com.example.routinetaskmanager.featureReminder

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.example.routinetaskmanager.core.ui.CommonButton
import com.example.routinetaskmanager.core.ui.CommonOutlinedButton

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
                    text = "next reminder",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.secondary
                )
                time?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface
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

                ) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.bodyMedium,
                        textDecoration = TextDecoration.Underline,
                        fontWeight = FontWeight.SemiBold
                    )
                    if (reminderTime != null) {
                        Text(
                            text = reminderTime,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CommonOutlinedButton(
                        onClick = onOutlinedButtonClick,
                        text = outlinedButtonText,
                        contentPadding = PaddingValues(
                            8.dp
                        )
                    )
                    CommonButton(
                        onClick = onFilledButtonClick,
                        text = filledButtonText,
                        contentPadding = PaddingValues(
                            8.dp
                        )
                    )
                }
            }
        }
    }
}