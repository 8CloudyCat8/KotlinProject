package com.example.kotlinproject

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kotlinproject.models.Movie
import com.example.kotlinproject.models.UserPreferences
import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonNull
import com.google.gson.JsonObject
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.security.cert.X509Certificate
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

class MovieViewModel(private val userPreferences: UserPreferences) : ViewModel() {
    var movies: SnapshotStateList<Movie> = mutableStateListOf()
    var isLoading by mutableStateOf(false)
    var errorMessage by mutableStateOf<String?>(null)
    var currentPage by mutableIntStateOf(1)
    var selectedSortOption by mutableStateOf("popularity")
    var selectedOrderOption by mutableStateOf("desc")
    var lazyGridState: LazyGridState = LazyGridState()
    var language by mutableStateOf("ru-RU")
    var favoriteMovies = mutableStateListOf<Movie>()

    var minRate by mutableStateOf(0.0f)

    private var isRecentlyAddedToFavorites = false

    private var isRecentlyRemovedFromFavorites = false

    init {
        Log.d("MovieViewModel", "Initializing ViewModel")
        loadFavoriteMovies()

        viewModelScope.launch {
            userPreferences.languageFlow.collect { lang ->
                Log.d("MovieViewModel", "Language changed: $lang")
                language = lang
                initFetchMovies()
            }
        }

        viewModelScope.launch {
            userPreferences.sliderValueFlow.collect { value ->
                Log.d("MovieViewModel", "Slider value changed: $value")
                minRate = value
                initFetchMovies()
            }
        }
    }

    val sortOptions = listOf(
        SortOption("popularity", "Популярность"),
        SortOption("revenue", "Выручка"),
        SortOption("title", "Название"),
        SortOption("primary_release_date", "Дата релиза"),
        SortOption("vote_average", "Средняя оценка"),
        SortOption("vote_count", "Количество голосов"),
    )

    val orderOptions = listOf(
        SortOption("desc", "По убыванию"),
        SortOption("asc", "По возрастанию")
    )

    fun initFetchMovies() {
        if (isRecentlyAddedToFavorites || isRecentlyRemovedFromFavorites) {
            Log.d("MovieViewModel", "Recently added to favorites, skipping fetch.")
            return
        }

        Log.d("MovieViewModel", "Starting initFetchMovies")
        isLoading = true
        errorMessage = null
        currentPage = 1
        val sortOption = "${selectedSortOption}.${selectedOrderOption}"
        Log.d("MovieViewModel", "Fetching movies with sort option: $sortOption")

        viewModelScope.launch {
            fetchMovies(currentPage, sortOption) { result, error ->
                Log.d("MovieViewModel", "fetchMovies callback called")
                if (result != null) {
                    Log.d("MovieViewModel", "Movies fetched: ${result.size}")
                    movies.clear()
                    movies.addAll(result)
                } else {
                    Log.e("MovieViewModel", "Error fetching movies: $error")
                }
                isLoading = false
                errorMessage = error
            }
        }
    }


    private fun loadFavoriteMovies() {
        Log.d("MovieViewModel", "Loading favorite movies")
        viewModelScope.launch {
            userPreferences.favoriteMoviesFlow.collect { favorites ->
                Log.d("MovieViewModel", "Favorite movies loaded: ${favorites.size}")
                favoriteMovies.clear()
                favoriteMovies.addAll(favorites)
            }
        }
    }

    fun addToFavorites(context: Context, movie: Movie) {
        Log.d("MovieViewModel", "Adding movie to favorites: ${movie.title}")
        val updatedMovie = movie.copy(isFavorite = true)
        val index = movies.indexOf(movie)

        if (index != -1) {
            movies[index] = updatedMovie
            Log.d("MovieViewModel", "Updated movie in list: ${movie.title}")
        }

        if (!favoriteMovies.contains(updatedMovie)) {
            favoriteMovies.add(updatedMovie)
            saveFavoriteMovies()
            isRecentlyAddedToFavorites = true

            viewModelScope.launch {
                kotlinx.coroutines.delay(1000)
                isRecentlyAddedToFavorites = false
            }
        }
    }

    fun removeFromFavorites(context: Context, movie: Movie) {
        Log.d("MovieViewModel", "Removing movie from favorites: ${movie.title}")
        val updatedMovie = movie.copy(isFavorite = false)
        if (favoriteMovies.remove(movie)) {
            Log.d("MovieViewModel", "Removed movie from favorites list")
            val index = movies.indexOf(movie)
            if (index != -1) {
                movies[index] = updatedMovie
            }
            saveFavoriteMovies()
            isRecentlyRemovedFromFavorites = true

            viewModelScope.launch {
                kotlinx.coroutines.delay(1000)
                isRecentlyRemovedFromFavorites = false
            }
        }
    }

    private fun saveFavoriteMovies() {
        Log.d("MovieViewModel", "Saving favorite movies to DataStore")
        viewModelScope.launch {
            userPreferences.saveFavoriteMovies(favoriteMovies.toList())
            Log.d("MovieViewModel", "Favorite movies saved")
        }
    }

    fun isFavorite(movie: Movie): Boolean {
        val isFav = favoriteMovies.contains(movie)
        Log.d("MovieViewModel", "Checking if movie is favorite: ${movie.title}, result: $isFav")
        return isFav
    }

    private fun fetchMovies(page: Int, sortOption: String, callback: (List<Movie>?, String?) -> Unit) {
        Log.d("MovieViewModel", "Fetching movies from API, page: $page, sortOption: $sortOption")

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
            val url = "https://api.themoviedb.org/3/discover/movie?include_adult=false&include_video=false&language=$language&page=$page&sort_by=$sortOption&vote_average.gte=$minRate"
            val request = Request.Builder()
                .url(url)
                .addHeader("accept", "application/json")
                .addHeader("Authorization", "Bearer eyJhbGciOiJIUzI1NiJ9.eyJhdWQiOiIyMTU3ZDhkZWMwMDdkMmE2NTk0ODMxODk1NTI4ZTE0MCIsIm5iZiI6MTczMDU4MTY3NC4xOTYwMjgsInN1YiI6IjY3MWNjMjhlMjdiZDU3ZDkxZjYyYzM2YSIsInNjb3BlcyI6WyJhcGlfcmVhZCJdLCJ2ZXJzaW9uIjoxfQ.SUUNKNWlh4Tmi_ifblWo7I1CEqbYrAWZmn15ziPqfm8")
                .build()

            Log.d("MovieList", "Fetching movies from URL: $url")

            try {
                val response: Response = client.newCall(request).execute()
                Log.d("MovieList", "Response Code: ${response.code}")

                if (response.isSuccessful) {
                    val responseData = response.body?.string() ?: return@Thread callback(null, "Пустой ответ")
                    Log.d("MovieList", "Response Data: $responseData")

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

                    Log.d("MovieList", "Movies parsed successfully: ${movieList.size}")
                    callback(movieList, null)
                } else {
                    Log.e("MovieList", "API Error: ${response.code} - ${response.message}")
                    callback(null, "API Error: ${response.code}")
                }
            } catch (e: Exception) {
                Log.e("MovieList", "Exception: ${e.message}")
                callback(null, "Exception: ${e.message}")
            }
        }.start()
    }

    fun loadMoreMovies(callback: (List<Movie>) -> Unit) {
        Log.d("MovieViewModel", "Loading more movies, current page: $currentPage")
        fetchMovies(currentPage + 1, "${selectedSortOption}.${selectedOrderOption}") { newMovies, _ ->
            if (newMovies != null && newMovies.isNotEmpty()) {
                Log.d("MovieViewModel", "New movies loaded: ${newMovies.size}")
                currentPage++
                callback(newMovies)
            } else {
                Log.d("MovieViewModel", "No more movies to load")
                callback(emptyList())
            }
        }
    }
}

data class SortOption(val value: String, val label: String)
