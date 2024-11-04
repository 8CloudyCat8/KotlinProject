package com.example.kotlinproject.pages

import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseIn
import androidx.compose.animation.core.EaseOut
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberImagePainter
import com.example.kotlinproject.MovieViewModel
import com.example.kotlinproject.models.Movie
import androidx.compose.ui.platform.LocalContext
import coil.compose.rememberAsyncImagePainter
import kotlinx.coroutines.launch

@SuppressLint("RememberReturnType", "UseOfNonLambdaOffsetOverload")
@Composable
fun HomePage(modifier: Modifier = Modifier, viewModel: MovieViewModel, onMovieClick: (Movie) -> Unit) {
    val context = LocalContext.current
    val favoriteMovies = viewModel.favoriteMovies

    val catEmojis = listOf("🐱", "😺", "😸")
    var currentEmojiIndex by remember { mutableIntStateOf(0) }

    val offsetY = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF131313)),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Избранные",
            fontSize = 40.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier
                .padding(vertical = 16.dp)
                .padding(horizontal = 16.dp)
                .fillMaxWidth(),
            textAlign = TextAlign.Center
        )

        if (favoriteMovies.isEmpty()) {
            Text(
                text = catEmojis[currentEmojiIndex],
                fontSize = 128.sp,
                modifier = Modifier
                    .fillMaxSize()
                    .wrapContentSize(Alignment.Center)
                    .offset(y = offsetY.value.dp)
                    .clickable {
                        currentEmojiIndex = (currentEmojiIndex + 1) % catEmojis.size
                        scope.launch {
                            val bounces = List(7) { i -> -30f * (1 - i * 0.15f) }
                            for ((index, bounceHeight) in bounces.withIndex()) {
                                offsetY.animateTo(
                                    targetValue = bounceHeight,
                                    animationSpec = tween(
                                        durationMillis = 300 - index * 30,
                                        easing = EaseOut
                                    )
                                )
                                offsetY.animateTo(
                                    targetValue = 0f,
                                    animationSpec = tween(
                                        durationMillis = 300 - index * 15,
                                        easing = EaseIn
                                    )
                                )
                            }
                        }
                    }
            )
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp)
            ) {
                items(favoriteMovies, key = { it.id }) { movie ->
                    MovieCard(
                        movie = movie,
                        onClick = { onMovieClick(movie) },
                        onFavoriteClick = { remove ->
                            if (remove) {
                                viewModel.removeFromFavorites(context, movie)
                            } else {
                                viewModel.addToFavorites(context, movie)
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun MovieCard(movie: Movie, onClick: () -> Unit, onFavoriteClick: (Boolean) -> Unit) {
    var isVisible by remember { mutableStateOf(true) }

    val animatedOffsetY by animateFloatAsState(
        targetValue = if (!isVisible) 50f else 0f,
        animationSpec = tween(durationMillis = 500)
    )

    LaunchedEffect(isVisible) {
        if (!isVisible) {
            kotlinx.coroutines.delay(500)
            onFavoriteClick(true)
        }
    }

    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(),
        exit = fadeOut(animationSpec = tween(durationMillis = 500))
    ) {
        Box(
            modifier = Modifier
                .padding(8.dp)
                .size(150.dp, 250.dp)
                .offset(y = animatedOffsetY.dp)
                .animateContentSize()
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
                            .fillMaxSize()
                            .background(
                                brush = Brush.verticalGradient(
                                    colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.8f)),
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
                            if (movie.isFavorite) {
                                isVisible = false
                            } else {
                                onFavoriteClick(false)
                            }
                        },
                        modifier = Modifier.align(Alignment.TopEnd)
                    ) {
                        Icon(
                            imageVector = if (movie.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = if (movie.isFavorite) "Удалить из избранного" else "Добавить в избранное",
                            tint = if (movie.isFavorite) Color(0xFFF7B2CA) else Color.White,
                        )
                    }
                }
            }
        }
    }
}
