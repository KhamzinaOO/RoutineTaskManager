package com.example.routinetaskmanager.featureWidgets

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.routinetaskmanager.core.presentation.ui.CommonButton
import com.example.routinetaskmanager.core.presentation.ui.OvalNumberFieldWithHint

@Composable
fun AmountTracker(
    modifier: Modifier,
    title : String,
    progressText : String,
    progress : Float,
    textFieldHint : String,
    onAddButtonClicked : () -> Unit
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
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    OvalNumberFieldWithHint(
                        hintText = textFieldHint
                    ) {

                    }

                    CommonButton(
                        onClick = onAddButtonClicked,
                        text = "Add"
                    )
                }
            }
        }
    }
}