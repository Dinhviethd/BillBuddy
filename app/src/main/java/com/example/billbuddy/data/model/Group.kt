package com.example.billbuddy.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId

data class Group(
    @DocumentId
    val documentId: String = "",
    val name: String = "",
    val description: String = "",
    val memberIds: List<String> = emptyList(),
    val createdBy: String = "",
    val createdAt: Timestamp? = null
)
