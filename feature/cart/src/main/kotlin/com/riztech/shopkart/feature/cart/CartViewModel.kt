package com.riztech.shopkart.feature.cart

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.riztech.shopkart.domain.model.Cart
import com.riztech.shopkart.domain.model.Order
import com.riztech.shopkart.domain.usecase.*
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class CartUiState(
    val cart: Cart = Cart.EMPTY,
    val isPlacingOrder: Boolean = false,
    val placedOrder: Order? = null,
)

@HiltViewModel
class CartViewModel @Inject constructor(
    observeCart: ObserveCartUseCase,
    private val updateQuantity: UpdateCartQuantityUseCase,
    private val removeFromCart: RemoveFromCartUseCase,
    private val placeOrder: PlaceOrderUseCase,
) : ViewModel() {

    private val _isPlacingOrder = MutableStateFlow(false)
    private val _placedOrder = MutableStateFlow<Order?>(null)

    /**
     * The cart is not copied into local state - it is the repository's Flow,
     * combined with the bits of state this screen genuinely owns. There is one
     * source of truth, so the basket cannot drift from what is stored.
     */
    val state: StateFlow<CartUiState> =
        combine(observeCart(), _isPlacingOrder, _placedOrder) { cart, placing, placed ->
            CartUiState(cart = cart, isPlacingOrder = placing, placedOrder = placed)
        }.stateIn(
            scope = viewModelScope,
            // Keep collecting for five seconds after the screen goes away, so a
            // rotation does not tear down and re-run the query.
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = CartUiState(),
        )

    private val _messages = Channel<String>(Channel.BUFFERED)
    val messages: Flow<String> = _messages.receiveAsFlow()

    fun onQuantityChange(productId: String, quantity: Int) {
        viewModelScope.launch {
            // Zero means remove. The use case owns that rule so swipe, stepper
            // and any future caller behave identically.
            updateQuantity(productId, quantity)
        }
    }

    fun onRemove(productId: String) {
        viewModelScope.launch {
            removeFromCart(productId)
            _messages.send("Removed from basket")
        }
    }

    fun onCheckout() {
        val cart = state.value.cart
        if (cart.isEmpty || _isPlacingOrder.value) return

        viewModelScope.launch {
            _isPlacingOrder.value = true
            when (val result = placeOrder(cart)) {
                is PlaceOrderResult.Placed -> _placedOrder.value = result.order
                PlaceOrderResult.CartEmpty -> _messages.send("Your basket is empty")
            }
            _isPlacingOrder.value = false
        }
    }

    fun onOrderConfirmationDismissed() {
        _placedOrder.value = null
    }
}
