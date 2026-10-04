package com.riztech.shopkart.feature.catalog.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.riztech.shopkart.domain.model.Product
import com.riztech.shopkart.domain.usecase.AddToCartResult
import com.riztech.shopkart.domain.usecase.AddToCartUseCase
import com.riztech.shopkart.domain.usecase.GetProductUseCase
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
    val quantityInCart: Int = 0,
)

@HiltViewModel
class ProductDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getProduct: GetProductUseCase,
    private val addToCart: AddToCartUseCase,
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
    }

    fun load() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }
            when (val result = getProduct(productId)) {
                is DataResult.Success ->
                    _state.update { it.copy(product = result.data, isLoading = false) }
                is DataResult.Failure ->
                    _state.update {
                        it.copy(isLoading = false, errorMessage = "Could not load this product.")
                    }
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
                AddToCartResult.Added -> {
                    _state.update { it.copy(quantityInCart = it.quantityInCart + 1) }
                    _messages.send("Added to basket")
                }
                AddToCartResult.OutOfStock -> _messages.send("That item is out of stock")
                AddToCartResult.InvalidQuantity -> _messages.send("That quantity is not valid")
            }
        }
    }
}
