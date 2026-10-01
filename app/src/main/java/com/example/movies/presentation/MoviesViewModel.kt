package com.example.movies.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.movies.domain.model.Movie
import com.example.movies.domain.usecase.GetPopularMoviesUseCase
import com.example.movies.domain.usecase.ObserveFavoriteIdsUseCase
import com.example.movies.domain.usecase.SearchMoviesUseCase
import com.example.movies.domain.usecase.ToggleFavoriteUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Implemente este ViewModel até `./gradlew :app:testDebugUnitTest` ficar verde.
 * Passo a passo, prints e testes de cada etapa: README.md
 */
class MoviesViewModel(
    private val getPopularMovies: GetPopularMoviesUseCase,
    private val searchMovies: SearchMoviesUseCase,
    private val toggleFavorite: ToggleFavoriteUseCase,
    private val observeFavoriteIds: ObserveFavoriteIdsUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(MoviesUiState(isLoading = true))
    val uiState: StateFlow<MoviesUiState> = _uiState.asStateFlow()

    private var currentMovies: List<Movie> = emptyList()
    private var favoriteIds: Set<Int> = emptySet()

    private sealed interface Operation {
        data object Popular : Operation
        data class Search(val query: String) : Operation
    }

    private var lastOperation: Operation = Operation.Popular

    init {
        observeFavorites()
        // TODO (passo 1): disparar a carga inicial
    }

    private fun observeFavorites() {
        viewModelScope.launch {
            observeFavoriteIds().collect { ids ->
                favoriteIds = ids
                // TODO (passo 2): atualizar a lista do estado com os favoritos novos
            }
        }
    }

    fun onQueryChange(query: String) {
        // TODO: atualizar a query e buscar
    }

    fun onToggleFavorite(movieId: Int) {
        // TODO: alternar favorito
    }

    fun retry() {
        // TODO: repetir a última operação
    }
}
