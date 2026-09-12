package com.example.ui.components

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.CardWithDistribution
import com.example.data.ShopProfile
import com.example.print.PrintManagerHelper
import com.example.print.PrintOption
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun A4PrintPreviewDialog(
    profile: ShopProfile,
    monthYear: String,
    cards: List<CardWithDistribution>,
    onDismiss: () -> Unit,
    onDirectPrint: (PrintOption) -> Unit
) {
    var selectedOption by remember { mutableStateOf(PrintOption.ALL_SERVED_AND_UNSERVED) }
    val context = LocalContext.current
    val currentDeviceTime = remember {
        SimpleDateFormat("dd MMM yyyy, hh:mm:ss a", Locale.getDefault()).format(Date())
    }

    val filteredCards = remember(selectedOption, cards) {
        when (selectedOption) {
            PrintOption.ALL_SERVED -> cards.filter { it.isServed }
            PrintOption.ALL_UNSERVED -> cards.filter { !it.isServed }
            PrintOption.ALL_SERVED_AND_UNSERVED -> cards
        }
    }

    val totalFamilies = filteredCards.size
    val totalMembers = filteredCards.sumOf { it.numberOfMembers }
    val servedCount = filteredCards.count { it.isServed }
    val unservedCount = filteredCards.count { !it.isServed }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Scaffold(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp, vertical = 16.dp),
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                "A4 Print & PDF Preview",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                "Format: ISO A4 Sheet • 1st Page Shop Details",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, contentDescription = "Close preview")
                        }
                    },
                    actions = {
                        Button(
                            onClick = { onDirectPrint(selectedOption) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            ),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                        ) {
                            Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Print / PDF", fontWeight = FontWeight.SemiBold)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(Color(0xFFE2E8F0)) // Desk paper backdrop
            ) {
                // Print Option Selector Filter
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            "Select Print Option:",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            PrintOption.entries.forEach { option ->
                                FilterChip(
                                    selected = selectedOption == option,
                                    onClick = { selectedOption = option },
                                    label = {
                                        Text(
                                            option.label,
                                            fontSize = 11.sp,
                                            maxLines = 1
                                        )
                                    },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }

                // Scrollable Simulated A4 White Sheet
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(12.dp),
                    contentAlignment = Alignment.TopCenter
                ) {
                    // A4 Paper Representation
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(8.dp, RoundedCornerShape(2.dp))
                            .background(Color.White, RoundedCornerShape(2.dp))
                            .border(1.dp, Color(0xFFCBD5E1), RoundedCornerShape(2.dp))
                            .padding(16.dp)
                    ) {
                        // Header Banner
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    "PDS DISTRIBUTION & NONDRAWAL REGISTER",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFF1E3A8A),
                                    letterSpacing = 0.5.sp
                                )
                                Text(
                                    "Monthly Ration Card Entry • ${selectedOption.label}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFF475569)
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    "Month: $monthYear",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF0F172A)
                                )
                                Text(
                                    "Device Sync: $currentDeviceTime",
                                    fontSize = 10.sp,
                                    color = Color(0xFF64748B)
                                )
                                Text(
                                    "FPS: ${profile.fpsCode}",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF1E3A8A)
                                )
                            }
                        }

                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 10.dp),
                            thickness = 2.dp,
                            color = Color(0xFF1E3A8A)
                        )

                        // Metric summary mini-boxes
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            SummaryA4Box("Total Families", "$totalFamilies", Color(0xFF1E3A8A), Modifier.weight(1f))
                            SummaryA4Box("Total Members", "$totalMembers", Color(0xFF0284C7), Modifier.weight(1f))
                            SummaryA4Box("Served", "$servedCount", Color(0xFF16A34A), Modifier.weight(1f))
                            SummaryA4Box("Unserved", "$unservedCount", Color(0xFFDC2626), Modifier.weight(1f))
                        }

                        Spacer(Modifier.height(12.dp))

                        // Table Header
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFF1E3A8A))
                                .padding(vertical = 6.dp, horizontal = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Sl", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(22.dp), textAlign = TextAlign.Center)
                            Text("Card No", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1.2f))
                            Text("Cat", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(36.dp), textAlign = TextAlign.Center)
                            Text("Head of Family", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1.6f))
                            Text("Mbr", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(28.dp), textAlign = TextAlign.Center)
                            Text("Mobile", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1.2f))
                            Text("Status", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1.1f), textAlign = TextAlign.Center)
                            Text("Served Date & Time", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1.7f))
                        }

                        // Table Rows
                        if (filteredCards.isEmpty()) {
                            Text(
                                "No records found for ${selectedOption.label}",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 24.dp),
                                textAlign = TextAlign.Center,
                                color = Color.Gray,
                                fontSize = 12.sp
                            )
                        } else {
                            filteredCards.forEachIndexed { index, card ->
                                val rowBg = if (index % 2 == 0) Color(0xFFF8FAFC) else Color.White
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(rowBg)
                                        .border(0.5.dp, Color(0xFFE2E8F0))
                                        .padding(vertical = 5.dp, horizontal = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("${index + 1}", fontSize = 10.sp, modifier = Modifier.width(22.dp), textAlign = TextAlign.Center)
                                    Text(card.cardNo, fontSize = 10.sp, fontWeight = FontWeight.SemiBold, fontFamily = FontFamily.Monospace, modifier = Modifier.weight(1.2f))
                                    Text(card.category.ifBlank { "PHH" }, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E3A8A), modifier = Modifier.width(36.dp), textAlign = TextAlign.Center)
                                    Text(card.headOfFamilyName, fontSize = 10.sp, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1.6f))
                                    Text("${card.numberOfMembers}", fontSize = 10.sp, modifier = Modifier.width(28.dp), textAlign = TextAlign.Center)
                                    Text(card.mobileNo.ifBlank { "—" }, fontSize = 9.sp, fontFamily = FontFamily.Monospace, modifier = Modifier.weight(1.2f))

                                    // Status
                                    Box(
                                        modifier = Modifier
                                            .weight(1.2f)
                                            .padding(horizontal = 2.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = if (card.isServed) "SERVED" else "UNSERVED",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (card.isServed) Color(0xFF15803D) else Color(0xFFB91C1C),
                                            modifier = Modifier
                                                .background(
                                                    if (card.isServed) Color(0xFFDCFCE7) else Color(0xFFFEE2E2),
                                                    RoundedCornerShape(3.dp)
                                                )
                                                .padding(horizontal = 4.dp, vertical = 2.dp)
                                        )
                                    }

                                    // Served Date & Time
                                    val servedTime = if (card.isServed && card.servedDateTime.isNotBlank()) {
                                        card.servedDateTime
                                    } else if (card.isServed) {
                                        currentDeviceTime
                                    } else {
                                        "—"
                                    }
                                    Text(
                                        servedTime,
                                        fontSize = 9.sp,
                                        fontWeight = if (card.isServed) FontWeight.SemiBold else FontWeight.Normal,
                                        color = if (card.isServed) Color(0xFF1E3A8A) else Color(0xFF64748B),
                                        modifier = Modifier.weight(1.8f)
                                    )
                                }
                            }
                        }

                        Spacer(Modifier.height(20.dp))

                        // CRITICAL REQUIREMENT: Only 1st page bottom side Show shop details
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFFF0FDF4), RoundedCornerShape(4.dp))
                                .border(1.5.dp, Color(0xFF1E3A8A), RoundedCornerShape(4.dp))
                                .padding(12.dp)
                        ) {
                            Text(
                                "★ FAIR PRICE SHOP (FPS) DETAILS (Page 1 Registered Details)",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF166534)
                            )
                            HorizontalDivider(
                                modifier = Modifier.padding(vertical = 6.dp),
                                thickness = 1.dp,
                                color = Color(0xFF86EFAC)
                            )

                            Row(modifier = Modifier.fillMaxWidth()) {
                                Column(modifier = Modifier.weight(1f)) {
                                    ShopDetailText("Shop Name", profile.shopName)
                                    ShopDetailText("Proprietor / Secretary", profile.proprietorName)
                                    ShopDetailText("FPS Code", profile.fpsCode)
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    ShopDetailText("Licence No", profile.licenceNo)
                                    ShopDetailText("Area / Sector", profile.area)
                                    ShopDetailText("Shop Address", profile.shopAddress)
                                }
                            }

                            Spacer(Modifier.height(14.dp))

                            // Dealer & Officer Signatures
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Box(
                                        modifier = Modifier
                                            .width(150.dp)
                                            .height(1.dp)
                                            .background(Color(0xFF0F172A))
                                    )
                                    Spacer(Modifier.height(4.dp))
                                    Text(
                                        "Signature / Seal of FPS Dealer",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF0F172A)
                                    )
                                }
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Box(
                                        modifier = Modifier
                                            .width(150.dp)
                                            .height(1.dp)
                                            .background(Color(0xFF0F172A))
                                    )
                                    Spacer(Modifier.height(4.dp))
                                    Text(
                                        "Food Inspector / Sub-Divisional Officer",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF0F172A)
                                    )
                                }
                            }
                        }

                        Spacer(Modifier.height(12.dp))
                        Text(
                            "— Smart Register PDS Distribution • A4 Document Preview —",
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center,
                            fontSize = 10.sp,
                            color = Color(0xFF94A3B8)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SummaryA4Box(label: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .background(Color(0xFFF8FAFC), RoundedCornerShape(3.dp))
            .border(1.dp, Color(0xFFCBD5E1), RoundedCornerShape(3.dp))
            .padding(vertical = 4.dp, horizontal = 2.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(value, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = color)
            Text(label, fontSize = 9.sp, color = Color(0xFF64748B), textAlign = TextAlign.Center)
        }
    }
}

@Composable
private fun ShopDetailText(label: String, value: String) {
    Row(modifier = Modifier.padding(vertical = 1.dp)) {
        Text("$label: ", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
        Text(value.ifBlank { "—" }, fontSize = 10.sp, color = Color(0xFF334155))
    }
}
