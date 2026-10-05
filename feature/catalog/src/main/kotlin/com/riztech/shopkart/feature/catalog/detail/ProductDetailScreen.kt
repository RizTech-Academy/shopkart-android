package com.riztech.shopkart.feature.catalog.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.CloudDone
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.riztech.shopkart.designsystem.component.ErrorState
import com.riztech.shopkart.designsystem.component.LoadingState
import com.riztech.shopkart.designsystem.component.ProductCard
import com.riztech.shopkart.designsystem.component.ProductImage
import com.riztech.shopkart.designsystem.component.QuantityStepper
import com.riztech.shopkart.designsystem.component.RatingStars
import com.riztech.shopkart.designsystem.component.badge
import com.riztech.shopkart.designsystem.format.format
import com.riztech.shopkart.designsystem.theme.BrandColors
import com.riztech.shopkart.domain.model.Product

@Composable
fun ProductDetailScreen(
    onBack: () -> Unit,
    onCartClick: () -> Unit = {},
    onProductClick: (String) -> Unit = {},
    viewModel: ProductDetailViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.messages.collect { snackbarHostState.showSnackbar(it) }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0),
        bottomBar = {
            state.product?.let { product ->
                BuyBar(
                    product = product,
                    quantityInCart = state.quantityInCart,
                    onAdd = viewModel::addToCart,
                    onQuantityChange = viewModel::onQuantityChange,
                    onCartClick = onCartClick,
                )
            }
        },
    ) { padding ->
        Box(Modifier.padding(padding)) {
            when {
                state.isLoading -> LoadingState()

                state.errorMessage != null ->
                    ErrorState(message = state.errorMessage!!, onRetry = viewModel::load)

                else -> state.product?.let { product ->
                    ProductContent(
                        product = product,
                        related = state.related,
                        onProductClick = onProductClick,
                    )
                }
            }

            Row(
                Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                RoundIconButton(Icons.AutoMirrored.Filled.ArrowBack, "Back", onBack)
                BadgedBox(
                    badge = {
                        if (state.cartItemCount > 0) {
                            Badge(containerColor = BrandColors.Coral) { Text(state.cartItemCount.toString()) }
                        }
                    },
                ) {
                    RoundIconButton(Icons.Outlined.ShoppingBag, "Basket", onCartClick)
                }
            }
        }
    }
}

@Composable
private fun RoundIconButton(icon: ImageVector, description: String, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
        shadowElevation = 2.dp,
        modifier = Modifier.size(44.dp),
    ) {
        Box(contentAlignment = Alignment.Center) { Icon(icon, contentDescription = description) }
    }
}

@Composable
private fun ProductContent(product: Product, related: List<Product>, onProductClick: (String) -> Unit) {
    Column(Modifier.verticalScroll(rememberScrollState())) {
        ProductImage(
            url = product.imageUrl,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f),
        )

        // The sheet overlaps the photo slightly, which reads as one object
        // rather than a picture with text pasted underneath.
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .offset(y = (-28).dp),
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
            color = MaterialTheme.colorScheme.background,
        ) {
            Column(Modifier.padding(20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        product.category.replaceFirstChar { it.uppercase() },
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.weight(1f),
                    )
                    product.badge()?.let { badge ->
                        Text(
                            badge.label,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier
                                .background(MaterialTheme.colorScheme.secondaryContainer, RoundedCornerShape(50))
                                .padding(horizontal = 10.dp, vertical = 4.dp),
                        )
                    }
                }
                Spacer(Modifier.height(6.dp))
                Text(product.title, style = MaterialTheme.typography.headlineSmall)
                Spacer(Modifier.height(8.dp))
                RatingStars(product.rating, starSize = 18.dp)
                Spacer(Modifier.height(16.dp))
                StockChip(inStock = product.inStock)

                Spacer(Modifier.height(24.dp))
                Text("About this product", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))
                Text(
                    product.description,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                Spacer(Modifier.height(20.dp))
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = MaterialTheme.shapes.large,
                ) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Perk(Icons.Outlined.CloudDone, "Saved for offline", "Browse this page again without a connection.")
                        Perk(Icons.Outlined.Inventory2, "Live stock", "Availability comes straight from the shop.")
                    }
                }

                if (related.isNotEmpty()) {
                    Spacer(Modifier.height(28.dp))
                    Text("You may also like", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(12.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(related, key = { it.id }) { item ->
                            ProductCard(
                                product = item,
                                onClick = { onProductClick(item.id) },
                                modifier = Modifier.width(160.dp),
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StockChip(inStock: Boolean) {
    val color = if (inStock) BrandColors.Mint else MaterialTheme.colorScheme.error
    Row(
        Modifier
            .background(color.copy(alpha = 0.12f), RoundedCornerShape(50))
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(Icons.Outlined.CheckCircle, contentDescription = null, tint = color, modifier = Modifier.size(16.dp))
        Spacer(Modifier.width(6.dp))
        Text(
            if (inStock) "In stock" else "Out of stock",
            style = MaterialTheme.typography.labelLarge,
            color = color,
        )
    }
}

@Composable
private fun Perk(icon: ImageVector, title: String, body: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            Modifier
                .size(40.dp)
                .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
        }
        Spacer(Modifier.width(12.dp))
        Column {
            Text(title, style = MaterialTheme.typography.titleSmall)
            Text(body, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

/**
 * Price and the one action that matters, always on screen.
 *
 * Once the product is in the basket the button becomes a stepper, so the
 * shopper can see and change how many they have without leaving the page.
 */
@Composable
private fun BuyBar(
    product: Product,
    quantityInCart: Int,
    onAdd: () -> Unit,
    onQuantityChange: (Int) -> Unit,
    onCartClick: () -> Unit,
) {
    Surface(shadowElevation = 12.dp, color = MaterialTheme.colorScheme.surface) {
        Row(
            Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f)) {
                Text("Price", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(product.price.format(), style = MaterialTheme.typography.headlineSmall)
            }
            when {
                !product.inStock -> Button(onClick = {}, enabled = false) { Text("Out of stock") }
                quantityInCart > 0 -> Row(verticalAlignment = Alignment.CenterVertically) {
                    QuantityStepper(quantity = quantityInCart, onQuantityChange = onQuantityChange)
                    Spacer(Modifier.width(8.dp))
                    Button(onClick = onCartClick) { Text("View basket") }
                }
                else -> Button(
                    onClick = onAdd,
                    contentPadding = PaddingValues(horizontal = 28.dp, vertical = 14.dp),
                ) {
                    Icon(Icons.Outlined.ShoppingBag, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Add to basket")
                }
            }
        }
    }
}
