package com.riztech.shopkart.feature.catalog

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.riztech.shopkart.designsystem.component.*
import com.riztech.shopkart.domain.repository.ProductSort

@OptIn(ExperimentalMaterial3Api::class)
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

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("ShopKart") },
                actions = {
                    IconButton(onClick = onCartClick) {
                        BadgedBox(
                            badge = {
                                if (state.cartItemCount > 0) {
                                    Badge { Text(state.cartItemCount.toString()) }
                                }
                            },
                        ) {
                            Icon(Icons.Default.ShoppingCart, contentDescription = "Basket")
                        }
                    }
                },
            )
        },
    ) { padding ->
        Column(Modifier.padding(padding)) {
            OutlinedTextField(
                value = state.search,
                onValueChange = viewModel::onSearchChange,
                placeholder = { Text("Search products") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
            )

            if (state.categories.isNotEmpty()) {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    item {
                        FilterChip(
                            selected = state.selectedCategory == null,
                            onClick = { viewModel.onCategorySelected(null) },
                            label = { Text("All") },
                        )
                    }
                    items(state.categories, key = { it.slug }) { category ->
                        FilterChip(
                            selected = state.selectedCategory == category.slug,
                            onClick = { viewModel.onCategorySelected(category.slug) },
                            label = { Text(category.name) },
                        )
                    }
                }
            }

            SortRow(selected = state.sort, onSelect = viewModel::onSortSelected)

            when {
                state.isLoading && state.products.isEmpty() -> LoadingState()

                state.errorMessage != null && state.products.isEmpty() ->
                    ErrorState(message = state.errorMessage!!, onRetry = viewModel::retry)

                state.isEmpty -> EmptyState(
                    title = "Nothing matches",
                    body = if (state.hasActiveFilter) {
                        "Try a different search or clear the filters."
                    } else {
                        "The catalogue is empty."
                    },
                )

                else -> LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    items(state.products, key = { it.id }) { product ->
                        ProductCard(product = product, onClick = { onProductClick(product.id) })
                    }
                }
            }
        }
    }
}

@Composable
private fun SortRow(selected: ProductSort, onSelect: (ProductSort) -> Unit) {
    val options = listOf(
        ProductSort.Relevance to "Relevance",
        ProductSort.PriceAsc to "Price",
        ProductSort.RatingDesc to "Rating",
        ProductSort.TitleAsc to "A-Z",
    )

    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(options) { (sort, label) ->
            FilterChip(
                selected = selected == sort,
                onClick = { onSelect(sort) },
                label = { Text(label) },
            )
        }
    }
}
