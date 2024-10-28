package com.example.kotlinproject.pages

import android.annotation.SuppressLint
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.rememberImagePainter
import com.example.kotlinproject.models.Movie
import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonNull
import com.google.gson.JsonObject
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.security.cert.X509Certificate
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

data class SortOption(val value: String, val label: String)

@Composable
fun MovieListPage(onMovieClick: (Movie) -> Unit) {
    var movies by remember { mutableStateOf<List<Movie>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var currentPage by remember { mutableStateOf(1) }

    var selectedSortOption by remember { mutableStateOf(SortOption("popularity", "Популярность")) }
    var selectedOrderOption by remember { mutableStateOf(SortOption("desc", "По убыванию")) }

    val sortOptions = listOf(
        SortOption("popularity", "Популярность"),
        SortOption("original_title", "Оригинальное название"),
        SortOption("revenue", "Выручка"),
        SortOption("title", "Название"),
        SortOption("primary_release_date", "Дата релиза"),
        SortOption("vote_average", "Средняя оценка"),
        SortOption("vote_count", "Количество голосов")
    )

    val orderOptions = listOf(
        SortOption("desc", "По убыванию"),
        SortOption("asc", "По возрастанию")
    )

    LaunchedEffect(selectedSortOption.value, selectedOrderOption.value) {
        isLoading = true
        val sortOption = "${selectedSortOption.value}.${selectedOrderOption.value}"
        fetchMovies(currentPage, sortOption) { result, error ->
            if (result != null) {
                movies = result
            }
            isLoading = false
            errorMessage = error
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(top = 16.dp)) {
        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 16.dp), verticalAlignment = Alignment.CenterVertically) {
            Text("Сортировать по:", modifier = Modifier.padding(end = 8.dp))
            var expandedSort by remember { mutableStateOf(false) }
            Box {
                Text(selectedSortOption.label, Modifier.clickable { expandedSort = !expandedSort })
                DropdownMenu(expanded = expandedSort, onDismissRequest = { expandedSort = false }) {
                    sortOptions.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option.label) },
                            onClick = {
                                selectedSortOption = option
                                expandedSort = false
                                currentPage = 1
                                movies = emptyList()
                            },
                            modifier = Modifier,
                            interactionSource = remember { MutableInteractionSource() }
                        )
                    }
                }
            }
        }

        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            Text("Порядок:", modifier = Modifier.padding(end = 8.dp))
            var expandedOrder by remember { mutableStateOf(false) }
            Box {
                Text(selectedOrderOption.label, Modifier.clickable { expandedOrder = !expandedOrder })
                DropdownMenu(expanded = expandedOrder, onDismissRequest = { expandedOrder = false }) {
                    orderOptions.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option.label) },
                            onClick = {
                                selectedOrderOption = option
                                expandedOrder = false
                                currentPage = 1
                                movies = emptyList()
                            },
                            modifier = Modifier,
                            interactionSource = remember { MutableInteractionSource() }
                        )
                    }
                }
            }
        }

        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.padding(16.dp))
        } else if (errorMessage != null) {
            Text(text = "Ошибка: $errorMessage", color = MaterialTheme.colorScheme.error)
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 20.dp),
                content = {
                    items(movies) { movie ->
                        MovieListItem(movie = movie, onClick = { onMovieClick(movie) })
                    }

                    item {
                        if (isLoading) {
                            CircularProgressIndicator(modifier = Modifier.padding(16.dp))
                        } else {
                            LaunchedEffect(currentPage) {
                                loadMoreMovies(currentPage, movies.size, "${selectedSortOption.value}.${selectedOrderOption.value}") { newMovies ->
                                    if (newMovies.isNotEmpty()) {
                                        movies = movies + newMovies
                                        currentPage++
                                    }
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
fun MovieListItem(movie: Movie, onClick: () -> Unit) {
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

    Card(
        modifier = Modifier
            .padding(8.dp)
            .clickable(onClick = onClick)
            .size(150.dp, 250.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            Image(
                painter = rememberImagePainter("https://image.tmdb.org/t/p/w500${movie.posterPath}"),
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
                    .border(1.dp, Color.White.copy(alpha = 0.7f), shape = MaterialTheme.shapes.small)
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
        }
    }
}

private fun fetchMovies(page: Int, sortOption: String, callback: (List<Movie>?, String?) -> Unit) {
    val trustAllCerts = object : X509TrustManager {
        @SuppressLint("TrustAllX509TrustManager")
        override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) {}
        @SuppressLint("TrustAllX509TrustManager")
        override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) {}
        override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
    }

    val sslContext = SSLContext.getInstance("SSL").apply {
        init(null, arrayOf<TrustManager>(trustAllCerts), java.security.SecureRandom())
    }

    val client = OkHttpClient.Builder()
        .sslSocketFactory(sslContext.socketFactory, trustAllCerts)
        .hostnameVerifier { _, _ -> true }
        .build()

    Thread {
        val url = "https://api.themoviedb.org/3/discover/movie?include_adult=false&include_video=false&language=ru-RU&page=$page&sort_by=$sortOption"
        val request = Request.Builder()
            .url(url)
            .addHeader("accept", "application/json")
            .addHeader("Authorization", "Bearer eyJhbGciOiJIUzI1NiJ9.eyJhdWQiOiIyMTU3ZDhkZWMwMDdkMmE2NTk0ODMxODk1NTI4ZTE0MCIsIm5iZiI6MTczMDA0MjU1My42NDk0MjEsInN1YiI6IjY3MWNjMjhlMjdiZDU3ZDkxZjYyYzM2YSIsInNjb3BlcyI6WyJhcGlfcmVhZCJdLCJ2ZXJzaW9uIjoxfQ.eINlcwN5wuiMORzser26fsUOvfE3RpL-8bMnxhexSxs")
            .build()

        try {
            val response: Response = client.newCall(request).execute()

            if (response.isSuccessful) {
                val responseData = response.body?.string() ?: return@Thread callback(null, "Пустой ответ")
                val jsonObject = Gson().fromJson(responseData, JsonObject::class.java)
                val jsonArray: JsonArray = jsonObject.getAsJsonArray("results")

                val movieList = mutableListOf<Movie>()
                for (movie in jsonArray) {
                    val movieObj = movie.asJsonObject

                    val title = if (movieObj.has("title") && movieObj.get("title") != JsonNull.INSTANCE) {
                        movieObj.get("title").asString
                    } else {
                        "Неизвестно"
                    }

                    val overview = if (movieObj.has("overview") && movieObj.get("overview") != JsonNull.INSTANCE) {
                        movieObj.get("overview").asString
                    } else {
                        "Нет описания"
                    }

                    val releaseDate = if (movieObj.has("release_date") && movieObj.get("release_date") != JsonNull.INSTANCE) {
                        movieObj.get("release_date").asString
                    } else {
                        ""
                    }

                    val year = if (releaseDate.length >= 4) {
                        releaseDate.substring(0, 4).toIntOrNull() ?: 0
                    } else {
                        0
                    }

                    val voteAverage = if (movieObj.has("vote_average") && movieObj.get("vote_average") != JsonNull.INSTANCE) {
                        movieObj.get("vote_average").asDouble
                    } else {
                        0.0
                    }

                    val posterPath = if (movieObj.has("poster_path") && movieObj.get("poster_path") != JsonNull.INSTANCE) {
                        movieObj.get("poster_path").asString
                    } else {
                        ""
                    }

                    movieList.add(
                        Movie(
                            id = movieObj.get("id").asInt,
                            title = title,
                            year = year,
                            director = "",
                            actors = emptyList(),
                            description = overview,
                            posterPath = posterPath,
                            voteAverage = voteAverage
                        )
                    )
                }

                callback(movieList, null)
            } else {
                callback(null, "Ошибка: ${response.code}")
            }
        } catch (e: Exception) {
            callback(null, "Ошибка: ${e.message}")
        }
    }.start()
}

private fun loadMoreMovies(currentPage: Int, currentMoviesCount: Int, sortOption: String, callback: (List<Movie>) -> Unit) {
    fetchMovies(currentPage + 1, sortOption) { newMovies, _ ->
        if (newMovies != null && newMovies.isNotEmpty()) {
            callback(newMovies)
        } else {
            callback(emptyList())
        }
    }
}