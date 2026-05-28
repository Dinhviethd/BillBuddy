package com.example.billbuddy.data.repo

import com.example.billbuddy.data.model.User
import com.example.billbuddy.utils.Resource

interface UserRepository {
    suspend fun findUserByEmail(email: String): Resource<User>
    suspend fun findUserById(userId: String): Resource<User>
}