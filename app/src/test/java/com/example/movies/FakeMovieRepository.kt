package com.example.movies

import com.example.movies.domain.model.Movie
import com.example.movies.domain.repository.MovieRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class FakeMovieRepository : MovieRepository {

    val popular: List<Movie> = listOf(
        Movie(1, "The Matrix", 1999, 8.7),
        Movie(2, "Inception", 2010, 8.8),
        Movie(3, "Interstellar", 2014, 8.64),
    )

    var failGetPopular: Boolean = false
    var failSearch: Boolean = false

    private val favorites = MutableStateFlow<Set<Int>>(emptySet())

    override suspend fun getPopular(): List<Movie> {
        if (failGetPopular) throw RuntimeException("Falha ao carregar populares")
        return popular
    }

    override suspend fun search(query: String): List<Movie> {
        if (failSearch) throw RuntimeException("Falha na busca")
        return popular.filter { it.title.contains(query, ignoreCase = true) }
    }

    override fun observeFavoriteIds(): Flow<Set<Int>> = favorites.asStateFlow()

    override suspend fun toggleFavorite(movieId: Int) {
        favorites.update { if (movieId in it) it - movieId else it + movieId }
    }
}
