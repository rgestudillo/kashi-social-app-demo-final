package com.kashi.democalai

import com.google.common.truth.Truth.assertThat
import org.junit.Test

/**
 * Unit tests for basic app configuration and constants.
 */
class AppConfigTest {
    
    @Test
    fun `app package name should be correct`() {
        // Given
        val expectedPackageName = "com.kashi.democalai"
        
        // When
        val actualPackageName = this::class.java.packageName
        
        // Then
        assertThat(actualPackageName).isEqualTo(expectedPackageName)
    }
    
    @Test
    fun `basic arithmetic operations should work correctly`() {
        // Given
        val a = 2
        val b = 2
        
        // When
        val sum = a + b
        val product = a * b
        val difference = a - b
        
        // Then
        assertThat(sum).isEqualTo(4)
        assertThat(product).isEqualTo(4)
        assertThat(difference).isEqualTo(0)
    }
    
    @Test
    fun `string operations should work correctly`() {
        // Given
        val firstName = "John"
        val lastName = "Doe"
        
        // When
        val fullName = "$firstName $lastName"
        
        // Then
        assertThat(fullName).isEqualTo("John Doe")
        assertThat(fullName).contains(firstName)
        assertThat(fullName).contains(lastName)
    }
    
    @Test
    fun `list operations should work correctly`() {
        // Given
        val numbers = listOf(1, 2, 3, 4, 5)
        
        // When
        val sum = numbers.sum()
        val max = numbers.maxOrNull()
        val filtered = numbers.filter { it > 3 }
        
        // Then
        assertThat(sum).isEqualTo(15)
        assertThat(max).isEqualTo(5)
        assertThat(filtered).containsExactly(4, 5)
    }
}