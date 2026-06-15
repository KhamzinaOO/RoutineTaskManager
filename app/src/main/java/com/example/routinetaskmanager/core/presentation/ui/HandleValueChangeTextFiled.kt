package com.example.routinetaskmanager.core.presentation.ui

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.routinetaskmanager.R

@Composable
fun HandleValueChangeTextFiled(
    value: String,
    onValueChange: (String) -> Unit,
    onIncrement : () -> Unit,
    onDecrement : () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        CommonIconButton(
            color = Color.Transparent,
            icon = painterResource(R.drawable.ic_remove),
            contentDescription = "Decrement",
            tint = MaterialTheme.colorScheme.secondary,
            onClick = onDecrement
        )

        OvalNumberField(
            value = value,
            onValueChange = onValueChange
        )

        CommonIconButton(
            color = Color.Transparent,
            icon = Icons.Default.Add,
            contentDescription = "Increment",
            tint = MaterialTheme.colorScheme.secondary,
            onClick = onIncrement
        )
    }
}

@Composable
fun HandleValueChangeTimeTextFiled(
    hours: String,
    minutes: String,
    onHoursChange: (String) -> Unit,
    onMinutesChange: (String) -> Unit,
    onIncrement : () -> Unit,
    onDecrement : () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        CommonIconButton(
            color = Color.Transparent,
            icon = painterResource(R.drawable.ic_remove),
            contentDescription = "Decrement",
            tint = MaterialTheme.colorScheme.secondary,
            onClick = onDecrement
        )

        OvalTimeField(
            hours = hours,
            minutes = minutes,
            onHoursChange = onHoursChange,
            onMinutesChange = onMinutesChange,
        )

        CommonIconButton(
            color = Color.Transparent,
            icon = Icons.Default.Add,
            contentDescription = "Increment",
            tint = MaterialTheme.colorScheme.secondary,
            onClick = onIncrement
        )
    }
}

@Composable
fun OvalNumberFieldWithHint(
    modifier: Modifier = Modifier,
    hintText : String,
    value: String = "0",
    onValueChange: (String) -> Unit,
) {
    BasicTextField(
        value = value,
        onValueChange = { newValue ->
            if (newValue.all { it.isDigit() }) {
                onValueChange(newValue)
            }
        },
        singleLine = true,
        textStyle = MaterialTheme.typography.bodyLarge.copy(
            textAlign = TextAlign.End
        ),
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Number,
            imeAction = ImeAction.Done
        ),
        modifier = modifier
            .width(70.dp)
            .height(34.dp)
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline,
                shape = CircleShape
            )
            .padding(horizontal = 10.dp, vertical = 4.dp),
        decorationBox = { innerTextField ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier.weight(1f)
                ){
                    innerTextField()
                }

                Spacer(modifier = Modifier.width(2.dp))

                Text(
                    modifier = Modifier,
                    text = hintText,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    )
}

@Composable
fun OvalTimeField(
    modifier: Modifier = Modifier,
    hours: String,
    minutes: String,
    onHoursChange: (String) -> Unit,
    onMinutesChange: (String) -> Unit,
) {
    Row(
        modifier = modifier
            .height(34.dp)
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline,
                shape = CircleShape
            )
            .padding(horizontal = 10.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        TimePartField(
            value = hours,
            onValueChange = onHoursChange,
            modifier = Modifier.width(24.dp)
        )

        Text(
            text = ":",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface
        )

        TimePartField(
            value = minutes,
            onValueChange = onMinutesChange,
            modifier = Modifier.width(24.dp)
        )
    }
}

@Composable
private fun TimePartField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    BasicTextField(
        value = value,
        onValueChange = { newValue ->
            if (newValue.length <= 2 && newValue.all { it.isDigit() }) {
                onValueChange(newValue)
            }
        },
        singleLine = true,
        textStyle = MaterialTheme.typography.bodyLarge.copy(
            textAlign = TextAlign.Center
        ),
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Number,
            imeAction = ImeAction.Done
        ),
        modifier = modifier,
        decorationBox = { innerTextField ->
            Box(
                contentAlignment = Alignment.Center
            ) {
                innerTextField()
            }
        }
    )
}
@Composable
fun OvalNumberField(
    modifier: Modifier = Modifier,
    value: String = "0",
    onValueChange: (String) -> Unit,
) {
    BasicTextField(
        value = value,
        onValueChange = { newValue ->
            if (newValue.all { it.isDigit() }) {
                onValueChange(newValue)
            }
        },
        singleLine = true,
        textStyle = MaterialTheme.typography.bodyLarge.copy(
            textAlign = TextAlign.Center
        ),
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Number,
            imeAction = ImeAction.Done
        ),
        modifier = modifier
            .width(70.dp)
            .height(34.dp)
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline,
                shape = CircleShape
            )
            .padding(vertical = 4.dp),
        decorationBox = { innerTextField ->
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                innerTextField()
            }
        }
    )
}

