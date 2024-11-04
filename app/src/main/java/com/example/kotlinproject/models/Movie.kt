package com.example.kotlinproject.models

data class Movie(
    val id: Int,
    val title: String,
    val year: Int,
    val director: String,
    val actors: List<String>,
    val description: String,
    val posterPath: String,
    val voteAverage: Double,
    var isFavorite: Boolean = false
)