package com.example.pexelsapp.presentation.home

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.pexelsapp.R
import com.example.pexelsapp.domain.model.CuratedImage
import com.example.pexelsapp.domain.model.FeaturedCollection
import com.example.pexelsapp.ui.theme.LightGrayBackground
import com.example.pexelsapp.ui.theme.RedBackground
import com.example.pexelsapp.ui.theme.StrongGrayBackground
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    featuredCollections: List<FeaturedCollection>,
    curatedImages: List<CuratedImage>,
    isLoading: Boolean,
    isLoadingMore: Boolean,
    hasMoreItems: Boolean,
    selectedCollection: String?,
    errorMessage: String? = null,
    hasNetworkError: Boolean = false,
    hasRealCachedData: Boolean = false,
    usingFallbackData: Boolean = false,
    currentSearchQuery: String? = null,
    searchDebounceTime: Long = 500L,
    onSearchQueryChanged: (String) -> Unit,
    onSearchSubmitted: (String) -> Unit,
    onClearSearch: () -> Unit,
    onExploreClick: () -> Unit,
    onRefresh: () -> Unit,
    onLoadMoreImages: () -> Unit,
    onRetryClick: () -> Unit = {},
    onFeaturedCollectionClick: (String) -> Unit = {},
    onImageClick: (CuratedImage) -> Unit = {}
) {
    var searchQuery by remember { mutableStateOf(currentSearchQuery ?: "") }
    val context = LocalContext.current
    val hasFeaturedCollections = featuredCollections.isNotEmpty()
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(currentSearchQuery) {
        searchQuery = currentSearchQuery ?: ""
    }

    var debounceJob by remember { mutableStateOf<Job?>(null) }

    val shouldShowNetworkStub = hasNetworkError &&
            curatedImages.isEmpty() &&
            !hasRealCachedData

    val shouldShowEmptyState = curatedImages.isEmpty() &&
            !isLoading &&
            !shouldShowNetworkStub

    LaunchedEffect(hasNetworkError, hasRealCachedData) {
        if (hasNetworkError && hasRealCachedData && curatedImages.isNotEmpty()) {
            android.widget.Toast.makeText(
                context,
                "Отсутствует соединение. Показаны данные из кэша.",
                android.widget.Toast.LENGTH_SHORT
            ).show()
        }
    }

    LaunchedEffect(errorMessage) {
        if (errorMessage != null && !hasNetworkError) {
            android.widget.Toast.makeText(
                context,
                errorMessage,
                android.widget.Toast.LENGTH_LONG
            ).show()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp)
            .padding(top = 24.dp)
    ) {

        SearchBar(
            query = searchQuery,
            onQueryChanged = { query ->
                searchQuery = query
                debounceJob?.cancel()
                debounceJob = coroutineScope.launch {
                    delay(searchDebounceTime)
                    onSearchQueryChanged(query)
                }
            },
            onSearchSubmitted = { query ->
                debounceJob?.cancel()
                onSearchSubmitted(query)
            },
            onClearClick = {
                searchQuery = ""
                debounceJob?.cancel()
                onClearSearch()
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
        )

        // Progress Bar (AC6)
        if (isLoading) {
            HorizontalProgressBar(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = if (hasFeaturedCollections) 16.dp else 8.dp)
            )
        }

        // показываем если есть данные И не показываем Stub
        if (hasFeaturedCollections && !shouldShowNetworkStub && !isLoading) {
            FeaturedCollectionsRow(
                collections = featuredCollections,
                selectedCollectionId = selectedCollection,
                onCollectionClick = { collection ->
                    onFeaturedCollectionClick(collection.title)
                },
                modifier = Modifier.padding(bottom = 11.dp)
            )
        } else if (isLoading && !hasFeaturedCollections) {
            HorizontalProgressBar(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp)
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            when {
                // показываем  Stub
                shouldShowNetworkStub -> {
                    NetworkErrorStub(
                        modifier = Modifier.fillMaxSize(),
                        onRetryClick = onRetryClick
                    )
                }

                shouldShowEmptyState -> {
                    EmptyStateStub(
                        modifier = Modifier.fillMaxSize(),
                        onExploreClick = {
                            onClearSearch()
                            onExploreClick()
                        },
                        onClearSearch = {
                            searchQuery = ""
                            onClearSearch()
                        }
                    )
                }

                // AC14: данные из кэша и сети
                else -> {
                    Column(modifier = Modifier.fillMaxSize()) {
                        // Error message display (non-network errors)
                        if (errorMessage != null && !hasNetworkError) {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 16.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.errorContainer
                                )
                            ) {
                                Text(
                                    text = errorMessage,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onErrorContainer,
                                    modifier = Modifier.padding(16.dp)
                                )
                            }
                        }

                        if (usingFallbackData) {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 16.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                                )
                            ) {
                                Text(
                                    text = "Using offline data",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(16.dp)
                                )
                            }
                        }

                        CuratedImagesGrid(
                            images = curatedImages,
                            isLoadingMore = isLoadingMore,
                            hasMoreItems = hasMoreItems,
                            onLoadMore = onLoadMoreImages,
                            onImageClick = onImageClick,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SearchBar(
    query: String,
    onQueryChanged: (String) -> Unit,
    onSearchSubmitted: (String) -> Unit,
    onClearClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var currentQuery by remember { mutableStateOf(query) }
    val keyboardController = LocalSoftwareKeyboardController.current

    LaunchedEffect(query) {
        currentQuery = query
    }

    OutlinedTextField(
        value = currentQuery,
        onValueChange = { newQuery ->
            currentQuery = newQuery
            onQueryChanged(newQuery)
        },
        modifier = modifier,
        placeholder = {
            Text(
                text = "Search for photos...",
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )
        },
        textStyle = MaterialTheme.typography.bodyMedium.copy(
            color = Color.Black
        ),
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search",
                tint = RedBackground
            )
        },
        trailingIcon = {
            if (currentQuery.isNotEmpty()) {
                Icon(
                    imageVector = Icons.Default.Clear,
                    contentDescription = "Clear search",
                    modifier = Modifier.clickable {
                        currentQuery = ""
                        onClearClick()
                    },
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        singleLine = true,

        keyboardActions = androidx.compose.foundation.text.KeyboardActions(
            onSearch = {
                keyboardController?.hide()
                onSearchSubmitted(currentQuery)
            }
        ),
        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
            imeAction = androidx.compose.ui.text.input.ImeAction.Search
        ),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = LightGrayBackground,
            unfocusedContainerColor = LightGrayBackground,
            focusedBorderColor = Color.Black,
            unfocusedBorderColor = LightGrayBackground,
            focusedTextColor = Color.Black,
            unfocusedTextColor = Color.Black
        ),
        shape = RoundedCornerShape(50.dp)
    )
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
fun FeaturedCollectionsRow(
    collections: List<FeaturedCollection>,
    selectedCollectionId: String?,
    onCollectionClick: (FeaturedCollection) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 4.dp)
    ) {
        items(collections) { collection ->
            FeaturedCollectionCard(
                collection = collection,
                isSelected = collection.id == selectedCollectionId,
                onClick = {
                    onCollectionClick(collection)
                }
            )
        }
    }
}

@Composable
fun FeaturedCollectionCard(
    collection: FeaturedCollection,
    isSelected: Boolean = false,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isTapped by remember { mutableStateOf(false) }

    val customRedBackground = Color(0xFFBB1020)

    val scale by animateFloatAsState(
        targetValue = if (isTapped) 0.95f else 1f,
        animationSpec = androidx.compose.animation.core.tween(durationMillis = 100),
        label = "collection_card_scale"
    )

    val backgroundColor by animateColorAsState(
        targetValue = if (isSelected) customRedBackground else LightGrayBackground
    )

    LaunchedEffect(isTapped) {
        if (isTapped) {
            kotlinx.coroutines.delay(100)
            isTapped = false
        }
    }

    Surface(
        shape = RoundedCornerShape(50),
        color = backgroundColor,
        modifier = modifier
            .padding(horizontal = 4.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = {
                    isTapped = true
                    onClick()
                }
            )
    ) {
        Box(
            modifier = Modifier
                .padding(horizontal = 20.dp, vertical = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = collection.title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = if (isSelected) Color.White else Color.Black
            )
        }
    }
}

@Composable
fun CuratedImagesGrid(
    images: List<CuratedImage>,
    isLoadingMore: Boolean,
    hasMoreItems: Boolean,
    onLoadMore: () -> Unit,
    onImageClick: (CuratedImage) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyVerticalStaggeredGrid(
        columns = StaggeredGridCells.Fixed(2),
        modifier = modifier,
        contentPadding = PaddingValues(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalItemSpacing = 8.dp
    ) {
        items(images) { image ->
            CuratedImageCard(
                image = image,
                onClick = { onImageClick(image) },
                modifier = Modifier.animateContentSize()
            )
        }

        if (hasMoreItems && !isLoadingMore) {
            item {

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp)
                        .padding(vertical = 8.dp)
                ) {
                    LaunchedEffect(images.size) {
                        if (images.size >= 30 && images.size % 30 == 0) {
                            onLoadMore()
                        }
                    }
                }
            }
        }

        if (isLoadingMore) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(32.dp),
                        color = RedBackground
                    )
                }
            }
        }
    }
}

@Composable
fun CuratedImageCard(
    image: CuratedImage,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isTapped by remember { mutableStateOf(false) }

    val scale by animateFloatAsState(
        targetValue = if (isTapped) 0.95f else 1f,
        animationSpec = androidx.compose.animation.core.tween(durationMillis = 100),
        label = "image_card_scale"
    )

    LaunchedEffect(isTapped) {
        if (isTapped) {
            kotlinx.coroutines.delay(100)
            isTapped = false
        }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = {

                    isTapped = true
                    onClick()
                }
            ),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isTapped) 2.dp else 4.dp
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(image.width.toFloat() / image.height.toFloat()),
            contentAlignment = Alignment.Center
        ) {
            AsyncImage(
                model = image.thumbnailUrl,
                contentDescription = "Photo by ${image.photographer}",
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(image.width.toFloat() / image.height.toFloat()),
                contentScale = ContentScale.Fit,
                placeholder = painterResource(R.drawable.ic_launcher_background),
                error = painterResource(R.drawable.ic_launcher_foreground)
            )
        }
    }
}

@Composable
fun EmptyStateStub(
    modifier: Modifier = Modifier,
    onExploreClick: () -> Unit,
    onClearSearch: () -> Unit
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Text(
                text = "No results found",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = StrongGrayBackground,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 5.dp)
            )

            Text(
                text = "Explore",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = RedBackground,
                modifier = Modifier
                    .clickable {
                        onClearSearch()
                        onExploreClick()
                    }
                    .padding(top = 4.dp)
            )
        }
    }
}

@Composable
fun NetworkErrorStub(
    modifier: Modifier = Modifier,
    onRetryClick: () -> Unit
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.no_network_icon),
                contentDescription = "No network connection",
                modifier = Modifier
                    .size(128.dp)
                    .padding(bottom = 24.dp)
            )

            Text(
                text = "Try Again",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = RedBackground,
                modifier = Modifier
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        onRetryClick()
                    }
                    .padding(vertical = 8.dp)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    val sampleCollections = listOf(
        FeaturedCollection("1", "Nature", "Beautiful landscapes"),
        FeaturedCollection("2", "Architecture", "Modern designs")
    )

    val sampleImages = listOf(
        CuratedImage(
            id = "1",
            photographer = "John Doe",
            imageUrl = "https://picsum.photos/600/800?random=1",
            thumbnailUrl = "https://picsum.photos/300/400?random=1",
            width = 600,
            height = 800,
            tags = listOf("nature")
        )
    )

    HomeScreen(
        featuredCollections = sampleCollections,
        curatedImages = sampleImages,
        isLoading = false,
        isLoadingMore = false,
        hasMoreItems = true,
        selectedCollection = "1",
        currentSearchQuery = "Nature",
        onSearchQueryChanged = {},
        onSearchSubmitted = {},
        onClearSearch = {},
        onExploreClick = {},
        onRefresh = {},
        onLoadMoreImages = {},
        onRetryClick = {},
        onFeaturedCollectionClick = {},
        onImageClick = {}
    )
}