package com.example.pexelsapp

import android.view.View
import android.view.WindowManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.delay
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.pexelsapp.domain.model.CuratedImage
import com.example.pexelsapp.presentation.bookmarks.BookmarkScreen
import com.example.pexelsapp.presentation.details.DetailScreen
import com.example.pexelsapp.presentation.home.HomeScreen
import com.example.pexelsapp.presentation.home.HomeViewModel
import com.example.pexelsapp.presentation.navigation.BottomBarScreen
import com.example.pexelsapp.presentation.navigation.BottomNavigationBar
import com.example.pexelsapp.ui.SplashScreen
import com.example.pexelsapp.ui.theme.PexelsAppTheme
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import org.koin.androidx.compose.koinViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setFlags()
        
        setContent {
            PexelsAppTheme {
                MainScreen()
            }
        }
    }
    
    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            setFlags()
        }
    }
    
    private fun setFlags() {
        window.apply {

            addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
            addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            
            // for API 30+
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                insetsController?.hide(android.view.WindowInsets.Type.navigationBars())
                insetsController?.hide(android.view.WindowInsets.Type.statusBars())
            } else {
                // for old API
                decorView.systemUiVisibility = (
                    View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                            or View.SYSTEM_UI_FLAG_FULLSCREEN
                            or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                )
            }
        }
    }
}

@Composable
fun MainScreen() {
    val context = LocalContext.current
    val systemUiController = rememberSystemUiController()

    LaunchedEffect(Unit) {
        while (true) {
            // Re-apply system UI hiding
            systemUiController.setNavigationBarColor(
                color = androidx.compose.ui.graphics.Color.Transparent,
                darkIcons = true
            )
            systemUiController.setSystemBarsColor(
                color = androidx.compose.ui.graphics.Color.Transparent,
                darkIcons = true
            )
            systemUiController.isNavigationBarVisible = false
            systemUiController.isStatusBarVisible = false

            delay(500)
        }
    }

    // hide native nav
    LaunchedEffect(context) {
        var lastTouchTime = 0L
        val touchListener = View.OnTouchListener { _, event ->
            lastTouchTime = System.currentTimeMillis()

            // Immediately hide navigation bar on touch
            systemUiController.isNavigationBarVisible = false
            systemUiController.isStatusBarVisible = false

            // Re-apply flags
            (context as? ComponentActivity)?.window?.decorView?.systemUiVisibility = (
                    View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                            or View.SYSTEM_UI_FLAG_FULLSCREEN
                            or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    )

            false
        }

        (context as? ComponentActivity)?.window?.decorView?.setOnTouchListener(touchListener)
    }

    val homeViewModel: HomeViewModel = koinViewModel()
    val navController = rememberNavController()
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = currentBackStackEntry?.destination?.route
    val isDetailScreen = currentDestination?.contains("/detail/") == true
    val uiState by homeViewModel.uiState.collectAsState()
    val shouldShowBottomNav = !isDetailScreen && !uiState.isLoading

    Scaffold(
        bottomBar = {
            if (shouldShowBottomNav) {
                val currentScreen = when (currentDestination) {
                    BottomBarScreen.Bookmarks.route -> BottomBarScreen.Bookmarks
                    else -> BottomBarScreen.Home
                }

                BottomNavigationBar(
                    currentScreen = currentScreen,
                    onNavigate = { screen ->
                        navController.navigate(screen.route) {
                            popUpTo(navController.graph.startDestinationId) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = BottomBarScreen.Home.route,
            modifier = Modifier.padding(paddingValues)
        ) {
            composable(BottomBarScreen.Home.route) {
                if (uiState.isLoading) {
                    SplashScreen()
                } else {
                    HomeScreen(
                        featuredCollections = uiState.featuredCollections,
                        curatedImages = uiState.curatedImages,
                        selectedCollection = uiState.selectedCollection,
                        isLoading = uiState.isRefreshing || uiState.isPerformingSearch,
                        isLoadingMore = uiState.isLoadingMore,
                        hasMoreItems = uiState.hasMoreItems,
                        errorMessage = uiState.errorMessage,
                        hasNetworkError = uiState.hasNetworkError,
                        hasRealCachedData = uiState.hasRealCachedData,
                        usingFallbackData = uiState.usingFallbackData,
                        currentSearchQuery = uiState.searchQuery,
                        searchDebounceTime = 500L,
                        onSearchQueryChanged = homeViewModel::onSearchQueryChanged,
                        onSearchSubmitted = homeViewModel::onSearchSubmitted,
                        onClearSearch = homeViewModel::onClearSearch,
                        onExploreClick = homeViewModel::onExploreClick,
                        onRefresh = homeViewModel::onRefresh,
                        onLoadMoreImages = homeViewModel::loadMoreImages,
                        onRetryClick = homeViewModel::onRetryClick,
                        onFeaturedCollectionClick = homeViewModel::onFeaturedCollectionSelected,
                        onImageClick = { image ->
                            navController.navigate("${BottomBarScreen.Home.route}/detail/${image.id}")
                        }
                    )
                }
            }

            composable(
                route = "${BottomBarScreen.Home.route}/detail/{imageId}"
            ) { backStackEntry ->
                val imageId = backStackEntry.arguments?.getString("imageId")
                val image = uiState.curatedImages.find { it.id == imageId }
                    ?: CuratedImage(
                        id = imageId ?: "sample_1",
                        photographer = "Sample Photographer",
                        imageUrl = "https://images.pexels.com/photos/674010/pexels-photo-674010.jpeg",
                        thumbnailUrl = "https://images.pexels.com/photos/674010/pexels-photo-674010.jpeg?w=400",
                        width = 1920,
                        height = 1080,
                        tags = listOf("sample", "test")
                    )

                val detailViewModel: com.example.pexelsapp.presentation.details.DetailViewModel = koinViewModel()

                DetailScreen(
                    image = image,
                    viewModel = detailViewModel,
                    onBackClick = {
                        navController.popBackStack()
                    },
                    onDownloadClick = {
                        println("Download clicked")
                    }
                )
            }

            composable(BottomBarScreen.Bookmarks.route) {
                val bookmarkViewModel: com.example.pexelsapp.presentation.bookmarks.BookmarkViewModel = koinViewModel()

                BookmarkScreen(
                    viewModel = bookmarkViewModel,
                    onImageClick = { image ->
                        val encodedImageUrl = java.net.URLEncoder.encode(image.imageUrl, "UTF-8")
                        val encodedThumbnailUrl = java.net.URLEncoder.encode(image.thumbnailUrl, "UTF-8")
                        val encodedPhotographer = java.net.URLEncoder.encode(image.photographer, "UTF-8")
                        val encodedTags = java.net.URLEncoder.encode(image.tags.joinToString(","), "UTF-8")

                        navController.navigate(
                            "${BottomBarScreen.Bookmarks.route}/detail/${image.id}/$encodedPhotographer/$encodedImageUrl/$encodedThumbnailUrl/${image.width}/${image.height}/$encodedTags"
                        )
                    },
                    onExploreClick = {
                        navController.navigate(BottomBarScreen.Home.route) {
                            popUpTo(navController.graph.startDestinationId) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }

            composable(
                route = "${BottomBarScreen.Bookmarks.route}/detail/{imageId}/{photographer}/{imageUrl}/{thumbnailUrl}/{width}/{height}/{tags}"
            ) { backStackEntry ->
                val imageId = backStackEntry.arguments?.getString("imageId") ?: ""
                val photographer = backStackEntry.arguments?.getString("photographer")?.let {
                    java.net.URLDecoder.decode(it, "UTF-8")
                } ?: "Unknown Photographer"
                val imageUrl = backStackEntry.arguments?.getString("imageUrl")?.let {
                    java.net.URLDecoder.decode(it, "UTF-8")
                } ?: ""
                val thumbnailUrl = backStackEntry.arguments?.getString("thumbnailUrl")?.let {
                    java.net.URLDecoder.decode(it, "UTF-8")
                } ?: ""
                val width = backStackEntry.arguments?.getString("width")?.toIntOrNull() ?: 1920
                val height = backStackEntry.arguments?.getString("height")?.toIntOrNull() ?: 1080
                val tags = backStackEntry.arguments?.getString("tags")?.let {
                    java.net.URLDecoder.decode(it, "UTF-8").split(",").filter { it.isNotEmpty() }
                } ?: emptyList()

                val image = CuratedImage(
                    id = imageId,
                    photographer = photographer,
                    imageUrl = imageUrl,
                    thumbnailUrl = thumbnailUrl,
                    width = width,
                    height = height,
                    tags = tags
                )

                val detailViewModel: com.example.pexelsapp.presentation.details.DetailViewModel = koinViewModel()

                DetailScreen(
                    image = image,
                    viewModel = detailViewModel,
                    onBackClick = {
                        navController.popBackStack()
                    },
                    onDownloadClick = {
                        println("Download clicked")
                    }
                )
            }
        }
    }
}

@Composable
fun MainScreenPreview() {
    PexelsAppTheme {
        MainScreen()
    }
}