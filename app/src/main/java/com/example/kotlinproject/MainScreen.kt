package com.example.kotlinproject

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.example.kotlinproject.pages.HomePage
import com.example.kotlinproject.pages.MovieDetailPage
import com.example.kotlinproject.pages.MovieListPage
import com.example.kotlinproject.pages.SettingsPage
import com.example.kotlinproject.models.Movie

@Composable
fun MainScreen(modifier: Modifier = Modifier) {
    val navItemList = listOf(
        NavItem("Home", Icons.Default.Home),
        NavItem("Movies", Icons.Default.Notifications),
        NavItem("Settings", Icons.Default.Settings)
    )

    var selectedIndex by remember { mutableIntStateOf(0) }
    var selectedMovie by remember { mutableStateOf<Movie?>(null) }

    // Обработка кнопки "Назад"
    BackHandler(enabled = selectedIndex == 3 && selectedMovie != null) {
        selectedMovie = null
        selectedIndex = 1
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar {
                navItemList.forEachIndexed { index, navItem ->
                    NavigationBarItem(
                        selected = selectedIndex == index,
                        onClick = {
                            selectedIndex = index
                        },
                        icon = { Icon(imageVector = navItem.icon, contentDescription = "Icon") },
                        label = { Text(navItem.label) }
                    )
                }
            }
        }
    ) { innerPadding ->
        ContentScreen(modifier = Modifier.padding(innerPadding), selectedIndex, selectedMovie) { movie ->
            selectedMovie = movie
            selectedIndex = 3
        }
    }
}

@Composable
fun ContentScreen(modifier: Modifier = Modifier, selectedIndex: Int, selectedMovie: Movie?, onMovieClick: (Movie) -> Unit) {
    when (selectedIndex) {
        0 -> HomePage(modifier = modifier)
        1 -> MovieListPage(onMovieClick = onMovieClick)
        2 -> SettingsPage(modifier = modifier)
        3 -> {
            if (selectedMovie != null) {
                MovieDetailPage(movie = selectedMovie)
            }
        }
    }
}
