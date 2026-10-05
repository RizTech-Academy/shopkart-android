package com.riztech.shopkart.designsystem.component

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Checkroom
import androidx.compose.material.icons.outlined.Chair
import androidx.compose.material.icons.outlined.Computer
import androidx.compose.material.icons.outlined.Headphones
import androidx.compose.material.icons.outlined.Kitchen
import androidx.compose.material.icons.outlined.Landscape
import androidx.compose.material.icons.outlined.Sell
import androidx.compose.material.icons.outlined.Storefront
import androidx.compose.ui.graphics.vector.ImageVector

/** An icon per catalogue category; unknown categories get a generic tag. */
fun categoryIcon(slug: String?): ImageVector = when (slug) {
    null -> Icons.Outlined.Storefront
    "audio" -> Icons.Outlined.Headphones
    "computing" -> Icons.Outlined.Computer
    "outdoors" -> Icons.Outlined.Landscape
    "apparel" -> Icons.Outlined.Checkroom
    "kitchen" -> Icons.Outlined.Kitchen
    "home" -> Icons.Outlined.Chair
    else -> Icons.Outlined.Sell
}
