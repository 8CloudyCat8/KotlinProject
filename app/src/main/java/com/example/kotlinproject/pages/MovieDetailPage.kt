package com.example.kotlinproject.pages

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign // Импортируем для выравнивания текста
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.palette.graphics.Palette
import coil.compose.rememberAsyncImagePainter
import com.example.kotlinproject.models.Movie
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.URL

@Composable
fun MovieDetailPage(movie: Movie) {
    val animatable = remember { Animatable(1.1f) }
    val gradientAnimatable = remember { Animatable(0f) }
    val backgroundAnimatables = listOf(remember { Animatable(0f) }, remember { Animatable(0f) }, remember { Animatable(0f) }) // Анимации для альфа каналов цветов фона
    val animationDuration = 600
    var dominantColors by remember { mutableStateOf(listOf(Color(0xFF131313), Color(0xFF131313), Color(0xFF131313))) }
    var textColor by remember { mutableStateOf(Color.White) }

    LaunchedEffect(Unit) {
        launch {
            animatable.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = animationDuration)
            )
        }
        launch {
            gradientAnimatable.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = animationDuration)
            )
        }
    }

    LaunchedEffect(dominantColors) {
        backgroundAnimatables.forEachIndexed { index, animatable ->
            launch {
                animatable.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(durationMillis = animationDuration)
                )
            }
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                    colors = dominantColors.mapIndexed { index, color ->
                        color.copy(alpha = backgroundAnimatables[index].value)
                    }
                )
            )
            .padding(horizontal = 16.dp)
    ) {
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(400.dp)
                    .padding(top = 24.dp)
                    .clip(RoundedCornerShape(16.dp))
            ) {
                Image(
                    painter = rememberAsyncImagePainter("https://image.tmdb.org/t/p/w500${movie.posterPath}"),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer(scaleX = animatable.value, scaleY = animatable.value),
                    contentScale = ContentScale.Crop
                )
                GradientEffect(gradientAnimatable.value)

                LaunchedEffect(movie.posterPath) {
                    extractDominantColors("https://image.tmdb.org/t/p/w500${movie.posterPath}") { colors ->
                        dominantColors = listOf(colors.first, colors.second, Color(0xFF131313))
                        textColor = if (getColorBrightness(colors.second) > 0.5) Color.Black else Color.White
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
            AnimatedText(
                text = movie.title,
                style = MaterialTheme.typography.headlineMedium.copy(color = textColor, fontSize = 28.sp),
                totalAnimationTime = animationDuration - 200
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "Год: ${movie.year}", style = MaterialTheme.typography.bodyMedium.copy(color = textColor, fontSize = 20.sp))
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "Описание:", style = MaterialTheme.typography.bodyMedium.copy(color = textColor, fontSize = 20.sp))
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = movie.description,
                style = MaterialTheme.typography.bodyMedium.copy(color = textColor, fontSize = 18.sp),
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}


@Composable
fun GradientEffect(animationProgress: Float) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height

        val gradientWidth = width * animationProgress
        val gradientHeight = height * animationProgress

        drawRect(
            brush = androidx.compose.ui.graphics.Brush.linearGradient(
                colors = listOf(Color.Transparent, Color(0x80FFFFFF)),
                start = androidx.compose.ui.geometry.Offset(0f, 0f),
                end = androidx.compose.ui.geometry.Offset(gradientWidth, gradientHeight)
            ),
            size = androidx.compose.ui.geometry.Size(width, height)
        )
    }
}

@Composable
fun AnimatedText(text: String, style: androidx.compose.ui.text.TextStyle, totalAnimationTime: Int) {
    var displayedText by remember { mutableStateOf("") }

    LaunchedEffect(text) {
        displayedText = ""
        val delayTime = totalAnimationTime / text.length
        for (char in text) {
            displayedText += char
            kotlinx.coroutines.delay(delayTime.toLong())
        }
    }

    Text(text = displayedText, style = style)
}

private suspend fun extractDominantColors(imageUrl: String, onColorsExtracted: (Pair<Color, Color>) -> Unit) {
    val bitmap = loadImageFromUrl(imageUrl)
    bitmap?.let {
        Palette.from(it).generate { palette ->
            val swatches = palette?.swatches
            if (swatches != null && swatches.size >= 2) {
                val sortedSwatches = swatches.sortedByDescending { it.population }
                val dominantColor = Color(sortedSwatches[0].rgb)
                val secondDominantColor = Color(sortedSwatches[1].rgb)

                val (lightColor, darkColor) = if (getColorBrightness(dominantColor) > getColorBrightness(secondDominantColor)) {
                    Pair(dominantColor, secondDominantColor)
                } else {
                    Pair(secondDominantColor, dominantColor)
                }

                Log.d("DominantColors", "Most frequently occurring colors: $lightColor, $darkColor")
                onColorsExtracted(Pair(lightColor, darkColor))
            }
        }
    }
}

private fun getColorBrightness(color: Color): Float {
    val r = color.red
    val g = color.green
    val b = color.blue
    return (0.299 * r + 0.587 * g + 0.114 * b).toFloat()
}

private suspend fun loadImageFromUrl(url: String): Bitmap? {
    return withContext(Dispatchers.IO) {
        try {
            val inputStream = URL(url).openStream()
            BitmapFactory.decodeStream(inputStream)
        } catch (e: Exception) {
            Log.e("ImageLoadError", "Error loading image from $url: ${e.message}", e)
            null
        }
    }
}
