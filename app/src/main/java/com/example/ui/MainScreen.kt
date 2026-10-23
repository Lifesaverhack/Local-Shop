package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonOutline
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Listing
import com.example.ui.chat.ChatConversationScreen
import com.example.ui.chat.MessagesInboxScreen
import com.example.ui.detail.ProductDetailScreen
import com.example.ui.favorites.FavoritesScreen
import com.example.ui.home.HomeScreen
import com.example.ui.profile.ProfileScreen
import com.example.ui.sell.SellScreen
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.NavyDark
import com.example.ui.theme.NavyPrimary

enum class MainTab {
    HOME,
    FAVORITES,
    SELL,
    MESSAGES,
    PROFILE
}

@Composable
fun MainScreen(
    viewModel: MarketplaceViewModel,
    onSignOut: () -> Unit
) {
    var currentTab by remember { mutableStateOf(MainTab.HOME) }
    var viewingListing by remember { mutableStateOf<Listing?>(null) }
    var viewingChatId by remember { mutableStateOf<String?>(null) }

    val snackbarHostState = remember { SnackbarHostState() }
    val statusMessage by viewModel.statusMessage.collectAsState()

    LaunchedEffect(statusMessage) {
        statusMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearStatusMessage()
        }
    }

    // Handle back button hierarchically
    BackHandler(enabled = viewingChatId != null || viewingListing != null || currentTab != MainTab.HOME) {
        when {
            viewingChatId != null -> viewingChatId = null
            viewingListing != null -> viewingListing = null
            currentTab != MainTab.HOME -> currentTab = MainTab.HOME
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            // Hide bottom bar when in chat conversation or product detail view
            if (viewingListing == null && viewingChatId == null) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 6.dp,
                    modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)
                ) {
                    NavigationBarItem(
                        selected = currentTab == MainTab.HOME,
                        onClick = { currentTab = MainTab.HOME },
                        icon = {
                            Icon(
                                imageVector = if (currentTab == MainTab.HOME) Icons.Filled.ShoppingBag else Icons.Outlined.ShoppingBag,
                                contentDescription = "Home"
                            )
                        },
                        label = { Text("Home", fontSize = 11.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = NavyPrimary,
                            selectedTextColor = NavyPrimary,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer
                        ),
                        modifier = Modifier.testTag("nav_home")
                    )

                    NavigationBarItem(
                        selected = currentTab == MainTab.FAVORITES,
                        onClick = { currentTab = MainTab.FAVORITES },
                        icon = {
                            Icon(
                                imageVector = if (currentTab == MainTab.FAVORITES) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                                contentDescription = "Favorites"
                            )
                        },
                        label = { Text("Favorites", fontSize = 11.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = NavyPrimary,
                            selectedTextColor = NavyPrimary,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer
                        ),
                        modifier = Modifier.testTag("nav_favorites")
                    )

                    // Prominent SELL tab in the middle
                    NavigationBarItem(
                        selected = currentTab == MainTab.SELL,
                        onClick = { currentTab = MainTab.SELL },
                        icon = {
                            Surface(
                                modifier = Modifier.size(36.dp),
                                shape = CircleShape,
                                color = GoldAccent
                            ) {
                                Box(contentAlignment = androidx.compose.ui.Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = "Sell an item",
                                        tint = NavyDark,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                        },
                        label = { Text("Sell", fontSize = 11.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = NavyPrimary,
                            selectedTextColor = NavyPrimary,
                            indicatorColor = Color.Transparent
                        ),
                        modifier = Modifier.testTag("nav_sell")
                    )

                    NavigationBarItem(
                        selected = currentTab == MainTab.MESSAGES,
                        onClick = { currentTab = MainTab.MESSAGES },
                        icon = {
                            Icon(
                                imageVector = if (currentTab == MainTab.MESSAGES) Icons.Filled.ChatBubble else Icons.Filled.ChatBubbleOutline,
                                contentDescription = "Messages"
                            )
                        },
                        label = { Text("Messages", fontSize = 11.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = NavyPrimary,
                            selectedTextColor = NavyPrimary,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer
                        ),
                        modifier = Modifier.testTag("nav_messages")
                    )

                    NavigationBarItem(
                        selected = currentTab == MainTab.PROFILE,
                        onClick = { currentTab = MainTab.PROFILE },
                        icon = {
                            Icon(
                                imageVector = if (currentTab == MainTab.PROFILE) Icons.Filled.Person else Icons.Filled.PersonOutline,
                                contentDescription = "Profile"
                            )
                        },
                        label = { Text("Profile", fontSize = 11.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = NavyPrimary,
                            selectedTextColor = NavyPrimary,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer
                        ),
                        modifier = Modifier.testTag("nav_profile")
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when {
                viewingChatId != null -> {
                    ChatConversationScreen(
                        chatId = viewingChatId!!,
                        viewModel = viewModel,
                        onBack = { viewingChatId = null }
                    )
                }
                viewingListing != null -> {
                    ProductDetailScreen(
                        listing = viewingListing!!,
                        viewModel = viewModel,
                        onBack = { viewingListing = null },
                        onOpenChat = { chatId ->
                            viewingListing = null
                            viewingChatId = chatId
                        }
                    )
                }
                else -> {
                    when (currentTab) {
                        MainTab.HOME -> HomeScreen(
                            viewModel = viewModel,
                            onListingClick = { listing -> viewingListing = listing },
                            onSellClick = { currentTab = MainTab.SELL }
                        )
                        MainTab.FAVORITES -> FavoritesScreen(
                            viewModel = viewModel,
                            onListingClick = { listing -> viewingListing = listing },
                            onExploreClick = { currentTab = MainTab.HOME }
                        )
                        MainTab.SELL -> SellScreen(
                            viewModel = viewModel,
                            onListingCreated = { newListingId ->
                                currentTab = MainTab.HOME
                            }
                        )
                        MainTab.MESSAGES -> MessagesInboxScreen(
                            viewModel = viewModel,
                            onChatClick = { chatId -> viewingChatId = chatId },
                            onExploreClick = { currentTab = MainTab.HOME }
                        )
                        MainTab.PROFILE -> ProfileScreen(
                            viewModel = viewModel,
                            onListingClick = { listing -> viewingListing = listing },
                            onSignOut = onSignOut
                        )
                    }
                }
            }
        }
    }
}
