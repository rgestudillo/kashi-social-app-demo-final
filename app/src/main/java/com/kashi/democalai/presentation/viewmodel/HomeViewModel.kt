package com.kashi.democalai.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.kashi.democalai.data.model.Post
import com.kashi.democalai.data.repository.PostsRepository
import com.kashi.democalai.utils.AnalyticsHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val posts: List<Post> = emptyList(),
    val isLoading: Boolean = false,
    val isCreatingPost: Boolean = false,
    val newPostText: String = "",
    val error: String? = null,
    val showOnlyMyPosts: Boolean = false,
    // Pagination fields
    val isLoadingMore: Boolean = false,
    val hasMorePosts: Boolean = true,
    val lastVisiblePost: DocumentSnapshot? = null
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val postsRepository: PostsRepository,
    private val analyticsHelper: AnalyticsHelper
) : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    companion object {
        private const val TAG = "HomeViewModel"
    }

    init {
        Log.d(TAG, "🚀 HomeViewModel: Initializing")
        loadPosts()
    }

    private fun loadPosts() {
        viewModelScope.launch {
            Log.d(TAG, "📋 loadPosts: Starting to load posts")
            
            // Only show initial loading if we don't have posts yet
            if (_uiState.value.posts.isEmpty()) {
                Log.d(TAG, "📋 loadPosts: Setting isLoading = true (no posts yet)")
                _uiState.value = _uiState.value.copy(isLoading = true)
            }
            
            try {
                if (_uiState.value.showOnlyMyPosts) {
                    Log.d(TAG, "📋 loadPosts: Loading user posts only")
                    val currentUserId = auth.currentUser?.uid
                    if (currentUserId != null) {
                        postsRepository.getUserPostsRealtime(currentUserId).collect { posts ->
                            Log.d(TAG, "📋 loadPosts: Received ${posts.size} user posts from flow")
                            
                            // Log feed viewed analytics
                            analyticsHelper.logFeedViewed("my_posts", posts.size)
                            
                            _uiState.value = _uiState.value.copy(
                                posts = posts,
                                isLoading = false,
                                error = null,
                                lastVisiblePost = posts.lastOrNull()?.let { 
                                    Log.d(TAG, "📋 loadPosts: Setting lastVisiblePost for user posts")
                                    // We'll need the DocumentSnapshot, but for now just track the post
                                    null 
                                }
                            )
                        }
                    } else {
                        Log.e(TAG, "❌ loadPosts: User not authenticated")
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = "User not authenticated"
                        )
                    }
                } else {
                    Log.d(TAG, "📋 loadPosts: Loading all posts")
                    postsRepository.getAllPostsRealtime().collect { posts ->
                        Log.d(TAG, "📋 loadPosts: Received ${posts.size} posts from flow")
                        
                        // Log feed viewed analytics
                        analyticsHelper.logFeedViewed("all_posts", posts.size)
                        
                        _uiState.value = _uiState.value.copy(
                            posts = posts,
                            isLoading = false,
                            error = null
                        )
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "❌ loadPosts: Error loading posts", e)
                
                // Log error analytics
                analyticsHelper.logError(
                    errorType = "posts_load_failed",
                    errorMessage = e.message ?: "Failed to load posts",
                    screenName = "home"
                )
                
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to load posts"
                )
            }
        }
    }

    fun loadMorePosts() {
        val currentState = _uiState.value
        
        // Prevent loading if already loading or no more posts
        if (currentState.isLoadingMore || !currentState.hasMorePosts) {
            Log.d(TAG, "🚫 loadMorePosts: Skipping - isLoadingMore=${currentState.isLoadingMore}, hasMorePosts=${currentState.hasMorePosts}")
            return
        }

        Log.d(TAG, "📄 loadMorePosts: Starting to load more posts")
        Log.d(TAG, "📄 loadMorePosts: Current posts count = ${currentState.posts.size}")
        
        // Log load more analytics
        val feedType = if (currentState.showOnlyMyPosts) "my_posts" else "all_posts"
        analyticsHelper.logLoadMorePosts(feedType, currentState.posts.size)
        
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoadingMore = true, error = null)
            Log.d(TAG, "📄 loadMorePosts: Set isLoadingMore = true")

            try {
                val result = if (currentState.showOnlyMyPosts) {
                    Log.d(TAG, "📄 loadMorePosts: Loading more user posts")
                    val currentUserId = auth.currentUser?.uid
                    if (currentUserId != null) {
                        postsRepository.loadMoreUserPosts(currentUserId, currentState.lastVisiblePost)
                    } else {
                        Log.e(TAG, "❌ loadMorePosts: User not authenticated")
                        Result.failure(Exception("User not authenticated"))
                    }
                } else {
                    Log.d(TAG, "📄 loadMorePosts: Loading more all posts")
                    postsRepository.loadMorePosts(currentState.lastVisiblePost)
                }

                result
                    .onSuccess { (newPosts, newLastVisible) ->
                        Log.d(TAG, "✅ loadMorePosts: Success! Loaded ${newPosts.size} new posts")
                        Log.d(TAG, "📄 loadMorePosts: newLastVisible = ${newLastVisible?.id ?: "null"}")
                        
                        val updatedPosts = currentState.posts + newPosts
                        val hasMore = newPosts.size >= 20 // Assuming PAGE_SIZE is 20
                        
                        Log.d(TAG, "📄 loadMorePosts: Total posts now = ${updatedPosts.size}")
                        Log.d(TAG, "📄 loadMorePosts: hasMore = $hasMore")
                        
                        _uiState.value = _uiState.value.copy(
                            posts = updatedPosts,
                            isLoadingMore = false,
                            hasMorePosts = hasMore,
                            lastVisiblePost = newLastVisible,
                            error = null
                        )
                    }
                    .onFailure { exception ->
                        Log.e(TAG, "❌ loadMorePosts: Failed to load more posts", exception)
                        
                        // Log load more error
                        analyticsHelper.logError(
                            errorType = "load_more_posts_failed",
                            errorMessage = exception.message ?: "Failed to load more posts",
                            screenName = "home"
                        )
                        
                        _uiState.value = _uiState.value.copy(
                            isLoadingMore = false,
                            error = exception.message ?: "Failed to load more posts"
                        )
                    }
            } catch (e: Exception) {
                Log.e(TAG, "❌ loadMorePosts: Exception during load more", e)
                
                // Log exception analytics
                analyticsHelper.logError(
                    errorType = "load_more_posts_exception",
                    errorMessage = e.message ?: "Failed to load more posts",
                    screenName = "home"
                )
                
                _uiState.value = _uiState.value.copy(
                    isLoadingMore = false,
                    error = e.message ?: "Failed to load more posts"
                )
            }
        }
    }

    fun updateNewPostText(text: String) {
        Log.d(TAG, "✏️ updateNewPostText: ${text.length} characters")
        _uiState.value = _uiState.value.copy(newPostText = text)
    }

    fun createPost() {
        viewModelScope.launch {
            val currentText = _uiState.value.newPostText.trim()
            if (currentText.isEmpty()) {
                Log.w(TAG, "⚠️ createPost: Post text is empty")
                
                // Log post creation cancelled due to empty text
                analyticsHelper.logPostCreationCancelled()
                
                _uiState.value = _uiState.value.copy(error = "Post cannot be empty")
                return@launch
            }

            Log.d(TAG, "📝 createPost: Creating post with ${currentText.length} characters")
            
            // Log post creation started
            analyticsHelper.logPostCreationStarted()
            
            _uiState.value = _uiState.value.copy(isCreatingPost = true, error = null)

            postsRepository.createPost(currentText)
                .onSuccess {
                    Log.d(TAG, "✅ createPost: Post created successfully")
                    
                    // Log successful post creation
                    analyticsHelper.logPostCreated(currentText.length)
                    analyticsHelper.logUserEngagement("post_created")
                    
                    _uiState.value = _uiState.value.copy(
                        isCreatingPost = false,
                        newPostText = "",
                        error = null
                    )
                }
                .onFailure { exception ->
                    Log.e(TAG, "❌ createPost: Failed to create post", exception)
                    
                    // Log post creation error
                    analyticsHelper.logError(
                        errorType = "post_creation_failed",
                        errorMessage = exception.message ?: "Failed to create post",
                        screenName = "home"
                    )
                    
                    _uiState.value = _uiState.value.copy(
                        isCreatingPost = false,
                        error = exception.message ?: "Failed to create post"
                    )
                }
        }
    }

    fun toggleFilter() {
        val newShowOnlyMyPosts = !_uiState.value.showOnlyMyPosts
        Log.d(TAG, "🔄 toggleFilter: Switching to showOnlyMyPosts = $newShowOnlyMyPosts")
        
        // Log filter toggle analytics
        val newFilterState = if (newShowOnlyMyPosts) "my_posts" else "all_posts"
        analyticsHelper.logFilterToggled(newFilterState)
        analyticsHelper.logUserEngagement("filter_toggled")
        
        _uiState.value = _uiState.value.copy(
            showOnlyMyPosts = newShowOnlyMyPosts,
            // Reset pagination state when switching filters
            posts = emptyList(),
            hasMorePosts = true,
            lastVisiblePost = null
        )
        loadPosts()
    }

    fun refreshPosts() {
        viewModelScope.launch {
            Log.d(TAG, "🔄 refreshPosts: Starting refresh")
            
            // Log refresh analytics
            val feedType = if (_uiState.value.showOnlyMyPosts) "my_posts" else "all_posts"
            analyticsHelper.logFeedRefreshed(feedType)
            analyticsHelper.logUserEngagement("feed_refreshed")
            
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            try {
                val refreshResult = if (_uiState.value.showOnlyMyPosts) {
                    Log.d(TAG, "🔄 refreshPosts: Refreshing user posts")
                    val currentUserId = auth.currentUser?.uid
                    if (currentUserId != null) {
                        postsRepository.refreshUserPosts(currentUserId)
                    } else {
                        Result.failure(Exception("User not authenticated"))
                    }
                } else {
                    Log.d(TAG, "🔄 refreshPosts: Refreshing all posts")
                    postsRepository.refreshAllPosts()
                }
                
                refreshResult
                    .onSuccess {
                        Log.d(TAG, "✅ refreshPosts: Refresh successful")
                        // Add a minimum delay for better UX (shows the refresh animation)
                        kotlinx.coroutines.delay(300)
                        
                        // Reset pagination state on refresh
                        _uiState.value = _uiState.value.copy(
                            isLoading = false, 
                            error = null,
                            hasMorePosts = true,
                            lastVisiblePost = null
                        )
                    }
                    .onFailure { exception ->
                        Log.e(TAG, "❌ refreshPosts: Refresh failed", exception)
                        
                        // Log refresh error
                        analyticsHelper.logError(
                            errorType = "refresh_posts_failed",
                            errorMessage = exception.message ?: "Failed to refresh posts",
                            screenName = "home"
                        )
                        
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = exception.message ?: "Failed to refresh posts"
                        )
                    }
                
            } catch (e: Exception) {
                Log.e(TAG, "❌ refreshPosts: Exception during refresh", e)
                
                // Log refresh exception
                analyticsHelper.logError(
                    errorType = "refresh_posts_exception",
                    errorMessage = e.message ?: "Failed to refresh posts",
                    screenName = "home"
                )
                
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to refresh posts"
                )
            }
        }
    }

    fun clearError() {
        Log.d(TAG, "🧹 clearError: Clearing error state")
        _uiState.value = _uiState.value.copy(error = null)
    }
    
    fun trackPostView(post: Post) {
        val currentUserId = auth.currentUser?.uid
        val isOwnPost = currentUserId == post.userId
        
        analyticsHelper.logPostViewed(
            postId = post.id,
            authorId = post.userId,
            isOwnPost = isOwnPost
        )
        
        Log.d(TAG, "📊 trackPostView: Tracked view for post ${post.id}, isOwnPost=$isOwnPost")
    }
} 