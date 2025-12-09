package com.example.levelup.ui.catalog

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import coil.compose.AsyncImage
import com.example.levelup.LevelUpApp
import com.example.levelup.R
import com.example.levelup.data.model.Product
import com.example.levelup.data.remote.RetrofitClient
import com.example.levelup.data.remote.api.ProductApiService
import com.example.levelup.data.repo.CartRepository
import com.example.levelup.data.repo.ProductRepository
import com.example.levelup.ui.cart.CartScreen
import com.example.levelup.ui.viewmodel.CartViewModel
import com.example.levelup.ui.viewmodel.ProductState
import com.example.levelup.ui.viewmodel.ProductViewModel
import com.example.levelup.utils.formatPrice
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

import androidx.compose.runtime.derivedStateOf
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween


@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun ProductListScreen(
    currentUserEmail: String,
    currentUserName: String,
    currentUserPhoto: String?,
    isAdmin: Boolean,
    onLogout: () -> Unit,
    onProfile: () -> Unit,
    onAdmin: () -> Unit
) {
    val api = remember { RetrofitClient.createService(ProductApiService::class.java) }
    val repo = remember { ProductRepository(LevelUpApp.database.productDao(), api) }
    val viewModel = remember { ProductViewModel(repo) }

    val cartRepo = remember { CartRepository(LevelUpApp.database.cartDao()) }
    val cartVM = remember(currentUserEmail) { CartViewModel(cartRepo, currentUserEmail) }
    val cartItems by cartVM.cartItems.collectAsState()
    val cartCount = cartItems.sumOf { it.quantity }

    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    val products = when (state) {
        is ProductState.Success -> (state as ProductState.Success).products
        else -> emptyList()
    }

    val pagerState = rememberPagerState(initialPage = 0) { 2 }
    val lazyListState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    var showCart by remember { mutableStateOf(false) }
    var showDetail by remember { mutableStateOf(false) }
    var selectedId by remember { mutableStateOf<Int?>(null) }

    val blur = if (showCart || showDetail) 16.dp else 0.dp

    val sections by remember(products) {
        mutableStateOf(buildCatalogSections(products))
    }
    val sectionIndex = remember(sections) { calculateSectionStartIndices(sections) }

    LaunchedEffect(state) {
        if (state is ProductState.Error) {
            snackbarHostState.showSnackbar((state as ProductState.Error).message)
        }
    }

    Scaffold(
        topBar = {
            CatalogTopBar(
                title = if (pagerState.currentPage == 0) "Inicio" else "Catálogo",
                currentUserName = currentUserName,
                currentUserPhoto = currentUserPhoto,
                cartCount = cartCount,
                isAdmin = isAdmin,
                onCartClick = { showCart = true },
                onProfileClick = onProfile,
                onAdminClick = onAdmin,
                onLogout = onLogout
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .blur(blur)
            ) {
                VerticalPager(
                    state = pagerState,
                    userScrollEnabled = !(showCart || showDetail)
                ) { page: Int ->
                    if (page == 0) {
                        HomePage(
                            sections = sections,
                            onViewCatalog = {
                                scope.launch { pagerState.animateScrollToPage(1) }
                            },
                            onNavigateToSection = { sec ->
                                scope.launch {
                                    pagerState.animateScrollToPage(1)
                                    sectionIndex[sec.title]?.let { index ->
                                        lazyListState.scrollToItem(index)
                                    }
                                }
                            }
                        )
                    } else {
                        CatalogPage(
                            sections = sections,
                            products = products,
                            sectionIndex = sectionIndex,
                            lazyListState = lazyListState,
                            onAddToCart = { id -> cartVM.addProduct(id, 1) },
                            onProductSelected = {
                                selectedId = it
                                showDetail = true
                            }
                        )
                    }
                }
            }

            // Fondo oscurecido
            if (showCart || showDetail) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.45f))
                        .clickable {
                            showCart = false
                            showDetail = false
                            selectedId = null
                        }
                )
            }

            // Carrito
            AnimatedVisibility(
                visible = showCart,
                enter = slideInHorizontally(initialOffsetX = { it }),
                exit = slideOutHorizontally(targetOffsetX = { it })
            ) {
                Box(
                    Modifier.fillMaxSize(),
                    contentAlignment = Alignment.CenterEnd
                ) {
                    Surface(
                        tonalElevation = 8.dp,
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(0.85f)
                    ) {
                        CartScreen(
                            userEmail = currentUserEmail,
                            onBack = { showCart = false },
                            viewModel = cartVM
                        )
                    }
                }
            }

            // Detalle de producto
            if (showDetail && selectedId != null) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth(0.9f)
                            .wrapContentHeight(),
                        tonalElevation = 8.dp,
                        shape = MaterialTheme.shapes.large
                    ) {
                        ProductDetailScreen(
                            productId = selectedId!!,
                            userEmail = currentUserEmail,
                            onBack = {
                                showDetail = false
                                selectedId = null
                            }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CatalogTopBar(
    title: String,
    currentUserName: String,
    currentUserPhoto: String?,
    cartCount: Int,
    isAdmin: Boolean,
    onCartClick: () -> Unit,
    onProfileClick: () -> Unit,
    onAdminClick: () -> Unit,
    onLogout: () -> Unit
) {
    val cartScale = remember { Animatable(1f) }

    LaunchedEffect(cartCount) {
        // pequeña animación cada vez que cambia el contador del carrito
        cartScale.snapTo(1f)
        cartScale.animateTo(
            targetValue = 1.15f,
            animationSpec = tween(durationMillis = 180)
        )
        cartScale.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 120)
        )
    }

    TopAppBar(
        title = { Text(title) },
        actions = {
            BadgedBox(
                badge = {
                    if (cartCount > 0) {
                        Badge { Text(cartCount.toString()) }
                    }
                }
            ) {
                IconButton(
                    onClick = onCartClick,
                    modifier = Modifier.scale(cartScale.value)
                ) {
                    Icon(Icons.Default.ShoppingCart, contentDescription = "Carrito")
                }
            }

            UserAccountMenu(
                currentUserName = currentUserName,
                currentUserPhoto = currentUserPhoto,
                isAdmin = isAdmin,
                onProfileClick = onProfileClick,
                onAdminClick = onAdminClick,
                onLogout = onLogout
            )
        }
    )
}

@Composable
private fun UserAccountMenu(
    currentUserName: String,
    currentUserPhoto: String?,
    isAdmin: Boolean,
    onProfileClick: () -> Unit,
    onAdminClick: () -> Unit,
    onLogout: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        TextButton(onClick = { expanded = true }) {
            if (!currentUserPhoto.isNullOrBlank()) {
                AsyncImage(
                    model = currentUserPhoto,
                    contentDescription = "Foto de perfil",
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            } else {
                Icon(
                    imageVector = Icons.Default.AccountCircle,
                    contentDescription = null
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(currentUserName)
            Icon(
                imageVector = Icons.Default.ArrowDropDown,
                contentDescription = "Abrir menú de usuario"
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            DropdownMenuItem(
                text = { Text("Mi Perfil") },
                onClick = {
                    expanded = false
                    onProfileClick()
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.AccountCircle,
                        contentDescription = "Mi Perfil"
                    )
                }
            )

            if (isAdmin) {
                DropdownMenuItem(
                    text = { Text("Panel de Admin") },
                    onClick = {
                        expanded = false
                        onAdminClick()
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Panel de Admin"
                        )
                    }
                )
            }

            HorizontalDivider(
                modifier = Modifier,
                thickness = DividerDefaults.Thickness,
                color = DividerDefaults.color
            )

            DropdownMenuItem(
                text = { Text("Cerrar sesión") },
                onClick = {
                    expanded = false
                    onLogout()
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Logout,
                        contentDescription = "Cerrar sesión"
                    )
                }
            )
        }
    }
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
            contentPadding = PaddingValues(horizontal = 32.dp)
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
                items(section.products.take(5)) { product ->
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
    Column(
        modifier = Modifier.width(140.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.1f))
        ) {
            AsyncImage(
                model = product.imageUrl,
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
private fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    val neonColor = Color(0xFF00E676)
    var pressed by remember { mutableStateOf(false) }

    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.96f else 1f,
        animationSpec = tween(durationMillis = 100),
        label = "headerScale"
    )

    LaunchedEffect(pressed) {
        if (pressed) {
            delay(110)
            pressed = false
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 52.dp)   // hitbox cómodo
            .padding(horizontal = 8.dp)
            .let { base ->
                if (onClick != null) {
                    base.clickable {
                        pressed = true
                        onClick()
                    }
                } else base
            }
            .graphicsLayer(
                scaleX = scale,
                scaleY = scale
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall.copy(
                color = neonColor,
                fontWeight = FontWeight.ExtraBold,
                shadow = Shadow(
                    color = neonColor.copy(alpha = 0.7f),
                    offset = Offset(0f, 0f),
                    blurRadius = 18f
                )
            ),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun ProductCard(
    product: Product,
    onAddToCart: () -> Unit,
    onClick: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var pressed by remember { mutableStateOf(false) }
    var appeared by remember { mutableStateOf(false) }

    // Animación de entrada (fade + subida suave), se ejecuta una vez cuando se compone la card
    LaunchedEffect(Unit) {
        appeared = true
    }

    val alpha by animateFloatAsState(
        targetValue = if (appeared) 1f else 0f,
        animationSpec = tween(durationMillis = 220),
        label = "cardAlpha"
    )

    val translateY by animateFloatAsState(
        targetValue = if (appeared) 0f else 40f,
        animationSpec = tween(durationMillis = 220),
        label = "cardOffset"
    )

    // Escala leve al presionar el botón
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.96f else 1f,
        animationSpec = tween(durationMillis = 120),
        label = "cardScale"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer(
                translationY = translateY,
                alpha = alpha,
                scaleX = scale,
                scaleY = scale
            )
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(Modifier.padding(16.dp)) {

            AsyncImage(
                model = product.imageUrl,
                contentDescription = product.name,
                modifier = Modifier
                    .height(140.dp)
                    .fillMaxWidth(),
                contentScale = ContentScale.Fit
            )

            Spacer(Modifier.height(8.dp))

            Text(product.name, fontWeight = FontWeight.Bold)
            Text("Precio: ${product.price.formatPrice()}", fontWeight = FontWeight.Medium)

            Spacer(Modifier.height(8.dp))

            Button(
                onClick = {
                    scope.launch {
                        pressed = true
                        onAddToCart()
                        delay(140)
                        pressed = false
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

/**
 * Lógica de secciones:
 * - Si hay pocos productos (<= 6): solo "Catálogo completo"
 * - Si hay más: Destacados (4), Ofertas (4), Top ventas (resto)
 */
private fun buildCatalogSections(products: List<Product>): List<CatalogSection> {
    if (products.isEmpty()) return emptyList()

    if (products.size <= 6) {
        return listOf(
            CatalogSection(
                title = "Catálogo completo",
                products = products
            )
        )
    }

    val sorted = products.sortedBy { it.id }

    val destacados = sorted.take(4)
    val ofertas = sorted.drop(4).take(4)
    val topVentas = sorted.drop(8)

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

/**
 * Calcula el índice de inicio de cada sección dentro del LazyColumn
 * (1 ítem para el header + N productos).
 */
private fun calculateSectionStartIndices(sections: List<CatalogSection>): Map<String, Int> {
    val result = mutableMapOf<String, Int>()
    var currentIndex = 0
    sections.forEach { section ->
        result[section.title] = currentIndex
        currentIndex += 1 + section.products.size
    }
    return result
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
    sectionIndex: Map<String, Int>,
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
        // Sección actualmente "activa" según el primer ítem visible
        val currentSectionTitle by remember(sections, sectionIndex) {
            derivedStateOf {
                val firstVisible = lazyListState.firstVisibleItemIndex
                var current = sections.firstOrNull()?.title.orEmpty()
                sectionIndex.entries
                    .sortedBy { it.value }
                    .forEach { (title, index) ->
                        if (firstVisible >= index) {
                            current = title
                        }
                    }
                current
            }
        }

        Box(modifier = modifier.fillMaxSize()) {

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                state = lazyListState,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                var globalItemIndex = 0

                sections.forEach { section ->

                    // Dentro del LazyColumn
                    item {
                        SectionHeader(title = section.title)
                        globalItemIndex++
                    }


                    // Productos de la sección
                    itemsIndexed(section.products) { _, product ->
                        val itemIndexForParallax = globalItemIndex
                        globalItemIndex++

                        ProductCard(
                            product = product,
                            onAddToCart = { onAddToCart(product.id) },
                            onClick = { onProductSelected(product.id) }
                        )
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }

            val scope = rememberCoroutineScope()

            SectionHeader(
                title = currentSectionTitle,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .background(
                        MaterialTheme.colorScheme.background.copy(alpha = 0.95f)
                    )
                    .zIndex(1f),
                onClick = {
                    scope.launch {
                        lazyListState.animateScrollToItem(0)
                    }
                }
            )
        }
    }
}

@Composable
private fun InlineSectionTitle(title: String) {
    val accent = Color(0xFF00E676)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp, bottom = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.ExtraBold,
                color = accent,
                shadow = Shadow(
                    color = accent.copy(alpha = 0.55f),
                    offset = Offset(0f, 0f),
                    blurRadius = 14f
                )
            )
        )
    }
}




