package com.example.billbuddy.data.repo

import com.example.billbuddy.data.model.Expense
import com.example.billbuddy.utils.Resource
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.onStart
import javax.inject.Inject

class ExpenseRepositoryImpl @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firebaseDatabase: FirebaseDatabase
) : ExpenseRepository {

    override fun observeExpenses(): Flow<Resource<List<Expense>>> = callbackFlow {
        val uid = firebaseAuth.currentUser?.uid
        if (uid == null) {
            trySend(Resource.Error("User not logged in"))
            close()
            return@callbackFlow
        }

        val ref = firebaseDatabase.reference
            .child("users")
            .child(uid)
            .child("expenses")

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val items = snapshot.children.mapNotNull { child ->
                    val value = child.getValue(Expense::class.java) ?: return@mapNotNull null
                    val id = child.key ?: value.id
                    value.copy(id = id)
                }
                trySend(Resource.Success(items.sortedByDescending { it.createdAt }))
            }

            override fun onCancelled(error: DatabaseError) {
                trySend(Resource.Error(error.message))
            }
        }

        ref.addValueEventListener(listener)

        awaitClose { ref.removeEventListener(listener) }
    }.onStart { emit(Resource.Loading()) }

    override fun addExpense(expense: Expense): Flow<Resource<Unit>> = callbackFlow {
        val uid = firebaseAuth.currentUser?.uid
        if (uid == null) {
            trySend(Resource.Error("User not logged in"))
            close()
            return@callbackFlow
        }

        val ref = firebaseDatabase.reference
            .child("users")
            .child(uid)
            .child("expenses")

        val key = ref.push().key
        if (key == null) {
            trySend(Resource.Error("Unable to generate expense id"))
            close()
            return@callbackFlow
        }

        val payload = expense.copy(id = key)
        ref.child(key).setValue(payload)
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

