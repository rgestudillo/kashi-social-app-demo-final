package com.kashi.democalai.data.repository

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.kashi.democalai.data.model.Post
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PostsRepository @Inject constructor() {
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val postsCollection = firestore.collection("posts")

    companion object {
        private const val TAG = "PostsRepository"
    }

    suspend fun createPost(message: String): Result<Unit> {
        return try {
            val currentUser = auth.currentUser
            if (currentUser == null) {
                return Result.failure(Exception("User not authenticated"))
            }

            val post = Post(
                userId = currentUser.uid,
                userName = currentUser.displayName ?: "Unknown User",
                userEmail = currentUser.email ?: "",
                message = message.trim()
            )

            postsCollection.add(post).await()
            Log.d(TAG, "Post created successfully")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error creating post", e)
            Result.failure(e)
        }
    }

    fun getAllPostsRealtime(): Flow<List<Post>> = callbackFlow {
        val listener = postsCollection
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Error listening to posts", error)
                    return@addSnapshotListener
                }

                val posts = snapshot?.documents?.mapNotNull { doc ->
                    try {
                        doc.toObject(Post::class.java)
                    } catch (e: Exception) {
                        Log.e(TAG, "Error converting document to Post", e)
                        null
                    }
                } ?: emptyList()

                Log.d(TAG, "Received ${posts.size} posts from Firestore")
                trySend(posts)
            }

        awaitClose { listener.remove() }
    }

    fun getUserPostsRealtime(userId: String): Flow<List<Post>> = callbackFlow {
        val listener = postsCollection
            .whereEqualTo("userId", userId)
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Error listening to user posts", error)
                    return@addSnapshotListener
                }

                val posts = snapshot?.documents?.mapNotNull { doc ->
                    try {
                        doc.toObject(Post::class.java)
                    } catch (e: Exception) {
                        Log.e(TAG, "Error converting document to Post", e)
                        null
                    }
                } ?: emptyList()

                Log.d(TAG, "Received ${posts.size} user posts from Firestore")
                trySend(posts)
            }

        awaitClose { listener.remove() }
    }
} 