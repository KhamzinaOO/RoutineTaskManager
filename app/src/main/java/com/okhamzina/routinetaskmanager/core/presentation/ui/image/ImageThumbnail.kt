package com.okhamzina.routinetaskmanager.core.presentation.ui.image

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.okhamzina.routinetaskmanager.R
import com.okhamzina.routinetaskmanager.core.presentation.ui.CommonIconButton

@Composable
fun ImageThumbnail(
    modifier : Modifier = Modifier,
    imagePath : String,
    onClick : () -> Unit
    ){
    val context = LocalContext.current
    AsyncImage(
        model = ImageRequest.Builder(context = context)
            .data(imagePath.toUri())
            .crossfade(true)
            .memoryCacheKey(imagePath)
            .build(),
        contentScale = ContentScale.Crop,
        contentDescription = stringResource(R.string.image_content_description),
        modifier = modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)

    )
}

@Composable
fun ImageThumbnailWithClearIcon(
    modifier : Modifier = Modifier,
    imagePath : String,
    onClick : () -> Unit,
    onClearClick : () -> Unit
){
    Box(
        modifier = Modifier.wrapContentSize()
    ){
        ImageThumbnail(
            modifier = modifier,
            imagePath = imagePath,
            onClick = onClick
        )
        CommonIconButton(
            icon = painterResource(R.drawable.ic_clear),
            contentDescription = stringResource(R.string.action_clear),
            tint = MaterialTheme.colorScheme.primary,
            onClick = onClearClick,
            modifier = Modifier
                .size(18.dp)
                .align(Alignment.TopEnd)
        )
    }
}
