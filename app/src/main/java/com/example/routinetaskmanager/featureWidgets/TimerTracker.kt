package com.example.routinetaskmanager.featureWidgets

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.routinetaskmanager.R
import com.example.routinetaskmanager.core.ui.CommonIconButton

@Composable
fun TimerTracker(
    modifier : Modifier,
    title : String,
    progressText : String,
    progress : Float,
    onStartClick : () -> Unit,
    onEndClick  : () -> Unit,
    onRepeatClick : () -> Unit
){
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                CommonTracker(
                    modifier = Modifier.weight(1f),
                    progressText = progressText,
                    progress = progress
                )

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.Start
                    ){
                        CommonIconButton(
                            modifier = Modifier
                                .size(35.dp),
                            icon = painterResource(R.drawable.ic_refresh),
                            contentDescription = "refresh",
                            color = MaterialTheme.colorScheme.surfaceDim,
                            onClick = onRepeatClick
                        )
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ){
                        CommonIconButton(
                            modifier = Modifier
                                .size(44.dp),
                            icon = painterResource(R.drawable.ic_play_arrow),
                            contentDescription = "refresh",
                            tint = MaterialTheme.colorScheme.onPrimary,
                            color = MaterialTheme.colorScheme.primary,
                            onClick = onStartClick
                        )
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.Start
                    ){
                        CommonIconButton(
                            modifier = Modifier
                                .size(35.dp),
                            icon = painterResource(R.drawable.ic_pause),
                            contentDescription = "refresh",
                            color = MaterialTheme.colorScheme.surfaceDim,
                            onClick = onEndClick
                        )
                    }
                }
            }
        }
    }
}