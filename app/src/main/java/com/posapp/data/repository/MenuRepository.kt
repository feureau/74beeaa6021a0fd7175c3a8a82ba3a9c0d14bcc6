package com.posapp.data.repository

import com.posapp.data.local.MenuDataSource
import com.posapp.domain.model.Menu
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MenuRepository(private val menuDataSource: MenuDataSource) {

    suspend fun getMenu(): Menu = withContext(Dispatchers.IO) {
        menuDataSource.loadMenu()
    }
}