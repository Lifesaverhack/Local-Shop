package com.example.ui.sell

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.ui.MarketplaceViewModel
import com.example.ui.components.ALL_CATEGORIES
import com.example.ui.components.CONDITIONS_LIST
import com.example.ui.components.DHANGADHI_LOCALITIES
import com.example.ui.components.NEPAL_CITIES
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.NavyPrimary

// Curated high quality demo product images for immediate marketplace testing
val SAMPLE_PHOTO_PRESETS = listOf(
    "https://images.unsplash.com/photo-1511707171634-5f897ff02aa9?w=600&q=80" to "Smartphone",
    "https://images.unsplash.com/photo-1485965120184-e220f721d03e?w=600&q=80" to "Bicycle",
    "https://images.unsplash.com/photo-1555041469-a586c61ea9bc?w=600&q=80" to "Sofa / Furniture",
    "https://images.unsplash.com/photo-1505740420928-5e560c06d30e?w=600&q=80" to "Headphones",
    "https://images.unsplash.com/photo-1544716278-ca5e3f4abd8c?w=600&q=80" to "Books / Education",
    "https://images.unsplash.com/photo-1584269600464-37b1b58a9fe7?w=600&q=80" to "Home Appliance"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SellScreen(
    viewModel: MarketplaceViewModel,
    onListingCreated: (String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var priceText by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Mobile phones and electronics") }
    var selectedCondition by remember { mutableStateOf("Used - Good") }
    var selectedCity by remember { mutableStateOf("Dhangadhi") }
    var locality by remember { mutableStateOf("Campus Chowk") }
    var photoUrlInput by remember { mutableStateOf("") }
    var photosList by remember { mutableStateOf(listOf("https://images.unsplash.com/photo-1511707171634-5f897ff02aa9?w=600&q=80")) }

    var isSubmitting by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    var categoryExpanded by remember { mutableStateOf(false) }
    var cityExpanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("List an Item for Sale", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
                .testTag("sell_screen"),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = EmeraldGreen)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "100% Free local listing in Dhangadhi & Kailali. No fees.",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                        color = NavyPrimary
                    )
                }
            }

            // Title
            OutlinedTextField(
                value = title,
                onValueChange = { if (it.length <= 100) title = it },
                label = { Text("Product Title *") },
                placeholder = { Text("e.g. iPhone 13 128GB Blue") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("sell_title_input"),
                singleLine = true,
                supportingText = { Text("${title.length}/100") }
            )

            // Price in NPR
            OutlinedTextField(
                value = priceText,
                onValueChange = { priceText = it.filter { char -> char.isDigit() } },
                label = { Text("Price in Nepalese Rupees (NPR) *") },
                placeholder = { Text("e.g. 45000") },
                leadingIcon = { Text("Rs.", fontWeight = FontWeight.Bold, color = EmeraldGreen, modifier = Modifier.padding(start = 12.dp)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("sell_price_input"),
                singleLine = true
            )

            // Category Dropdown
            Box(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = selectedCategory,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Category *") },
                    trailingIcon = {
                        IconButton(onClick = { categoryExpanded = true }) {
                            Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = null)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { categoryExpanded = true }
                )
                DropdownMenu(
                    expanded = categoryExpanded,
                    onDismissRequest = { categoryExpanded = false },
                    modifier = Modifier.fillMaxWidth(0.9f)
                ) {
                    ALL_CATEGORIES.filter { it != "All" }.forEach { cat ->
                        DropdownMenuItem(
                            text = { Text(cat) },
                            onClick = {
                                selectedCategory = cat
                                categoryExpanded = false
                            }
                        )
                    }
                }
            }

            // Condition Chips
            Column {
                Text(
                    text = "Item Condition *",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold)
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CONDITIONS_LIST.forEach { cond ->
                        val isSelected = selectedCondition == cond
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedCondition = cond },
                            label = { Text(cond) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = NavyPrimary,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }
            }

            // Location Section (City & Locality)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // City Dropdown
                Box(modifier = Modifier.weight(1f)) {
                    OutlinedTextField(
                        value = selectedCity,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("City *") },
                        trailingIcon = {
                            IconButton(onClick = { cityExpanded = true }) {
                                Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = null)
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { cityExpanded = true }
                    )
                    DropdownMenu(
                        expanded = cityExpanded,
                        onDismissRequest = { cityExpanded = false }
                    ) {
                        NEPAL_CITIES.forEach { city ->
                            DropdownMenuItem(
                                text = { Text(city) },
                                onClick = {
                                    selectedCity = city
                                    cityExpanded = false
                                }
                            )
                        }
                    }
                }

                // Locality input
                OutlinedTextField(
                    value = locality,
                    onValueChange = { locality = it },
                    label = { Text("Locality / Ward") },
                    placeholder = { Text("e.g. Hasanpur") },
                    modifier = Modifier.weight(1.2f),
                    singleLine = true
                )
            }

            // Description
            OutlinedTextField(
                value = description,
                onValueChange = { if (it.length <= 1500) description = it },
                label = { Text("Description *") },
                placeholder = { Text("Provide details: reason for selling, age of item, warranty status, accessories included, etc.") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
                    .testTag("sell_description_input"),
                supportingText = { Text("${description.length}/1500") }
            )

            // Photos Section
            Column {
                Text(
                    text = "Product Photos",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold)
                )
                Text(
                    text = "Add clear photos of your item to attract serious local buyers.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Photo Presets quick selector
                Text(
                    text = "Quick photo presets:",
                    style = MaterialTheme.typography.labelSmall,
                    color = GoldAccent
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SAMPLE_PHOTO_PRESETS.forEach { (url, label) ->
                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    if (!photosList.contains(url)) {
                                        photosList = photosList + url
                                    }
                                },
                            color = MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Text(
                                text = "+ $label",
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Custom URL input
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = photoUrlInput,
                        onValueChange = { photoUrlInput = it },
                        placeholder = { Text("Paste image URL (https://...)") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (photoUrlInput.isNotBlank()) {
                                photosList = photosList + photoUrlInput.trim()
                                photoUrlInput = ""
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary)
                    ) {
                        Text("Add")
                    }
                }

                // Selected Photos preview row
                if (photosList.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        photosList.forEachIndexed { index, url ->
                            Box(
                                modifier = Modifier
                                    .size(72.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                AsyncImage(
                                    model = url,
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                                IconButton(
                                    onClick = {
                                        photosList = photosList.filterIndexed { i, _ -> i != index }
                                    },
                                    modifier = Modifier
                                        .size(24.dp)
                                        .align(Alignment.TopEnd)
                                        .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(bottomStart = 8.dp))
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Remove photo",
                                        tint = Color.White,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            errorMessage?.let { err ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                ) {
                    Text(
                        text = err,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            // Publish Button
            Button(
                onClick = {
                    val price = priceText.toDoubleOrNull()
                    if (title.isBlank() || title.length < 3) {
                        errorMessage = "Please enter a descriptive title (at least 3 characters)"
                        return@Button
                    }
                    if (price == null || price <= 0) {
                        errorMessage = "Please enter a valid price in NPR"
                        return@Button
                    }
                    if (description.isBlank() || description.length < 5) {
                        errorMessage = "Please enter a description (at least 5 characters)"
                        return@Button
                    }

                    errorMessage = null
                    isSubmitting = true

                    viewModel.createListing(
                        title = title.trim(),
                        description = description.trim(),
                        priceNpr = price,
                        category = selectedCategory,
                        condition = selectedCondition,
                        city = selectedCity,
                        locality = locality.trim().ifBlank { null },
                        photos = photosList,
                        onSuccess = { newId ->
                            isSubmitting = false
                            onListingCreated(newId)
                        }
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("publish_listing_button"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
                enabled = !isSubmitting
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                } else {
                    Text(
                        text = "Publish Listing in $selectedCity",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}
