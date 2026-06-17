package com.example.routinetaskmanager.featureReminder.presentation.common.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.routinetaskmanager.R
import com.example.routinetaskmanager.core.presentation.ui.CommonIconButton
import com.example.routinetaskmanager.core.presentation.ui.CommonTextFiled

@Composable
fun InstructionsTextField(
    modifier: Modifier = Modifier,
    placeholder : String,
    value : String,
    onValueChange : (String) -> Unit,
    checkboxTextFields : @Composable (ColumnScope.() -> Unit) = {},
    additionalBottomIcon : @Composable (RowScope.() -> Unit) = {},
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    onTakePictureClick : () -> Unit
){
    Card(
        colors = CardDefaults.cardColors(
            contentColor = MaterialTheme.colorScheme.primary,
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = modifier,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            CommonTextFiled (
                placeholder = placeholder,
                value = value,
                onValueChange = onValueChange,
                singleLine = false,
                minLines = 3,
                maxLines = 6,
                keyboardOptions = keyboardOptions,
                keyboardActions = keyboardActions
            )

            checkboxTextFields()

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {

                additionalBottomIcon()

                CommonIconButton(
                    icon = painterResource(R.drawable.ic_add_photo),
                    contentDescription = stringResource(R.string.add_photo_content_description),
                    tint = MaterialTheme.colorScheme.primary,
                    onClick = onTakePictureClick
                )
            }
        }
    }
}
