package com.riztech.shopkart.feature.catalog

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.riztech.shopkart.domain.repository.CartRepository
import com.riztech.shopkart.domain.repository.ProductFilter
import com.riztech.shopkart.domain.repository.ProductSort
import com.riztech.shopkart.domain.usecase.GetCategoriesUseCase
import com.riztech.shopkart.domain.usecase.GetProductsUseCase
import com.riztech.shopkart.domain.util.DataError
import com.riztech.shopkart.domain.util.DataResult
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

@OptIn(FlowPreview::class)
@HiltViewModel
class CatalogViewModel @Inject constructor(
    private val getProducts: GetProductsUseCase,
    private val getCategories: GetCategoriesUseCase,
    cartRepository: CartRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(CatalogUiState())
    val state: StateFlow<CatalogUiState> = _state.asStateFlow()

    /** Drives the search; separate so it can be debounced without lagging the field. */
    private val query = MutableStateFlow(ProductFilter())

    init {
        viewModelScope.launch {
            query
                // The text field updates on every keystroke so typing stays
                // instant, but the network does not. Without this a six-letter
                // search fires six requests and the answers can arrive out of
                // order.
                .debounce { if (it.search.isNullOrBlank()) 0L else SEARCH_DEBOUNCE_MS }
                .distinctUntilChanged()
                .collectLatest { filter -> load(filter) }
        }

        viewModelScope.launch {
            when (val result = getCategories()) {
                is DataResult.Success -> _state.update { it.copy(categories = result.data) }
                is DataResult.Failure -> Unit // the catalogue still works without the filter chips
            }
        }

        // The badge comes straight from the cart's Flow, so it is correct the
        // moment anything changes it, from any screen.
        cartRepository.observeCart()
            .onEach { cart -> _state.update { it.copy(cartItemCount = cart.itemCount) } }
            .launchIn(viewModelScope)
    }

    fun onSearchChange(value: String) {
        _state.update { it.copy(search = value) }
        query.update { it.copy(search = value) }
    }

    fun onCategorySelected(slug: String?) {
        _state.update { it.copy(selectedCategory = slug) }
        query.update { it.copy(category = slug) }
    }

    fun onSortSelected(sort: ProductSort) {
        _state.update { it.copy(sort = sort) }
        query.update { it.copy(sort = sort) }
    }

    fun retry() {
        viewModelScope.launch { load(query.value) }
    }

    private suspend fun load(filter: ProductFilter) {
        _state.update { it.copy(isLoading = true, errorMessage = null) }

        when (val result = getProducts(filter)) {
            is DataResult.Success ->
                _state.update { it.copy(products = result.data, isLoading = false) }
            is DataResult.Failure ->
                _state.update {
                    it.copy(isLoading = false, errorMessage = result.error.toMessage())
                }
        }
    }

    /**
     * Error enum to words.
     *
     * Done here rather than in the data layer because wording is a presentation
     * concern, and here it can be localised.
     */
    private fun DataError.toMessage(): String = when (this) {
        DataError.Network -> "No connection, and nothing saved to show you yet."
        DataError.Server -> "The shop is having trouble. Please try again."
        DataError.NotFound -> "We could not find that."
        DataError.Unknown -> "Something went wrong."
    }

    private companion object {
        const val SEARCH_DEBOUNCE_MS = 300L
    }
}
