package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.CardWithDistribution
import androidx.compose.foundation.clickable
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.ui.window.Dialog
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import com.example.ui.util.rememberInputSanitizer
import com.example.ui.util.sanitizeInputText
import com.example.ui.components.BarcodeScannerModal
import com.example.ui.util.TouchSoundKit
import androidx.compose.ui.platform.LocalContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DistributionScreen(
    monthYear: String,
    monthFormatted: String,
    cards: List<CardWithDistribution>,
    allCards: List<CardWithDistribution>,
    searchQuery: String,
    selectedFilter: String,
    onSearchChange: (String) -> Unit,
    onFilterChange: (String) -> Unit,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onMonthSelected: (String) -> Unit,
    onToggleDistribution: (cardNo: String, currentServed: Boolean) -> Unit,
    onUpdateMobileNo: (cardNo: String, mobileNo: String) -> Unit = { _, _ -> }
) {
    val totalCount = allCards.size
    val servedCount = allCards.count { it.isServed }
    val unservedCount = allCards.count { !it.isServed }

    var currentDateTime by remember { mutableStateOf(Date()) }
    LaunchedEffect(Unit) {
        while (true) {
            currentDateTime = Date()
            delay(1000L)
        }
    }
    val liveDateFormat = remember { 
        SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).apply { timeZone = TimeZone.getDefault() } 
    }
    val liveTimeFormat = remember { 
        SimpleDateFormat("hh:mm:ss a", Locale.getDefault()).apply { timeZone = TimeZone.getDefault() } 
    }
    val context = LocalContext.current

    var monthDropdownExpanded by remember { mutableStateOf(false) }
    var cardToUnserve by remember { mutableStateOf<CardWithDistribution?>(null) }
    var cardForMobileDialog by remember { mutableStateOf<CardWithDistribution?>(null) }
    var inputMobileNumber by remember { mutableStateOf("") }
    var mobileDialogError by remember { mutableStateOf("") }
    var showBarcodeScanner by remember { mutableStateOf(false) }
    val searchFocusRequester = remember { FocusRequester() }
    val searchSanitizer = rememberInputSanitizer()
    val dialogMobileSanitizer = rememberInputSanitizer()
    
    val monthOptions = remember {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.MONTH, -6) // go back 6 months
        val options = mutableListOf<Pair<String, String>>()
        val sdfVal = SimpleDateFormat("yyyy-MM", Locale.getDefault())
        val sdfDisp = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
        for (i in 0..18) { // next 18 months
            options.add(sdfVal.format(calendar.time) to sdfDisp.format(calendar.time))
            calendar.add(Calendar.MONTH, 1)
        }
        options
    }

    if (showBarcodeScanner) {
        BarcodeScannerModal(
            onDismiss = { showBarcodeScanner = false },
            onBarcodeScanned = { scannedCode ->
                TouchSoundKit.playPop(context)
                showBarcodeScanner = false
                onSearchChange(scannedCode)
                try {
                    searchFocusRequester.requestFocus()
                } catch (_: Exception) {}
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Top Header Labels with Current Date & Time on the right side
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f, fill = false)) {
                Text(
                    text = "Monthly Distribution",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(Modifier.height(3.dp))
                Text(
                    text = "Record monthly ration withdrawal",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(Modifier.width(10.dp))

            // Live Current Date & Time Badge (Right side of Monthly Distribution)
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)),
                modifier = Modifier.testTag("current_date_time_badge")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Schedule,
                                contentDescription = "Current Date and Time",
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = liveDateFormat.format(currentDateTime),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = liveTimeFormat.format(currentDateTime),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            fontFamily = FontFamily.Monospace,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }

        // Search & Month Selector Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "Select Month",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(8.dp))
                
                // Dropdown field
                Surface(
                    onClick = {
                        TouchSoundKit.playPop(context)
                        monthDropdownExpanded = true
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = monthFormatted,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowDown,
                            contentDescription = "Select month and year",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                if (monthDropdownExpanded) {
                    MonthYearPickerDialog(
                        initialMonthYear = monthYear,
                        onDismissRequest = { monthDropdownExpanded = false },
                        onDateSelected = {
                            monthDropdownExpanded = false
                            onMonthSelected(it)
                        }
                    )
                }
                
                Spacer(Modifier.height(16.dp))
                
                // Search Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(IntrinsicSize.Min),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = {
                            onSearchChange(it)
                        },
                        modifier = Modifier
                            .weight(1f)
                            .focusRequester(searchFocusRequester)
                            .testTag("distribution_search_box"),
                        placeholder = { 
                            Text(
                                "Search RC or Name",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            ) 
                        },
                        leadingIcon = {
                            Icon(Icons.Default.Search, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        },
                        trailingIcon = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(end = 4.dp)
                            ) {
                                if (searchQuery.isNotEmpty()) {
                                    IconButton(
                                        onClick = { 
                                            TouchSoundKit.playTap(context)
                                            onSearchChange("")
                                            try {
                                                searchFocusRequester.requestFocus()
                                            } catch (_: Exception) {}
                                        },
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Icon(Icons.Default.Clear, contentDescription = "Clear search", modifier = Modifier.size(18.dp))
                                    }
                                }
                                IconButton(
                                    onClick = {
                                        TouchSoundKit.playPop(context)
                                        showBarcodeScanner = true
                                    },
                                    modifier = Modifier
                                        .size(38.dp)
                                        .testTag("distribution_barcode_scan_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.QrCodeScanner,
                                        contentDescription = "Scan Barcode with Camera",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }
                        },
                        keyboardOptions = KeyboardOptions(
                            autoCorrectEnabled = false,
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Search
                        ),
                        keyboardActions = KeyboardActions(
                            onSearch = {
                                // Focus or dismiss handled gracefully
                            }
                        ),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                            focusedBorderColor = MaterialTheme.colorScheme.primary
                        )
                    )
                    
                    Spacer(Modifier.width(8.dp))
                    
                    Button(
                        onClick = { 
                            TouchSoundKit.playTap(context)
                            try {
                                searchFocusRequester.requestFocus()
                            } catch (_: Exception) {}
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                        modifier = Modifier
                            .fillMaxHeight()
                            .padding(vertical = 2.dp),
                        contentPadding = PaddingValues(horizontal = 18.dp)
                    ) {
                        Text("Search", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }
                }
            }
        }
        
        Spacer(Modifier.height(8.dp))
        if (cards.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                contentAlignment = Alignment.TopCenter
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth().padding(32.dp)
                    ) {
                        Icon(
                            Icons.Default.Search,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                        )
                        Spacer(Modifier.height(16.dp))
                        Text(
                            if (searchQuery.isNotEmpty()) "No cards found" else if (allCards.isEmpty()) "No cards in register" else "No cards found for this filter",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            if (searchQuery.isNotEmpty()) "No cards match \"$searchQuery\" in $monthFormatted." else if (allCards.isEmpty()) "Use the 'Create' tab to add ration cards to the register." else "Try clearing your search.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(cards, key = { it.cardNo }) { item ->
                    DistributionCardItem(
                        item = item,
                        onToggle = { 
                            if (item.isServed) {
                                // Open confirmation message box "do you want to unserved"
                                TouchSoundKit.playTap(context)
                                cardToUnserve = item
                            } else {
                                // Mark as Served directly with served sound!
                                TouchSoundKit.playServed(context)
                                onToggleDistribution(item.cardNo, false)
                                if (searchQuery.isNotEmpty()) {
                                    onSearchChange("")
                                }
                                try {
                                    searchFocusRequester.requestFocus()
                                } catch (_: Exception) {}
                            }
                        },
                        onAddOrEditMobile = { target ->
                            TouchSoundKit.playPop(context)
                            cardForMobileDialog = target
                            inputMobileNumber = target.mobileNo
                            mobileDialogError = ""
                        }
                    )
                }
            }
        }

        // Add / Update Mobile Number Dialog:
        if (cardForMobileDialog != null) {
            val targetCard = cardForMobileDialog!!
            AlertDialog(
                onDismissRequest = {
                    cardForMobileDialog = null
                    inputMobileNumber = ""
                    mobileDialogError = ""
                },
                icon = {
                    Icon(
                        imageVector = Icons.Default.PhoneAndroid,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(32.dp)
                    )
                },
                title = {
                    Text(
                        text = if (targetCard.mobileNo.isBlank()) "Add Mobile Number" else "Update Mobile Number",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                text = {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Card No:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(
                                        text = targetCard.cardNo,
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
                                    Text(
                                        text = targetCard.headOfFamilyName,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }

                        OutlinedTextField(
                            value = inputMobileNumber,
                            onValueChange = { input ->
                                if (input.length <= 10 && input.all { it.isDigit() }) {
                                    inputMobileNumber = input
                                    mobileDialogError = ""
                                }
                            },
                            label = { Text("Mobile No") },
                            placeholder = { Text("Enter 10-digit mobile number") },
                            leadingIcon = {
                                Icon(Icons.Default.Phone, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            },
                            isError = mobileDialogError.isNotEmpty(),
                            supportingText = {
                                if (mobileDialogError.isNotEmpty()) {
                                    Text(mobileDialogError, color = MaterialTheme.colorScheme.error)
                                } else {
                                    Text("${inputMobileNumber.length}/10 digits")
                                }
                            },
                            keyboardOptions = KeyboardOptions(
                                autoCorrectEnabled = false,
                                keyboardType = KeyboardType.Phone,
                                imeAction = ImeAction.Done
                            ),
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("dialog_mobile_number_input")
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (inputMobileNumber.isNotBlank() && inputMobileNumber.length != 10) {
                                mobileDialogError = "Invalid Mobile Number"
                                return@Button
                            }
                            TouchSoundKit.playSuccess(context)
                            onUpdateMobileNo(targetCard.cardNo, inputMobileNumber)
                            cardForMobileDialog = null
                            inputMobileNumber = ""
                            mobileDialogError = ""
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("dialog_save_mobile_button")
                    ) {
                        Text("SAVE", fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    OutlinedButton(
                        onClick = {
                            TouchSoundKit.playTap(context)
                            cardForMobileDialog = null
                            inputMobileNumber = ""
                            mobileDialogError = ""
                        },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("dialog_cancel_mobile_button")
                    ) {
                        Text("CANCEL", fontWeight = FontWeight.Bold)
                    }
                },
                shape = RoundedCornerShape(16.dp)
            )
        }

        // Unserved Confirmation Dialog:
        if (cardToUnserve != null) {
            val targetCard = cardToUnserve!!
            AlertDialog(
                onDismissRequest = {
                    cardToUnserve = null
                    try {
                        searchFocusRequester.requestFocus()
                    } catch (_: Exception) {}
                },
                icon = {
                    Icon(
                        Icons.Default.HelpOutline,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(36.dp)
                    )
                },
                title = {
                    Text(
                        text = "do you want to unserved",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                text = {
                    Text(
                        text = "Do you want to mark ration card ${targetCard.cardNo} (${targetCard.headOfFamilyName}) as unserved for $monthFormatted?",
                        style = MaterialTheme.typography.bodyMedium
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            TouchSoundKit.playUnserved(context)
                            val cardNo = targetCard.cardNo
                            cardToUnserve = null
                            onToggleDistribution(cardNo, true)
                            if (searchQuery.isNotEmpty()) {
                                onSearchChange("")
                            }
                            try {
                                searchFocusRequester.requestFocus()
                            } catch (_: Exception) {}
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                        modifier = Modifier.testTag("unserved_dialog_ok_button")
                    ) {
                        Text("OK", fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    OutlinedButton(
                        onClick = {
                            TouchSoundKit.playTap(context)
                            cardToUnserve = null
                            try {
                                searchFocusRequester.requestFocus()
                            } catch (_: Exception) {}
                        },
                        modifier = Modifier.testTag("unserved_dialog_no_button")
                    ) {
                        Text("NO", fontWeight = FontWeight.Bold)
                    }
                }
            )
        }
    }
}

@Composable
fun DistributionCardItem(
    item: CardWithDistribution,
    onToggle: () -> Unit,
    onAddOrEditMobile: (CardWithDistribution) -> Unit = {}
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("distribution_card_${item.cardNo}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (item.isServed) MaterialTheme.colorScheme.surface
            else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Card Number Badge
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = item.cardNo,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }

                // Served / Unserved Status Badge
                if (item.isServed) {
                    Surface(
                        color = Color(0xFFDCFCE7),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF15803D), modifier = Modifier.size(14.dp))
                            Spacer(Modifier.width(4.dp))
                            Text(
                                "SERVED",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF15803D)
                            )
                        }
                    }
                } else {
                    Surface(
                        color = Color(0xFFFEE2E2),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Pending, contentDescription = null, tint = Color(0xFFB91C1C), modifier = Modifier.size(14.dp))
                            Spacer(Modifier.width(4.dp))
                            Text(
                                "UNSERVED",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFB91C1C)
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            // Head of Family Name
            Text(
                text = item.headOfFamilyName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(Modifier.height(6.dp))

            // Member count and mobile row
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
                            text = item.category,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onTertiaryContainer,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                    }
                    Icon(
                        Icons.Default.People,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = "${item.numberOfMembers} Members",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (item.mobileNo.isNotBlank()) {
                    Surface(
                        onClick = { onAddOrEditMobile(item) },
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.testTag("edit_mobile_${item.cardNo}")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Phone,
                                contentDescription = null,
                                modifier = Modifier.size(13.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                text = item.mobileNo,
                                style = MaterialTheme.typography.bodySmall,
                                fontFamily = FontFamily.Monospace,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(Modifier.width(4.dp))
                            Icon(
                                Icons.Default.Edit,
                                contentDescription = "Edit mobile",
                                modifier = Modifier.size(11.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    // Option "ADD" if mobile number not present of Head of the family
                    Surface(
                        onClick = { onAddOrEditMobile(item) },
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.8f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)),
                        modifier = Modifier.testTag("add_mobile_${item.cardNo}")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Add,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(Modifier.width(2.dp))
                            Icon(
                                Icons.Default.Phone,
                                contentDescription = null,
                                modifier = Modifier.size(12.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                text = "ADD",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }

            // If Served: Show Device Synced Date and Time (Dark Blue)
            AnimatedVisibility(visible = item.isServed) {
                Column(modifier = Modifier.padding(top = 8.dp)) {
                    Surface(
                        color = Color(0xFFDBEAFE).copy(alpha = 0.6f),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Schedule,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = Color(0xFF1E3A8A)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = "Served On: ${item.servedDateTime.ifBlank { "Synced with device" }}",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF1E3A8A)
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(10.dp))

            // Quick Mark Action Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                if (item.isServed) {
                    OutlinedButton(
                        onClick = onToggle,
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        ),
                        modifier = Modifier.testTag("unserve_button_${item.cardNo}")
                    ) {
                        Icon(Icons.Default.Undo, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Mark Unserved", fontSize = 12.sp)
                    }
                } else {
                    Button(
                        onClick = onToggle,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF16A34A)
                        ),
                        modifier = Modifier.testTag("serve_button_${item.cardNo}")
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Mark as Served", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun MonthYearPickerDialog(
    initialMonthYear: String, // "2026-09"
    onDismissRequest: () -> Unit,
    onDateSelected: (String) -> Unit
) {
    val months = remember {
        listOf(
            "01" to "Jan", "02" to "Feb", "03" to "Mar", "04" to "Apr",
            "05" to "May", "06" to "Jun", "07" to "Jul", "08" to "Aug",
            "09" to "Sep", "10" to "Oct", "11" to "Nov", "12" to "Dec"
        )
    }
    val years = remember {
        (2015..2035).map { it.toString() }
    }

    val initMonth = initialMonthYear.substringAfter("-", "01")
    val initYear = initialMonthYear.substringBefore("-", Calendar.getInstance().get(Calendar.YEAR).toString())

    var selectedMonthIndex by remember { 
        mutableStateOf(months.indexOfFirst { it.first == initMonth }.coerceAtLeast(0)) 
    }
    var selectedYearIndex by remember {
        mutableStateOf(years.indexOf(initYear).takeIf { it >= 0 } ?: (years.indexOf("2026").takeIf { it >= 0 } ?: 0))
    }

    Dialog(onDismissRequest = onDismissRequest) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                Text(
                    text = "Set month",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Normal,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth().height(150.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    WheelPicker(
                        items = months.map { it.second },
                        selectedIndex = selectedMonthIndex,
                        onItemSelected = { selectedMonthIndex = it },
                        modifier = Modifier.weight(1f)
                    )
                    
                    Spacer(Modifier.width(16.dp))
                    
                    WheelPicker(
                        items = years,
                        selectedIndex = selectedYearIndex,
                        onItemSelected = { selectedYearIndex = it },
                        modifier = Modifier.weight(1f)
                    )
                }
                
                Spacer(modifier = Modifier.height(28.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = {
                        val cal = Calendar.getInstance()
                        val m = String.format("%02d", cal.get(Calendar.MONTH) + 1)
                        val y = cal.get(Calendar.YEAR).toString()
                        selectedMonthIndex = months.indexOfFirst { it.first == m }.coerceAtLeast(0)
                        selectedYearIndex = years.indexOf(y).coerceAtLeast(0)
                        onDateSelected("$y-$m")
                    }) {
                        Text("Clear", fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    
                    Row {
                        TextButton(onClick = onDismissRequest) {
                            Text("Cancel", fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        TextButton(onClick = { 
                            val safeMonthIdx = selectedMonthIndex.coerceIn(0, months.lastIndex)
                            val safeYearIdx = selectedYearIndex.coerceIn(0, years.lastIndex)
                            val monthVal = months[safeMonthIdx].first
                            val yearVal = years[safeYearIdx]
                            onDateSelected("$yearVal-$monthVal")
                        }) {
                            Text("Set", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun WheelPicker(
    items: List<String>,
    selectedIndex: Int,
    onItemSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState(initialFirstVisibleItemIndex = selectedIndex.coerceIn(0, items.lastIndex))
    val flingBehavior = rememberSnapFlingBehavior(lazyListState = listState)
    val coroutineScope = rememberCoroutineScope()

    // Determine centered item as the wheel scrolls or snaps
    val centeredIndex by remember {
        derivedStateOf {
            val layoutInfo = listState.layoutInfo
            val visibleItems = layoutInfo.visibleItemsInfo
            if (visibleItems.isEmpty()) {
                selectedIndex
            } else {
                val viewportCenter = (layoutInfo.viewportStartOffset + layoutInfo.viewportEndOffset) / 2
                val closest = visibleItems.minByOrNull { item ->
                    Math.abs((item.offset + item.size / 2) - viewportCenter)
                }
                closest?.index?.coerceIn(0, items.lastIndex) ?: selectedIndex
            }
        }
    }

    // Keep parent selection synchronized when user scrolls or snaps
    LaunchedEffect(centeredIndex) {
        if (centeredIndex != selectedIndex && centeredIndex in items.indices) {
            onItemSelected(centeredIndex)
        }
    }

    Box(modifier = modifier.height(150.dp), contentAlignment = Alignment.Center) {
        LazyColumn(
            state = listState,
            flingBehavior = flingBehavior,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(vertical = 50.dp)
        ) {
            itemsIndexed(items) { index, item ->
                val isSelected = index == centeredIndex
                Box(
                    modifier = Modifier
                        .height(50.dp)
                        .fillMaxWidth()
                        .clickable { 
                            onItemSelected(index)
                            coroutineScope.launch {
                                listState.animateScrollToItem(index)
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = item,
                        style = MaterialTheme.typography.titleLarge,
                        fontSize = if (isSelected) 22.sp else 18.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f)
                    )
                }
            }
        }
        
        HorizontalDivider(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = 50.dp)
                .width(100.dp), 
            thickness = 2.dp, 
            color = MaterialTheme.colorScheme.outline
        )
        HorizontalDivider(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .offset(y = (-50).dp)
                .width(100.dp), 
            thickness = 2.dp, 
            color = MaterialTheme.colorScheme.outline
        )
    }
}
