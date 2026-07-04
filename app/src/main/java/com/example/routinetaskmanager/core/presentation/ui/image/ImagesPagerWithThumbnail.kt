package com.example.routinetaskmanager.core.presentation.ui.image

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.example.routinetaskmanager.R
import kotlinx.coroutines.launch

@Composable
fun ImagePagerWithThumbnail(
    imagePaths : List<String>,
    onClick: (Int) -> Unit,
    modifier: Modifier = Modifier
){
    if (imagePaths.isEmpty()) return

    val pagerState = rememberPagerState(
        pageCount = { imagePaths.size }
    )
    val thumbnailListState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val currentPage = pagerState.currentPage.coerceIn(0, imagePaths.lastIndex)

    LaunchedEffect(imagePaths.size) {
        if (pagerState.currentPage > imagePaths.lastIndex) {
            pagerState.scrollToPage(imagePaths.lastIndex)
        }
    }

    LaunchedEffect(currentPage) {
        thumbnailListState.animateScrollToItem(currentPage)
    }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxWidth()
                .height(260.dp)
        ) { page ->
            ImageCard(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable(onClick = { onClick(page) }),
                imagePath = imagePaths[page]
            )
        }

        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            state = thumbnailListState,
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            itemsIndexed(
                items = imagePaths,
                key = { index, path -> "$index:$path" }
            ){ index, path ->
                val isCurrent = index == currentPage
                val thumbnailShape = RoundedCornerShape(12.dp)
                val selectedModifier = if (isCurrent) {
                    Modifier.border(
                        width = 2.dp,
                        color = MaterialTheme.colorScheme.primary,
                        shape = thumbnailShape
                    )
                } else {
                    Modifier
                }

                ImageThumbnail(
                    modifier = Modifier
                        .size(64.dp)
                        .then(selectedModifier)
                        .padding(2.dp),
                    imagePath = path,
                    onClick = {
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(index)
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun ImageCard(
    modifier: Modifier = Modifier,
    imagePath : String
){
    val context = LocalContext.current

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        )
    ) {
        AsyncImage(
            modifier = Modifier
                .fillMaxSize()
                .padding(4.dp)
                .clip(RoundedCornerShape(12.dp)),
            model = ImageRequest.Builder(context = context)
                .data(imagePath.toUri())
                .crossfade(true)
                .memoryCacheKey(imagePath)
                .build(),
            contentDescription = stringResource(R.string.image_content_description),
            contentScale = ContentScale.Crop
        )
    }
}
