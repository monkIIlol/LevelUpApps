package com.example.levelup.ui.viewmodel

import com.example.levelup.data.model.Product
import com.example.levelup.data.repo.ProductRepository
import com.example.levelup.ui.viewmodel.ProductState
import com.example.levelup.ui.viewmodel.ProductViewModel
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ProductViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setUp() {
        // Hacemos que Dispatchers.Main use el dispatcher de test
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        // Volvemos a dejar Main como estaba
        Dispatchers.resetMain()
    }

    @Test
    fun loadProducts_cuandoRepoDevuelveLista_emiteEstadoSuccess() = runTest {
        // Arrange
        val repository = mockk<ProductRepository>()

        val fakeProducts = listOf(
            Product(
                id = 1,
                name = "Producto test",
                description = "Descripción de prueba",
                price = 10000,
                stock = 5,
                imageUrl = "test"
            )
        )

        coEvery { repository.getAllProducts() } returns fakeProducts

        // Creamos el ViewModel pasando el repo mockeado
        val viewModel = ProductViewModel(repository)

        // (Opcional) forzar una recarga manual
        viewModel.loadProducts()

        // Act + Assert
        val state = viewModel.state.value

        assertTrue(state is ProductState.Success)

        state as ProductState.Success
        assertEquals(fakeProducts, state.products)
    }
}