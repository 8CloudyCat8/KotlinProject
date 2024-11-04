package com.example.kotlinproject

import androidx.activity.compose.BackHandler
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.kotlinproject.models.Movie
import com.example.kotlinproject.models.UserPreferences
import com.example.kotlinproject.pages.HomePage
import com.example.kotlinproject.pages.MovieDetailPage
import com.example.kotlinproject.pages.MovieListPage
import com.example.kotlinproject.pages.SettingsPage

@Composable
fun MainScreen(modifier: Modifier = Modifier, userPreferences: UserPreferences) {
    val navItemList = listOf(
        NavItem("Home", Icons.Default.Home),
        NavItem("Movies", Icons.Default.Notifications),
        NavItem("Settings", Icons.Default.Settings)
    )

    var selectedIndex by remember { mutableIntStateOf(0) }
    var selectedMovie by remember { mutableStateOf<Movie?>(null) }

    BackHandler(enabled = selectedIndex == 3 && selectedMovie != null) {
        selectedMovie = null
        selectedIndex = 1
    }

    val isModified by userPreferences.isModifiedFlow.collectAsState(initial = false)

    Scaffold(
        modifier = modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar(
                containerColor = Color(0xFF131313)
            ) {
                navItemList.forEachIndexed { index, navItem ->
                    NavigationBarItem(
                        selected = selectedIndex == index,
                        onClick = {
                            selectedIndex = index
                        },
                        icon = {
                            val iconColor = when {
                                index == 2 && isModified -> Color(0xFFFFA500)
                                selectedIndex == index -> Color(0xFF131313)
                                else -> Color.White
                            }
                            Icon(imageVector = navItem.icon, contentDescription = navItem.label, tint = iconColor)
                        },
                        label = { Text(navItem.label) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color(0xFF131313),
                            selectedTextColor = Color(0xFFFFFFFF),
                            unselectedIconColor = Color.White,
                            unselectedTextColor = Color.White
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        ContentScreen(
            modifier = Modifier.padding(innerPadding),
            selectedIndex = selectedIndex,
            selectedMovie = selectedMovie,
            onMovieClick = { movie ->
                selectedMovie = movie
                selectedIndex = 3
            },
            userPreferences = userPreferences
        ) {
            selectedIndex = 1
        }
    }
}


@Composable
fun ContentScreen(
    modifier: Modifier = Modifier,
    selectedIndex: Int,
    selectedMovie: Movie?,
    onMovieClick: (Movie) -> Unit,
    userPreferences: UserPreferences,
    onNavigateToMovieList: () -> Unit
) {
    val viewModel: MovieViewModel = viewModel(factory = MovieViewModelFactory(userPreferences))

    Box(modifier = modifier.background(Color(0xFF131313))) {
        Crossfade(
            targetState = selectedIndex,
            animationSpec = tween(durationMillis = 1000)
        ) { index ->
            when (index) {
                0 -> HomePage(modifier = Modifier.fillMaxSize(), viewModel = viewModel, onMovieClick = onMovieClick)
                1 -> MovieListPage(onMovieClick = onMovieClick, userPreferences = userPreferences)
                2 -> SettingsPage(
                    modifier = Modifier.fillMaxSize(),
                    userPreferences = userPreferences,
                    onLanguageChanged = {},
                    onNavigateToMovieList = onNavigateToMovieList
                )
                3 -> {
                    selectedMovie?.let {
                        MovieDetailPage(movie = it)
                    }
                }
            }
        }
    }
}
