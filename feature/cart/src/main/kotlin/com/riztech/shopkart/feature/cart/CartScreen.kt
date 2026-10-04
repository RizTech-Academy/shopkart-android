package com.riztech.shopkart.feature.cart

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.riztech.shopkart.designsystem.component.EmptyState
import com.riztech.shopkart.designsystem.component.QuantityStepper
import com.riztech.shopkart.designsystem.format.format
import com.riztech.shopkart.domain.model.CartLine

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartScreen(
    onBack: () -> Unit,
    onContinueShopping: () -> Unit,
    viewModel: CartViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.messages.collect { snackbarHostState.showSnackbar(it) }
    }

    state.placedOrder?.let { order ->
        AlertDialog(
            onDismissRequest = viewModel::onOrderConfirmationDismissed,
            title = { Text("Order placed") },
            text = {
                Text(
                    "Reference ${order.reference}\n" +
                        "${order.itemCount} item(s), ${order.total.format()}",
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.onOrderConfirmationDismissed()
                        onContinueShopping()
                    },
                ) { Text("Continue shopping") }
            },
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Basket") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            if (!state.cart.isEmpty) {
                CheckoutBar(
                    subtotal = state.cart.subtotal.format(),
                    itemCount = state.cart.itemCount,
                    isPlacing = state.isPlacingOrder,
                    onCheckout = viewModel::onCheckout,
                )
            }
        },
    ) { padding ->
        if (state.cart.isEmpty) {
            EmptyState(
                title = "Your basket is empty",
                body = "Products you add will appear here.",
                modifier = Modifier.padding(padding),
                action = { Button(onClick = onContinueShopping) { Text("Browse products") } },
            )
        } else {
            LazyColumn(
                modifier = Modifier.padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(state.cart.lines, key = { it.product.id }) { line ->
                    CartLineRow(
                        line = line,
                        onQuantityChange = { viewModel.onQuantityChange(line.product.id, it) },
                    )
                }
            }
        }
    }
}

@Composable
private fun CartLineRow(line: CartLine, onQuantityChange: (Int) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        AsyncImage(
            model = line.product.imageUrl,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.size(64.dp),
        )
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(
                line.product.title,
                style = MaterialTheme.typography.titleSmall,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            // The line total, not the unit price: the number next to a quantity
            // should be the one that adds up to the subtotal shown below.
            Text(line.lineTotal.format(), style = MaterialTheme.typography.bodyMedium)
        }
        QuantityStepper(quantity = line.quantity, onQuantityChange = onQuantityChange)
    }
}

@Composable
private fun CheckoutBar(
    subtotal: String,
    itemCount: Int,
    isPlacing: Boolean,
    onCheckout: () -> Unit,
) {
    Surface(tonalElevation = 3.dp) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f)) {
                Text("Subtotal", style = MaterialTheme.typography.labelMedium)
                Text(
                    subtotal,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                )
            }
            Button(onClick = onCheckout, enabled = !isPlacing) {
                if (isPlacing) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                    )
                } else {
                    Text("Checkout ($itemCount)")
                }
            }
        }
    }
}
