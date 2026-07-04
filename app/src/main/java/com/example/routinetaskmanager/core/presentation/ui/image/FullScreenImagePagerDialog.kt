package com.example.routinetaskmanager.core.presentation.ui.image

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.zIndex
import com.example.routinetaskmanager.R

@Composable
fun FullscreenImagePagerDialog(
    imagePaths: List<String>,
    initialIndex: Int,
    onDismiss: () -> Unit
) {
    if (imagePaths.isEmpty()) return

    val safeInitialIndex = initialIndex.coerceIn(0, imagePaths.lastIndex)

    val pagerState = rememberPagerState(
        initialPage = safeInitialIndex,
        pageCount = { imagePaths.size }
    )

    var isImageZoomed by remember {
        mutableStateOf(false)
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
                .systemBarsPadding()
        ) {
            HorizontalPager(
                state = pagerState,
                userScrollEnabled = !isImageZoomed,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                ZoomableImage(
                    imagePath = imagePaths[page],
                    onZoomChanged = { isZoomed ->
                        isImageZoomed = isZoomed
                    },
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                )
            }

            IconButton(
                onClick = onDismiss,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(12.dp)
                    .size(48.dp)
                    .zIndex(10f)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = stringResource(R.string.action_close),
                    tint = Color.White
                )
            }
        }
    }
}
