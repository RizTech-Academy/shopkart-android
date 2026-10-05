package com.riztech.shopkart.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.riztech.shopkart.designsystem.format.format
import com.riztech.shopkart.designsystem.theme.BrandColors
import com.riztech.shopkart.designsystem.theme.PriceStyle
import com.riztech.shopkart.domain.model.Product

/** A label earned from the product's own numbers, never invented. */
enum class ProductBadge(val label: String) { TopRated("Top rated"), Popular("Popular") }

fun Product.badge(): ProductBadge? = when {
    rating.average >= 4.7 && rating.count >= 100 -> ProductBadge.TopRated
    rating.count >= 500 -> ProductBadge.Popular
    else -> null
}

/**
 * A product in the catalogue grid: photo first, then name, rating and price.
 *
 * The add button sits on the card so a shopper who already knows what they want
 * does not have to open the product to buy it. It is hidden, not disabled, when
 * the product is out of stock - a greyed-out button invites a confused tap.
 */
@Composable
fun ProductCard(
    product: Product,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    onAdd: (() -> Unit)? = null,
) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Box {
            ProductImage(
                url = product.imageUrl,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .padding(8.dp)
                    .clip(MaterialTheme.shapes.medium),
            )
            product.badge()?.let { badge ->
                Text(
                    badge.label,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White,
                    modifier = Modifier
                        .padding(14.dp)
                        .background(
                            if (badge == ProductBadge.TopRated) BrandColors.Primary else BrandColors.Accent,
                            RoundedCornerShape(50),
                        )
                        .padding(horizontal = 8.dp, vertical = 3.dp),
                )
            }
            if (!product.inStock) {
                Text(
                    "Out of stock",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.White,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(50))
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                )
            }
        }
        Column(Modifier.padding(start = 12.dp, end = 12.dp, bottom = 12.dp)) {
            Text(
                product.title,
                style = MaterialTheme.typography.titleSmall,
                maxLines = 2,
                minLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(4.dp))
            RatingStars(product.rating, starSize = 12.dp)
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(product.price.format(), style = PriceStyle, modifier = Modifier.weight(1f))
                if (onAdd != null && product.inStock) {
                    FilledIconButton(
                        onClick = onAdd,
                        modifier = Modifier.size(36.dp),
                        shape = CircleShape,
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add ${product.title} to basket")
                    }
                }
            }
        }
    }
}

/**
 * A product photo on a soft tinted ground, so a loading or missing image is a
 * calm placeholder rather than a hole in the grid.
 */
@Composable
fun ProductImage(url: String, modifier: Modifier = Modifier) {
    Box(modifier.background(MaterialTheme.colorScheme.surfaceVariant)) {
        AsyncImage(
            model = url,
            // Decorative: the product's name is always next to its picture.
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.matchParentSize(),
        )
    }
}
