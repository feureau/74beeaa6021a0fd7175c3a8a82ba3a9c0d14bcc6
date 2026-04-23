package com.posapp.data.local

import android.content.Context
import com.google.gson.Gson
import com.posapp.domain.model.Category
import com.posapp.domain.model.Menu
import com.posapp.domain.model.MenuItem
import java.io.InputStreamReader

class MenuDataSource(private val context: Context) {

    fun loadMenu(): Menu {
        val inputStream = context.resources.openRawResource(context.resources.getIdentifier("menu", "raw", context.packageName))
        val reader = InputStreamReader(inputStream)
        val gson = Gson()
        val menuData = gson.fromJson(reader, MenuData::class.java)
        reader.close()
        return menuData.toMenu()
    }
}

data class MenuData(
    val restaurant: String,
    val categories: List<CategoryData>
)

data class CategoryData(
    val name: String,
    val items: List<ItemData>
)

data class ItemData(
    val id: String,
    val name: String,
    val price: Double
)

fun MenuData.toMenu(): Menu = Menu(
    restaurant = this.restaurant,
    categories = this.categories.map { cat ->
        Category(
            name = cat.name,
            items = cat.items.map { item ->
                MenuItem(
                    id = item.id,
                    name = item.name,
                    price = item.price
                )
            }
        )
    }
)