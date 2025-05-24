package com.kashi.democalai.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.kashi.democalai.data.model.Post
import com.kashi.democalai.data.repository.PostsRepository
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
    val showOnlyMyPosts: Boolean = false
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val postsRepository: PostsRepository
) : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadPosts()
    }

    private fun loadPosts() {
        viewModelScope.launch {
            // Only show initial loading if we don't have posts yet
            if (_uiState.value.posts.isEmpty()) {
                _uiState.value = _uiState.value.copy(isLoading = true)
            }
            
            try {
                if (_uiState.value.showOnlyMyPosts) {
                    val currentUserId = auth.currentUser?.uid
                    if (currentUserId != null) {
                        postsRepository.getUserPostsRealtime(currentUserId).collect { posts ->
                            _uiState.value = _uiState.value.copy(
                                posts = posts,
                                isLoading = false,
                                error = null
                            )
                        }
                    } else {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = "User not authenticated"
                        )
                    }
                } else {
                    postsRepository.getAllPostsRealtime().collect { posts ->
                        _uiState.value = _uiState.value.copy(
                            posts = posts,
                            isLoading = false,
                            error = null
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to load posts"
                )
            }
        }
    }

    fun updateNewPostText(text: String) {
        _uiState.value = _uiState.value.copy(newPostText = text)
    }

    fun createPost() {
        viewModelScope.launch {
            val currentText = _uiState.value.newPostText.trim()
            if (currentText.isEmpty()) {
                _uiState.value = _uiState.value.copy(error = "Post cannot be empty")
                return@launch
            }

            _uiState.value = _uiState.value.copy(isCreatingPost = true, error = null)

            postsRepository.createPost(currentText)
                .onSuccess {
                    _uiState.value = _uiState.value.copy(
                        isCreatingPost = false,
                        newPostText = "",
                        error = null
                    )
                }
                .onFailure { exception ->
                    _uiState.value = _uiState.value.copy(
                        isCreatingPost = false,
                        error = exception.message ?: "Failed to create post"
                    )
                }
        }
    }

    fun toggleFilter() {
        _uiState.value = _uiState.value.copy(
            showOnlyMyPosts = !_uiState.value.showOnlyMyPosts
        )
        loadPosts()
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun refreshPosts() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            try {
                val refreshResult = if (_uiState.value.showOnlyMyPosts) {
                    val currentUserId = auth.currentUser?.uid
                    if (currentUserId != null) {
                        postsRepository.refreshUserPosts(currentUserId)
                    } else {
                        Result.failure(Exception("User not authenticated"))
                    }
                } else {
                    postsRepository.refreshAllPosts()
                }
                
                refreshResult
                    .onSuccess {
                        // Add a minimum delay for better UX (shows the refresh animation)
                        kotlinx.coroutines.delay(300)
                        _uiState.value = _uiState.value.copy(isLoading = false, error = null)
                    }
                    .onFailure { exception ->
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = exception.message ?: "Failed to refresh posts"
                        )
                    }
                
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to refresh posts"
                )
            }
        }
    }
} 