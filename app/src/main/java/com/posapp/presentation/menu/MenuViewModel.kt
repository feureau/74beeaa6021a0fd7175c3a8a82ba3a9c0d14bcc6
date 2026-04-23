package com.posapp.presentation.menu

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.posapp.data.repository.MenuRepository
import com.posapp.domain.model.CartItem
import com.posapp.domain.model.Category
import com.posapp.domain.model.MenuItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class MenuUiState(
    val isLoading: Boolean = true,
    val categories: List<Category> = emptyList(),
    val storeName: String = "",
    val cart: List<CartItem> = emptyList(),
    val cartTotal: Double = 0.0
)

class MenuViewModel(private val menuRepository: MenuRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(MenuUiState())
    val uiState: StateFlow<MenuUiState> = _uiState.asStateFlow()

    init {
        loadMenu()
    }

    private fun loadMenu() {
        viewModelScope.launch {
            val menu = menuRepository.getMenu()
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                categories = menu.categories,
                storeName = menu.restaurant
            )
        }
    }

    fun addToCart(menuItem: MenuItem) {
        val currentCart = _uiState.value.cart.toMutableList()
        val existingItem = currentCart.find { it.menuItem.id == menuItem.id }

        if (existingItem != null) {
            existingItem.quantity++
        } else {
            currentCart.add(CartItem(menuItem, 1))
        }

        val total = currentCart.sumOf { it.totalPrice }
        _uiState.value = _uiState.value.copy(cart = currentCart, cartTotal = total)
    }

    fun removeFromCart(menuItem: MenuItem) {
        val currentCart = _uiState.value.cart.toMutableList()
        val existingItem = currentCart.find { it.menuItem.id == menuItem.id }

        if (existingItem != null) {
            if (existingItem.quantity > 1) {
                existingItem.quantity--
            } else {
                currentCart.remove(existingItem)
            }
        }

        val total = currentCart.sumOf { it.totalPrice }
        _uiState.value = _uiState.value.copy(cart = currentCart, cartTotal = total)
    }

    fun clearCart() {
        _uiState.value = _uiState.value.copy(cart = emptyList(), cartTotal = 0.0)
    }

    fun getCart(): List<CartItem> = _uiState.value.cart

    fun getCartTotal(): Double = _uiState.value.cartTotal
}

class MenuViewModelFactory(private val menuRepository: MenuRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MenuViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MenuViewModel(menuRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}