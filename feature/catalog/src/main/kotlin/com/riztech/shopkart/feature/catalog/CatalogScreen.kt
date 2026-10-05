package com.riztech.shopkart.feature.catalog

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.riztech.shopkart.designsystem.component.*
import com.riztech.shopkart.designsystem.theme.BrandColors
import com.riztech.shopkart.domain.model.Category
import com.riztech.shopkart.domain.model.Product
import com.riztech.shopkart.domain.repository.ProductSort

/** A home-screen banner that opens one category. Copy only, no invented offers. */
private data class Promo(val category: String, val title: String, val body: String, val colors: List<Color>)

private val PROMOS = listOf(
    Promo("audio", "Sound that moves with you", "Headphones, speakers and studio mics", listOf(BrandColors.Indigo, Color(0xFF7B5CFF))),
    Promo("kitchen", "Better coffee at home", "Grinders, pour-over and cast iron", listOf(BrandColors.Coral, Color(0xFFFFA24A))),
    Promo("outdoors", "Ready for the trail", "Packs, bottles and light for camp", listOf(BrandColors.Mint, Color(0xFF0E7C8C))),
)

@Composable
fun CatalogScreen(
    onProductClick: (String) -> Unit,
    onCartClick: () -> Unit,
    viewModel: CatalogViewModel = hiltViewModel(),
) {
    // collectAsStateWithLifecycle, not collectAsState: the latter keeps
    // collecting while the app is backgrounded, which keeps the ViewModel awake
    // doing work nobody can see.
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.messages.collect { snackbarHostState.showSnackbar(it) }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        // The app's bottom bar owns the navigation insets.
        contentWindowInsets = WindowInsets.statusBars,
    ) { padding ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.padding(padding),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            fullWidth { Header(cartCount = state.cartItemCount, onCartClick = onCartClick) }
            fullWidth { SearchField(value = state.search, onValueChange = viewModel::onSearchChange) }

            if (!state.hasActiveFilter && state.products.isNotEmpty()) {
                fullWidth {
                    PromoPager(
                        products = state.products,
                        onOpen = viewModel::onCategorySelected,
                    )
                }
            }

            if (state.categories.isNotEmpty()) {
                fullWidth {
                    CategoryRow(
                        categories = state.categories,
                        selected = state.selectedCategory,
                        onSelect = viewModel::onCategorySelected,
                    )
                }
            }

            if (state.topRated.isNotEmpty()) {
                fullWidth { SectionTitle("Top rated", "Highest rated by shoppers") }
                fullWidth {
                    TopRatedRow(products = state.topRated, onProductClick = onProductClick)
                }
            }

            fullWidth {
                val title = state.categories.firstOrNull { it.slug == state.selectedCategory }?.name
                    ?: if (state.search.isNotBlank()) "Results" else "All products"
                Column {
                    SectionTitle(title, "${state.products.size} items")
                    SortRow(selected = state.sort, onSelect = viewModel::onSortSelected)
                }
            }

            when {
                state.isLoading && state.products.isEmpty() ->
                    fullWidth { LoadingState(Modifier.height(240.dp)) }

                state.errorMessage != null && state.products.isEmpty() ->
                    fullWidth {
                        ErrorState(
                            message = state.errorMessage!!,
                            onRetry = viewModel::retry,
                            modifier = Modifier.height(360.dp),
                        )
                    }

                state.isEmpty ->
                    fullWidth {
                        EmptyState(
                            title = "Nothing matches",
                            body = if (state.hasActiveFilter) {
                                "Try a different search or clear the filters."
                            } else {
                                "The catalogue is empty."
                            },
                            modifier = Modifier.height(360.dp),
                        )
                    }

                else -> items(state.products, key = { it.id }) { product ->
                    ProductCard(
                        product = product,
                        onClick = { onProductClick(product.id) },
                        onAdd = { viewModel.onAddToCart(product) },
                    )
                }
            }
        }
    }
}

private fun LazyGridScope.fullWidth(content: @Composable () -> Unit) {
    item(span = { GridItemSpan(maxLineSpan) }) { content() }
}

@Composable
private fun Header(cartCount: Int, onCartClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 12.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(
                "Welcome to ShopKart",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text("Discover something good", style = MaterialTheme.typography.headlineSmall)
        }
        Surface(
            onClick = onCartClick,
            shape = CircleShape,
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 2.dp,
            modifier = Modifier.size(48.dp),
        ) {
            Box(contentAlignment = Alignment.Center) {
                BadgedBox(
                    badge = {
                        if (cartCount > 0) {
                            Badge(containerColor = BrandColors.Coral) { Text(cartCount.toString()) }
                        }
                    },
                ) {
                    Icon(Icons.Outlined.ShoppingBag, contentDescription = "Basket, $cartCount items")
                }
            }
        }
    }
}

@Composable
private fun SearchField(value: String, onValueChange: (String) -> Unit) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = { Text("Search headphones, coffee, jackets…") },
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
        trailingIcon = {
            if (value.isNotEmpty()) {
                IconButton(onClick = { onValueChange("") }) {
                    Icon(Icons.Default.Close, contentDescription = "Clear search")
                }
            }
        },
        singleLine = true,
        shape = RoundedCornerShape(50),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.surface,
            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
        ),
        modifier = Modifier.fillMaxWidth(),
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun PromoPager(products: List<Product>, onOpen: (String) -> Unit) {
    val promos = PROMOS.filter { promo -> products.any { it.category == promo.category } }
    if (promos.isEmpty()) return
    val pagerState = rememberPagerState { promos.size }

    Column {
        HorizontalPager(state = pagerState, pageSpacing = 12.dp) { page ->
            val promo = promos[page]
            val hero = products.filter { it.category == promo.category }.maxByOrNull { it.rating.count }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .clip(MaterialTheme.shapes.large)
                    .background(Brush.linearGradient(promo.colors))
                    .clickable { onOpen(promo.category) }
                    .padding(start = 20.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(Modifier.weight(1f)) {
                    Text(promo.title, style = MaterialTheme.typography.titleLarge, color = Color.White)
                    Spacer(Modifier.height(4.dp))
                    Text(promo.body, style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.85f))
                    Spacer(Modifier.height(12.dp))
                    Row(
                        Modifier
                            .background(Color.White, RoundedCornerShape(50))
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text("Shop now", style = MaterialTheme.typography.labelMedium, color = promo.colors.first())
                        Spacer(Modifier.width(4.dp))
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            tint = promo.colors.first(),
                            modifier = Modifier.size(14.dp),
                        )
                    }
                }
                if (hero != null) {
                    ProductImage(
                        url = hero.imageUrl,
                        modifier = Modifier
                            .padding(12.dp)
                            .size(120.dp)
                            .clip(MaterialTheme.shapes.medium),
                    )
                }
            }
        }
        Row(
            Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.Center,
        ) {
            repeat(promos.size) { index ->
                val active = pagerState.currentPage == index
                Box(
                    Modifier
                        .padding(horizontal = 3.dp)
                        .height(6.dp)
                        .width(if (active) 18.dp else 6.dp)
                        .background(
                            if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                            RoundedCornerShape(50),
                        ),
                )
            }
        }
    }
}

@Composable
private fun CategoryRow(categories: List<Category>, selected: String?, onSelect: (String?) -> Unit) {
    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item { CategoryTile(name = "All", slug = null, selected = selected == null, onSelect = onSelect) }
        items(categories, key = { it.slug }) { category ->
            CategoryTile(
                name = category.name,
                slug = category.slug,
                selected = selected == category.slug,
                onSelect = onSelect,
            )
        }
    }
}

@Composable
private fun CategoryTile(name: String, slug: String?, selected: Boolean, onSelect: (String?) -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(MaterialTheme.shapes.medium)
            .clickable { onSelect(slug) }
            .padding(4.dp),
    ) {
        Box(
            Modifier
                .size(60.dp)
                .background(
                    if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                    CircleShape,
                ),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                categoryIcon(slug),
                contentDescription = null,
                tint = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary,
            )
        }
        Spacer(Modifier.height(6.dp))
        Text(
            name,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
private fun TopRatedRow(products: List<Product>, onProductClick: (String) -> Unit) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        items(products, key = { "top-${it.id}" }) { product ->
            ProductCard(
                product = product,
                onClick = { onProductClick(product.id) },
                modifier = Modifier.width(168.dp),
            )
        }
    }
}

@Composable
private fun SectionTitle(title: String, subtitle: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        verticalAlignment = Alignment.Bottom,
    ) {
        Text(title, style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f))
        Text(subtitle, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun SortRow(selected: ProductSort, onSelect: (ProductSort) -> Unit) {
    val options = listOf(
        ProductSort.Relevance to "Relevance",
        ProductSort.PriceAsc to "Price: low to high",
        ProductSort.RatingDesc to "Top rated",
        ProductSort.TitleAsc to "A–Z",
    )

    LazyRow(
        contentPadding = PaddingValues(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(options) { (sort, label) ->
            FilterChip(
                selected = selected == sort,
                onClick = { onSelect(sort) },
                label = { Text(label) },
                shape = RoundedCornerShape(50),
                colors = FilterChipDefaults.filterChipColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                ),
            )
        }
    }
}
