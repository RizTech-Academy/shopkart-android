package com.riztech.shopkart.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.riztech.shopkart.designsystem.theme.BrandColors
import com.riztech.shopkart.domain.usecase.ObserveCartUseCase
import com.riztech.shopkart.feature.cart.CartScreen
import com.riztech.shopkart.feature.catalog.CatalogScreen
import com.riztech.shopkart.feature.catalog.detail.ProductDetailScreen
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

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

/** The basket count for the bottom bar's badge, straight from the cart's Flow. */
@HiltViewModel
class AppShellViewModel @Inject constructor(observeCart: ObserveCartUseCase) : ViewModel() {
    val cartItemCount: StateFlow<Int> = observeCart()
        .map { it.itemCount }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)
}

@Composable
fun ShopKartNavHost(shell: AppShellViewModel = hiltViewModel()) {
    val navController = rememberNavController()
    val cartCount by shell.cartItemCount.collectAsStateWithLifecycle()
    val route = navController.currentBackStackEntryAsState().value?.destination?.route

    Scaffold(
        bottomBar = {
            // The product page has its own buy bar; two bars stacked would crowd it.
            if (route == Routes.CATALOG || route == Routes.CART) {
                BottomBar(route = route, cartCount = cartCount, navController = navController)
            }
        },
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Routes.CATALOG,
            modifier = Modifier.padding(bottom = padding.calculateBottomPadding()),
        ) {
            composable(Routes.CATALOG) {
                CatalogScreen(
                    onProductClick = { navController.navigate(Routes.productDetail(it)) },
                    onCartClick = { navController.switchTab(Routes.CART) },
                )
            }

            composable(
                route = Routes.PRODUCT_DETAIL,
                arguments = listOf(navArgument("productId") { type = NavType.StringType }),
            ) {
                // No productId parameter here: the ViewModel reads it from
                // SavedStateHandle, so the argument survives process death without
                // the screen having to thread it through.
                ProductDetailScreen(
                    onBack = navController::popBackStack,
                    onCartClick = { navController.switchTab(Routes.CART) },
                    onProductClick = { navController.navigate(Routes.productDetail(it)) },
                )
            }

            composable(Routes.CART) {
                CartScreen(
                    // A tab, not a pushed screen: the bottom bar is the way back.
                    onBack = null,
                    onContinueShopping = { navController.switchTab(Routes.CATALOG) },
                )
            }
        }
    }
}

@Composable
private fun BottomBar(route: String?, cartCount: Int, navController: NavHostController) {
    NavigationBar(containerColor = MaterialTheme.colorScheme.surface, tonalElevation = 0.dp) {
        NavigationBarItem(
            selected = route == Routes.CATALOG,
            onClick = { navController.switchTab(Routes.CATALOG) },
            icon = { Icon(if (route == Routes.CATALOG) Icons.Filled.Home else Icons.Outlined.Home, contentDescription = null) },
            label = { Text("Home") },
            colors = itemColors(),
        )
        NavigationBarItem(
            selected = route == Routes.CART,
            onClick = { navController.switchTab(Routes.CART) },
            icon = {
                BadgedBox(
                    badge = {
                        if (cartCount > 0) Badge(containerColor = BrandColors.Accent) { Text(cartCount.toString()) }
                    },
                ) {
                    Icon(
                        if (route == Routes.CART) Icons.Filled.ShoppingBag else Icons.Outlined.ShoppingBag,
                        contentDescription = null,
                    )
                }
            },
            label = { Text("Basket") },
            colors = itemColors(),
        )
    }
}

@Composable
private fun itemColors() = NavigationBarItemDefaults.colors(
    indicatorColor = MaterialTheme.colorScheme.primaryContainer,
    selectedIconColor = MaterialTheme.colorScheme.primary,
    selectedTextColor = MaterialTheme.colorScheme.primary,
)

/**
 * Tab switching that does not pile up copies of the same screen: pop back to
 * the start destination, keep each tab's state, and reuse an existing entry.
 */
private fun NavHostController.switchTab(route: String) {
    navigate(route) {
        popUpTo(graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}
