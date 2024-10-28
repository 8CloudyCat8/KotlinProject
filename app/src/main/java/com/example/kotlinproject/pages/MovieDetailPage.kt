package com.example.kotlinproject.pages

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.example.kotlinproject.models.Movie
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun MovieDetailPage(movie: Movie) {
    val animatable = remember { Animatable(1.1f) }
    val gradientAnimatable = remember { Animatable(0f) }
    val animationDuration = 600

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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(400.dp)
                .padding(top = 16.dp)
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
        }

        Spacer(modifier = Modifier.height(24.dp))

        AnimatedText(text = movie.title, style = MaterialTheme.typography.headlineMedium, totalAnimationTime = animationDuration - 200)

        Spacer(modifier = Modifier.height(8.dp))
        Text(text = "Год: ${movie.year}", style = MaterialTheme.typography.bodyMedium)
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = "Описание:", style = MaterialTheme.typography.bodyMedium)
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = movie.description, style = MaterialTheme.typography.bodyMedium)
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
            delay(delayTime.toLong())
        }
    }

    Text(text = displayedText, style = style)
}
