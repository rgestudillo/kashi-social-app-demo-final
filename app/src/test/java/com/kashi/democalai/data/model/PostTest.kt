package com.kashi.democalai.data.model

import com.google.firebase.Timestamp
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class PostTest {

    @Test
    fun `should create Post with all parameters`() {
        // Given
        val id = "post123"
        val userId = "user456"
        val userName = "Test User"
        val userEmail = "test@example.com"
        val message = "This is a test post"
        val timestamp = Timestamp.now()

        // When
        val post = Post(
            id = id,
            userId = userId,
            userName = userName,
            userEmail = userEmail,
            message = message,
            timestamp = timestamp
        )

        // Then
        assertThat(post.id).isEqualTo(id)
        assertThat(post.userId).isEqualTo(userId)
        assertThat(post.userName).isEqualTo(userName)
        assertThat(post.userEmail).isEqualTo(userEmail)
        assertThat(post.message).isEqualTo(message)
        assertThat(post.timestamp).isEqualTo(timestamp)
    }

    @Test
    fun `should create Post with default values`() {
        // When
        val post = Post()

        // Then
        assertThat(post.id).isEmpty()
        assertThat(post.userId).isEmpty()
        assertThat(post.userName).isEmpty()
        assertThat(post.userEmail).isEmpty()
        assertThat(post.message).isEmpty()
        assertThat(post.timestamp).isNull()
    }

    @Test
    fun `should create Post with partial parameters`() {
        // Given
        val userId = "user123"
        val message = "Hello World"

        // When
        val post = Post(
            userId = userId,
            message = message
        )

        // Then
        assertThat(post.userId).isEqualTo(userId)
        assertThat(post.message).isEqualTo(message)
        assertThat(post.id).isEmpty()
        assertThat(post.userName).isEmpty()
        assertThat(post.userEmail).isEmpty()
        assertThat(post.timestamp).isNull()
    }

    @Test
    fun `should support data class equality`() {
        // Given
        val post1 = Post(
            id = "post1",
            userId = "user1",
            userName = "User One",
            userEmail = "user1@example.com",
            message = "Test message",
            timestamp = null
        )
        
        val post2 = Post(
            id = "post1",
            userId = "user1",
            userName = "User One",
            userEmail = "user1@example.com",
            message = "Test message",
            timestamp = null
        )

        // Then
        assertThat(post1).isEqualTo(post2)
        assertThat(post1.hashCode()).isEqualTo(post2.hashCode())
    }

    @Test
    fun `should support data class copy`() {
        // Given
        val originalPost = Post(
            id = "post1",
            userId = "user1",
            userName = "User One",
            userEmail = "user1@example.com",
            message = "Original message",
            timestamp = null
        )

        // When
        val copiedPost = originalPost.copy(message = "Updated message")

        // Then
        assertThat(copiedPost.id).isEqualTo(originalPost.id)
        assertThat(copiedPost.userId).isEqualTo(originalPost.userId)
        assertThat(copiedPost.userName).isEqualTo(originalPost.userName)
        assertThat(copiedPost.userEmail).isEqualTo(originalPost.userEmail)
        assertThat(copiedPost.message).isEqualTo("Updated message")
        assertThat(copiedPost.timestamp).isEqualTo(originalPost.timestamp)
    }

    @Test
    fun `should handle empty and null values`() {
        // When
        val post = Post(
            id = "",
            userId = "",
            userName = "",
            userEmail = "",
            message = "",
            timestamp = null
        )

        // Then
        assertThat(post.id).isEmpty()
        assertThat(post.userId).isEmpty()
        assertThat(post.userName).isEmpty()
        assertThat(post.userEmail).isEmpty()
        assertThat(post.message).isEmpty()
        assertThat(post.timestamp).isNull()
    }
} 