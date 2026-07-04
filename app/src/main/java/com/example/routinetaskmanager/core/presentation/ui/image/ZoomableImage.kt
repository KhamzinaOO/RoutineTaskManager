package com.example.routinetaskmanager.core.presentation.ui.image

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculateCentroid
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateZoom
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
import androidx.compose.ui.input.pointer.pointerInput
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
            .pointerInput(Unit) {
                awaitEachGesture {
                    awaitFirstDown(requireUnconsumed = false)

                    do {
                        val event = awaitPointerEvent()
                        val pressedPointersCount = event.changes.count { it.pressed }

                        if (pressedPointersCount > 1) {
                            val oldScale = scale
                            val newScale = (scale * event.calculateZoom()).coerceIn(minScale, maxScale)
                            val actualZoom = newScale / oldScale
                            val centroid = event.calculateCentroid()
                            val panChange = event.calculatePan()

                            scale = newScale
                            offset = if (newScale > minScale) {
                                (offset - centroid) * actualZoom + centroid + panChange
                            } else {
                                Offset.Zero
                            }

                            event.changes.forEach { change ->
                                change.consume()
                            }
                        } else if (scale > minScale) {
                            offset += event.calculatePan()

                            event.changes.forEach { change ->
                                change.consume()
                            }
                        }
                    } while (event.changes.any { change -> change.pressed })
                }
            }
    )
}
