package com.riztech.shopkart.designsystem.component

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.StarHalf
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.riztech.shopkart.designsystem.theme.BrandColors
import com.riztech.shopkart.domain.model.Rating
import kotlin.math.floor

/**
 * Five stars plus the average and the number of ratings.
 *
 * Read out as one sentence ("Rated 4.6 out of 5 from 218 ratings") rather than
 * five unlabelled icons and two loose numbers.
 */
@Composable
fun RatingStars(
    rating: Rating,
    modifier: Modifier = Modifier,
    starSize: Dp = 14.dp,
    showCount: Boolean = true,
) {
    Row(
        modifier = modifier.clearAndSetSemantics {
            contentDescription = "Rated ${rating.average} out of 5 from ${rating.count} ratings"
        },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        val full = floor(rating.average).toInt()
        val half = rating.average - full >= 0.5
        repeat(5) { index ->
            val icon = when {
                index < full -> Icons.Filled.Star
                index == full && half -> Icons.AutoMirrored.Filled.StarHalf
                else -> Icons.Outlined.StarOutline
            }
            Icon(icon, contentDescription = null, tint = BrandColors.Gold, modifier = Modifier.size(starSize))
        }
        Spacer(Modifier.width(4.dp))
        Text(
            if (showCount) "${rating.average} (${rating.count})" else "${rating.average}",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
