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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
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
    val lazyListState = rememberLazyListState()
    val scrollScope = rememberCoroutineScope()

    val sections by remember(products) {
        mutableStateOf(buildCatalogSections(products))
    }

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
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                UserAccountMenu(
                                    currentUserName = currentUserName,
                                    onLogout = onLogout
                                )
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
                        }
                    )
            },
                snackbarHost = { SnackbarHost(snackbarHostState) }
            ) { padding ->
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    state = lazyListState,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        CatalogHero(
                            onViewProductsClick = {
                                scrollScope.launch {
                                    if (sections.isNotEmpty()) {
                                        lazyListState.animateScrollToItem(1)
                                    }
                                }
                            }
                        )
                    }
                    if (products.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 48.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator()
                            }
                        }
                    } else {
                        sections.forEach { section ->
                            item {
                                SectionHeader(title = section.title)
                            }
                            items(section.products, key = { it.id }) { product ->
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
                        item {
                            Spacer(modifier = Modifier.height(32.dp))
                        }
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
                        viewModel = cartViewModel)
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

@Composable
private fun CatalogHero(onViewProductsClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.setup),
                contentDescription = "Sube de nivel tu setup",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.55f))
            )
            Column(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(horizontal = 24.dp)
            ) {
                Text(
                    text = "Sube de nivel tu setup",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Black,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Consolas, accesorios, PC gamers y más. Envíos a todo Chile.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.9f)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = onViewProductsClick,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E676))
                ) {
                    Text("Ver productos", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.headlineSmall,
        color = Color(0xFF00E676),
        fontWeight = FontWeight.ExtraBold,
        modifier = Modifier.fillMaxWidth(),
        textAlign = TextAlign.Center
    )
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

private data class CatalogSection(
    val title: String,
    val products: List<Product>
)

private fun buildCatalogSections(products: List<Product>): List<CatalogSection> {
    if (products.isEmpty()) return emptyList()

    val destacados = products.take(4)
    val ofertas = products.drop(destacados.size).take(3)
    val topVentas = products.drop(destacados.size + ofertas.size)

    val sections = mutableListOf<CatalogSection>()

    if (destacados.isNotEmpty()) {
        sections.add(CatalogSection(title = "¡Destacados!", products = destacados))
    }

    if (ofertas.isNotEmpty()) {
        sections.add(CatalogSection(title = "¡Ofertas del Mes!", products = ofertas))
    }

    if (topVentas.isNotEmpty()) {
        sections.add(CatalogSection(title = "Top ventas", products = topVentas))
    }

    return sections
}

@Composable
private fun UserAccountMenu(
    currentUserName: String,
    onLogout: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        TextButton(onClick = { expanded = true }) {
            Icon(
                imageVector = Icons.Default.AccountCircle,
                contentDescription = null
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(currentUserName)
            Icon(
                imageVector = Icons.Default.ArrowDropDown,
                contentDescription = null
            )
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            DropdownMenuItem(
                text = { Text("Cerrar sesión") },
                onClick = {
                    expanded = false
                    onLogout()
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.AccountCircle,
                        contentDescription = null
                    )
                }
            )
        }
    }
}
