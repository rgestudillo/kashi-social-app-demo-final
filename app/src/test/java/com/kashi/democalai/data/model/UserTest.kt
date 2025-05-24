package com.kashi.democalai.data.model

import com.google.firebase.Timestamp
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class UserTest {

    @Test
    fun `should create User with all parameters`() {
        // Given
        val id = "user123"
        val email = "test@example.com"
        val displayName = "Test User"
        val profileImageUrl = "https://example.com/image.jpg"
        val bio = "This is a test bio"
        val createdAt = Timestamp.now()
        val lastLoginAt = Timestamp.now()
        val isActive = true
        val postCount = 10
        val followersCount = 50
        val followingCount = 25

        // When
        val user = User(
            id = id,
            email = email,
            displayName = displayName,
            profileImageUrl = profileImageUrl,
            bio = bio,
            createdAt = createdAt,
            lastLoginAt = lastLoginAt,
            isActive = isActive,
            postCount = postCount,
            followersCount = followersCount,
            followingCount = followingCount
        )

        // Then
        assertThat(user.id).isEqualTo(id)
        assertThat(user.email).isEqualTo(email)
        assertThat(user.displayName).isEqualTo(displayName)
        assertThat(user.profileImageUrl).isEqualTo(profileImageUrl)
        assertThat(user.bio).isEqualTo(bio)
        assertThat(user.createdAt).isEqualTo(createdAt)
        assertThat(user.lastLoginAt).isEqualTo(lastLoginAt)
        assertThat(user.isActive).isEqualTo(isActive)
        assertThat(user.postCount).isEqualTo(postCount)
        assertThat(user.followersCount).isEqualTo(followersCount)
        assertThat(user.followingCount).isEqualTo(followingCount)
    }

    @Test
    fun `should create User with default values`() {
        // When
        val user = User()

        // Then
        assertThat(user.id).isEmpty()
        assertThat(user.email).isEmpty()
        assertThat(user.displayName).isEmpty()
        assertThat(user.profileImageUrl).isNull()
        assertThat(user.bio).isNull()
        assertThat(user.createdAt).isNull()
        assertThat(user.lastLoginAt).isNull()
        assertThat(user.isActive).isTrue()
        assertThat(user.postCount).isEqualTo(0)
        assertThat(user.followersCount).isEqualTo(0)
        assertThat(user.followingCount).isEqualTo(0)
    }

    @Test
    fun `should create User with partial parameters`() {
        // Given
        val id = "user456"
        val email = "partial@example.com"
        val displayName = "Partial User"

        // When
        val user = User(
            id = id,
            email = email,
            displayName = displayName
        )

        // Then
        assertThat(user.id).isEqualTo(id)
        assertThat(user.email).isEqualTo(email)
        assertThat(user.displayName).isEqualTo(displayName)
        assertThat(user.profileImageUrl).isNull()
        assertThat(user.bio).isNull()
        assertThat(user.createdAt).isNull()
        assertThat(user.lastLoginAt).isNull()
        assertThat(user.isActive).isTrue() // Default value
        assertThat(user.postCount).isEqualTo(0) // Default value
        assertThat(user.followersCount).isEqualTo(0) // Default value
        assertThat(user.followingCount).isEqualTo(0) // Default value
    }

    @Test
    fun `should support data class equality`() {
        // Given
        val user1 = User(
            id = "user1",
            email = "test@example.com",
            displayName = "Test User",
            profileImageUrl = null,
            bio = "Test bio",
            createdAt = null,
            lastLoginAt = null,
            isActive = true,
            postCount = 5,
            followersCount = 10,
            followingCount = 8
        )
        
        val user2 = User(
            id = "user1",
            email = "test@example.com",
            displayName = "Test User",
            profileImageUrl = null,
            bio = "Test bio",
            createdAt = null,
            lastLoginAt = null,
            isActive = true,
            postCount = 5,
            followersCount = 10,
            followingCount = 8
        )

        // Then
        assertThat(user1).isEqualTo(user2)
        assertThat(user1.hashCode()).isEqualTo(user2.hashCode())
    }

    @Test
    fun `should support data class copy`() {
        // Given
        val originalUser = User(
            id = "user1",
            email = "original@example.com",
            displayName = "Original User",
            profileImageUrl = null,
            bio = "Original bio",
            createdAt = null,
            lastLoginAt = null,
            isActive = true,
            postCount = 5,
            followersCount = 10,
            followingCount = 8
        )

        // When
        val copiedUser = originalUser.copy(
            displayName = "Updated User",
            postCount = 10
        )

        // Then
        assertThat(copiedUser.id).isEqualTo(originalUser.id)
        assertThat(copiedUser.email).isEqualTo(originalUser.email)
        assertThat(copiedUser.displayName).isEqualTo("Updated User")
        assertThat(copiedUser.profileImageUrl).isEqualTo(originalUser.profileImageUrl)
        assertThat(copiedUser.bio).isEqualTo(originalUser.bio)
        assertThat(copiedUser.createdAt).isEqualTo(originalUser.createdAt)
        assertThat(copiedUser.lastLoginAt).isEqualTo(originalUser.lastLoginAt)
        assertThat(copiedUser.isActive).isEqualTo(originalUser.isActive)
        assertThat(copiedUser.postCount).isEqualTo(10)
        assertThat(copiedUser.followersCount).isEqualTo(originalUser.followersCount)
        assertThat(copiedUser.followingCount).isEqualTo(originalUser.followingCount)
    }

    @Test
    fun `should handle null optional fields`() {
        // When
        val user = User(
            id = "user123",
            email = "test@example.com",
            displayName = "Test User",
            profileImageUrl = null,
            bio = null,
            createdAt = null,
            lastLoginAt = null,
            isActive = false,
            postCount = 0,
            followersCount = 0,
            followingCount = 0
        )

        // Then
        assertThat(user.profileImageUrl).isNull()
        assertThat(user.bio).isNull()
        assertThat(user.createdAt).isNull()
        assertThat(user.lastLoginAt).isNull()
        assertThat(user.isActive).isFalse()
    }

    @Test
    fun `should handle negative counts gracefully`() {
        // When
        val user = User(
            id = "user123",
            email = "test@example.com",
            displayName = "Test User",
            postCount = -1,
            followersCount = -5,
            followingCount = -3
        )

        // Then
        assertThat(user.postCount).isEqualTo(-1)
        assertThat(user.followersCount).isEqualTo(-5)
        assertThat(user.followingCount).isEqualTo(-3)
    }

    @Test
    fun `should handle empty strings`() {
        // When
        val user = User(
            id = "",
            email = "",
            displayName = "",
            profileImageUrl = "",
            bio = ""
        )

        // Then
        assertThat(user.id).isEmpty()
        assertThat(user.email).isEmpty()
        assertThat(user.displayName).isEmpty()
        assertThat(user.profileImageUrl).isEmpty()
        assertThat(user.bio).isEmpty()
    }
} 