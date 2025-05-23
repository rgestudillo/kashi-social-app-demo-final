package com.kashi.democalai.presentation.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.firebase.auth.FirebaseUser
import com.kashi.democalai.data.model.Post
import com.kashi.democalai.presentation.viewmodel.AuthViewModel
import com.kashi.democalai.presentation.viewmodel.HomeViewModel
import com.kashi.democalai.ui.theme.MyApplicationTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    authViewModel: AuthViewModel = hiltViewModel(),
    homeViewModel: HomeViewModel = hiltViewModel(),
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    val authUiState by authViewModel.uiState.collectAsStateWithLifecycle()
    val homeUiState by homeViewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    // Navigate to login when user is signed out
    LaunchedEffect(authUiState.user) {
        if (authUiState.user == null && !authUiState.isLoading) {
            onLogout()
        }
    }

    // Show error snackbar
    LaunchedEffect(homeUiState.error) {
        homeUiState.error?.let { error ->
            snackbarHostState.showSnackbar(error)
            homeViewModel.clearError()
        }
    }

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = {
                        Box(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            // Log Out button on the left
                            TextButton(
                                onClick = { authViewModel.signOut(context) },
                                modifier = Modifier.align(Alignment.CenterStart)
                            ) {
                                Text(
                                    text = "Log Out",
                                    color = Color(0xFF00A3FF),
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold,
                                )
                            }

                            // Wall text centered
                            Text(
                                text = "Wall",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.align(Alignment.Center)
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.White
                    )
                )
                // Divider at the bottom
                HorizontalDivider(
                    thickness = 1.dp,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Color.White
    ) { paddingValues ->
        HomeContent(
            user = authUiState.user,
            uiState = homeUiState,
            onTextChange = homeViewModel::updateNewPostText,
            onCreatePost = homeViewModel::createPost,
            onToggleFilter = homeViewModel::toggleFilter,
            modifier = Modifier.padding(paddingValues)
        )
    }
}

@Composable
private fun HomeContent(
    user: FirebaseUser?,
    uiState: com.kashi.democalai.presentation.viewmodel.HomeUiState,
    onTextChange: (String) -> Unit,
    onCreatePost: () -> Unit,
    onToggleFilter: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize().padding(16.dp)
    ) {
        // Post creation section
        CreatePostSection(
            text = uiState.newPostText,
            onTextChange = onTextChange,
            onCreatePost = onCreatePost,
            isCreating = uiState.isCreatingPost
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Filter button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (uiState.showOnlyMyPosts) "My Posts" else "All Posts",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            TextButton(onClick = onToggleFilter) {
                Text(
                    text = if (uiState.showOnlyMyPosts) "Show All" else "Show Mine",
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Posts list
        if (uiState.isLoading && uiState.posts.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            PostsList(posts = uiState.posts)
        }
    }
}

@Composable
private fun CreatePostSection(
    text: String,
    onTextChange: (String) -> Unit,
    onCreatePost: () -> Unit,
    isCreating: Boolean
) {
    Column(
        modifier = Modifier.padding(0.dp)
    ) {
        // Clean text field without border
        TextField(
            value = text,
            onValueChange = onTextChange,
            placeholder = {
                Text(
                    text = "Write something here...",
                    color = Color.Gray
                )
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isCreating,
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                disabledContainerColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent
            ),
            textStyle = MaterialTheme.typography.bodyLarge
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Add to the wall button
        Button(
            onClick = onCreatePost,
            enabled = !isCreating && text.trim().isNotEmpty(),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF00A3FF)
            ),
            shape = RoundedCornerShape(7.dp),
            modifier = Modifier.height(48.dp).padding(start = 13.dp)
        ) {
            if (isCreating) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = Color.White,
                    strokeWidth = 2.dp
                )
            } else {
                Text(
                    text = "Add to the wall",
                    color = Color.White,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun PostsList(posts: List<Post>) {
    LazyColumn {
        itemsIndexed(posts) { index, post ->
            AnimatedVisibility(
                visible = true,
                enter = slideInVertically(
                    initialOffsetY = { 50 },
                    animationSpec = tween(300, delayMillis = index * 50)
                ) + fadeIn(
                    animationSpec = tween(300, delayMillis = index * 50)
                )
            ) {
                PostItem(post = post)
            }
            
            if (index < posts.size - 1) {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun PostItem(post: Post) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        Icon(
            imageVector = Icons.Outlined.AccountCircle,
            contentDescription = "User Avatar",
            tint = Color.Black,
            modifier = Modifier.size(50.dp)
        )

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = post.userName.takeIf { it.isNotEmpty() } ?: "Unknown User",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                
                post.timestamp?.let { timestamp ->
                    Text(
                        text = formatTimestamp(timestamp.toDate()),
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = post.message,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Black
            )
        }
    }
}

private fun formatTimestamp(date: Date): String {
    val format = SimpleDateFormat("h:mma", Locale.getDefault())
    return format.format(date).lowercase()
}