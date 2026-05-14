package com.example.billbuddy.data.repo

import com.example.billbuddy.data.model.User
import com.example.billbuddy.utils.Resource
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : UserRepository {
    override suspend fun findUserByEmail(email: String): Resource<User> {
        return try {
            val snapshot = firestore.collection("users")
                .whereEqualTo("email", email)
                .limit(1)
                .get()
                .await()

            if (snapshot.isEmpty) {
                Resource.Error("Không tìm thấy người dùng với email này")
            } else {
                val user = snapshot.documents[0].toObject(User::class.java)
                    ?: return Resource.Error("Lỗi khi đọc dữ liệu người dùng")
                Resource.Success(user)
            }
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Lỗi không xác định")
        }
    }
    override suspend fun findUserById(userId: String): Resource<User> {
        return try {
            val snapshot = firestore.collection("users")
                .document(userId)
                .get()
                .await()

            val user = snapshot.toObject(User::class.java)
                ?: return Resource.Error("Không tìm thấy người dùng")
            Resource.Success(user)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Lỗi không xác định")
        }
    }
}