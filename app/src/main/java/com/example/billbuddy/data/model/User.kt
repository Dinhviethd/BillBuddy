package com.example.billbuddy.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId

data class User(
    @DocumentId
    val documentId: String = "",
    val email: String = "",
    val displayName: String = "",
    val photoURL: String = "",
    val createdAt: Timestamp? = null,
    val updatedAt: Timestamp? = null
)
