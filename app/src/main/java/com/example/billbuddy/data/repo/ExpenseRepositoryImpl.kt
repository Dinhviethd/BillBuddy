package com.example.billbuddy.data.repo

import com.example.billbuddy.data.model.Expense
import com.example.billbuddy.utils.Resource
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.onStart
import javax.inject.Inject

class ExpenseRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : ExpenseRepository {

    companion object {
        private const val COLLECTION_EXPENSES = "EXPENSES"
    }

    override fun observeExpenses(userId: String): Flow<Resource<List<Expense>>> = callbackFlow {
        if (userId.isBlank()) {
            trySend(Resource.Success(emptyList()))
            close()
            return@callbackFlow
        }

        trySend(Resource.Loading())

        val registration = firestore.collection(COLLECTION_EXPENSES)
            .whereEqualTo("userId", userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Resource.Error(error.localizedMessage ?: "Không thể tải dữ liệu chi tiêu"))
                    return@addSnapshotListener
                }

                val expenses = snapshot?.documents.orEmpty().mapNotNull { document ->
                    val amount = when (val rawAmount = document.get("amount")) {
                        is Number -> rawAmount.toDouble()
                        else -> null
                    } ?: 0.0

                    Expense(
                        documentId = document.id,
                        amount = amount,
                        description = document.getString("description") ?: "",
                        date = document.getTimestamp("date"),
                        categoryId = document.getString("categoryId") ?: "",
                        userId = document.getString("userId") ?: userId,
                        groupId = document.getString("groupId"),
                        splitMethod = document.getString("splitMethod"),
                        createdAt = document.getTimestamp("createdAt")
                    )
                }

                trySend(Resource.Success(expenses))
            }

        awaitClose {
            registration.remove()
        }
    }.onStart { emit(Resource.Loading()) }
}

