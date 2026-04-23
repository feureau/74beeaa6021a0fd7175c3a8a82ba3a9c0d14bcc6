package com.posapp.data.repository

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.posapp.data.local.AppDatabase
import com.posapp.data.local.OrderDao
import com.posapp.data.local.OrderEntity
import com.posapp.domain.model.CartItem
import com.posapp.domain.model.MenuItem
import com.posapp.domain.model.Order
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.Calendar

class OrderRepository(private val orderDao: OrderDao) {

    private val gson = Gson()

    suspend fun saveOrder(items: List<CartItem>, total: Double): Long {
        val itemsJson = gson.toJson(items.map {
            OrderItemJson(it.menuItem.id, it.menuItem.name, it.menuItem.price, it.quantity)
        })
        val entity = OrderEntity(
            itemsJson = itemsJson,
            total = total,
            timestamp = System.currentTimeMillis()
        )
        return orderDao.insertOrder(entity)
    }

    fun getAllOrders(): Flow<List<Order>> {
        return orderDao.getAllOrders().map { entities ->
            entities.map { it.toOrder() }
        }
    }

    fun getTodayOrders(): Flow<List<Order>> {
        val (start, end) = getTodayRange()
        return orderDao.getOrdersForDay(start, end).map { entities ->
            entities.map { it.toOrder() }
        }
    }

    fun getTodayTotal(): Flow<Double?> {
        val (start, end) = getTodayRange()
        return orderDao.getTotalForDay(start, end)
    }

    suspend fun deleteOrder(orderId: Long) {
        orderDao.deleteOrder(orderId)
    }

    private fun getTodayRange(): Pair<Long, Long> {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val start = calendar.timeInMillis

        calendar.add(Calendar.DAY_OF_MONTH, 1)
        val end = calendar.timeInMillis

        return start to end
    }

    private fun OrderEntity.toOrder(): Order {
        val type = object : TypeToken<List<OrderItemJson>>() {}.type
        val itemsJson: List<OrderItemJson> = gson.fromJson(this.itemsJson, type)
        val cartItems = itemsJson.map { json ->
            CartItem(
                menuItem = MenuItem(json.id, json.name, json.price),
                quantity = json.quantity
            )
        }
        return Order(
            id = this.id,
            items = cartItems,
            total = this.total,
            timestamp = this.timestamp
        )
    }
}

data class OrderItemJson(
    val id: String,
    val name: String,
    val price: Double,
    val quantity: Int
)