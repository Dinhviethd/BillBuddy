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
    private val firestore: FirebaseFirestore
) : ExpenseRepository {

    override fun observeExpenses(): Flow<Resource<List<Expense>>> = callbackFlow {
        val uid = firebaseAuth.currentUser?.uid
        if (uid == null) {
            trySend(Resource.Error("User not logged in"))
            close()
            return@callbackFlow
        }

        val query = firestore.collection("expenses")
            .whereEqualTo("userId", uid)
            .orderBy("createdAt", Query.Direction.DESCENDING)

        val listener = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                trySend(Resource.Error(error.localizedMessage ?: "Unknown Error"))
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
        val uid = firebaseAuth.currentUser?.uid
        if (uid == null) {
            trySend(Resource.Error("User not logged in"))
            close()
            return@callbackFlow
        }

        val payload = expense.copy(userId = uid)
        
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

    override fun observeGroupExpenses(groupId: String): Flow<Resource<List<Expense>>> = callbackFlow {
        val query = firestore.collection("expenses")
            .whereEqualTo("groupId", groupId)
            .orderBy("createdAt", Query.Direction.DESCENDING)

        val listener = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                trySend(Resource.Error(error.localizedMessage ?: "Unknown Error"))
                return@addSnapshotListener
            }
            if (snapshot != null) {
                val items = snapshot.toObjects(Expense::class.java)
                trySend(Resource.Success(items))
            }
        }

        awaitClose { listener.remove() }
    }.onStart { emit(Resource.Loading()) }
}
