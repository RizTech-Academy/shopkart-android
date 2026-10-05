package com.riztech.shopkart.designsystem.component

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

/**
 * Increment and decrement for a cart line, in one pill.
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
    Row(
        modifier = modifier
            .height(40.dp)
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(50)),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = { onQuantityChange(quantity - 1) }, modifier = Modifier.size(40.dp)) {
            if (quantity <= 1) {
                Icon(
                    Icons.Outlined.DeleteOutline,
                    contentDescription = "Remove from basket",
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(20.dp),
                )
            } else {
                Icon(Icons.Default.Remove, contentDescription = "Decrease quantity", modifier = Modifier.size(20.dp))
            }
        }
        Text(
            text = quantity.toString(),
            style = MaterialTheme.typography.titleSmall,
            textAlign = TextAlign.Center,
            modifier = Modifier.widthIn(min = 20.dp),
        )
        IconButton(
            onClick = { onQuantityChange(quantity + 1) },
            enabled = quantity < maxQuantity,
            modifier = Modifier.size(40.dp),
        ) {
            Icon(Icons.Default.Add, contentDescription = "Increase quantity", modifier = Modifier.size(20.dp))
        }
    }
}
