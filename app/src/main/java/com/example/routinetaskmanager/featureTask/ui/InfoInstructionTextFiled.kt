package com.example.routinetaskmanager.featureTask.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.routinetaskmanager.R
import com.example.routinetaskmanager.core.presentation.ui.CommonIconButton
import com.example.routinetaskmanager.featureReminder.presentation.common.ui.components.InstructionsTextField
import com.example.routinetaskmanager.featureTask.model.CheckboxFiledValue

@Composable
fun InfoInstructionsTextField(
    modifier: Modifier = Modifier,
    placeholder: String,
    value: String,
    onValueChange: (String) -> Unit,
    values: List<CheckboxFiledValue>,
    isCheckboxEnabled: Boolean,
    onCheckboxValueChange: (CheckboxFiledValue) -> Unit,
    onCheckChange: (Boolean) -> Unit,
    onAddCheckboxClick: () -> Unit,
    onCheckboxIconClick : () -> Unit,
    onDeleteClick: (CheckboxFiledValue) -> Unit,
    onTakePictureClick : () -> Unit
){

    InstructionsTextField(
        modifier = modifier,
        placeholder = placeholder,
        value = value,
        onValueChange = onValueChange,
        checkboxTextFields = {
            if(isCheckboxEnabled){
                CheckboxInstructions(
                    values = values,
                    onValueChange = onCheckboxValueChange,
                    onAddCheckboxClick = onAddCheckboxClick,
                    onCheckChange = onCheckChange,
                    onDeleteClick = onDeleteClick
                )
            }
        },
        additionalBottomIcon = {
            CommonIconButton(
                icon = painterResource(R.drawable.ic_check_box),
                contentDescription = "Checkbox",
                tint = MaterialTheme.colorScheme.primary,
                onClick = onCheckboxIconClick
            )
        },
        onTakePictureClick = onTakePictureClick
    )
}



@Composable
fun CheckboxInstructions(
    values : List<CheckboxFiledValue>,
    onValueChange: (CheckboxFiledValue) -> Unit,
    onCheckChange: (Boolean) -> Unit,
    onAddCheckboxClick : () -> Unit,
    onDeleteClick : (CheckboxFiledValue) -> Unit
){
    Column(

    ) {
        values.forEach {value ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = value.isChecked,
                    onCheckedChange = { onCheckChange(it) }
                )

                TextField(
                    modifier = Modifier.weight(1f),
                    value = value.text,
                    colors = TextFieldDefaults.colors(
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        focusedContainerColor = MaterialTheme.colorScheme.background
                    ),
                    onValueChange = { newValue ->
                        onValueChange(value.copy(text = newValue))
                    }
                )

                CommonIconButton(
                    icon = Icons.Default.Delete,
                    color = Color.Transparent,
                    contentDescription = "Delete",
                    tint = MaterialTheme.colorScheme.secondary,
                    onClick = { onDeleteClick(value) }
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier
                    .padding(4.dp)
                    .clip(CircleShape)
                    .clickable(
                        onClick = onAddCheckboxClick
                    ),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ){
                Text("Add checkbox instruction")
                Box(
                    modifier = Modifier.size(50.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Add,
                        "Add"
                    )
                }
            }
        }
    }
}