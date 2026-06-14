package com.example.routinetaskmanager.core.presentation.ui.image

import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import coil3.compose.AsyncImage

@Composable
fun ZoomableImage(
    imagePath: String,
    onZoomChanged: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    val minScale = 1f
    val maxScale = 5f

    LaunchedEffect(scale) {
        onZoomChanged(scale > minScale)
    }

    val transformableState = rememberTransformableState { centroid, zoomChange, panChange, _ ->
        val oldScale = scale
        val newScale = (scale * zoomChange).coerceIn(minScale, maxScale)
        val actualZoom = newScale / oldScale

        scale = newScale

        offset = if (newScale > minScale) {
            (offset - centroid) * actualZoom + centroid + panChange
        } else {
            Offset.Zero
        }
    }

    AsyncImage(
        model = imagePath,
        contentDescription = null,
        contentScale = ContentScale.Fit,
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                translationX = offset.x
                translationY = offset.y
                transformOrigin = TransformOrigin(0f, 0f)
            }
            .transformable(transformableState)
    )
}