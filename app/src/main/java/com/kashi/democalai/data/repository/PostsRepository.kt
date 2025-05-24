package com.kashi.democalai.data.repository

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.kashi.democalai.data.model.Post
import com.kashi.democalai.data.model.User
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PostsRepository @Inject constructor(
    private val userRepository: UserRepository
) {
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val postsCollection = firestore.collection("posts")

    companion object {
        private const val TAG = "PostsRepository"
        private const val PAGE_SIZE = 20 // Load 20 posts per page
    }

    suspend fun createPost(user: User, message: String): Result<Unit> {
        return try {
            Log.d(TAG, "📝 CreatePost: Creating post for user ${user.displayName}")
            
            val post = Post(
                userId = user.id,
                userName = user.displayName,
                userEmail = user.email,
                message = message.trim()
            )

            postsCollection.add(post).await()
            
            // Increment user's post count
            userRepository.incrementPostCount(user.id)
                .onFailure { e ->
                    Log.w(TAG, "⚠️ CreatePost: Failed to increment post count", e)
                }
            
            Log.d(TAG, "✅ CreatePost: Post created successfully")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "❌ CreatePost: Error creating post", e)
            Result.failure(e)
        }
    }

    // Get initial posts with pagination
    fun getAllPostsRealtime(): Flow<List<Post>> = callbackFlow {
        Log.d(TAG, "🔄 getAllPostsRealtime: Starting realtime listener with PAGE_SIZE=$PAGE_SIZE")
        
        val listener = postsCollection
            .orderBy("timestamp", Query.Direction.DESCENDING) // Latest first
            .limit(PAGE_SIZE.toLong()) // Only load PAGE_SIZE posts
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "❌ getAllPostsRealtime: Error listening to posts", error)
                    return@addSnapshotListener
                }

                val posts = snapshot?.documents?.mapNotNull { doc ->
                    try {
                        doc.toObject(Post::class.java)
                    } catch (e: Exception) {
                        Log.e(TAG, "❌ getAllPostsRealtime: Error converting document to Post", e)
                        null
                    }
                } ?: emptyList()

                Log.d(TAG, "📦 getAllPostsRealtime: Received ${posts.size} posts (INITIAL PAGE)")
                posts.forEachIndexed { index, post ->
                    Log.d(TAG, "📄 Post $index: ${post.userName} - ${post.message.take(30)}...")
                }
                trySend(posts)
            }

        awaitClose { 
            Log.d(TAG, "🔌 getAllPostsRealtime: Closing listener")
            listener.remove() 
        }
    }

    // Load more posts (pagination)
    suspend fun loadMorePosts(lastVisible: DocumentSnapshot?): Result<Pair<List<Post>, DocumentSnapshot?>> {
        return try {
            Log.d(TAG, "📄 loadMorePosts: Starting to load more posts")
            Log.d(TAG, "📄 loadMorePosts: lastVisible = ${lastVisible?.id ?: "null"}")
            
            val query = if (lastVisible != null) {
                Log.d(TAG, "📄 loadMorePosts: Loading posts AFTER ${lastVisible.id}")
                postsCollection
                    .orderBy("timestamp", Query.Direction.DESCENDING)
                    .startAfter(lastVisible)
                    .limit(PAGE_SIZE.toLong())
            } else {
                Log.d(TAG, "📄 loadMorePosts: Loading FIRST page (no lastVisible)")
                postsCollection
                    .orderBy("timestamp", Query.Direction.DESCENDING)
                    .limit(PAGE_SIZE.toLong())
            }

            val snapshot = query.get().await()
            val posts = snapshot.documents.mapNotNull { doc ->
                try {
                    doc.toObject(Post::class.java)
                } catch (e: Exception) {
                    Log.e(TAG, "❌ loadMorePosts: Error converting document to Post", e)
                    null
                }
            }

            val newLastVisible = snapshot.documents.lastOrNull()
            
            Log.d(TAG, "✅ loadMorePosts: Loaded ${posts.size} more posts")
            Log.d(TAG, "📄 loadMorePosts: New lastVisible = ${newLastVisible?.id ?: "null"}")
            Log.d(TAG, "📄 loadMorePosts: Has more posts = ${posts.size == PAGE_SIZE}")
            
            posts.forEachIndexed { index, post ->
                Log.d(TAG, "📄 LoadMore Post $index: ${post.userName} - ${post.message.take(30)}...")
            }
            
            Result.success(Pair(posts, newLastVisible))
        } catch (e: Exception) {
            Log.e(TAG, "❌ loadMorePosts: Error loading more posts", e)
            Result.failure(e)
        }
    }

    fun getUserPostsRealtime(userId: String): Flow<List<Post>> = callbackFlow {
        Log.d(TAG, "🔄 getUserPostsRealtime: Starting user posts listener for $userId")
        
        val listener = postsCollection
            .whereEqualTo("userId", userId)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(PAGE_SIZE.toLong()) // Paginate user posts too
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "❌ getUserPostsRealtime: Error listening to user posts", error)
                    return@addSnapshotListener
                }

                val posts = snapshot?.documents?.mapNotNull { doc ->
                    try {
                        doc.toObject(Post::class.java)
                    } catch (e: Exception) {
                        Log.e(TAG, "❌ getUserPostsRealtime: Error converting document to Post", e)
                        null
                    }
                } ?: emptyList()

                Log.d(TAG, "📦 getUserPostsRealtime: Received ${posts.size} user posts (INITIAL PAGE)")
                trySend(posts)
            }

        awaitClose { 
            Log.d(TAG, "🔌 getUserPostsRealtime: Closing user posts listener")
            listener.remove() 
        }
    }

    // Load more user posts
    suspend fun loadMoreUserPosts(userId: String, lastVisible: DocumentSnapshot?): Result<Pair<List<Post>, DocumentSnapshot?>> {
        return try {
            Log.d(TAG, "📄 loadMoreUserPosts: Loading more posts for user $userId")
            
            val query = if (lastVisible != null) {
                postsCollection
                    .whereEqualTo("userId", userId)
                    .orderBy("timestamp", Query.Direction.DESCENDING)
                    .startAfter(lastVisible)
                    .limit(PAGE_SIZE.toLong())
            } else {
                postsCollection
                    .whereEqualTo("userId", userId)
                    .orderBy("timestamp", Query.Direction.DESCENDING)
                    .limit(PAGE_SIZE.toLong())
            }

            val snapshot = query.get().await()
            val posts = snapshot.documents.mapNotNull { doc ->
                try {
                    doc.toObject(Post::class.java)
                } catch (e: Exception) {
                    Log.e(TAG, "❌ loadMoreUserPosts: Error converting document to Post", e)
                    null
                }
            }

            val newLastVisible = snapshot.documents.lastOrNull()
            
            Log.d(TAG, "✅ loadMoreUserPosts: Loaded ${posts.size} more user posts")
            Result.success(Pair(posts, newLastVisible))
        } catch (e: Exception) {
            Log.e(TAG, "❌ loadMoreUserPosts: Error loading more user posts", e)
            Result.failure(e)
        }
    }

    suspend fun refreshAllPosts(): Result<Unit> {
        return try {
            Log.d(TAG, "🔄 refreshAllPosts: Refreshing first page from server")
            
            // Force a cache refresh by getting fresh data from server
            val snapshot = postsCollection
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(PAGE_SIZE.toLong()) // Only refresh first page
                .get(com.google.firebase.firestore.Source.SERVER)
                .await()
            
            Log.d(TAG, "✅ refreshAllPosts: Refreshed ${snapshot.documents.size} posts from server")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "❌ refreshAllPosts: Error refreshing posts", e)
            Result.failure(e)
        }
    }

    suspend fun refreshUserPosts(userId: String): Result<Unit> {
        return try {
            Log.d(TAG, "🔄 refreshUserPosts: Refreshing user posts from server")
            
            // Force a cache refresh by getting fresh data from server
            val snapshot = postsCollection
                .whereEqualTo("userId", userId)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(PAGE_SIZE.toLong()) // Only refresh first page
                .get(com.google.firebase.firestore.Source.SERVER)
                .await()
            
            Log.d(TAG, "✅ refreshUserPosts: Refreshed ${snapshot.documents.size} user posts from server")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "❌ refreshUserPosts: Error refreshing user posts", e)
            Result.failure(e)
        }
    }
} 