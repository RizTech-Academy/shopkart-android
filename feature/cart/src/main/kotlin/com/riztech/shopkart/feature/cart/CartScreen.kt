package com.riztech.shopkart.feature.cart

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.riztech.shopkart.designsystem.component.EmptyState
import com.riztech.shopkart.designsystem.component.ProductImage
import com.riztech.shopkart.designsystem.component.QuantityStepper
import com.riztech.shopkart.designsystem.format.format
import com.riztech.shopkart.designsystem.theme.BrandColors
import com.riztech.shopkart.designsystem.theme.PriceStyle
import com.riztech.shopkart.domain.model.Cart
import com.riztech.shopkart.domain.model.CartLine
import com.riztech.shopkart.domain.model.Order

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartScreen(
    onBack: (() -> Unit)?,
    onContinueShopping: () -> Unit,
    viewModel: CartViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.messages.collect { snackbarHostState.showSnackbar(it) }
    }

    state.placedOrder?.let { order ->
        OrderPlacedSheet(
            order = order,
            onDismiss = viewModel::onOrderConfirmationDismissed,
            onContinue = {
                viewModel.onOrderConfirmationDismissed()
                onContinueShopping()
            },
        )
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        // The app's bottom bar owns the navigation insets.
        contentWindowInsets = WindowInsets.statusBars,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(if (state.cart.isEmpty) "Basket" else "Basket · ${state.cart.itemCount}")
                },
                navigationIcon = {
                    if (onBack != null) {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                ),
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            if (!state.cart.isEmpty) {
                CheckoutBar(
                    total = state.cart.subtotal.format(),
                    isPlacing = state.isPlacingOrder,
                    onCheckout = viewModel::onCheckout,
                )
            }
        },
    ) { padding ->
        if (state.cart.isEmpty) {
            EmptyState(
                title = "Your basket is empty",
                body = "Find something you love. Everything you add appears here, even offline.",
                icon = Icons.Outlined.ShoppingBag,
                modifier = Modifier.padding(padding),
                action = { Button(onClick = onContinueShopping) { Text("Start shopping") } },
            )
        } else {
            LazyColumn(
                modifier = Modifier.padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(state.cart.lines, key = { it.product.id }) { line ->
                    CartLineCard(
                        line = line,
                        onQuantityChange = { viewModel.onQuantityChange(line.product.id, it) },
                    )
                }
                item { Summary(cart = state.cart) }
            }
        }
    }
}

@Composable
private fun CartLineCard(line: CartLine, onQuantityChange: (Int) -> Unit) {
    Card(
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            ProductImage(
                url = line.product.imageUrl,
                modifier = Modifier
                    .size(84.dp)
                    .clip(MaterialTheme.shapes.medium),
            )
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    line.product.category.replaceFirstChar { it.uppercase() },
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                )
                Text(
                    line.product.title,
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // The line total, not the unit price: the number next to a
                    // quantity should be the one that adds up to the total.
                    Text(line.lineTotal.format(), style = PriceStyle, modifier = Modifier.weight(1f))
                    QuantityStepper(quantity = line.quantity, onQuantityChange = onQuantityChange)
                }
                if (line.quantity > 1) {
                    Text(
                        "${line.product.price.format()} each",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Composable
private fun Summary(cart: Cart) {
    Card(
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("Order summary", style = MaterialTheme.typography.titleMedium)
            SummaryRow("Items", cart.itemCount.toString())
            SummaryRow("Subtotal", cart.subtotal.format())
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Total", style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                Text(cart.subtotal.format(), style = MaterialTheme.typography.titleLarge)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Outlined.Lock,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    "Demo shop: placing an order records it, no payment is taken.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun SummaryRow(label: String, value: String) {
    Row {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1f))
        Text(value, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun CheckoutBar(total: String, isPlacing: Boolean, onCheckout: () -> Unit) {
    Surface(shadowElevation = 12.dp, color = MaterialTheme.colorScheme.surface) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f)) {
                Text("Total", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(total, style = MaterialTheme.typography.headlineSmall)
            }
            Button(
                onClick = onCheckout,
                enabled = !isPlacing,
                contentPadding = PaddingValues(horizontal = 32.dp, vertical = 14.dp),
            ) {
                if (isPlacing) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary,
                    )
                } else {
                    Text("Place order")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun OrderPlacedSheet(order: Order, onDismiss: () -> Unit, onContinue: () -> Unit) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                Modifier
                    .size(80.dp)
                    .background(BrandColors.Mint.copy(alpha = 0.15f), CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Box(
                    Modifier
                        .size(52.dp)
                        .background(BrandColors.Mint, CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Filled.Check, contentDescription = null, tint = MaterialTheme.colorScheme.surface)
                }
            }
            Spacer(Modifier.height(16.dp))
            Text("Order placed", style = MaterialTheme.typography.headlineSmall)
            Spacer(Modifier.height(4.dp))
            Text(
                "Thank you! We've saved your order.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(20.dp))
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    SummaryRow("Reference", order.reference)
                    SummaryRow("Items", order.itemCount.toString())
                    SummaryRow("Total", order.total.format())
                }
            }
            Spacer(Modifier.height(20.dp))
            Button(onClick = onContinue, modifier = Modifier.fillMaxWidth()) { Text("Continue shopping") }
        }
    }
}
