package com.example.pexelsapp.presentation.navigation

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.pexelsapp.R


val bottomBarItems = listOf(
    BottomBarScreen.Home,
    BottomBarScreen.Bookmarks
)

@Composable
fun BottomNavigationBar(
    currentScreen: BottomBarScreen,
    onNavigate: (BottomBarScreen) -> Unit
) {

    val indicatorOffset by animateDpAsState(
        targetValue = when (currentScreen) {
            BottomBarScreen.Home -> 75.dp
            BottomBarScreen.Bookmarks -> 235.dp
        },
        animationSpec = tween(durationMillis = 300),
        label = "indicator_animation"
    )

    NavigationBar(
        modifier = Modifier
            .fillMaxWidth()
            .height(70.dp),
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 8.dp)
        ) {

            Box(
                modifier = Modifier
                    .width(50.dp)
                    .height(4.dp)
                    .align(Alignment.TopStart)
                    .offset(x = indicatorOffset)
                    .clip(CircleShape)
                    .background(Color(0xFFFF6B6B))
            )

            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                bottomBarItems.forEach { destination ->
                    NavigationBarItem(
                        selected = currentScreen == destination,
                        onClick = { onNavigate(destination) },
                        icon = {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {

                                Spacer(modifier = Modifier.height(3.dp))

                                Icon(
                                    painter = painterResource(
                                        if (currentScreen == destination)
                                            getActiveIcon(destination)
                                        else
                                            getInactiveIcon(destination)
                                    ),
                                    contentDescription = destination.title,
                                    tint = if (currentScreen == destination)
                                        Color(0xFFFF6B6B) // Красный цвет для активной иконки
                                    else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        },
                        label = null,
                        alwaysShowLabel = false,
                        colors = NavigationBarItemDefaults.colors(

                            selectedTextColor = Color.Transparent,
                            unselectedTextColor = Color.Transparent,
                            indicatorColor = Color.Transparent
                        )
                    )
                }
            }
        }
    }
}

// Helper functions to get icons
private fun getActiveIcon(destination: BottomBarScreen): Int {
    return when (destination) {
        BottomBarScreen.Home -> R.drawable.home_button_active
        BottomBarScreen.Bookmarks -> R.drawable.bookmark_button_active
    }
}

private fun getInactiveIcon(destination: BottomBarScreen): Int {
    return when (destination) {
        BottomBarScreen.Home -> R.drawable.home_button_inactive
        BottomBarScreen.Bookmarks -> R.drawable.bookmark_button_inactive
    }
}