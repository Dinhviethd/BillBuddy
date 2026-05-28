package com.example.billbuddy.data.repo

import com.example.billbuddy.data.model.Budget
import com.example.billbuddy.utils.Resource
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class BudgetRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : BudgetRepository {

    override fun getBudgets(userId: String): Flow<Resource<List<Budget>>> = callbackFlow {
        val subscription = firestore.collection("budgets")
            .whereEqualTo("userId", userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Resource.Error(error.localizedMessage ?: "Unknown error"))
                    return@addSnapshotListener
                }
                val budgets = snapshot?.toObjects(Budget::class.java) ?: emptyList()
                trySend(Resource.Success(budgets))
            }
        awaitClose { subscription.remove() }
    }

    override fun addBudget(budget: Budget): Flow<Resource<Unit>> = flow {
        emit(Resource.Loading())
        try {
            firestore.collection("budgets").add(budget).await()
            emit(Resource.Success(Unit))
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "Failed to add budget"))
        }
    }

    override fun updateBudget(budget: Budget): Flow<Resource<Unit>> = flow {
        emit(Resource.Loading())
        try {
            firestore.collection("budgets").document(budget.documentId).set(budget).await()
            emit(Resource.Success(Unit))
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "Failed to update budget"))
        }
    }

    override fun deleteBudget(budgetId: String): Flow<Resource<Unit>> = flow {
        emit(Resource.Loading())
        try {
            firestore.collection("budgets").document(budgetId).delete().await()
            emit(Resource.Success(Unit))
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "Failed to delete budget"))
        }
    }
}
