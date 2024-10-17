package com.example.kotlinproject.pages

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.kotlinproject.models.Movie

@Composable
fun MovieDetailPage(movie: Movie) {
    Column(modifier = Modifier.padding(16.dp)) {
        Spacer(modifier = Modifier.height(32.dp))
        Text(text = movie.title, style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = "Год: ${movie.year}", style = MaterialTheme.typography.bodyMedium)
        Text(text = "Режиссер: ${movie.director}", style = MaterialTheme.typography.bodyMedium)
        Text(text = "Актеры: ${movie.actors.joinToString(", ")}", style = MaterialTheme.typography.bodyMedium)
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = movie.description, style = MaterialTheme.typography.bodyMedium)
    }
}
