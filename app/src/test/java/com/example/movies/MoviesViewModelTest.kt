package com.example.movies

import com.example.movies.domain.usecase.GetPopularMoviesUseCase
import com.example.movies.domain.usecase.ObserveFavoriteIdsUseCase
import com.example.movies.domain.usecase.SearchMoviesUseCase
import com.example.movies.domain.usecase.ToggleFavoriteUseCase
import com.example.movies.presentation.MoviesViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MoviesViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private fun viewModel(repo: FakeMovieRepository) = MoviesViewModel(
        getPopularMovies = GetPopularMoviesUseCase(repo),
        searchMovies = SearchMoviesUseCase(repo),
        toggleFavorite = ToggleFavoriteUseCase(repo),
        observeFavoriteIds = ObserveFavoriteIdsUseCase(repo),
    )

    @Test
    fun `carga inicial carrega populares e formata year e rating`() = runTest {
        val repo = FakeMovieRepository()
        val vm = viewModel(repo)

        advanceUntilIdle()

        val state = vm.uiState.value
        assertFalse("nao deveria ficar em loading", state.isLoading)
        assertNull(state.errorMessage)
        assertEquals(repo.popular.size, state.movies.size)

        val matrix = state.movies.first { it.title == "The Matrix" }
        assertEquals("1999", matrix.year)
        assertEquals("8.7", matrix.rating.replace(",", "."))
        assertFalse(matrix.isFavorite)

        val interstellar = state.movies.first { it.title == "Interstellar" }
        assertEquals("8.6", interstellar.rating.replace(",", "."))
    }

    @Test
    fun `favoritar marca o filme como favorito na lista`() = runTest {
        val repo = FakeMovieRepository()
        val vm = viewModel(repo)
        advanceUntilIdle()

        vm.onToggleFavorite(1)
        advanceUntilIdle()

        val matrix = vm.uiState.value.movies.first { it.id == 1 }
        assertTrue("o item da lista deveria refletir o favorito", matrix.isFavorite)
    }

    @Test
    fun `favorito alterado no repositorio reflete na lista`() = runTest {
        val repo = FakeMovieRepository()
        val vm = viewModel(repo)
        advanceUntilIdle()

        repo.toggleFavorite(2)
        advanceUntilIdle()

        val inception = vm.uiState.value.movies.first { it.id == 2 }
        assertTrue("isFavorite deveria vir do observeFavoriteIds", inception.isFavorite)
    }

    @Test
    fun `busca filtra os filmes e atualiza a query no estado`() = runTest {
        val repo = FakeMovieRepository()
        val vm = viewModel(repo)
        advanceUntilIdle()

        vm.onQueryChange("matrix")
        advanceUntilIdle()

        val state = vm.uiState.value
        assertEquals("matrix", state.query)
        assertEquals(1, state.movies.size)
        assertEquals("The Matrix", state.movies.first().title)
    }

    @Test
    fun `query em branco volta para os populares`() = runTest {
        val repo = FakeMovieRepository()
        val vm = viewModel(repo)
        advanceUntilIdle()

        vm.onQueryChange("matrix")
        advanceUntilIdle()
        vm.onQueryChange("")
        advanceUntilIdle()

        assertEquals(repo.popular.size, vm.uiState.value.movies.size)
    }

    @Test
    fun `erro na carga inicial expoe mensagem e sai do loading`() = runTest {
        val repo = FakeMovieRepository().apply { failGetPopular = true }
        val vm = viewModel(repo)

        advanceUntilIdle()

        val state = vm.uiState.value
        assertFalse(state.isLoading)
        assertNotNull("deveria expor uma mensagem de erro", state.errorMessage)
    }

    @Test
    fun `retry apos erro recarrega com sucesso`() = runTest {
        val repo = FakeMovieRepository().apply { failGetPopular = true }
        val vm = viewModel(repo)
        advanceUntilIdle()

        repo.failGetPopular = false
        vm.retry()
        advanceUntilIdle()

        val state = vm.uiState.value
        assertNull(state.errorMessage)
        assertTrue(state.movies.isNotEmpty())
    }

    @Test
    fun `retry apos erro na busca repete a busca`() = runTest {
        val repo = FakeMovieRepository().apply { failSearch = true }
        val vm = viewModel(repo)
        advanceUntilIdle()

        vm.onQueryChange("matrix")
        advanceUntilIdle()
        assertNotNull("deveria expor uma mensagem de erro", vm.uiState.value.errorMessage)

        repo.failSearch = false
        vm.retry()
        advanceUntilIdle()

        val state = vm.uiState.value
        assertNull(state.errorMessage)
        assertEquals("matrix", state.query)
        assertEquals(listOf("The Matrix"), state.movies.map { it.title })
    }
}
