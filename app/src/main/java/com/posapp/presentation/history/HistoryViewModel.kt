package com.posapp.presentation.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.posapp.data.repository.OrderRepository
import com.posapp.domain.model.Order
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class HistoryUiState(
    val isLoading: Boolean = true,
    val orders: List<Order> = emptyList(),
    val todayTotal: Double = 0.0,
    val currentDate: String = ""
)

class HistoryViewModel(private val orderRepository: OrderRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(HistoryUiState())
    val uiState: StateFlow<HistoryUiState> = _uiState.asStateFlow()

    private val dateFormat = SimpleDateFormat("dd MMMM yyyy", Locale("id", "ID"))

    init {
        loadOrders()
    }

    private fun loadOrders() {
        viewModelScope.launch {
            orderRepository.getTodayOrders().collect { orders ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    orders = orders,
                    currentDate = dateFormat.format(Date())
                )
            }
        }

        viewModelScope.launch {
            orderRepository.getTodayTotal().collect { total ->
                _uiState.value = _uiState.value.copy(
                    todayTotal = total ?: 0.0
                )
            }
        }
    }

    fun deleteOrder(orderId: Long) {
        viewModelScope.launch {
            orderRepository.deleteOrder(orderId)
        }
    }
}

class HistoryViewModelFactory(private val orderRepository: OrderRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HistoryViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return HistoryViewModel(orderRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}