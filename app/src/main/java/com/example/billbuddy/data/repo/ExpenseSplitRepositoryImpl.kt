package com.example.billbuddy.data.repo

import com.example.billbuddy.data.model.Expense
import com.example.billbuddy.data.model.ExpenseSplit
import com.example.billbuddy.utils.Resource
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class ExpenseSplitRepositoryImpl @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
) : ExpenseSplitRepository {

    override fun splitExpense(expense: Expense, splits: List<ExpenseSplit>): Flow<Resource<Unit>> = callbackFlow {
        try {
            val batch = firestore.batch()
            
            // 1. Add the main expense
            val expenseRef = firestore.collection("expenses").document()
            val expenseToSave = expense.copy(
                documentId = expenseRef.id,
                createdAt = Timestamp.now()
            )
            batch.set(expenseRef, expenseToSave)

            // 2. Add each split
            splits.forEach { split ->
                val splitRef = firestore.collection("expense_splits").document()
                val splitToSave = split.copy(
                    documentId = splitRef.id,
                    expenseId = expenseRef.id,
                    createdAt = Timestamp.now()
                )
                batch.set(splitRef, splitToSave)
            }

            batch.commit().await()
            trySend(Resource.Success(Unit))
            close()
        } catch (e: Exception) {
            trySend(Resource.Error(e.localizedMessage ?: "Unknown Error"))
            close()
        }
        awaitClose { }
    }.onStart { emit(Resource.Loading()) }

    override fun observeSplitsByExpense(expenseId: String): Flow<Resource<List<ExpenseSplit>>> = callbackFlow {
        val query = firestore.collection("expense_splits")
            .whereEqualTo("expenseId", expenseId)

        val listener = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                trySend(Resource.Error(error.localizedMessage ?: "Unknown Error"))
                return@addSnapshotListener
            }
            if (snapshot != null) {
                val items = snapshot.toObjects(ExpenseSplit::class.java)
                trySend(Resource.Success(items))
            }
        }

        awaitClose { listener.remove() }
    }.onStart { emit(Resource.Loading()) }

    override fun observeUserSplitsInGroup(uid: String, groupId: String): Flow<Resource<List<ExpenseSplit>>> = callbackFlow {
        val query = firestore.collection("expense_splits")
            .whereEqualTo("paidBy", uid)
            .whereEqualTo("groupId", groupId)

        val listener = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                trySend(Resource.Error(error.localizedMessage ?: "Unknown Error"))
                return@addSnapshotListener
            }
            if (snapshot != null) {
                val items = snapshot.toObjects(ExpenseSplit::class.java)
                trySend(Resource.Success(items))
            }
        }

        awaitClose { listener.remove() }
    }.onStart { emit(Resource.Loading()) }

    override fun observeAllSettledSplitsInGroup(groupId: String): Flow<Resource<List<ExpenseSplit>>> = callbackFlow {
        val query = firestore.collection("expense_splits")
            .whereEqualTo("groupId", groupId)
            .whereEqualTo("status", "SETTLED")

        val listener = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                trySend(Resource.Error(error.localizedMessage ?: "Unknown Error"))
                return@addSnapshotListener
            }
            if (snapshot != null) {
                val items = snapshot.toObjects(ExpenseSplit::class.java)
                trySend(Resource.Success(items))
            }
        }

        awaitClose { listener.remove() }
    }.onStart { emit(Resource.Loading()) }

    override fun settleSplit(splitId: String): Flow<Resource<Unit>> = callbackFlow {
        firestore.collection("expense_splits").document(splitId)
            .update("status", "SETTLED")
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
