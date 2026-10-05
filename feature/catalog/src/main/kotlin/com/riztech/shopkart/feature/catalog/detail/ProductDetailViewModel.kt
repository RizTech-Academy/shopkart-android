package com.riztech.shopkart.feature.catalog.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.riztech.shopkart.domain.model.Product
import com.riztech.shopkart.domain.repository.ProductFilter
import com.riztech.shopkart.domain.usecase.AddToCartResult
import com.riztech.shopkart.domain.usecase.AddToCartUseCase
import com.riztech.shopkart.domain.usecase.GetProductUseCase
import com.riztech.shopkart.domain.usecase.GetProductsUseCase
import com.riztech.shopkart.domain.usecase.ObserveCartUseCase
import com.riztech.shopkart.domain.usecase.UpdateCartQuantityUseCase
import com.riztech.shopkart.domain.util.DataResult
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class ProductDetailUiState(
    val product: Product? = null,
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    /** Read from the basket itself, so it is right however the quantity changed. */
    val quantityInCart: Int = 0,
    val related: List<Product> = emptyList(),
    val cartItemCount: Int = 0,
)

@HiltViewModel
class ProductDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getProduct: GetProductUseCase,
    private val getProducts: GetProductsUseCase,
    private val addToCart: AddToCartUseCase,
    private val updateQuantity: UpdateCartQuantityUseCase,
    observeCart: ObserveCartUseCase,
) : ViewModel() {

    private val productId: String = checkNotNull(savedStateHandle["productId"])

    private val _state = MutableStateFlow(ProductDetailUiState())
    val state: StateFlow<ProductDetailUiState> = _state.asStateFlow()

    /**
     * One-shot messages, as a Channel rather than part of the state.
     *
     * A snackbar held in state fires again on every configuration change -
     * rotate the device and the confirmation reappears. A Channel is consumed
     * exactly once.
     */
    private val _messages = Channel<String>(Channel.BUFFERED)
    val messages: Flow<String> = _messages.receiveAsFlow()

    init {
        load()
        observeCart()
            .onEach { cart ->
                _state.update {
                    it.copy(quantityInCart = cart.quantityOf(productId), cartItemCount = cart.itemCount)
                }
            }
            .launchIn(viewModelScope)
    }

    fun load() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }
            when (val result = getProduct(productId)) {
                is DataResult.Success -> {
                    _state.update { it.copy(product = result.data, isLoading = false) }
                    loadRelated(result.data)
                }
                is DataResult.Failure ->
                    _state.update {
                        it.copy(isLoading = false, errorMessage = "Could not load this product.")
                    }
            }
        }
    }

    /** More from the same category. Optional: the page works without it. */
    private suspend fun loadRelated(product: Product) {
        val result = getProducts(ProductFilter(category = product.category))
        if (result is DataResult.Success) {
            _state.update { state ->
                state.copy(related = result.data.filter { it.id != product.id }.take(6))
            }
        }
    }

    fun addToCart() {
        val product = _state.value.product ?: return
        viewModelScope.launch {
            // The use case refuses an out-of-stock product. The rule lives
            // there, so a deep link or a stale screen cannot bypass it by
            // calling this directly. Every outcome is handled explicitly - the
            // `when` is exhaustive, so a new result type breaks the build
            // rather than falling through silently.
            when (addToCart(product, quantity = 1)) {
                AddToCartResult.Added -> _messages.send("Added to basket")
                AddToCartResult.OutOfStock -> _messages.send("That item is out of stock")
                AddToCartResult.InvalidQuantity -> _messages.send("That quantity is not valid")
            }
        }
    }

    fun onQuantityChange(quantity: Int) {
        viewModelScope.launch { updateQuantity(productId, quantity) }
    }
}
