package com.riztech.shopkart.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.riztech.shopkart.feature.cart.CartScreen
import com.riztech.shopkart.feature.catalog.CatalogScreen
import com.riztech.shopkart.feature.catalog.detail.ProductDetailScreen

/**
 * Routes as constants, not string literals at every call site.
 *
 * A typo in a navigate() string compiles fine and fails at runtime with a route
 * not found. Keeping them here means there is one spelling of each.
 */
object Routes {
    const val CATALOG = "catalog"
    const val CART = "cart"
    const val PRODUCT_DETAIL = "product/{productId}"

    fun productDetail(productId: String) = "product/$productId"
}

@Composable
fun ShopKartNavHost() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Routes.CATALOG) {

        composable(Routes.CATALOG) {
            CatalogScreen(
                onProductClick = { navController.navigate(Routes.productDetail(it)) },
                onCartClick = { navController.navigate(Routes.CART) },
            )
        }

        composable(
            route = Routes.PRODUCT_DETAIL,
            arguments = listOf(navArgument("productId") { type = NavType.StringType }),
        ) {
            // No productId parameter here: the ViewModel reads it from
            // SavedStateHandle, so the argument survives process death without
            // the screen having to thread it through.
            ProductDetailScreen(onBack = navController::popBackStack)
        }

        composable(Routes.CART) {
            CartScreen(
                onBack = navController::popBackStack,
                onContinueShopping = {
                    // Return to the catalogue rather than stacking another copy
                    // of it on top of the basket.
                    navController.popBackStack(Routes.CATALOG, inclusive = false)
                },
            )
        }
    }
}
