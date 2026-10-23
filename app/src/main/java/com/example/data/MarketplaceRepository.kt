package com.example.data

import android.content.Context
import com.example.R
import com.example.model.ChatMessage
import com.example.model.ChatThread
import com.example.model.Favorite
import com.example.model.Listing
import com.example.model.Report
import com.example.model.UserProfile
import com.example.util.OperationType
import com.example.util.handleFirestoreError
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import java.util.UUID

class MarketplaceRepository(private val db: FirebaseFirestore) {

    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    private val auth get() = Firebase.auth

    private fun requireUserId(): String {
        return auth.currentUser?.uid
            ?: throw IllegalStateException("User must be signed in with Google before accessing Firestore.")
    }

    // --- Listings ---

    fun observeListings(): Flow<List<Listing>> =
        db.collection("listings")
            .whereEqualTo("status", "active")
            .snapshots()
            .map { snapshot ->
                snapshot.toObjects(Listing::class.java).sortedByDescending { it.createdAt }
            }
            .catch { e ->
                if (e is Exception) handleFirestoreError(e, OperationType.LIST, "listings")
                throw e
            }

    fun observeMyListings(sellerId: String): Flow<List<Listing>> =
        db.collection("listings")
            .whereEqualTo("sellerId", sellerId)
            .snapshots()
            .map { snapshot ->
                snapshot.toObjects(Listing::class.java).sortedByDescending { it.createdAt }
            }
            .catch { e ->
                if (e is Exception) handleFirestoreError(e, OperationType.LIST, "listings")
                throw e
            }

    suspend fun getListing(id: String): Listing? {
        val docRef = db.collection("listings").document(id)
        return try {
            val snapshot = docRef.get().await()
            if (snapshot.exists()) snapshot.toObject(Listing::class.java) else null
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.GET, docRef.path)
            null
        }
    }

    suspend fun createListing(listing: Listing): Result<String> {
        val uid = requireUserId()
        val docRef = db.collection("listings").document()
        val payload = mapOf(
            "sellerId" to uid,
            "sellerName" to listing.sellerName,
            "title" to listing.title,
            "description" to listing.description,
            "priceNpr" to listing.priceNpr,
            "category" to listing.category,
            "condition" to listing.condition,
            "city" to listing.city,
            "locality" to listing.locality,
            "photos" to listing.photos,
            "status" to "active",
            "isFeatured" to listing.isFeatured,
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        ).filterValues { it != null }

        return try {
            docRef.set(payload).await()
            Result.success(docRef.id)
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            Result.failure(e)
        }
    }

    suspend fun updateListing(listing: Listing): Result<Unit> {
        requireUserId()
        val docRef = db.collection("listings").document(listing.id)
        val updates = mapOf(
            "title" to listing.title,
            "description" to listing.description,
            "priceNpr" to listing.priceNpr,
            "category" to listing.category,
            "condition" to listing.condition,
            "city" to listing.city,
            "locality" to listing.locality,
            "photos" to listing.photos,
            "status" to listing.status,
            "isFeatured" to listing.isFeatured,
            "updatedAt" to FieldValue.serverTimestamp()
        ).filterValues { it != null }

        return try {
            docRef.update(updates).await()
            Result.success(Unit)
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            Result.failure(e)
        }
    }

    suspend fun markListingSold(listingId: String): Result<Unit> {
        requireUserId()
        val docRef = db.collection("listings").document(listingId)
        return try {
            docRef.update(
                mapOf(
                    "status" to "sold",
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            Result.failure(e)
        }
    }

    suspend fun deleteListing(listingId: String): Result<Unit> {
        requireUserId()
        val docRef = db.collection("listings").document(listingId)
        return try {
            docRef.delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docRef.path)
            Result.failure(e)
        }
    }

    // --- Favorites ---

    fun observeFavoriteIds(userId: String): Flow<Set<String>> =
        db.collection("users").document(userId).collection("favorites")
            .snapshots()
            .map { snapshot ->
                snapshot.documents.mapNotNull { it.getString("listingId") }.toSet()
            }
            .catch { e ->
                if (e is Exception) handleFirestoreError(e, OperationType.LIST, "users/$userId/favorites")
                throw e
            }

    suspend fun toggleFavorite(userId: String, listingId: String, currentIsFav: Boolean): Result<Unit> {
        val uid = requireUserId()
        if (uid != userId) return Result.failure(IllegalAccessException("Cannot modify another user's favorites"))
        val docRef = db.collection("users").document(userId).collection("favorites").document(listingId)
        return try {
            if (currentIsFav) {
                docRef.delete().await()
            } else {
                val payload = mapOf(
                    "listingId" to listingId,
                    "createdAt" to FieldValue.serverTimestamp()
                )
                docRef.set(payload).await()
            }
            Result.success(Unit)
        } catch (e: Exception) {
            val op = if (currentIsFav) OperationType.DELETE else OperationType.CREATE
            handleFirestoreError(e, op, docRef.path)
            Result.failure(e)
        }
    }

    // --- Chats ---

    fun observeChats(userId: String): Flow<List<ChatThread>> =
        db.collection("chats")
            .whereArrayContains("participantIds", userId)
            .snapshots()
            .map { snapshot ->
                snapshot.toObjects(ChatThread::class.java).sortedByDescending { it.updatedAt ?: it.createdAt }
            }
            .catch { e ->
                if (e is Exception) handleFirestoreError(e, OperationType.LIST, "chats")
                throw e
            }

    fun observeMessages(chatId: String): Flow<List<ChatMessage>> =
        db.collection("chats").document(chatId).collection("messages")
            .orderBy("createdAt", Query.Direction.ASCENDING)
            .snapshots()
            .map { snapshot -> snapshot.toObjects(ChatMessage::class.java) }
            .catch { e ->
                if (e is Exception) handleFirestoreError(e, OperationType.LIST, "chats/$chatId/messages")
                throw e
            }

    suspend fun getOrCreateChat(listing: Listing, buyerName: String): Result<String> {
        val buyerId = requireUserId()
        if (buyerId == listing.sellerId) {
            return Result.failure(IllegalStateException("Cannot chat with yourself regarding your own listing."))
        }

        val chatsRef = db.collection("chats")
        try {
            // Check if thread already exists for this listing and buyer
            val existing = chatsRef
                .whereEqualTo("listingId", listing.id)
                .whereEqualTo("buyerId", buyerId)
                .get()
                .await()

            if (!existing.isEmpty) {
                return Result.success(existing.documents.first().id)
            }

            // Create new chat thread
            val newChatDoc = chatsRef.document()
            val payload = mapOf(
                "listingId" to listing.id,
                "listingTitle" to listing.title,
                "listingPrice" to listing.priceNpr,
                "buyerId" to buyerId,
                "buyerName" to buyerName,
                "sellerId" to listing.sellerId,
                "sellerName" to listing.sellerName,
                "participantIds" to listOf(buyerId, listing.sellerId),
                "lastMessage" to "Inquiry about: ${listing.title}",
                "lastMessageTimestamp" to FieldValue.serverTimestamp(),
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            newChatDoc.set(payload).await()
            return Result.success(newChatDoc.id)
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, "chats")
            return Result.failure(e)
        }
    }

    suspend fun sendMessage(chatId: String, text: String, senderName: String): Result<String> {
        val uid = requireUserId()
        val messagesRef = db.collection("chats").document(chatId).collection("messages")
        val messageDoc = messagesRef.document()

        val msgPayload = mapOf(
            "chatId" to chatId,
            "senderId" to uid,
            "senderName" to senderName,
            "text" to text.trim(),
            "createdAt" to FieldValue.serverTimestamp()
        )

        val chatDoc = db.collection("chats").document(chatId)
        val chatUpdates = mapOf(
            "lastMessage" to text.trim(),
            "lastMessageTimestamp" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )

        return try {
            messageDoc.set(msgPayload).await()
            chatDoc.update(chatUpdates).await()
            Result.success(messageDoc.id)
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, messageDoc.path)
            Result.failure(e)
        }
    }

    // --- Reports ---

    suspend fun submitReport(listingId: String, reason: String, details: String?): Result<String> {
        val uid = requireUserId()
        val docRef = db.collection("reports").document()
        val payload = mapOf(
            "listingId" to listingId,
            "reporterId" to uid,
            "reason" to reason,
            "details" to details,
            "createdAt" to FieldValue.serverTimestamp()
        ).filterValues { it != null }

        return try {
            docRef.set(payload).await()
            Result.success(docRef.id)
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            Result.failure(e)
        }
    }

    // --- User Profile ---

    suspend fun getUserProfile(userId: String): UserProfile? {
        val docRef = db.collection("users").document(userId)
        return try {
            val snapshot = docRef.get().await()
            if (snapshot.exists()) snapshot.toObject(UserProfile::class.java) else null
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.GET, docRef.path)
            null
        }
    }

    suspend fun saveUserProfile(profile: UserProfile): Result<Unit> {
        val uid = requireUserId()
        val docRef = db.collection("users").document(uid)
        return try {
            val exists = docRef.get().await().exists()
            if (!exists) {
                val payload = mapOf(
                    "userId" to uid,
                    "displayName" to profile.displayName,
                    "email" to profile.email,
                    "city" to profile.city,
                    "phone" to profile.phone,
                    "avatarUrl" to profile.avatarUrl,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                ).filterValues { it != null }
                docRef.set(payload).await()
            } else {
                val updates = mapOf(
                    "displayName" to profile.displayName,
                    "city" to profile.city,
                    "phone" to profile.phone,
                    "avatarUrl" to profile.avatarUrl,
                    "updatedAt" to FieldValue.serverTimestamp()
                ).filterValues { it != null }
                docRef.update(updates).await()
            }
            Result.success(Unit)
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, docRef.path)
            Result.failure(e)
        }
    }
}
