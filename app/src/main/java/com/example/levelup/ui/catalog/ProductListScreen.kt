package com.example.levelup.ui.catalog

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.levelup.R
import com.example.levelup.LevelUpApp
import com.example.levelup.data.model.CartItem
import com.example.levelup.data.repo.CartRepository
import com.example.levelup.data.repo.ProductRepository
import com.example.levelup.ui.cart.CartScreen
import com.example.levelup.ui.viewmodel.CartViewModel
import com.example.levelup.ui.viewmodel.ProductViewModel
import com.example.levelup.utils.formatPrice
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductListScreen(
    onNavigateCart: () -> Unit,
    onNavigateDetail: (Int) -> Unit
) {
    val productRepo = remember { ProductRepository(LevelUpApp.database.productDao()) }
    val cartRepo = remember { CartRepository(LevelUpApp.database.cartDao()) }
    val viewModel = remember { ProductViewModel(productRepo) }
    val products by viewModel.products.collectAsState()
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var isCartOpen by remember { mutableStateOf(false) }
    var selectedProductId by remember { mutableStateOf<Int?>(null) }
    var showProductDetail by remember { mutableStateOf(false) }

    val cartViewModel = remember { CartViewModel(cartRepo) }
    val cartItems by cartViewModel.cartItems.collectAsState()
    val cartCount = cartItems.sumOf { it.quantity }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Catálogo") },
                    actions = {
                        BadgedBox(
                            badge = {
                                if (cartCount > 0) {
                                    Badge {
                                        Text(cartCount.toString())
                                    }
                                }
                            }
                        ) {
                            IconButton(onClick = { isCartOpen = true }) {
                                Icon(Icons.Default.ShoppingCart, contentDescription = "Carrito")
                            }
                        }
                    }
                )
            },
            snackbarHost = { SnackbarHost(snackbarHostState) }
        ) { padding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(products) { product ->
                    ProductCard(
                        product = product,
                        onAddToCart = {
                            scope.launch {
                                cartRepo.addToCart(CartItem(productId = product.id, quantity = 1))
                                snackbarHostState.showSnackbar("Producto agregado al carrito")
                            }
                        },
                        onClick = {
                            selectedProductId = product.id
                            showProductDetail = true
                        }
                    )
                }
            }
        }

        // Fondo oscuro solo cuando se muestra detalle o carrito
        if (isCartOpen || showProductDetail) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.4f))
                    .clickable(enabled = true) {
                        isCartOpen = false
                        showProductDetail = false
                        selectedProductId = null
                    }
            )
        }

        // Carrito — movido a la derecha
        AnimatedVisibility(
            visible = isCartOpen,
            enter = slideInHorizontally(initialOffsetX = { it }),
            exit = slideOutHorizontally(targetOffsetX = { it })
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(0.85f)
                    .align(Alignment.CenterEnd),
                tonalElevation = 8.dp,
                shape = MaterialTheme.shapes.medium
            ) {
                CartScreen(onBack = { isCartOpen = false })
            }
        }

        // Detalle centrado sin blur sobre sí mismo
        if (showProductDetail && selectedProductId != null) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .wrapContentHeight(),
                    shape = MaterialTheme.shapes.large,
                    tonalElevation = 8.dp
                ) {
                    ProductDetailScreen(
                        productId = selectedProductId!!,
                        onBack = {
                            showProductDetail = false
                            selectedProductId = null
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun ProductCard(
    product: com.example.levelup.data.model.Product,
    onAddToCart: () -> Unit,
    onClick: () -> Unit
) {
    val imageRes = when (product.imageUrl) {
        "catan" -> R.drawable.catan
        "carcasone" -> R.drawable.carcasone
        "xbosseries" -> R.drawable.xbosseries
        "hyperxcloud" -> R.drawable.hyperxcloud
        "pley5" -> R.drawable.pley5
        "pcgamer" -> R.drawable.pcgamer
        "sillagamer" -> R.drawable.sillagamer
        "logitchg502" -> R.drawable.logitchg502
        "mousepadrazer" -> R.drawable.mousepadrazer
        "polera_negra" -> R.drawable.polera_negra
        else -> R.drawable.ic_launcher_foreground
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Image(
                painter = painterResource(imageRes),
                contentDescription = product.name,
                modifier = Modifier
                    .size(120.dp)
                    .align(Alignment.CenterHorizontally)
            )
            Spacer(Modifier.height(8.dp))
            Text(product.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(product.description, style = MaterialTheme.typography.bodyMedium)
            Text("Precio: ${product.price.formatPrice()}", fontWeight = FontWeight.Medium)
            Spacer(Modifier.height(8.dp))
            Button(onClick = onAddToCart, modifier = Modifier.fillMaxWidth()) {
                Text("Agregar al carrito")
            }
        }
    }
}
