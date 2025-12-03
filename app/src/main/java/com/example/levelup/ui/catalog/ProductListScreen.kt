package com.example.levelup.ui.catalog

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally

import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
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
import com.example.levelup.ui.cart.CartScreen
import com.example.levelup.ui.viewmodel.CartViewModel
import com.example.levelup.ui.viewmodel.ProductViewModel
import com.example.levelup.utils.formatPrice
import kotlinx.coroutines.launch
import com.example.levelup.data.remote.RetrofitClient
import com.example.levelup.data.remote.api.ProductApiService

import com.example.levelup.ui.viewmodel.ProductState


@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun ProductListScreen(
    currentUserEmail: String,
    currentUserName: String,
    onLogout: () -> Unit
) {
    // Servicio de API para productos
    val productApi = remember {
        RetrofitClient.createService(ProductApiService::class.java)
    }

    // Repositorio de productos con Room + API
    val productRepo = remember {
        ProductRepository(
            dao = LevelUpApp.database.productDao(),
            api = productApi
        )
    }
    val productViewModel = remember { ProductViewModel(productRepo) }

    val cartRepo = remember { CartRepository(LevelUpApp.database.cartDao()) }

    // Estado expuesto por el ViewModel (ProductState)
    val state by productViewModel.state.collectAsState()

    val products: List<Product> = when (state) {
        is ProductState.Success -> (state as ProductState.Success).products
        else -> emptyList()
    }

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state) {
        if (state is ProductState.Error) {
            snackbarHostState.showSnackbar(
                (state as ProductState.Error).message
            )
        }
    }

    val cartViewModel = remember(currentUserEmail) { CartViewModel(cartRepo, currentUserEmail) }
    val cartItems by cartViewModel.cartItems.collectAsState()
    val cartCount = cartItems.sumOf { it.quantity }

    var isCartOpen by remember { mutableStateOf(false) }
    var selectedProductId by remember { mutableStateOf<Int?>(null) }
    var showProductDetail by remember { mutableStateOf(false) }

    val blurRadius = if (isCartOpen || showProductDetail) 16.dp else 0.dp
    val lazyListState = rememberLazyListState()
    val pagerState = rememberPagerState(initialPage = 0, pageCount = { 2 })
    val coroutineScope = rememberCoroutineScope()

    // Secciones construidas en base a los productos del estado
    val sections by remember(products) {
        mutableStateOf(buildCatalogSections(products))
    }

    val sectionStartIndices by remember(sections) {
        mutableStateOf(calculateSectionStartIndices(sections))
    }


    val navigateToSection: (CatalogSection) -> Unit = { section ->
        coroutineScope.launch {
            pagerState.animateScrollToPage(1)
            sectionStartIndices[section.title]?.let { index ->
                lazyListState.animateScrollToItem(index)
            }
        }
    }



    val currentPage by remember { derivedStateOf { pagerState.currentPage } }

    Scaffold(
        topBar = {
            CatalogTopBar(
                title = if (currentPage == 0) "Inicio" else "Catálogo",
                currentUserName = currentUserName,
                cartCount = cartCount,
                onCartClick = { isCartOpen = true },
                onLogout = onLogout
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .blur(blurRadius)
            ) {
                VerticalPager(
                    state = pagerState,
                    userScrollEnabled = !isCartOpen && !showProductDetail,
                    modifier = Modifier.fillMaxSize()
                ) { page ->
                    when (page) {
                        0 -> HomePage(
                            sections = sections,
                            onViewCatalog = {
                                coroutineScope.launch { pagerState.animateScrollToPage(1) }
                            },
                            onNavigateToSection = navigateToSection,
                            modifier = Modifier.fillMaxSize()
                        )

                        else -> CatalogPage(
                            sections = sections,
                            products = products,
                            snackbarHostState = snackbarHostState,
                            lazyListState = lazyListState,
                            onAddToCart = { productId -> cartViewModel.addProduct(productId, 1) },
                            onProductSelected = { productId ->
                                selectedProductId = productId
                                showProductDetail = true
                            },
                            modifier = Modifier.fillMaxSize()
                        )
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
}

@Composable
private fun HomePage(
    sections: List<CatalogSection>,
    onViewCatalog: () -> Unit,
    onNavigateToSection: (CatalogSection) -> Unit,
    modifier: Modifier = Modifier
) {
    CatalogHome(
        sections = sections,
        onViewCatalog = onViewCatalog,
        onNavigateToSection = onNavigateToSection,
        modifier = modifier
    )
}

@Composable
private fun CatalogPage(
    sections: List<CatalogSection>,
    products: List<Product>,
    snackbarHostState: SnackbarHostState,
    lazyListState: LazyListState,
    onAddToCart: (Int) -> Unit,
    onProductSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    if (products.isEmpty()) {
        Box(
            modifier = modifier,
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    } else {
        LazyColumn(
            modifier = modifier
                .padding(horizontal = 16.dp, vertical = 12.dp),
            state = lazyListState,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            sections.forEach { section ->
                item(key = "header_${section.title}") {
                    SectionHeader(title = section.title)
                }
                items(section.products, key = { it.id }) { product ->
                    ProductCard(
                        product = product,
                        onAddToCart = { onAddToCart(product.id) },
                        onClick = { onProductSelected(product.id) },
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CatalogTopBar(
    title: String,
    currentUserName: String,
    cartCount: Int,
    onCartClick: () -> Unit,
    onLogout: () -> Unit
) {
    TopAppBar(
        title = { Text(title) },
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
                    IconButton(onClick = onCartClick) {
                        Icon(Icons.Default.ShoppingCart, contentDescription = "Carrito")
                    }
                }
            }
        }
    )
}

@Composable
private fun CatalogHome(
    sections: List<CatalogSection>,
    onViewCatalog: () -> Unit,
    onNavigateToSection: (CatalogSection) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {
        Image(
            painter = painterResource(id = R.drawable.setup),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.6f))
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 32.dp),
            verticalArrangement = Arrangement.spacedBy(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Sube de nivel tu setup",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Black,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Consolas, accesorios, PC gamers y más. Envíos a todo Chile.",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White.copy(alpha = 0.9f)
                )
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = onViewCatalog,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E676))
                ) {
                    Text("Explorar catálogo", fontWeight = FontWeight.Bold)
                }
            }

            if (sections.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Color.White)
                }
            } else {
                SectionCarousel(
                    sections = sections,
                    onNavigateToSection = onNavigateToSection
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun SectionCarousel(
    sections: List<CatalogSection>,
    onNavigateToSection: (CatalogSection) -> Unit
) {
    val pagerState = rememberPagerState(pageCount = { sections.size })

    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        HorizontalPager(
            state = pagerState,
            pageSpacing = 24.dp,
            contentPadding = PaddingValues(horizontal = 32.dp) //
        ) { page ->
            val section = sections[page]
            SectionPreview(
                section = section,
                onNavigateToSection = onNavigateToSection
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            sections.forEachIndexed { index, _ ->
                val isSelected = pagerState.currentPage == index
                Box(
                    modifier = Modifier
                        .padding(horizontal = 4.dp)
                        .size(if (isSelected) 12.dp else 8.dp)
                        .clip(CircleShape)
                        .background(
                            if (isSelected) Color(0xFF00E676)
                            else Color.White.copy(alpha = 0.6f)
                        )
                )
            }
        }
    }
}

@Composable
private fun SectionPreview(
    section: CatalogSection,
    onNavigateToSection: (CatalogSection) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp)
            .heightIn(min = 220.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.08f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = section.title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Black,
                color = Color.White
            )
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(section.products.take(5), key = { it.id }) { product ->
                    ProductPreview(product = product)
                }
            }
            Button(
                onClick = { onNavigateToSection(section) },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E676))
            ) {
                Text("Ver ${section.title}")
            }
        }
    }
}

@Composable
private fun ProductPreview(product: Product) {
    val imageRes = productImageResource(product.imageUrl)

    Column(
        modifier = Modifier.width(140.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.1f))
        ) {
            Image(
                painter = painterResource(id = imageRes),
                contentDescription = product.name,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                contentScale = ContentScale.Fit
            )
        }
        Text(
            text = product.name,
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = product.price.formatPrice(),
            style = MaterialTheme.typography.bodySmall,
            color = Color(0xFF00E676),
            fontWeight = FontWeight.SemiBold
        )
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

    val destacados = products.shuffled().take(4) // Usar shuffled para variedad
    val ofertas = products.filterNot { it in destacados }.shuffled().take(3)
    val topVentas = products.filterNot { it in destacados || it in ofertas }.shuffled()

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

private fun calculateSectionStartIndices(sections: List<CatalogSection>): Map<String, Int> {
    val result = mutableMapOf<String, Int>()
    var currentIndex = 0
    sections.forEach { section ->
        // Cada sección tiene un header (1) + la cantidad de productos
        result[section.title] = currentIndex
        currentIndex += 1 + section.products.size
    }
    return result
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
                        contentDescription = "Cerrar sesión"
                    )
                }
            )
        }
    }
}
