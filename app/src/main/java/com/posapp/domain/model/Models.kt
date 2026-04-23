package com.posapp.domain.model

data class MenuItem(
    val id: String,
    val name: String,
    val price: Double
)

data class Category(
    val name: String,
    val items: List<MenuItem>
)

data class Menu(
    val restaurant: String,
    val categories: List<Category>
)

data class CartItem(
    val menuItem: MenuItem,
    var quantity: Int = 1
) {
    val totalPrice: Double get() = menuItem.price * quantity
}

data class Order(
    val id: Long = 0,
    val items: List<CartItem>,
    val total: Double,
    val timestamp: Long = System.currentTimeMillis()
)