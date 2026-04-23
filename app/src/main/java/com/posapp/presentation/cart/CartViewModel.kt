package com.posapp.presentation.cart

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.posapp.data.repository.OrderRepository
import com.posapp.domain.model.CartItem
import com.posapp.presentation.printer.PrinterManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class CartUiState(
    val items: List<CartItem> = emptyList(),
    val total: Double = 0.0,
    val isPrinting: Boolean = false,
    val printSuccess: Boolean = false,
    val printError: String? = null,
    val orderSaved: Boolean = false
)

class CartViewModel(
    private val orderRepository: OrderRepository,
    private val printerManager: PrinterManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(CartUiState())
    val uiState: StateFlow<CartUiState> = _uiState.asStateFlow()

    fun updateCart(items: List<CartItem>, total: Double) {
        _uiState.value = _uiState.value.copy(items = items, total = total)
    }

    fun printAndSave(context: Context, storeName: String, footer: String) {
        if (_uiState.value.items.isEmpty()) return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isPrinting = true, printError = null)

            try {
                printerManager.printReceipt(
                    context = context,
                    storeName = storeName,
                    items = _uiState.value.items,
                    total = _uiState.value.total,
                    footer = footer
                )

                orderRepository.saveOrder(_uiState.value.items, _uiState.value.total)

                _uiState.value = _uiState.value.copy(
                    isPrinting = false,
                    printSuccess = true,
                    orderSaved = true,
                    items = emptyList(),
                    total = 0.0
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isPrinting = false,
                    printError = e.message ?: "Print failed"
                )
            }
        }
    }

    fun clearPrintStatus() {
        _uiState.value = _uiState.value.copy(printSuccess = false, printError = null)
    }
}

class CartViewModelFactory(
    private val orderRepository: OrderRepository,
    private val printerManager: PrinterManager
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CartViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CartViewModel(orderRepository, printerManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}