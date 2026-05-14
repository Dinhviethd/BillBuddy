package com.example.billbuddy.data.repo

import com.example.billbuddy.utils.Resource
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    val currentUser: FirebaseUser?
    fun login(email: String, password: String): Flow<Resource<FirebaseUser>>
    fun register(email:String, password: String): Flow<Resource<FirebaseUser>>
    fun logout()
    fun changePassword(newPassword: String): Flow<Resource<Unit>>
    fun updateUserProfile(user: com.example.billbuddy.data.model.User): Flow<Resource<Unit>>
    fun getUserData(uid: String): Flow<Resource<com.example.billbuddy.data.model.User>>
}
