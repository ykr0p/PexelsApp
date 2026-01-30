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
    val items = bottomBarItems

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .height(70.dp)
    ) {
        val itemWidth = maxWidth / items.size
        val indicatorWidth = 40.dp

        val selectedIndex = items.indexOf(currentScreen)

        val indicatorOffset by animateDpAsState(
            targetValue =
                itemWidth * selectedIndex +
                        itemWidth / 2 -
                        indicatorWidth / 2,
            animationSpec = tween(300),
            label = "indicator"
        )

        NavigationBar(
            modifier = Modifier.fillMaxSize(),
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            Box(modifier = Modifier.fillMaxSize()) {

                // 🔴 ПОЛОСКА
                Box(
                    modifier = Modifier
                        .offset(x = indicatorOffset)
                        .padding(top = 6.dp)
                        .width(indicatorWidth)
                        .height(4.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFF6B6B))
                )

                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    items.forEach { destination ->
                        NavigationBarItem(
                            selected = currentScreen == destination,
                            onClick = { onNavigate(destination) },
                            icon = {
                                Icon(
                                    painter = painterResource(
                                        if (currentScreen == destination)
                                            getActiveIcon(destination)
                                        else
                                            getInactiveIcon(destination)
                                    ),
                                    contentDescription = destination.title,
                                    tint = if (currentScreen == destination)
                                        Color(0xFFFF6B6B)
                                    else
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            },
                            alwaysShowLabel = false,
                            colors = NavigationBarItemDefaults.colors(
                                indicatorColor = Color.Transparent
                            )
                        )
                    }
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