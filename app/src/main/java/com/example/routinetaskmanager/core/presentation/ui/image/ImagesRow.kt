package com.example.routinetaskmanager.core.presentation.ui.image

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ImagesRow(
    modifier: Modifier = Modifier,
    imagePaths : List<String>,
    onImageClick: (index: Int) -> Unit,
){
    LazyRow(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        itemsIndexed(
            items = imagePaths,
            key = { _, path -> path }
        ){ index, path ->
            ImageThumbnail(
                imagePath = path,
                onClick = { onImageClick(index) },
                modifier = Modifier.size(96.dp)
            )
        }
    }
}

@Composable
fun ImagesRowWithClearIcons(
    modifier: Modifier = Modifier,
    imagePaths : List<String>,
    onImageClick: (index: Int) -> Unit,
    onDeleteClick: (index: Int) -> Unit
){
    LazyRow(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        itemsIndexed(
            items = imagePaths,
            key = { _, path -> path }
        ){ index, path ->
            ImageThumbnailWithClearIcon (
                imagePath = path,
                onClick = { onImageClick(index) },
                modifier = Modifier.size(96.dp),
                onClearClick = { onDeleteClick(index) }
            )
        }
    }
}