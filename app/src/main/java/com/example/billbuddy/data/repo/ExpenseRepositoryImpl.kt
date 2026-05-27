package com.example.billbuddy.data.repo

import com.example.billbuddy.data.model.Expense
import com.example.billbuddy.utils.Resource
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.onStart
import javax.inject.Inject

class ExpenseRepositoryImpl @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
) : ExpenseRepository {

    private val userId: String
        get() = firebaseAuth.currentUser?.uid ?: "xTPgr1YLscOiXamgRCqikOKaAdn1"

    override fun observeExpenses(): Flow<Resource<List<Expense>>> = callbackFlow {
        val listener = firestore.collection("expenses")
            .whereEqualTo("userId", userId)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Resource.Error(error.message ?: "Unknown Error"))
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val items = snapshot.toObjects(Expense::class.java)
                    trySend(Resource.Success(items))
                }
            }
        awaitClose { listener.remove() }
    }.onStart { emit(Resource.Loading()) }

    override fun addExpense(expense: Expense): Flow<Resource<Unit>> = callbackFlow {
        val payload = if (expense.userId.isEmpty()) {
            expense.copy(userId = userId)
        } else {
            expense
        }

        firestore.collection("expenses")
            .add(payload)
            .addOnSuccessListener {
                trySend(Resource.Success(Unit))
                close()
            }
            .addOnFailureListener { error ->
                trySend(Resource.Error(error.localizedMessage ?: "Unknown Error"))
                close()
            }
        awaitClose { }
    }.onStart { emit(Resource.Loading()) }

    override fun updateExpense(expense: Expense): Flow<Resource<Unit>> = callbackFlow {
        if (expense.documentId.isEmpty()) {
            trySend(Resource.Error("Invalid expense id"))
            close()
            return@callbackFlow
        }

        firestore.collection("expenses")
            .document(expense.documentId)
            .set(expense)
            .addOnSuccessListener {
                trySend(Resource.Success(Unit))
                close()
            }
            .addOnFailureListener { error ->
                trySend(Resource.Error(error.localizedMessage ?: "Unknown Error"))
                close()
            }

        awaitClose { }
    }.onStart { emit(Resource.Loading()) }

    override fun deleteExpense(expenseId: String): Flow<Resource<Unit>> = callbackFlow {
        if (expenseId.isEmpty()) {
            trySend(Resource.Error("Invalid expense id"))
            close()
            return@callbackFlow
        }

        firestore.collection("expenses")
            .document(expenseId)
            .delete()
            .addOnSuccessListener {
                trySend(Resource.Success(Unit))
                close()
            }
            .addOnFailureListener { error ->
                trySend(Resource.Error(error.localizedMessage ?: "Unknown Error"))
                close()
            }

        awaitClose { }
    }.onStart { emit(Resource.Loading()) }
}
