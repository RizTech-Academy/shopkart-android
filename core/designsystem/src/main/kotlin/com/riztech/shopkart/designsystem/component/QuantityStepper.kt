package com.riztech.shopkart.designsystem.component

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Increment and decrement for a cart line.
 *
 * At a quantity of one the decrement becomes a bin icon, because the next tap
 * removes the line. Showing a minus that does something other than decrement is
 * a small lie that costs users an undo.
 */
@Composable
fun QuantityStepper(
    quantity: Int,
    onQuantityChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
    maxQuantity: Int = 99,
) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = { onQuantityChange(quantity - 1) }) {
            if (quantity <= 1) {
                Icon(Icons.Default.Delete, contentDescription = "Remove from basket")
            } else {
                Icon(Icons.Default.Remove, contentDescription = "Decrease quantity")
            }
        }

        Text(
            text = quantity.toString(),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.widthIn(min = 24.dp),
        )

        IconButton(
            onClick = { onQuantityChange(quantity + 1) },
            enabled = quantity < maxQuantity,
        ) {
            Icon(Icons.Default.Add, contentDescription = "Increase quantity")
        }
    }
}
