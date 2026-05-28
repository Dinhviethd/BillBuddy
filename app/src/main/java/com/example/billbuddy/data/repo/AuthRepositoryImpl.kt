package com.example.billbuddy.data.repo

import com.example.billbuddy.utils.Resource
import com.example.billbuddy.data.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.onStart
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : AuthRepository {

    override val currentUser: FirebaseUser?
        get() = firebaseAuth.currentUser

    override fun login(email: String, password: String): Flow<Resource<FirebaseUser>> = callbackFlow {
        try {
            firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener {
                    it.user?.let { user ->
                        trySend(Resource.Success(user))
                    }
                    close()
                }
                .addOnFailureListener {
                    trySend(Resource.Error(it.localizedMessage ?: "Unknown Error"))
                    close()
                }
        } catch (e: Exception) {
            trySend(Resource.Error(e.localizedMessage ?: "Invalid input"))
            close()
        }
        awaitClose { }
    }.onStart { emit(Resource.Loading()) }

    override fun register(email: String, password: String): Flow<Resource<FirebaseUser>> = callbackFlow {
        try {
            firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener {
                    it.user?.let{
                        user -> trySend(Resource.Success(user))
                    }
                    close()
                }
                .addOnFailureListener {
                    trySend(Resource.Error(it.localizedMessage ?: "Unknown Error"))
                    close()
                }
        } catch (e: Exception) {
            trySend(Resource.Error(e.localizedMessage ?: "Invalid input"))
            close()
        }
        awaitClose { }
    }.onStart { emit(Resource.Loading()) }

    override fun logout() {
        firebaseAuth.signOut()
    }

    override fun changePassword(newPassword: String): Flow<Resource<Unit>> = callbackFlow {
        val user = firebaseAuth.currentUser
        if (user != null) {
            user.updatePassword(newPassword)
                .addOnSuccessListener {
                    trySend(Resource.Success(Unit))
                    close()
                }
                .addOnFailureListener {
                    trySend(Resource.Error(it.localizedMessage ?: "Failed to change password"))
                    close()
                }
        } else {
            trySend(Resource.Error("User not logged in"))
            close()
        }
        awaitClose { }
    }.onStart { emit(Resource.Loading()) }

    override fun updateUserProfile(user: User): Flow<Resource<Unit>> = callbackFlow {
        firestore.collection("users").document(user.documentId)
            .set(user)
            .addOnSuccessListener {
                trySend(Resource.Success(Unit))
                close()
            }
            .addOnFailureListener {
                trySend(Resource.Error(it.localizedMessage ?: "Update failed"))
                close()
            }
        awaitClose { }
    }.onStart { emit(Resource.Loading()) }

    override fun getUserData(uid: String): Flow<Resource<User>> = callbackFlow {
        firestore.collection("users").document(uid)
            .get()
            .addOnSuccessListener { doc ->
                val user = doc.toObject(User::class.java)
                if (user != null) {
                    trySend(Resource.Success(user))
                } else {
                    trySend(Resource.Error("User not found"))
                }
                close()
            }
            .addOnFailureListener {
                trySend(Resource.Error(it.localizedMessage ?: "Fetch failed"))
                close()
            }
        awaitClose { }
    }.onStart { emit(Resource.Loading()) }
}
