package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.CardWithDistribution
import com.example.data.ShopProfile
import com.example.ui.components.BarcodeScannerModal
import com.example.ui.components.ExcelExportDialog
import com.example.ui.components.PdfExportDialog
import com.example.ui.util.rememberInputSanitizer

enum class ReportFilterTab(val label: String) {
    SERVED("Served"),
    UNSERVED("Unserved"),
    ALL_CARD("All Card")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportScreen(
    profile: ShopProfile = ShopProfile(),
    monthYear: String,
    monthFormatted: String,
    allCards: List<CardWithDistribution>,
    onToggleDistribution: (cardNo: String, currentServed: Boolean) -> Unit,
    onOpenPrintPreview: () -> Unit,
    onExportCsv: () -> Unit = {}
) {
    var selectedTab by remember { mutableStateOf(ReportFilterTab.ALL_CARD) }
    var searchQuery by remember { mutableStateOf("") }
    var showBarcodeScanner by remember { mutableStateOf(false) }
    val searchSanitizer = rememberInputSanitizer()

    // Confirmation dialog state
    var cardToToggle by remember { mutableStateOf<CardWithDistribution?>(null) }
    
    // PDF and Excel Dialog states
    var showPdfExportDialog by remember { mutableStateOf(false) }
    var showExcelExportDialog by remember { mutableStateOf(false) }

    // Aggregate statistics
    val totalFamilies = allCards.size
    val totalMembers = allCards.sumOf { it.numberOfMembers }

    val categoryFamilyBreakdown = remember(allCards) {
        val standardCats = listOf("PHH", "SPHH", "AAY", "RKSY1", "RKSY2")
        val activeCats = (standardCats + allCards.map { it.category }).distinct()
        activeCats.mapNotNull { cat ->
            val count = allCards.count { it.category.equals(cat, ignoreCase = true) }
            if (count > 0) "$cat: $count" else null
        }.joinToString(" | ")
    }

    val categoryMembersBreakdown = remember(allCards) {
        val standardCats = listOf("PHH", "SPHH", "AAY", "RKSY1", "RKSY2")
        val activeCats = (standardCats + allCards.map { it.category }).distinct()
        activeCats.mapNotNull { cat ->
            val cardsInCat = allCards.filter { it.category.equals(cat, ignoreCase = true) }
            val memberCount = cardsInCat.sumOf { it.numberOfMembers }
            if (cardsInCat.isNotEmpty()) "$cat: $memberCount" else null
        }.joinToString(" | ")
    }

    val servedCards = remember(allCards) { allCards.filter { it.isServed } }
    val servedFamilies = servedCards.size
    val servedMembers = servedCards.sumOf { it.numberOfMembers }

    val unservedCards = remember(allCards) { allCards.filter { !it.isServed } }
    val unservedFamilies = unservedCards.size
    val unservedMembers = unservedCards.sumOf { it.numberOfMembers }

    val categoryServedBreakdown = remember(servedCards) {
        val standardCats = listOf("PHH", "SPHH", "AAY", "RKSY1", "RKSY2")
        val activeCats = (standardCats + servedCards.map { it.category }).distinct()
        activeCats.mapNotNull { cat ->
            val cardsInCat = servedCards.filter { it.category.equals(cat, ignoreCase = true) }
            val famCount = cardsInCat.size
            if (cardsInCat.isNotEmpty()) "$cat: $famCount" else null
        }.joinToString(" | ")
    }

    val categoryUnservedBreakdown = remember(unservedCards) {
        val standardCats = listOf("PHH", "SPHH", "AAY", "RKSY1", "RKSY2")
        val activeCats = (standardCats + unservedCards.map { it.category }).distinct()
        activeCats.mapNotNull { cat ->
            val cardsInCat = unservedCards.filter { it.category.equals(cat, ignoreCase = true) }
            val famCount = cardsInCat.size
            if (cardsInCat.isNotEmpty()) "$cat: $famCount" else null
        }.joinToString(" | ")
    }

    val tabFilteredCards = when (selectedTab) {
        ReportFilterTab.SERVED -> servedCards
        ReportFilterTab.UNSERVED -> unservedCards
        ReportFilterTab.ALL_CARD -> allCards
    }

    val displayedCards = remember(tabFilteredCards, searchQuery) {
        val query = searchQuery.trim()
        if (query.isEmpty()) {
            tabFilteredCards
        } else {
            tabFilteredCards.filter { card ->
                card.cardNo.contains(query, ignoreCase = true) ||
                card.headOfFamilyName.contains(query, ignoreCase = true) ||
                card.mobileNo.contains(query, ignoreCase = true)
            }
        }
    }

    // Confirmation Dialog for Served / Unserved action
    if (cardToToggle != null) {
        val targetCard = cardToToggle!!
        val isCurrentlyServed = targetCard.isServed

        AlertDialog(
            onDismissRequest = { cardToToggle = null },
            icon = {
                Icon(
                    imageVector = if (isCurrentlyServed) Icons.Default.Undo else Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = if (isCurrentlyServed) MaterialTheme.colorScheme.error else Color(0xFF15803D),
                    modifier = Modifier.size(32.dp)
                )
            },
            title = {
                Text(
                    text = if (isCurrentlyServed) "Do you want to unserved" else "Do you want to served",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Card No:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(
                                    targetCard.cardNo,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Head of Family:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(targetCard.headOfFamilyName, fontWeight = FontWeight.SemiBold)
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Members / Mobile:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("${targetCard.numberOfMembers} Members • ${targetCard.mobileNo.ifBlank { "N/A" }}")
                            }
                        }
                    }

                    Text(
                        text = if (isCurrentlyServed) {
                            "Pressing OK will mark this card as UNSERVED for $monthFormatted and update the report."
                        } else {
                            "Pressing OK will mark this card as SERVED with current timestamp for $monthFormatted and update the report."
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onToggleDistribution(targetCard.cardNo, isCurrentlyServed)
                        cardToToggle = null
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isCurrentlyServed) MaterialTheme.colorScheme.error else Color(0xFF15803D)
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("report_dialog_ok_button")
                ) {
                    Text("OK", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { cardToToggle = null },
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("report_dialog_no_button")
                ) {
                    Text("NO", fontWeight = FontWeight.Bold)
                }
            },
            shape = RoundedCornerShape(16.dp)
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Month & Quick Action Bar
        Surface(
            color = MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f, fill = false)) {
                    Text(
                        "Monthly Report",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "$monthFormatted ($monthYear)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    // 1. Print button
                    FilledTonalButton(
                        onClick = onOpenPrintPreview,
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        ),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                        modifier = Modifier.testTag("report_action_print")
                    ) {
                        Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.size(15.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Print", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    // 2. PDF button
                    FilledTonalButton(
                        onClick = { showPdfExportDialog = true },
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = Color(0xFFFEE2E2),
                            contentColor = Color(0xFFB91C1C)
                        ),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                        modifier = Modifier.testTag("report_action_pdf")
                    ) {
                        Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(15.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("PDF", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    // 3. Excel button
                    FilledTonalButton(
                        onClick = { showExcelExportDialog = true },
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = Color(0xFFDCFCE7),
                            contentColor = Color(0xFF15803D)
                        ),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                        modifier = Modifier.testTag("report_action_excel")
                    ) {
                        Icon(Icons.Default.TableChart, contentDescription = null, modifier = Modifier.size(15.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Excel", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // PDF Export Dialog with Details and Save to Device/Drive option
        if (showPdfExportDialog) {
            PdfExportDialog(
                profile = profile,
                monthYear = monthYear,
                cards = allCards,
                onDismiss = { showPdfExportDialog = false }
            )
        }

        // Excel Export Dialog with Details and Save to Device/Drive option
        if (showExcelExportDialog) {
            ExcelExportDialog(
                profile = profile,
                monthYear = monthYear,
                cards = allCards,
                onDismiss = { showExcelExportDialog = false }
            )
        }

        // Summary Metric Cards Grid
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Row 1: Total Head of Family & Total Members
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MetricSummaryCard(
                    title = "Total Head of Family",
                    count = "$totalFamilies Families",
                    sub = categoryFamilyBreakdown.ifBlank { "Total Registered Cards" },
                    color = MaterialTheme.colorScheme.primary,
                    bgColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                    modifier = Modifier.weight(1f)
                )
                MetricSummaryCard(
                    title = "All Members",
                    count = "$totalMembers Members",
                    sub = categoryMembersBreakdown.ifBlank { "Total Beneficiaries" },
                    color = Color(0xFF0284C7),
                    bgColor = Color(0xFFE0F2FE),
                    modifier = Modifier.weight(1f)
                )
            }

            // Row 2: Served vs Unserved Breakdown
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MetricSummaryCard(
                    title = "Served Families",
                    count = "$servedFamilies Families",
                    sub = categoryServedBreakdown.ifBlank { "$servedMembers Members Served" },
                    color = Color(0xFF15803D),
                    bgColor = Color(0xFFDCFCE7),
                    modifier = Modifier.weight(1f)
                )
                MetricSummaryCard(
                    title = "Unserved (Nondrawal)",
                    count = "$unservedFamilies Families",
                    sub = categoryUnservedBreakdown.ifBlank { "$unservedMembers Members Pending" },
                    color = Color(0xFFB91C1C),
                    bgColor = Color(0xFFFEE2E2),
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Filter Tabs
        PrimaryTabRow(
            selectedTabIndex = selectedTab.ordinal,
            containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp)
        ) {
            ReportFilterTab.entries.forEach { tab ->
                val count = when (tab) {
                    ReportFilterTab.SERVED -> servedFamilies
                    ReportFilterTab.UNSERVED -> unservedFamilies
                    ReportFilterTab.ALL_CARD -> totalFamilies
                }
                Tab(
                    selected = selectedTab == tab,
                    onClick = { selectedTab = tab },
                    text = {
                        Text(
                            "${tab.label} ($count)",
                            fontWeight = if (selectedTab == tab) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 13.sp
                        )
                    },
                    modifier = Modifier.testTag("report_tab_${tab.name.lowercase()}")
                )
            }
        }

        // Barcode Scanner Modal for Report Search
        if (showBarcodeScanner) {
            BarcodeScannerModal(
                onDismiss = { showBarcodeScanner = false },
                onBarcodeScanned = { scannedCode ->
                    showBarcodeScanner = false
                    searchQuery = scannedCode
                }
            )
        }

        // Search in Report
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Filter report by card no or name...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            trailingIcon = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(end = 4.dp)
                ) {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(
                            onClick = { searchQuery = "" },
                            modifier = Modifier.size(34.dp)
                        ) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear", modifier = Modifier.size(18.dp))
                        }
                    }
                    IconButton(
                        onClick = { showBarcodeScanner = true },
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("report_barcode_scan_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.QrCodeScanner,
                            contentDescription = "Scan Barcode with Camera",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            },
            keyboardOptions = KeyboardOptions(
                autoCorrectEnabled = false,
                keyboardType = KeyboardType.Phone
            ),
            singleLine = true,
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp)
                .testTag("report_search_input")
        )

        // List of Cards under Selected Filter
        if (displayedCards.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (searchQuery.isNotBlank()) "No cards match \"$searchQuery\" in ${selectedTab.label}"
                           else "No cards found under ${selectedTab.label}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                itemsIndexed(displayedCards, key = { _, item -> item.cardNo }) { index, item ->
                    ReportCardItem(
                        index = index + 1,
                        item = item,
                        onPressStatus = {
                            cardToToggle = item
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun MetricSummaryCard(
    title: String,
    count: String,
    sub: String,
    color: Color,
    bgColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = bgColor),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = title,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                text = count,
                fontSize = 15.sp,
                fontWeight = FontWeight.ExtraBold,
                color = color
            )
            Text(
                text = sub,
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ReportCardItem(
    index: Int,
    item: CardWithDistribution,
    onPressStatus: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Index Number
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(6.dp),
                modifier = Modifier.size(32.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        "$index",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(Modifier.width(12.dp))

            // Card details
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = item.headOfFamilyName,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = item.cardNo,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = MaterialTheme.colorScheme.tertiaryContainer,
                            modifier = Modifier.padding(end = 6.dp)
                        ) {
                            Text(
                                text = item.category.ifBlank { "PHH" },
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onTertiaryContainer,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                            )
                        }
                        Text(
                            text = "${item.numberOfMembers} Members • Mob: ${item.mobileNo.ifBlank { "N/A" }}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                if (item.isServed && item.servedDateTime.isNotBlank()) {
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = "Served on: ${item.servedDateTime}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF1E3A8A)
                    )
                }
            }

            Spacer(Modifier.width(10.dp))

            // Interactive Status Action Button / Badge
            if (item.isServed) {
                Surface(
                    onClick = onPressStatus,
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFDCFCE7),
                    border = BorderStroke(1.dp, Color(0xFF86EFAC)),
                    modifier = Modifier.testTag("report_status_served_${item.cardNo}")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = "Served",
                            tint = Color(0xFF15803D),
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = "SERVED",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF15803D)
                        )
                    }
                }
            } else {
                Surface(
                    onClick = onPressStatus,
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFFEE2E2),
                    border = BorderStroke(1.dp, Color(0xFFFCA5A5)),
                    modifier = Modifier.testTag("report_status_unserved_${item.cardNo}")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.RadioButtonUnchecked,
                            contentDescription = "Unserved",
                            tint = Color(0xFFB91C1C),
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = "UNSERVED",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFFB91C1C)
                        )
                    }
                }
            }
        }
    }
}
