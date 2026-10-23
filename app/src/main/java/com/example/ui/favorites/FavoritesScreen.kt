package com.example.ui.favorites

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.model.Listing
import com.example.ui.MarketplaceViewModel
import com.example.ui.components.EmptyStateView
import com.example.ui.home.ListingCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesScreen(
    viewModel: MarketplaceViewModel,
    onListingClick: (Listing) -> Unit,
    onExploreClick: () -> Unit
) {
    val favoriteIds by viewModel.favoriteIds.collectAsState()
    val allListings by viewModel.filteredListings.collectAsState()
    val favoriteListings = allListings.filter { favoriteIds.contains(it.id) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Saved Favorites (${favoriteListings.size})", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
                .testTag("favorites_screen")
        ) {
            if (favoriteListings.isEmpty()) {
                EmptyStateView(
                    title = "No Saved Items Yet",
                    subtitle = "Tap the heart icon on any listing to save it here for quick access later.",
                    icon = Icons.Default.FavoriteBorder,
                    actionText = "Explore Marketplace",
                    onAction = onExploreClick,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(favoriteListings, key = { it.id }) { listing ->
                        ListingCard(
                            listing = listing,
                            isFavorite = true,
                            onFavoriteClick = { viewModel.toggleFavorite(listing.id) },
                            onClick = { onListingClick(listing) }
                        )
                    }
                }
            }
        }
    }
}
