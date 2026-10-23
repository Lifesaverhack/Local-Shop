package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Chair
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Kitchen
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Checkroom
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.NavyPrimary
import java.text.NumberFormat
import java.util.Locale

fun formatNpr(amount: Double): String {
    val formatter = NumberFormat.getNumberInstance(Locale.US)
    return "Rs. " + formatter.format(amount.toLong())
}

@Composable
fun ConditionBadge(condition: String, modifier: Modifier = Modifier) {
    val (bgColor, textColor) = when (condition) {
        "New" -> Color(0xFFDCFCE7) to Color(0xFF166534)
        "Used - Like New" -> Color(0xFFE0E7FF) to Color(0xFF3730A3)
        "Used - Good" -> Color(0xFFFEF3C7) to Color(0xFF92400E)
        else -> Color(0xFFF1F5F9) to Color(0xFF475569)
    }

    Surface(
        modifier = modifier,
        color = bgColor,
        shape = RoundedCornerShape(6.dp)
    ) {
        Text(
            text = condition,
            color = textColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}

@Composable
fun StatusBadge(status: String, modifier: Modifier = Modifier) {
    val isSold = status.equals("sold", ignoreCase = true)
    val bgColor = if (isSold) Color(0xFFF1F5F9) else Color(0xFFDCFCE7)
    val textColor = if (isSold) Color(0xFF64748B) else Color(0xFF15803D)
    val text = if (isSold) "SOLD" else "AVAILABLE"

    Surface(
        modifier = modifier,
        color = bgColor,
        shape = RoundedCornerShape(6.dp)
    ) {
        Text(
            text = text,
            color = textColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
        )
    }
}

fun getCategoryIcon(category: String): ImageVector {
    return when (category) {
        "Mobile phones and electronics" -> Icons.Default.PhoneAndroid
        "Vehicles and bicycles" -> Icons.Default.DirectionsCar
        "Furniture" -> Icons.Default.Chair
        "Home appliances" -> Icons.Default.Kitchen
        "Clothing and accessories" -> Icons.Default.Checkroom
        "Books and education" -> Icons.Default.MenuBook
        "Construction materials" -> Icons.Default.Build
        else -> Icons.Default.MoreHoriz
    }
}

val ALL_CATEGORIES = listOf(
    "All",
    "Mobile phones and electronics",
    "Vehicles and bicycles",
    "Furniture",
    "Home appliances",
    "Clothing and accessories",
    "Books and education",
    "Construction materials",
    "Other items"
)

val NEPAL_CITIES = listOf(
    "Dhangadhi",
    "Attariya",
    "Tikapur",
    "Mahendranagar",
    "Nepalgunj",
    "Kohalpur",
    "Kathmandu",
    "Pokhara",
    "Butwal"
)

val DHANGADHI_LOCALITIES = listOf(
    "Campus Chowk",
    "Chauraha",
    "Hasanpur",
    "Boradandi",
    "Trinagar",
    "Milan Chowk",
    "Traffic Chowk",
    "Santoshi Tole",
    "Kailali Gaun",
    "Main Market"
)

val CONDITIONS_LIST = listOf(
    "New",
    "Used - Like New",
    "Used - Good",
    "Used - Fair"
)

@Composable
fun EmptyStateView(
    title: String,
    subtitle: String,
    icon: ImageVector,
    actionText: String? = null,
    onAction: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(
            modifier = Modifier.size(80.dp),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.surfaceVariant
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(40.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        if (actionText != null && onAction != null) {
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onAction,
                colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary)
            ) {
                Text(actionText)
            }
        }
    }
}

@Composable
fun ReportDialog(
    listingTitle: String,
    onDismiss: () -> Unit,
    onSubmit: (reason: String, details: String?) -> Unit
) {
    var selectedReason by remember { mutableStateOf("Inappropriate item") }
    var details by remember { mutableStateOf("") }
    val reasons = listOf(
        "Inappropriate item",
        "Suspected scam or fake seller",
        "Duplicate or spam listing",
        "Item already sold",
        "Incorrect price or description",
        "Other violation"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Report Listing", fontWeight = FontWeight.Bold) },
        text = {
            Column {
                Text(
                    text = "Help keep LocalBazaar Nepal safe. Why are you reporting \"$listingTitle\"?",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(12.dp))
                reasons.forEach { reason ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedReason = reason }
                            .padding(vertical = 4.dp)
                    ) {
                        RadioButton(
                            selected = (selectedReason == reason),
                            onClick = { selectedReason = reason }
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = reason, style = MaterialTheme.typography.bodyMedium)
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = details,
                    onValueChange = { details = it },
                    label = { Text("Additional details (optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSubmit(selectedReason, details.ifBlank { null }) },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Text("Submit Report")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
