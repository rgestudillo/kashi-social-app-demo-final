package com.kashi.democalai.presentation.viewmodel

import com.google.common.truth.Truth.assertThat
import com.google.firebase.firestore.DocumentSnapshot
import com.kashi.democalai.data.model.Post
import com.kashi.democalai.data.model.User
import org.junit.Test
import org.mockito.kotlin.mock

class UiStateTest {

    @Test
    fun `HomeUiState should have correct default values`() {
        // When
        val uiState = HomeUiState()

        // Then
        assertThat(uiState.posts).isEmpty()
        assertThat(uiState.isLoading).isFalse()
        assertThat(uiState.isCreatingPost).isFalse()
        assertThat(uiState.newPostText).isEmpty()
        assertThat(uiState.error).isNull()
        assertThat(uiState.showOnlyMyPosts).isFalse()
        assertThat(uiState.currentUser).isNull()
        assertThat(uiState.isLoadingMore).isFalse()
        assertThat(uiState.hasMorePosts).isTrue()
        assertThat(uiState.lastVisiblePost).isNull()
    }

    @Test
    fun `HomeUiState should support data class copy`() {
        // Given
        val posts = listOf(
            Post(id = "1", message = "Test post 1"),
            Post(id = "2", message = "Test post 2")
        )
        val user = User(id = "user1", displayName = "Test User")
        val documentSnapshot = mock<DocumentSnapshot>()
        
        val originalState = HomeUiState(
            posts = posts,
            isLoading = true,
            isCreatingPost = true,
            newPostText = "New post",
            error = "Some error",
            showOnlyMyPosts = true,
            currentUser = user,
            isLoadingMore = true,
            hasMorePosts = false,
            lastVisiblePost = documentSnapshot
        )

        // When
        val copiedState = originalState.copy(
            isLoading = false,
            error = null
        )

        // Then
        assertThat(copiedState.posts).isEqualTo(originalState.posts)
        assertThat(copiedState.isLoading).isFalse() // Changed
        assertThat(copiedState.isCreatingPost).isEqualTo(originalState.isCreatingPost)
        assertThat(copiedState.newPostText).isEqualTo(originalState.newPostText)
        assertThat(copiedState.error).isNull() // Changed
        assertThat(copiedState.showOnlyMyPosts).isEqualTo(originalState.showOnlyMyPosts)
        assertThat(copiedState.currentUser).isEqualTo(originalState.currentUser)
        assertThat(copiedState.isLoadingMore).isEqualTo(originalState.isLoadingMore)
        assertThat(copiedState.hasMorePosts).isEqualTo(originalState.hasMorePosts)
        assertThat(copiedState.lastVisiblePost).isEqualTo(originalState.lastVisiblePost)
    }

    @Test
    fun `HomeUiState should support equality`() {
        // Given
        val posts = listOf(Post(id = "1", message = "Test"))
        val user = User(id = "user1", displayName = "Test User")
        
        val state1 = HomeUiState(
            posts = posts,
            isLoading = false,
            currentUser = user,
            newPostText = "Hello"
        )
        
        val state2 = HomeUiState(
            posts = posts,
            isLoading = false,
            currentUser = user,
            newPostText = "Hello"
        )

        // Then
        assertThat(state1).isEqualTo(state2)
        assertThat(state1.hashCode()).isEqualTo(state2.hashCode())
    }

    @Test
    fun `AuthUiState should have correct default values`() {
        // When
        val uiState = AuthUiState()

        // Then
        assertThat(uiState.isLoading).isFalse()
        assertThat(uiState.isInitializing).isTrue()
        assertThat(uiState.user).isNull()
        assertThat(uiState.error).isNull()
    }

    @Test
    fun `AuthUiState should support data class copy`() {
        // Given
        val user = User(id = "user1", displayName = "Test User")
        val originalState = AuthUiState(
            isLoading = true,
            isInitializing = false,
            user = user,
            error = "Auth error"
        )

        // When
        val copiedState = originalState.copy(
            isLoading = false,
            error = null
        )

        // Then
        assertThat(copiedState.isLoading).isFalse() // Changed
        assertThat(copiedState.isInitializing).isEqualTo(originalState.isInitializing)
        assertThat(copiedState.user).isEqualTo(originalState.user)
        assertThat(copiedState.error).isNull() // Changed
    }

    @Test
    fun `AuthUiState should support equality`() {
        // Given
        val user = User(id = "user1", displayName = "Test User")
        
        val state1 = AuthUiState(
            isLoading = false,
            isInitializing = false,
            user = user,
            error = null
        )
        
        val state2 = AuthUiState(
            isLoading = false,
            isInitializing = false,
            user = user,
            error = null
        )

        // Then
        assertThat(state1).isEqualTo(state2)
        assertThat(state1.hashCode()).isEqualTo(state2.hashCode())
    }

    @Test
    fun `HomeUiState should handle empty posts list`() {
        // Given
        val uiState = HomeUiState(posts = emptyList())

        // Then
        assertThat(uiState.posts).isEmpty()
        assertThat(uiState.posts).hasSize(0)
    }

    @Test
    fun `HomeUiState should handle large posts list`() {
        // Given
        val largePosts = (1..100).map { 
            Post(id = "post$it", message = "Message $it") 
        }
        val uiState = HomeUiState(posts = largePosts)

        // Then
        assertThat(uiState.posts).hasSize(100)
        assertThat(uiState.posts.first().id).isEqualTo("post1")
        assertThat(uiState.posts.last().id).isEqualTo("post100")
    }

    @Test
    fun `HomeUiState should handle null user`() {
        // Given
        val uiState = HomeUiState(currentUser = null)

        // Then
        assertThat(uiState.currentUser).isNull()
    }

    @Test
    fun `AuthUiState should handle null user`() {
        // Given
        val uiState = AuthUiState(user = null)

        // Then
        assertThat(uiState.user).isNull()
    }

    @Test
    fun `HomeUiState should handle long error messages`() {
        // Given
        val longError = "This is a very long error message that might occur in the application " +
                "when something goes wrong with the network or database operations"
        val uiState = HomeUiState(error = longError)

        // Then
        assertThat(uiState.error).isEqualTo(longError)
        assertThat(uiState.error?.length).isGreaterThan(50)
    }

    @Test
    fun `HomeUiState should handle long post text`() {
        // Given
        val longText = "This is a very long post text that a user might type when creating " +
                "a new post in the social media application. It should be handled properly " +
                "by the UI state management system."
        val uiState = HomeUiState(newPostText = longText)

        // Then
        assertThat(uiState.newPostText).isEqualTo(longText)
        assertThat(uiState.newPostText.length).isGreaterThan(100)
    }

    @Test
    fun `HomeUiState should handle all loading states`() {
        // Given
        val uiState = HomeUiState(
            isLoading = true,
            isCreatingPost = true,
            isLoadingMore = true
        )

        // Then
        assertThat(uiState.isLoading).isTrue()
        assertThat(uiState.isCreatingPost).isTrue()
        assertThat(uiState.isLoadingMore).isTrue()
    }

    @Test
    fun `AuthUiState should handle both loading states`() {
        // Given
        val uiState = AuthUiState(
            isLoading = true,
            isInitializing = true
        )

        // Then
        assertThat(uiState.isLoading).isTrue()
        assertThat(uiState.isInitializing).isTrue()
    }
} 