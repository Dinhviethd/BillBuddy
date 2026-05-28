package com.example.billbuddy.data.repo

import com.example.billbuddy.data.model.Category
import com.example.billbuddy.utils.Resource
import kotlinx.coroutines.flow.Flow

interface CategoryRepository {
    fun getCategories(userId: String): Flow<Resource<List<Category>>>
    fun addCategory(category: Category): Flow<Resource<Unit>>
}
