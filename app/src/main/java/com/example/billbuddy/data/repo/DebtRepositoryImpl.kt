package com.example.billbuddy.data.repo

import com.example.billbuddy.data.model.Debt
import com.example.billbuddy.data.model.DebtStatus
import com.example.billbuddy.utils.Resource
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.onStart
import javax.inject.Inject

class DebtRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : DebtRepository {
    private val TAG = "DebtRepositoryImpl"

    override fun getDebtsByCreditor(userId: String): Flow<Resource<List<Debt>>> = callbackFlow {
        val sub = firestore.collection("debts")
            .whereEqualTo("creditorId", userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Resource.Error(error.localizedMessage ?: "Unknown Error"))
                    return@addSnapshotListener
                }
                val debts = snapshot?.toObjects(Debt::class.java) ?: emptyList()
                trySend(Resource.Success(debts))
            }
        awaitClose { sub.remove() }
    }.onStart { emit(Resource.Loading()) }

    override fun getDebtsByDebtor(userId: String): Flow<Resource<List<Debt>>> = callbackFlow {
        val sub = firestore.collection("debts")
            .whereEqualTo("debtorId", userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Resource.Error(error.localizedMessage ?: "Unknown Error"))
                    return@addSnapshotListener
                }
                val debts = snapshot?.toObjects(Debt::class.java) ?: emptyList()
                trySend(Resource.Success(debts))
            }
        awaitClose { sub.remove() }
    }.onStart { emit(Resource.Loading()) }

    override fun getDebtById(debtId: String): Flow<Resource<Debt>> = callbackFlow {
        val sub = firestore.collection("debts").document(debtId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Resource.Error(error.localizedMessage ?: "Unknown Error"))
                    return@addSnapshotListener
                }
                val debt = snapshot?.toObject(Debt::class.java)
                if (debt != null) {
                    trySend(Resource.Success(debt))
                } else {
                    trySend(Resource.Error("Debt not found"))
                }
            }
        awaitClose { sub.remove() }
    }.onStart { emit(Resource.Loading()) }

    override fun addDebt(debt: Debt): Flow<Resource<Unit>> = callbackFlow {
        firestore.collection("debts")
            .add(debt)
            .addOnSuccessListener {
                trySend(Resource.Success(Unit))
                close()
            }
            .addOnFailureListener {
                trySend(Resource.Error(it.localizedMessage ?: "Failed to add debt"))
                close()
            }
        awaitClose { }
    }.onStart { emit(Resource.Loading()) }

    override fun deleteDebt(debtId: String): Flow<Resource<Unit>> = callbackFlow {
        firestore.collection("debts").document(debtId)
            .delete()
            .addOnSuccessListener {
                trySend(Resource.Success(Unit))
                close()
            }
            .addOnFailureListener {
                trySend(Resource.Error(it.localizedMessage ?: "Failed to delete debt"))
                close()
            }
        awaitClose { }
    }.onStart { emit(Resource.Loading()) }

    override fun updateDebtStatus(debtId: String, status: DebtStatus): Flow<Resource<Unit>> = callbackFlow {
        firestore.collection("debts").document(debtId)
            .update("status", status)
            .addOnSuccessListener {
                trySend(Resource.Success(Unit))
                close()
            }
            .addOnFailureListener {
                trySend(Resource.Error(it.localizedMessage ?: "Failed to update status"))
                close()
            }
        awaitClose { }
    }.onStart { emit(Resource.Loading()) }
}
