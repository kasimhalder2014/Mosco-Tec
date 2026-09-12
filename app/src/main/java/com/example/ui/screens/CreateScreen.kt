package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.RationCard
import com.example.ui.components.BarcodeScannerModal
import com.example.ui.util.TouchSoundKit
import kotlinx.coroutines.launch

enum class CreateSubTab(val title: String) {
    ADD("Add"),
    EDIT("Edit"),
    DELETE("Delete")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateScreen(
    onAddCard: (cardNo: String, name: String, category: String, members: Int, mobile: String, onResult: (Boolean, String) -> Unit) -> Unit,
    onSearchCard: suspend (query: String) -> RationCard?,
    onUpdateCard: (cardNo: String, name: String, category: String, members: Int, mobile: String, onResult: (Boolean, String) -> Unit) -> Unit,
    onDeleteCard: (cardNo: String, onResult: (Boolean, String) -> Unit) -> Unit
) {
    var currentSubTab by remember { mutableStateOf(CreateSubTab.ADD) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Sub-tab Navigation (Add, Edit, Delete)
            PrimaryTabRow(
                selectedTabIndex = currentSubTab.ordinal,
                containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp)
            ) {
                CreateSubTab.entries.forEach { tab ->
                    Tab(
                        selected = currentSubTab == tab,
                        onClick = { currentSubTab = tab },
                        text = {
                            Text(
                                tab.title,
                                fontWeight = if (currentSubTab == tab) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        icon = {
                            when (tab) {
                                CreateSubTab.ADD -> Icon(Icons.Default.PersonAdd, contentDescription = null)
                                CreateSubTab.EDIT -> Icon(Icons.Default.Edit, contentDescription = null)
                                CreateSubTab.DELETE -> Icon(Icons.Default.Delete, contentDescription = null)
                            }
                        },
                        modifier = Modifier.testTag("tab_create_${tab.name.lowercase()}")
                    )
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                when (currentSubTab) {
                    CreateSubTab.ADD -> {
                        AddCardSection(
                            onSave = { cardNo, name, category, members, mobile ->
                                onAddCard(cardNo, name, category, members, mobile) { success, msg ->
                                    scope.launch { snackbarHostState.showSnackbar(msg) }
                                }
                            }
                        )
                    }
                    CreateSubTab.EDIT -> {
                        EditCardSection(
                            onSearch = onSearchCard,
                            onUpdate = { cardNo, name, category, members, mobile ->
                                onUpdateCard(cardNo, name, category, members, mobile) { success, msg ->
                                    scope.launch { snackbarHostState.showSnackbar(msg) }
                                }
                            }
                        )
                    }
                    CreateSubTab.DELETE -> {
                        DeleteCardSection(
                            onSearch = onSearchCard,
                            onDelete = { cardNo ->
                                onDeleteCard(cardNo) { success, msg ->
                                    scope.launch { snackbarHostState.showSnackbar(msg) }
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// 1. ADD SECTION: Name Head of family, Card No, Number of Members, Mobile No then save
// -------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCardSection(
    onSave: (cardNo: String, name: String, category: String, members: Int, mobile: String) -> Unit
) {
    var headOfFamilyName by remember { mutableStateOf("") }
    var cardNo by remember { mutableStateOf("") }
    
    val categories = listOf("Select", "PHH", "SPHH", "AAY", "RKSY1", "RKSY2")
    var category by remember { mutableStateOf(categories[0]) }
    var categoryDropdownExpanded by remember { mutableStateOf(false) }

    var numberOfMembersText by remember { mutableStateOf("") }
    var mobileNo by remember { mutableStateOf("") }
    var showBarcodeScanner by remember { mutableStateOf(false) }
    val context = LocalContext.current

    var formError by remember { mutableStateOf("") }

    if (showBarcodeScanner) {
        BarcodeScannerModal(
            onDismiss = { showBarcodeScanner = false },
            onBarcodeScanned = { scannedCode ->
                TouchSoundKit.playPop(context)
                showBarcodeScanner = false
                cardNo = scannedCode.trim().uppercase()
                formError = ""
            }
        )
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.size(40.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.PersonAdd, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    }
                }
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(
                        "Add New Ration Card",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "Register family under PDS Fair Price Shop",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            HorizontalDivider()

            // Name Head of the family
            OutlinedTextField(
                value = headOfFamilyName,
                onValueChange = {
                    headOfFamilyName = it
                    formError = ""
                },
                label = { Text("Name Head of the family *") },
                placeholder = { Text("e.g. Ramesh Chandra Mondal") },
                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    autoCorrectEnabled = false,
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Next
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("add_head_name_input")
            )

            // Card No with Barcode Scan Button
            OutlinedTextField(
                value = cardNo,
                onValueChange = {
                    cardNo = it
                    formError = ""
                },
                label = { Text("Card No *") },
                placeholder = { Text("e.g. 10048921") },
                leadingIcon = { Icon(Icons.Default.Badge, contentDescription = null) },
                trailingIcon = {
                    IconButton(
                        onClick = { 
                            TouchSoundKit.playPop(context)
                            showBarcodeScanner = true 
                        },
                        modifier = Modifier.testTag("add_card_scan_button")
                    ) {
                        Icon(
                            Icons.Default.QrCodeScanner,
                            contentDescription = "Scan Card Barcode",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    autoCorrectEnabled = false,
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Next
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("add_card_no_input")
            )

            // Category Dropdown
            ExposedDropdownMenuBox(
                expanded = categoryDropdownExpanded,
                onExpandedChange = { categoryDropdownExpanded = it }
            ) {
                OutlinedTextField(
                    value = category,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Category *") },
                    leadingIcon = { Icon(Icons.Default.Category, contentDescription = null) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryDropdownExpanded) },
                    colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                        .testTag("add_category_input")
                )
                ExposedDropdownMenu(
                    expanded = categoryDropdownExpanded,
                    onDismissRequest = { categoryDropdownExpanded = false }
                ) {
                    categories.forEach { selectionOption ->
                        DropdownMenuItem(
                            text = { Text(selectionOption) },
                            onClick = {
                                category = selectionOption
                                categoryDropdownExpanded = false
                            }
                        )
                    }
                }
            }

            // Number of Members
            OutlinedTextField(
                value = numberOfMembersText,
                onValueChange = { input ->
                    if (input.all { it.isDigit() }) {
                        numberOfMembersText = input
                    }
                },
                label = { Text("Number of Members *") },
                placeholder = { Text("e.g. 4") },
                leadingIcon = { Icon(Icons.Default.Groups, contentDescription = null) },
                keyboardOptions = KeyboardOptions(
                    autoCorrectEnabled = false,
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Next
                ),
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("add_members_input")
            )

            // Mobile No
            OutlinedTextField(
                value = mobileNo,
                onValueChange = { input ->
                    if (input.length <= 10 && input.all { it.isDigit() }) {
                        mobileNo = input
                    }
                },
                label = { Text("Mobile No") },
                placeholder = { Text("e.g. 9876543210") },
                leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                keyboardOptions = KeyboardOptions(
                    autoCorrectEnabled = false,
                    keyboardType = KeyboardType.Phone,
                    imeAction = ImeAction.Done
                ),
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("add_mobile_input")
            )

            if (formError.isNotEmpty()) {
                Text(
                    text = formError,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(Modifier.height(8.dp))

            // Save Button
            Button(
                onClick = {
                    if (headOfFamilyName.isBlank()) {
                        TouchSoundKit.playDelete(context)
                        formError = "Please enter Head of family name"
                        return@Button
                    }
                    val sanitizedCardNo = cardNo.trim().uppercase()
                    if (sanitizedCardNo.isBlank()) {
                        TouchSoundKit.playDelete(context)
                        formError = "Please enter Ration Card number"
                        return@Button
                    }
                    if (category == "Select" || category.isBlank()) {
                        TouchSoundKit.playDelete(context)
                        formError = "Please select a Category"
                        return@Button
                    }
                    if (numberOfMembersText.isBlank()) {
                        TouchSoundKit.playDelete(context)
                        formError = "Please enter Number of Members"
                        return@Button
                    }
                    val members = numberOfMembersText.trim().toIntOrNull() ?: 0
                    if (members <= 0) {
                        TouchSoundKit.playDelete(context)
                        formError = "Members count must be at least 1"
                        return@Button
                    }
                    val trimmedMobile = mobileNo.trim()
                    if (trimmedMobile.isNotBlank() && trimmedMobile.length != 10) {
                        TouchSoundKit.playDelete(context)
                        formError = "Invalid Mobile Number"
                        return@Button
                    }

                    TouchSoundKit.playSuccess(context)
                    onSave(sanitizedCardNo, headOfFamilyName.trim(), category, members, mobileNo.trim())
                    // Clear inputs for next entry
                    headOfFamilyName = ""
                    cardNo = ""
                    category = categories[0]
                    numberOfMembersText = ""
                    mobileNo = ""
                    formError = ""
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("add_save_button"),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Save, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Save Ration Card", fontWeight = FontWeight.Bold)
            }
        }
    }
}

// -------------------------------------------------------------
// 2. EDIT SECTION: Search box Card no or name, search then show Details and edit and save
// -------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditCardSection(
    onSearch: suspend (String) -> RationCard?,
    onUpdate: (cardNo: String, name: String, category: String, members: Int, mobile: String) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var searchedCard by remember { mutableStateOf<RationCard?>(null) }
    var hasSearched by remember { mutableStateOf(false) }
    var isSearching by remember { mutableStateOf(false) }
    var showBarcodeScanner by remember { mutableStateOf(false) }
    val searchFocusRequester = remember { FocusRequester() }
    val context = LocalContext.current

    var editName by remember { mutableStateOf("") }
    
    val categories = listOf("PHH", "SPHH", "AAY", "RKSY1", "RKSY2")
    var editCategory by remember { mutableStateOf(categories[0]) }
    var editCategoryDropdownExpanded by remember { mutableStateOf(false) }
    
    var editMembers by remember { mutableStateOf("") }
    var editMobile by remember { mutableStateOf("") }
    var editError by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    if (showBarcodeScanner) {
        BarcodeScannerModal(
            onDismiss = { showBarcodeScanner = false },
            onBarcodeScanned = { scannedCode ->
                TouchSoundKit.playPop(context)
                showBarcodeScanner = false
                searchQuery = scannedCode.trim()
                scope.launch {
                    isSearching = true
                    val result = onSearch(scannedCode.trim())
                    searchedCard = result
                    hasSearched = true
                    isSearching = false
                    if (result != null) {
                        editName = result.headOfFamilyName
                        editCategory = if (categories.contains(result.category)) result.category else categories[0]
                        editMembers = result.numberOfMembers.toString()
                        editMobile = result.mobileNo
                        editError = ""
                    }
                }
            }
        )
    }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        // Search Box
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "Search Card to Edit",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = {
                            searchQuery = it
                        },
                        placeholder = { Text("Card no or name") },
                        keyboardOptions = KeyboardOptions(
                            autoCorrectEnabled = false,
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Search
                        ),
                        keyboardActions = KeyboardActions(
                            onSearch = {
                                if (searchQuery.isNotBlank() && !isSearching) {
                                    TouchSoundKit.playKeypad(context)
                                    scope.launch {
                                        isSearching = true
                                        val result = onSearch(searchQuery.trim())
                                        searchedCard = result
                                        hasSearched = true
                                        isSearching = false
                                        if (result != null) {
                                            editName = result.headOfFamilyName
                                            editCategory = if (categories.contains(result.category)) result.category else categories[0]
                                            editMembers = result.numberOfMembers.toString()
                                            editMobile = result.mobileNo
                                            editError = ""
                                        }
                                    }
                                }
                            }
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .focusRequester(searchFocusRequester)
                            .testTag("edit_search_input"),
                        singleLine = true,
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        trailingIcon = {
                            IconButton(
                                onClick = { 
                                    TouchSoundKit.playPop(context)
                                    showBarcodeScanner = true 
                                },
                                modifier = Modifier.testTag("edit_barcode_scan_button")
                            ) {
                                Icon(
                                    Icons.Default.QrCodeScanner,
                                    contentDescription = "Scan Barcode with Camera",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    )
                    Spacer(Modifier.width(8.dp))
                    Button(
                        onClick = {
                            TouchSoundKit.playKeypad(context)
                            scope.launch {
                                isSearching = true
                                val result = onSearch(searchQuery.trim())
                                searchedCard = result
                                hasSearched = true
                                isSearching = false
                                if (result != null) {
                                    editName = result.headOfFamilyName
                                    editCategory = if (categories.contains(result.category)) result.category else categories[0]
                                    editMembers = result.numberOfMembers.toString()
                                    editMobile = result.mobileNo
                                    editError = ""
                                }
                            }
                        },
                        enabled = searchQuery.isNotBlank() && !isSearching,
                        modifier = Modifier.testTag("edit_search_button")
                    ) {
                        Text("Search")
                    }
                }
            }
        }

        // Search Results & Edit Form
        if (hasSearched) {
            if (searchedCard == null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                ) {
                    Text(
                        "No ration card found matching \"$searchQuery\". Please check the card number or family name.",
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            } else {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Card Details",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Surface(
                                color = MaterialTheme.colorScheme.primaryContainer,
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    searchedCard!!.cardNo,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }

                        HorizontalDivider()

                        OutlinedTextField(
                            value = editName,
                            onValueChange = {
                                editName = it
                                editError = ""
                            },
                            label = { Text("Head of the family name *") },
                            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                            keyboardOptions = KeyboardOptions(
                                autoCorrectEnabled = false,
                                keyboardType = KeyboardType.Text
                            ),
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("edit_head_name_input")
                        )

                        // Category Dropdown
                        ExposedDropdownMenuBox(
                            expanded = editCategoryDropdownExpanded,
                            onExpandedChange = { editCategoryDropdownExpanded = it }
                        ) {
                            OutlinedTextField(
                                value = editCategory,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Category *") },
                                leadingIcon = { Icon(Icons.Default.Category, contentDescription = null) },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = editCategoryDropdownExpanded) },
                                colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor()
                                    .testTag("edit_category_input")
                            )
                            ExposedDropdownMenu(
                                expanded = editCategoryDropdownExpanded,
                                onDismissRequest = { editCategoryDropdownExpanded = false }
                            ) {
                                categories.forEach { selectionOption ->
                                    DropdownMenuItem(
                                        text = { Text(selectionOption) },
                                        onClick = {
                                            editCategory = selectionOption
                                            editCategoryDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        OutlinedTextField(
                            value = editMembers,
                            onValueChange = { input ->
                                if (input.all { it.isDigit() }) {
                                    editMembers = input
                                }
                            },
                            label = { Text("Number of Members *") },
                            leadingIcon = { Icon(Icons.Default.Groups, contentDescription = null) },
                            keyboardOptions = KeyboardOptions(
                                autoCorrectEnabled = false,
                                keyboardType = KeyboardType.Number
                            ),
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("edit_members_input")
                        )

                        OutlinedTextField(
                            value = editMobile,
                            onValueChange = { input ->
                                if (input.length <= 10 && input.all { it.isDigit() }) {
                                    editMobile = input
                                }
                            },
                            label = { Text("Mobile No") },
                            leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                            keyboardOptions = KeyboardOptions(
                                autoCorrectEnabled = false,
                                keyboardType = KeyboardType.Phone
                            ),
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("edit_mobile_input")
                        )

                        if (editError.isNotEmpty()) {
                            Text(
                                editError,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }

                        Spacer(Modifier.height(4.dp))

                        Button(
                            onClick = {
                                if (editName.isBlank()) {
                                    TouchSoundKit.playDelete(context)
                                    editError = "Name cannot be empty"
                                    return@Button
                                }
                                if (editCategory == "Select" || editCategory.isBlank()) {
                                    TouchSoundKit.playDelete(context)
                                    editError = "Please select a Category"
                                    return@Button
                                }
                                if (editMembers.isBlank()) {
                                    TouchSoundKit.playDelete(context)
                                    editError = "Please enter Number of Members"
                                    return@Button
                                }
                                val members = editMembers.trim().toIntOrNull() ?: 0
                                if (members <= 0) {
                                    TouchSoundKit.playDelete(context)
                                    editError = "Members count must be at least 1"
                                    return@Button
                                }
                                val trimmedEditMobile = editMobile.trim()
                                if (trimmedEditMobile.isNotBlank() && trimmedEditMobile.length != 10) {
                                    TouchSoundKit.playDelete(context)
                                    editError = "Invalid Mobile Number"
                                    return@Button
                                }
                                TouchSoundKit.playSuccess(context)
                                onUpdate(searchedCard!!.cardNo, editName.trim(), editCategory, members, editMobile.trim())
                                
                                // Reset form and return to search box
                                hasSearched = false
                                searchedCard = null
                                searchQuery = ""
                                editName = ""
                                editCategory = categories[0]
                                editMembers = ""
                                editMobile = ""
                                editError = ""
                                try {
                                    searchFocusRequester.requestFocus()
                                } catch (_: Exception) {}
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("edit_save_button"),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Save, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("Save and Update Details", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// 3. DELETE SECTION: Search box search by card or name then show card details delete option
// if press delete open message box " Do you want to delete" ok or no
// If press ok then delete
// -------------------------------------------------------------
@Composable
fun DeleteCardSection(
    onSearch: suspend (String) -> RationCard?,
    onDelete: (cardNo: String) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var searchedCard by remember { mutableStateOf<RationCard?>(null) }
    var hasSearched by remember { mutableStateOf(false) }
    var isSearching by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var showBarcodeScanner by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    if (showBarcodeScanner) {
        BarcodeScannerModal(
            onDismiss = { showBarcodeScanner = false },
            onBarcodeScanned = { scannedCode ->
                TouchSoundKit.playPop(context)
                showBarcodeScanner = false
                searchQuery = scannedCode.trim()
                scope.launch {
                    isSearching = true
                    val result = onSearch(scannedCode.trim())
                    searchedCard = result
                    hasSearched = true
                    isSearching = false
                }
            }
        )
    }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        // Search Box
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "Search Card to Delete",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = {
                            searchQuery = it
                        },
                        placeholder = { Text("Card no or name") },
                        keyboardOptions = KeyboardOptions(
                            autoCorrectEnabled = false,
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Search
                        ),
                        keyboardActions = KeyboardActions(
                            onSearch = {
                                if (searchQuery.isNotBlank() && !isSearching) {
                                    TouchSoundKit.playKeypad(context)
                                    scope.launch {
                                        isSearching = true
                                        val result = onSearch(searchQuery.trim())
                                        searchedCard = result
                                        hasSearched = true
                                        isSearching = false
                                    }
                                }
                            }
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("delete_search_input"),
                        singleLine = true,
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        trailingIcon = {
                            IconButton(
                                onClick = { 
                                    TouchSoundKit.playPop(context)
                                    showBarcodeScanner = true 
                                },
                                modifier = Modifier.testTag("delete_barcode_scan_button")
                            ) {
                                Icon(
                                    Icons.Default.QrCodeScanner,
                                    contentDescription = "Scan Barcode with Camera",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    )
                    Spacer(Modifier.width(8.dp))
                    Button(
                        onClick = {
                            TouchSoundKit.playKeypad(context)
                            scope.launch {
                                isSearching = true
                                val result = onSearch(searchQuery.trim())
                                searchedCard = result
                                hasSearched = true
                                isSearching = false
                            }
                        },
                        enabled = searchQuery.isNotBlank() && !isSearching,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                        modifier = Modifier.testTag("delete_search_button")
                    ) {
                        Text("Search")
                    }
                }
            }
        }

        // Details & Delete Option
        if (hasSearched) {
            if (searchedCard == null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                ) {
                    Text(
                        "No ration card found matching \"$searchQuery\".",
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            } else {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            "Card Details Found",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )

                        HorizontalDivider()

                        CardDetailRow("Ration Card No:", searchedCard!!.cardNo, isMonospace = true)
                        CardDetailRow("Head of Family:", searchedCard!!.headOfFamilyName)
                        CardDetailRow("No. of Members:", "${searchedCard!!.numberOfMembers} Members")
                        CardDetailRow("Mobile Number:", searchedCard!!.mobileNo.ifBlank { "Not provided" })

                        Spacer(Modifier.height(10.dp))

                        // Delete Option Button
                        Button(
                            onClick = {
                                showDeleteConfirmDialog = true
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("delete_option_button"),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.DeleteForever, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("Delete Card from Register", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    // MANDATORY REQUIREMENT:
    // "if press delete open message box ' Do you want to delete' ok or no
    // If press ok then delete"
    if (showDeleteConfirmDialog && searchedCard != null) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            icon = {
                Icon(
                    Icons.Default.Warning,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(36.dp)
                )
            },
            title = {
                Text(
                    text = "Do you want to delete",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleLarge
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to permanently delete ration card ${searchedCard!!.cardNo} (${searchedCard!!.headOfFamilyName}) from the smart Register?",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val cardToDelete = searchedCard!!.cardNo
                        showDeleteConfirmDialog = false
                        searchedCard = null
                        hasSearched = false
                        searchQuery = ""
                        onDelete(cardToDelete)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier.testTag("dialog_ok_button")
                ) {
                    Text("OK", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showDeleteConfirmDialog = false },
                    modifier = Modifier.testTag("dialog_no_button")
                ) {
                    Text("NO", fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}

@Composable
private fun CardDetailRow(label: String, value: String, isMonospace: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            value,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold,
            fontFamily = if (isMonospace) FontFamily.Monospace else FontFamily.Default,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
