package com.example.levelup.ui.catalog

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.levelup.LevelUpApp
import com.example.levelup.R
import com.example.levelup.data.model.Product
import com.example.levelup.data.repo.CartRepository
import com.example.levelup.data.repo.ProductRepository
import com.example.levelup.ui.catalog.ProductDetailScreen
import com.example.levelup.ui.cart.CartScreen
import com.example.levelup.ui.viewmodel.CartViewModel
import com.example.levelup.ui.viewmodel.ProductViewModel
import com.example.levelup.utils.formatPrice
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun ProductListScreen(
    currentUserEmail: String,
    currentUserName: String,
    onLogout: () -> Unit
) {
    val productRepo = remember { ProductRepository(LevelUpApp.database.productDao()) }
    val cartRepo = remember { CartRepository(LevelUpApp.database.cartDao()) }
    val productViewModel = remember { ProductViewModel(productRepo) }
    val products by productViewModel.products.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val cartViewModel = remember(currentUserEmail) { CartViewModel(cartRepo, currentUserEmail) }
    val cartItems by cartViewModel.cartItems.collectAsState()
    val cartCount = cartItems.sumOf { it.quantity }

    var isCartOpen by remember { mutableStateOf(false) }
    var selectedProductId by remember { mutableStateOf<Int?>(null) }
    var showProductDetail by remember { mutableStateOf(false) }

    val blurRadius = if (isCartOpen || showProductDetail) 16.dp else 0.dp

    Box(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .blur(blurRadius)
        ) {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { Text("Catálogo") },
                        actions = {
                            BadgedBox(
                                badge = {
                                    if (cartCount > 0) {
                                        Badge { Text(cartCount.toString()) }
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
                            onAddToCart = { cartViewModel.addProduct(product.id, 1) },
                            onClick = {
                                selectedProductId = product.id
                                showProductDetail = true
                            },
                            snackbarHostState = snackbarHostState
                        )
                    }



                }
            }
        }

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

        AnimatedVisibility(
            visible = isCartOpen,
            enter = slideInHorizontally(initialOffsetX = { it }),
            exit = slideOutHorizontally(targetOffsetX = { it })
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize(),
                contentAlignment = Alignment.CenterEnd
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.CenterEnd
                ) {
                    Surface(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(0.85f),
                        tonalElevation = 8.dp,
                        shape = MaterialTheme.shapes.medium
                    ) {
                        CartScreen(
                            userEmail = currentUserEmail,
                            onBack = { isCartOpen = false },
                            viewModel = cartViewModel
                        )
                    }
                }
            }
        }

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
                        userEmail = currentUserEmail,
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
private fun ProductCard(
    product: Product,
    onAddToCart: () -> Unit,
    onClick: () -> Unit,
    snackbarHostState: SnackbarHostState
) {
    val imageRes = productImageResource(product.imageUrl)
    val scope = rememberCoroutineScope()

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
                    .height(140.dp)
                    .fillMaxWidth(),
                contentScale = ContentScale.Fit
            )
            Spacer(Modifier.height(8.dp))
            Text(product.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(product.description, style = MaterialTheme.typography.bodyMedium)
            Text("Precio: ${product.price.formatPrice()}", fontWeight = FontWeight.Medium)
            Spacer(Modifier.height(8.dp))
            Button(
                onClick = {
                    scope.launch {
                        onAddToCart()
                        snackbarHostState.showSnackbar("Producto agregado al carrito")
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Agregar al carrito")
            }
        }
    }
}
