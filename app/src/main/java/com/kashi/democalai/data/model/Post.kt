package com.kashi.democalai.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp

data class Post(
    @DocumentId
    val id: String = "",
    val userId: String = "",
    val userName: String = "",
    val userEmail: String = "",
    val message: String = "",
    @ServerTimestamp
    val timestamp: Timestamp? = null
) {
    // No-argument constructor for Firestore
    constructor() : this("", "", "", "", "", null)
} 