package com.example.billbuddy.data.repo

import com.example.billbuddy.utils.Resource
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.onStart
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val firebaseAuth: FirebaseAuth
) : AuthRepository {

    override val currentUser: FirebaseUser?
        get() = firebaseAuth.currentUser

    override fun login(email: String, password: String): Flow<Resource<FirebaseUser>> = callbackFlow {
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
        awaitClose { }
    }.onStart { emit(Resource.Loading()) }

    override fun register(email: String, password: String): Flow<Resource<FirebaseUser>> = callbackFlow {
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
        awaitClose { }
    }.onStart { emit(Resource.Loading()) }

    override fun logout() {
        firebaseAuth.signOut()
    }
}
