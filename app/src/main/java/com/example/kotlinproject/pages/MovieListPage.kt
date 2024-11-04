package com.example.kotlinproject.pages

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import com.example.kotlinproject.models.Movie
import com.example.kotlinproject.MovieViewModel
import com.example.kotlinproject.MovieViewModelFactory
import com.example.kotlinproject.models.UserPreferences

@Composable
fun MovieListPage(onMovieClick: (Movie) -> Unit, userPreferences: UserPreferences) {
    val viewModel: MovieViewModel = viewModel(factory = MovieViewModelFactory(userPreferences))

    val context = LocalContext.current

    val lazyGridState = viewModel.lazyGridState

    LaunchedEffect(viewModel.selectedSortOption, viewModel.selectedOrderOption) {
        if (viewModel.movies.isEmpty()) {
            viewModel.initFetchMovies()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF131313))
            .padding(top = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Сортировать по:", modifier = Modifier.padding(end = 8.dp), color = Color.White)
            var expandedSort by remember { mutableStateOf(false) }
            Box {
                val selectedSortLabel = viewModel.sortOptions.find { it.value == viewModel.selectedSortOption }?.label ?: viewModel.selectedSortOption
                Text(
                    text = selectedSortLabel,
                    color = Color.White,
                    modifier = Modifier.clickable { expandedSort = !expandedSort }
                )
                DropdownMenu(expanded = expandedSort, onDismissRequest = { expandedSort = false }) {
                    viewModel.sortOptions.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option.label) },
                            onClick = {
                                if (viewModel.selectedSortOption != option.value) {
                                    viewModel.selectedSortOption = option.value
                                    viewModel.initFetchMovies()
                                }
                                expandedSort = false
                            }
                        )
                    }
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Порядок:", modifier = Modifier.padding(end = 8.dp), color = Color.White)
            var expandedOrder by remember { mutableStateOf(false) }
            Box {
                val selectedOrderLabel = viewModel.orderOptions.find { it.value == viewModel.selectedOrderOption }?.label ?: viewModel.selectedOrderOption
                Text(
                    text = selectedOrderLabel,
                    color = Color.White,
                    modifier = Modifier.clickable { expandedOrder = !expandedOrder }
                )
                DropdownMenu(expanded = expandedOrder, onDismissRequest = { expandedOrder = false }) {
                    viewModel.orderOptions.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option.label) },
                            onClick = {
                                if (viewModel.selectedOrderOption != option.value) {
                                    viewModel.selectedOrderOption = option.value
                                    viewModel.initFetchMovies()
                                }
                                expandedOrder = false
                            }
                        )
                    }
                }
            }
        }

        if (viewModel.isLoading) {
            CircularProgressIndicator(modifier = Modifier.padding(16.dp))
        } else if (viewModel.errorMessage != null) {
            Text(text = "Ошибка: ${viewModel.errorMessage}", color = MaterialTheme.colorScheme.error)
        } else {
            LazyVerticalGrid(
                state = lazyGridState,
                columns = GridCells.Fixed(2),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 8.dp)
                    .padding(top = 20.dp),
                content = {
                    items(viewModel.movies) { movie ->
                        MovieListItem(
                            movie = movie,
                            onClick = { onMovieClick(movie) },
                            onFavoriteClick = {
                                if (viewModel.isFavorite(movie)) {
                                    viewModel.removeFromFavorites(context, movie)
                                } else {
                                    viewModel.addToFavorites(context, movie)
                                }
                            }
                        )
                    }
                    item {
                        if (viewModel.isLoading) {
                            CircularProgressIndicator(modifier = Modifier.padding(16.dp))
                        } else {
                            LaunchedEffect(viewModel.currentPage) {
                                viewModel.loadMoreMovies { newMovies ->
                                    viewModel.movies += newMovies
                                }
                            }
                        }
                    }
                }
            )
        }
    }
}

@Composable
fun MovieListItem(movie: Movie, onClick: () -> Unit, onFavoriteClick: () -> Unit) {
    fun getBackgroundColorBasedOnRating(rating: Double): Color {
        val red = Color(0xFFFF0000)
        val green = Color(0xFF00FF00)
        val fraction = (rating / 10).toFloat()
        return Color(
            red = ((1 - fraction) * red.red + fraction * green.red),
            green = ((1 - fraction) * red.green + fraction * green.green),
            blue = ((1 - fraction) * red.blue + fraction * green.blue),
            alpha = 0.7f
        )
    }

    var isAnimating by remember { mutableStateOf(false) }
    var animationCount by remember { mutableStateOf(0) }

    val heartScale by animateFloatAsState(
        targetValue = if (isAnimating) 1.5f else 1f,
        animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing)
    )
    val heartAlpha by animateFloatAsState(
        targetValue = if (isAnimating) 0.6f else 1f,
        animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing)
    )

    Box(
        modifier = Modifier
            .padding(8.dp)
            .size(150.dp, 250.dp)
    ) {
        Card(
            modifier = Modifier
                .fillMaxSize()
                .clickable(onClick = onClick),
            shape = MaterialTheme.shapes.medium
        ) {
            Box(
                modifier = Modifier.fillMaxSize()
            ) {
                Image(
                    painter = rememberAsyncImagePainter("https://image.tmdb.org/t/p/w500${movie.posterPath}"),
                    contentDescription = movie.title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                Box(
                    modifier = Modifier
                        .padding(8.dp)
                        .background(
                            color = getBackgroundColorBasedOnRating(movie.voteAverage),
                            shape = MaterialTheme.shapes.small
                        )
                        .border(
                            1.dp,
                            Color.White.copy(alpha = 0.7f),
                            shape = MaterialTheme.shapes.small
                        )
                        .align(Alignment.TopStart)
                        .padding(horizontal = 4.dp, vertical = 0.dp)
                ) {
                    Text(
                        text = "★ %.1f".format(movie.voteAverage),
                        color = Color.White,
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(4.dp)
                    )
                }

                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color.Black.copy(alpha = 0.4f),
                                    Color.Black.copy(alpha = 0.8f),
                                    Color.Black
                                ),
                                startY = 0f,
                                endY = 700f
                            )
                        )
                )

                Text(
                    text = movie.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(8.dp)
                )

                IconButton(
                    onClick = {
                        isAnimating = true
                        animationCount += 1
                        onFavoriteClick()
                    },
                    modifier = Modifier.align(Alignment.TopEnd)
                ) {
                    Icon(
                        imageVector = if (movie.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = if (movie.isFavorite) "Удалить из избранного" else "Добавить в избранное",
                        tint = if (movie.isFavorite) Color(0xFFF7B2CA) else Color.White,
                        modifier = Modifier.scale(heartScale).alpha(heartAlpha)
                    )
                }

                LaunchedEffect(animationCount) {
                    if (isAnimating) {
                        repeat(2) {
                            kotlinx.coroutines.delay(200)
                            isAnimating = false
                            kotlinx.coroutines.delay(200)
                            isAnimating = true
                        }
                        isAnimating = false
                    }
                }
            }
        }
    }
}