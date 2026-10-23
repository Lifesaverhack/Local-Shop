package com.example.data

import com.example.base.FirestoreEmulatorTestBase
import com.example.model.Listing
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.UUID

class MarketplaceRepositoryRuleTest : FirestoreEmulatorTestBase() {

    @Test
    fun createListing_authenticatedUser_returnsSuccessId() = runBlocking {
        signInTestUser(ALICE_EMAIL)
        val repository = MarketplaceRepository(firestore)

        val uniqueTitle = "Kailali Bike " + UUID.randomUUID().toString().take(6)
        val listing = Listing(
            sellerName = "Alice Sharma",
            title = uniqueTitle,
            description = "Well maintained bicycle in Dhangadhi Campus Chowk",
            priceNpr = 12500.0,
            category = "Vehicles and bicycles",
            condition = "Used - Good",
            city = "Dhangadhi",
            locality = "Campus Chowk",
            status = "active"
        )

        val result = withTimeout(DEFAULT_TIMEOUT_MS) { repository.createListing(listing) }
        assertTrue("Listing creation should succeed", result.isSuccess)
        val listingId = result.getOrThrow()
        assertTrue("Listing ID should not be blank", listingId.isNotBlank())
    }

    @Test
    fun observeListings_authenticatedUser_emitsActiveListings() = runBlocking {
        signInTestUser(ALICE_EMAIL)
        val repository = MarketplaceRepository(firestore)

        val uniqueTitle = "Phone " + UUID.randomUUID().toString().take(6)
        val listing = Listing(
            sellerName = "Alice Sharma",
            title = uniqueTitle,
            description = "Samsung Galaxy smartphone in good condition",
            priceNpr = 25000.0,
            category = "Mobile phones and electronics",
            condition = "Used - Like New",
            city = "Dhangadhi",
            status = "active"
        )
        val createResult = withTimeout(DEFAULT_TIMEOUT_MS) { repository.createListing(listing) }
        val listingId = createResult.getOrThrow()

        val emittedList = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.observeListings().first { list -> list.any { it.id == listingId } }
        }
        assertTrue(emittedList.any { it.id == listingId })
    }

    @Test
    fun updateListing_crossUser_failsWithPermissionDenied() = runBlocking {
        signInTestUser(ALICE_EMAIL)
        val aliceRepo = MarketplaceRepository(firestore)

        val uniqueTitle = "Alice Table " + UUID.randomUUID().toString().take(6)
        val listing = Listing(
            sellerName = "Alice Sharma",
            title = uniqueTitle,
            description = "Solid wooden study table",
            priceNpr = 7000.0,
            category = "Furniture",
            condition = "Used - Good",
            city = "Dhangadhi",
            status = "active"
        )
        val listingId = withTimeout(DEFAULT_TIMEOUT_MS) { aliceRepo.createListing(listing).getOrThrow() }

        // Bob tries to update Alice's listing
        signInTestUser(BOB_EMAIL)
        val bobRepo = MarketplaceRepository(firestore)
        val bobUpdate = bobRepo.updateListing(listing.copy(id = listingId, title = "Hacked Title"))
        assertTrue("Bob should fail to update Alice's listing", bobUpdate.isFailure)
    }

    @Test
    fun toggleFavorite_authenticatedUser_savesAndRemovesFavorite() = runBlocking {
        val aliceUid = signInTestUser(ALICE_EMAIL)
        val repository = MarketplaceRepository(firestore)
        val listingId = "listing_" + UUID.randomUUID().toString().take(8)

        val addResult = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.toggleFavorite(aliceUid, listingId, currentIsFav = false)
        }
        assertTrue("Adding favorite should succeed", addResult.isSuccess)

        val favs = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.observeFavoriteIds(aliceUid).first { it.contains(listingId) }
        }
        assertTrue("Favorite set should contain listingId", favs.contains(listingId))

        val removeResult = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.toggleFavorite(aliceUid, listingId, currentIsFav = true)
        }
        assertTrue("Removing favorite should succeed", removeResult.isSuccess)
    }

    @Test
    fun createListing_unauthenticatedUser_failsFast() = runBlocking {
        auth.signOut()
        val repository = MarketplaceRepository(firestore)
        val listing = Listing(
            sellerName = "Ghost",
            title = "Unauth item",
            description = "Should fail fast before hitting firestore",
            priceNpr = 100.0,
            category = "Other items",
            condition = "New",
            city = "Dhangadhi",
            status = "active"
        )

        val result = runCatching { repository.createListing(listing) }
        assertTrue("Unauthenticated listing creation must fail", result.isFailure || result.getOrNull()?.isFailure == true)
    }

    private companion object {
        const val ALICE_EMAIL = "alice_market@test.com"
        const val BOB_EMAIL = "bob_market@test.com"
        const val DEFAULT_TIMEOUT_MS = 5000L
    }
}
