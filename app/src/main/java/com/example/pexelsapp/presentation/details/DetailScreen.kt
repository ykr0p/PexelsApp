package com.example.pexelsapp.presentation.details

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.pexelsapp.R
import com.example.pexelsapp.domain.model.CuratedImage
import com.example.pexelsapp.ui.theme.LightGrayBackground
import com.example.pexelsapp.ui.theme.RedBackground

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    image: CuratedImage,
    viewModel: DetailViewModel,
    onBackClick: () -> Unit,
    onDownloadClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isBookmarked by viewModel.isBookmarked.collectAsState()
    val context = androidx.compose.ui.platform.LocalContext.current

    val isImageLoading by viewModel.isImageLoading.collectAsState()

    LaunchedEffect(image.id) {
        viewModel.checkBookmarkStatus(image.id)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(top = 18.dp, bottom = 15.dp)
    ) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {

                Box(
                    modifier = Modifier
                        .background(LightGrayBackground, RoundedCornerShape(12.dp))
                        .size(48.dp)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { onBackClick() }
                        .align(Alignment.CenterStart),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.Black
                    )
                }

                Text(
                    text = image.photographer,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Normal,
                    color = Color.Black,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.Center)
                )
            }

            if (isImageLoading) {
                HorizontalProgressBar(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                )
            }

            ZoomableImage(
                image = image,
                viewModel = viewModel,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(vertical = 16.dp)
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .width(180.dp)
                            .background(LightGrayBackground, RoundedCornerShape(50.dp))
                    ) {

                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .background(RedBackground, CircleShape)
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null
                                ) {

                                    viewModel.downloadImage(
                                        context = context,
                                        url = image.imageUrl,
                                        fileName = "${image.photographer}_${image.id}"
                                    )
                                    onDownloadClick()
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.icon_download),
                                contentDescription = "Download",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))

                        Text(
                            text = "Download",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium,
                            color = Color.Black
                        )
                    }


                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .background(LightGrayBackground, CircleShape)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) {

                                viewModel.toggleBookmark(image)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(
                                if (isBookmarked) R.drawable.bookmark_button_active
                                else R.drawable.bookmark_button_inactive
                            ),
                            contentDescription = if (isBookmarked) "Remove bookmark" else "Add bookmark",
                            tint = Color.Unspecified,
                            modifier = Modifier.size(25.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun HorizontalProgressBar(modifier: Modifier = Modifier) {
    LinearProgressIndicator(
        modifier = modifier
            .height(4.dp)
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(2.dp)
            ),
        color = MaterialTheme.colorScheme.primary
    )
}


@Composable
fun ZoomableImage(
    image: CuratedImage,
    viewModel: DetailViewModel,
    modifier: Modifier = Modifier
) {
    var scale by remember { mutableStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    var isGesturing by remember { mutableStateOf(false) }
    var lastGestureTime by remember { mutableStateOf(System.currentTimeMillis()) }

    val context = androidx.compose.ui.platform.LocalContext.current

    val maxScale = 3f
    val minScale = 1f

    LaunchedEffect(isGesturing, lastGestureTime) {
        if (isGesturing) {

            kotlinx.coroutines.delay(500)
            val currentTime = System.currentTimeMillis()
            if (currentTime - lastGestureTime >= 500) {
                isGesturing = false

                scale = 1f
                offset = Offset.Zero
            }
        }
    }

    Box(
        modifier = modifier
    ) {
        AsyncImage(
            model = image.imageUrl,
            contentDescription = "Photo by ${image.photographer}",
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(image.width.toFloat() / image.height.toFloat())
                .clip(RoundedCornerShape(16.dp))
                .pointerInput(Unit) {
                    detectTransformGestures { centroid, pan, zoom, rotation ->
                        isGesturing = true
                        lastGestureTime = System.currentTimeMillis()

                        scale = (scale * zoom).coerceIn(minScale, maxScale)

                        if (scale > 1f) {
                            offset += pan
                        } else {

                            offset = Offset.Zero
                        }
                    }
                }
                .graphicsLayer(
                    scaleX = scale,
                    scaleY = scale,
                    translationX = offset.x,
                    translationY = offset.y
                ),
            contentScale = ContentScale.Fit,
            placeholder = painterResource(R.drawable.ic_launcher_background),
            onSuccess = {
                viewModel.setImageLoading(false)
            },
            onError = {

                android.widget.Toast.makeText(
                    context,
                    "Failed to load image. Please check your internet connection.",
                    android.widget.Toast.LENGTH_LONG
                ).show()
            },

            error = painterResource(R.drawable.ic_launcher_foreground)
        )

        if (scale > 1.1f) {
            Text(
                text = "${(scale * 100).toInt()}%",
                style = MaterialTheme.typography.bodySmall,
                color = Color.White,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .background(Color.Black.copy(alpha = 0.7f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
                    .padding(top = 8.dp, end = 8.dp)
            )
        }
    }
}

private operator fun Offset.plus(other: Offset): Offset {
    return Offset(this.x + other.x, this.y + other.y)
}