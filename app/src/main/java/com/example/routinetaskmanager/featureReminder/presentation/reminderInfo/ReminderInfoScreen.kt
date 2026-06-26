package com.example.routinetaskmanager.featureReminder.presentation.reminderInfo

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.absolutePadding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.routinetaskmanager.R
import com.example.routinetaskmanager.core.presentation.ui.CommonButton
import com.example.routinetaskmanager.core.presentation.ui.CommonOutlinedButton
import com.example.routinetaskmanager.core.presentation.ui.TitleText
import com.example.routinetaskmanager.featureReminder.presentation.reminderInfo.model.ReminderInfoIntent
import com.example.routinetaskmanager.featureReminder.presentation.reminderInfo.model.ReminderInfoUiState

@Composable
fun ReminderInfoScreen(
    uiState: ReminderInfoUiState,
    onIntent : (ReminderInfoIntent) -> Unit
) {
    Column(
        modifier = Modifier
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        uiState.nextReminderDateTime?.let {
            NextReminderBlock(
                reminderTime = it,
                onSkipClick = {onIntent(ReminderInfoIntent.OnSkipButtonClick)},
                onDoNowClick = {onIntent(ReminderInfoIntent.OnDoButtonClick)},
                onSkipForTodayClick = {onIntent(ReminderInfoIntent.OnSkipAllForTodayClick)}
            )
        } ?: Text(
            text = stringResource(R.string.no_reminders_found_for_this_week)
        )
    }
}

@Composable
fun NextReminderBlock(
    reminderTime : String,
    onSkipClick: () ->  Unit,
    onDoNowClick: () -> Unit,
    onSkipForTodayClick: () -> Unit
){
    Column(

    ) {
        TitleText(
            text = stringResource(R.string.next_reminder)
        )
        NextReminderInfoCard(
            reminderTime = reminderTime,
            onOutlinedButtonClick = onSkipClick,
            onFilledButtonClick = onDoNowClick
        )
    }
    Button(
        contentPadding = PaddingValues(vertical = 2.dp),
        onClick = onSkipForTodayClick
    ){
        Text(stringResource(R.string.skip_for_today))
    }
}

@Composable
fun NextReminderInfoCard(
    reminderTime : String,
    onOutlinedButtonClick : () -> Unit,
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
                    text = reminderTime,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.secondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CommonOutlinedButton(
                        onClick = onOutlinedButtonClick,
                        text = stringResource(R.string.skip),
                        contentPadding = PaddingValues(
                            8.dp
                        )
                    )
                    CommonButton(
                        onClick = onFilledButtonClick,
                        text = stringResource(R.string.do_now),
                        contentPadding = PaddingValues(
                            8.dp
                        )
                    )
                }
            }
        }
    }
}
