package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.MarketplaceRepository
import com.example.model.ChatMessage
import com.example.model.ChatThread
import com.example.model.Listing
import com.example.model.UserProfile
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface UiState<out T> {
    data object Loading : UiState<Nothing>
    data class Success<T>(val data: T) : UiState<T>
    data class Error(val message: String) : UiState<Nothing>
}

enum class SortOrder {
    NEWEST,
    PRICE_LOW_TO_HIGH,
    PRICE_HIGH_TO_LOW
}

class MarketplaceViewModel(
    private val repository: MarketplaceRepository,
    val currentUserId: String
) : ViewModel() {

    // Filter states
    val searchQuery = MutableStateFlow("")
    val selectedCategory = MutableStateFlow<String?>("All")
    val selectedCity = MutableStateFlow("Dhangadhi")
    val selectedCondition = MutableStateFlow<String?>("All")
    val sortOrder = MutableStateFlow(SortOrder.NEWEST)

    // User profile
    private val _userProfile = MutableStateFlow<UserProfile?>(null)
    val userProfile: StateFlow<UserProfile?> = _userProfile.asStateFlow()

    // Favorites listing IDs set
    val favoriteIds: StateFlow<Set<String>> = repository.observeFavoriteIds(currentUserId)
        .catch { emit(emptySet()) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())

    // Raw listings from repository
    private val rawListings: StateFlow<List<Listing>> = repository.observeListings()
        .catch { emit(emptyList()) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    data class FilterParams(
        val query: String,
        val category: String?,
        val city: String,
        val condition: String?,
        val sort: SortOrder
    )

    private val filterParams = combine(
        searchQuery,
        selectedCategory,
        selectedCity,
        selectedCondition,
        sortOrder
    ) { query, cat, city, cond, sort ->
        FilterParams(query, cat, city, cond, sort)
    }

    // Filtered listings
    val filteredListings: StateFlow<List<Listing>> = combine(
        rawListings,
        filterParams
    ) { listings, params ->
        listings.filter { item ->
            val matchesQuery = params.query.isBlank() ||
                    item.title.contains(params.query, ignoreCase = true) ||
                    item.description.contains(params.query, ignoreCase = true) ||
                    (item.locality?.contains(params.query, ignoreCase = true) == true)

            val matchesCategory = params.category == null || params.category == "All" || item.category.equals(params.category, ignoreCase = true)
            val matchesCity = params.city.isBlank() || params.city == "All Cities" || item.city.equals(params.city, ignoreCase = true)
            val matchesCondition = params.condition == null || params.condition == "All" || item.condition.equals(params.condition, ignoreCase = true)

            matchesQuery && matchesCategory && matchesCity && matchesCondition
        }.let { list ->
            when (params.sort) {
                SortOrder.NEWEST -> list.sortedByDescending { it.createdAt }
                SortOrder.PRICE_LOW_TO_HIGH -> list.sortedBy { it.priceNpr }
                SortOrder.PRICE_HIGH_TO_LOW -> list.sortedByDescending { it.priceNpr }
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // My listings
    val myListings: StateFlow<List<Listing>> = repository.observeMyListings(currentUserId)
        .catch { emit(emptyList()) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Chats
    val myChats: StateFlow<List<ChatThread>> = repository.observeChats(currentUserId)
        .catch { emit(emptyList()) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Active Chat detail
    private val _activeChatId = MutableStateFlow<String?>(null)
    val activeChatId: StateFlow<String?> = _activeChatId.asStateFlow()

    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages.asStateFlow()

    // Operation status messages / snackbar
    private val _statusMessage = MutableStateFlow<String?>(null)
    val statusMessage: StateFlow<String?> = _statusMessage.asStateFlow()

    init {
        loadUserProfile()
    }

    fun loadUserProfile() {
        viewModelScope.launch {
            val profile = repository.getUserProfile(currentUserId)
            if (profile != null) {
                _userProfile.value = profile
            } else {
                val user = Firebase.auth.currentUser
                val newProfile = UserProfile(
                    userId = currentUserId,
                    displayName = user?.displayName ?: "Marketplace User",
                    email = user?.email ?: "user@localbazaar.np",
                    city = "Dhangadhi"
                )
                repository.saveUserProfile(newProfile)
                _userProfile.value = newProfile
            }
        }
    }

    fun updateUserProfile(displayName: String, city: String, phone: String?) {
        viewModelScope.launch {
            val updated = (_userProfile.value ?: UserProfile(userId = currentUserId)).copy(
                displayName = displayName,
                city = city,
                phone = phone
            )
            val res = repository.saveUserProfile(updated)
            if (res.isSuccess) {
                _userProfile.value = updated
                _statusMessage.value = "Profile updated successfully"
            } else {
                _statusMessage.value = "Failed to update profile: ${res.exceptionOrNull()?.message}"
            }
        }
    }

    fun toggleFavorite(listingId: String) {
        viewModelScope.launch {
            val isFav = favoriteIds.value.contains(listingId)
            val res = repository.toggleFavorite(currentUserId, listingId, isFav)
            if (res.isFailure) {
                _statusMessage.value = "Failed to update favorite"
            }
        }
    }

    fun createListing(
        title: String,
        description: String,
        priceNpr: Double,
        category: String,
        condition: String,
        city: String,
        locality: String?,
        photos: List<String>,
        onSuccess: (String) -> Unit
    ) {
        viewModelScope.launch {
            val sellerName = _userProfile.value?.displayName
                ?: Firebase.auth.currentUser?.displayName
                ?: "Local Seller"

            val listing = Listing(
                sellerId = currentUserId,
                sellerName = sellerName,
                title = title,
                description = description,
                priceNpr = priceNpr,
                category = category,
                condition = condition,
                city = city,
                locality = locality,
                photos = photos,
                status = "active"
            )

            val res = repository.createListing(listing)
            if (res.isSuccess) {
                _statusMessage.value = "Product listed for sale in $city!"
                onSuccess(res.getOrThrow())
            } else {
                _statusMessage.value = "Failed to post listing: ${res.exceptionOrNull()?.localizedMessage}"
            }
        }
    }

    fun markListingSold(listingId: String) {
        viewModelScope.launch {
            val res = repository.markListingSold(listingId)
            if (res.isSuccess) {
                _statusMessage.value = "Listing marked as sold"
            } else {
                _statusMessage.value = "Failed to update status"
            }
        }
    }

    fun deleteListing(listingId: String) {
        viewModelScope.launch {
            val res = repository.deleteListing(listingId)
            if (res.isSuccess) {
                _statusMessage.value = "Listing deleted"
            } else {
                _statusMessage.value = "Failed to delete listing"
            }
        }
    }

    fun openChat(listing: Listing, onChatOpened: (String) -> Unit) {
        viewModelScope.launch {
            val buyerName = _userProfile.value?.displayName
                ?: Firebase.auth.currentUser?.displayName
                ?: "Interested Buyer"

            val res = repository.getOrCreateChat(listing, buyerName)
            if (res.isSuccess) {
                val chatId = res.getOrThrow()
                setActiveChat(chatId)
                onChatOpened(chatId)
            } else {
                _statusMessage.value = res.exceptionOrNull()?.localizedMessage ?: "Unable to open chat"
            }
        }
    }

    fun setActiveChat(chatId: String) {
        _activeChatId.value = chatId
        viewModelScope.launch {
            repository.observeMessages(chatId)
                .catch { emit(emptyList()) }
                .collect { messages ->
                    _chatMessages.value = messages
                }
        }
    }

    fun sendMessage(chatId: String, text: String) {
        if (text.isBlank()) return
        viewModelScope.launch {
            val senderName = _userProfile.value?.displayName
                ?: Firebase.auth.currentUser?.displayName
                ?: "User"
            val res = repository.sendMessage(chatId, text, senderName)
            if (res.isFailure) {
                _statusMessage.value = "Failed to send message"
            }
        }
    }

    fun submitReport(listingId: String, reason: String, details: String?, onComplete: () -> Unit) {
        viewModelScope.launch {
            val res = repository.submitReport(listingId, reason, details)
            if (res.isSuccess) {
                _statusMessage.value = "Report submitted. Thank you for keeping our marketplace safe."
                onComplete()
            } else {
                _statusMessage.value = "Failed to submit report"
            }
        }
    }

    fun clearStatusMessage() {
        _statusMessage.value = null
    }
}
