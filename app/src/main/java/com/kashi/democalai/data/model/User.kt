package com.kashi.democalai.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp

data class User(
    @DocumentId
    val id: String = "",
    val email: String = "",
    val displayName: String = "",
    val profileImageUrl: String? = null,
    val bio: String? = null,
    @ServerTimestamp
    val createdAt: Timestamp? = null,
    @ServerTimestamp
    val lastLoginAt: Timestamp? = null,
    val isActive: Boolean = true,
    val postCount: Int = 0,
    val followersCount: Int = 0,
    val followingCount: Int = 0
) 