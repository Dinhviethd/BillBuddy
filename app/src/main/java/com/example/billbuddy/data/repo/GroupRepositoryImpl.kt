package com.example.billbuddy.data.repo

import com.example.billbuddy.data.model.Group
import com.example.billbuddy.utils.Resource
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class GroupRepositoryImpl @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
) : GroupRepository {

    override fun createGroup(group: Group): Flow<Resource<Unit>> = callbackFlow {
        val uid = firebaseAuth.currentUser?.uid
        if (uid == null) {
            trySend(Resource.Error("User not logged in"))
            close()
            return@callbackFlow
        }

        try {
            // Resolve member emails to UIDs
            val memberUids = mutableListOf<String>()
            memberUids.add(uid)
            
            for (email in group.memberIds) {
                if (email.isBlank() || email == firebaseAuth.currentUser?.email) continue
                val userSnapshot = firestore.collection("users")
                    .whereEqualTo("email", email)
                    .get()
                    .await()
                
                if (!userSnapshot.isEmpty) {
                    memberUids.add(userSnapshot.documents[0].id)
                }
            }

            val newGroup = group.copy(
                createdBy = uid,
                memberIds = memberUids.distinct(),
                createdAt = Timestamp.now()
            )

            firestore.collection("groups")
                .add(newGroup)
                .addOnSuccessListener {
                    trySend(Resource.Success(Unit))
                    close()
                }
                .addOnFailureListener { error ->
                    trySend(Resource.Error(error.localizedMessage ?: "Unknown Error"))
                    close()
                }
        } catch (e: Exception) {
            trySend(Resource.Error(e.localizedMessage ?: "Lỗi khi xử lý thành viên"))
            close()
        }

        awaitClose { }
    }.onStart { emit(Resource.Loading()) }

    override fun joinGroupByName(groupName: String): Flow<Resource<Unit>> = callbackFlow {
        val uid = firebaseAuth.currentUser?.uid
        if (uid == null) {
            trySend(Resource.Error("User not logged in"))
            close()
            return@callbackFlow
        }

        try {
            val querySnapshot = firestore.collection("groups")
                .whereEqualTo("name", groupName)
                .limit(1)
                .get()
                .await()

            if (querySnapshot.isEmpty) {
                trySend(Resource.Error("Group not found"))
                close()
            } else {
                val groupDoc = querySnapshot.documents[0]
                val groupId = groupDoc.id
                
                firestore.collection("groups").document(groupId)
                    .update("memberIds", FieldValue.arrayUnion(uid))
                    .addOnSuccessListener {
                        trySend(Resource.Success(Unit))
                        close()
                    }
                    .addOnFailureListener { error ->
                        trySend(Resource.Error(error.localizedMessage ?: "Unknown Error"))
                        close()
                    }
            }
        } catch (e: Exception) {
            trySend(Resource.Error(e.localizedMessage ?: "Unknown Error"))
            close()
        }

        awaitClose { }
    }.onStart { emit(Resource.Loading()) }

    override fun observeUserGroups(): Flow<Resource<List<Group>>> = callbackFlow {
        val uid = firebaseAuth.currentUser?.uid
        if (uid == null) {
            trySend(Resource.Error("User not logged in"))
            close()
            return@callbackFlow
        }

        val query = firestore.collection("groups")
            .whereArrayContains("memberIds", uid)
            .orderBy("createdAt", Query.Direction.DESCENDING)

        val listener = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                trySend(Resource.Error(error.localizedMessage ?: "Unknown Error"))
                return@addSnapshotListener
            }
            if (snapshot != null) {
                val items = snapshot.toObjects(Group::class.java)
                trySend(Resource.Success(items))
            }
        }

        awaitClose { listener.remove() }
    }.onStart { emit(Resource.Loading()) }

    override fun getGroupById(groupId: String): Flow<Resource<Group>> = callbackFlow {
        val listener = firestore.collection("groups").document(groupId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Resource.Error(error.localizedMessage ?: "Unknown Error"))
                    return@addSnapshotListener
                }
                if (snapshot != null && snapshot.exists()) {
                    val group = snapshot.toObject(Group::class.java)
                    if (group != null) {
                        trySend(Resource.Success(group))
                    }
                } else {
                    trySend(Resource.Error("Group not found"))
                }
            }
        awaitClose { listener.remove() }
    }.onStart { emit(Resource.Loading()) }

    override fun leaveGroup(groupId: String): Flow<Resource<Unit>> = callbackFlow {
        val uid = firebaseAuth.currentUser?.uid
        if (uid == null) {
            trySend(Resource.Error("User not logged in"))
            close()
            return@callbackFlow
        }

        firestore.collection("groups").document(groupId)
            .update("memberIds", FieldValue.arrayRemove(uid))
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
