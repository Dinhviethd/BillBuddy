package com.example.billbuddy.data.repo

import com.example.billbuddy.data.model.Category
import com.example.billbuddy.utils.Resource
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.onStart
import javax.inject.Inject

class CategoryRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : CategoryRepository {

    override fun getCategories(userId: String): Flow<Resource<List<Category>>> = callbackFlow {
        val listener = firestore.collection("categories")
            .whereEqualTo("userId", userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Resource.Error(error.localizedMessage ?: "Unknown Error"))
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val categories = snapshot.toObjects(Category::class.java)
                    trySend(Resource.Success(categories))
                }
            }
        awaitClose { listener.remove() }
    }.onStart { emit(Resource.Loading()) }

    override fun addCategory(category: Category): Flow<Resource<Unit>> = callbackFlow {
        firestore.collection("categories")
            .add(category)
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
