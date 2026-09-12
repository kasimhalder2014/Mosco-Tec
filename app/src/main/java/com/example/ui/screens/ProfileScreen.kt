package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ShopProfile
import com.example.ui.util.TouchSoundKit
import com.example.ui.util.sanitizeInputText
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    currentProfile: ShopProfile,
    onSaveProfile: (
        shopName: String,
        proprietorName: String,
        fpsCode: String,
        licenceNo: String,
        area: String,
        shopAddress: String,
        securityPin: String,
        isPinEnabled: Boolean,
        isBiometricEnabled: Boolean,
        onSuccess: () -> Unit
    ) -> Unit
) {
    var shopName by remember(currentProfile) { mutableStateOf(currentProfile.shopName) }
    var proprietorName by remember(currentProfile) { mutableStateOf(currentProfile.proprietorName) }
    var fpsCode by remember(currentProfile) { mutableStateOf(currentProfile.fpsCode) }
    var licenceNo by remember(currentProfile) { mutableStateOf(currentProfile.licenceNo) }
    var area by remember(currentProfile) { mutableStateOf(currentProfile.area) }
    var shopAddress by remember(currentProfile) { mutableStateOf(currentProfile.shopAddress) }
    var securityPin by remember(currentProfile) { mutableStateOf(currentProfile.securityPin) }
    var isPinEnabled by remember(currentProfile) { mutableStateOf(currentProfile.isPinEnabled) }
    var isBiometricEnabled by remember(currentProfile) { mutableStateOf(currentProfile.isBiometricEnabled) }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Profile Card Header Banner
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = androidx.compose.ui.graphics.Color(0xFF0288D1)),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = androidx.compose.ui.graphics.Color(0xFF0277BD),
                        modifier = Modifier.size(52.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Default.Storefront,
                                contentDescription = null,
                                tint = androidx.compose.ui.graphics.Color.White,
                                modifier = Modifier.size(30.dp)
                            )
                        }
                    }

                    Spacer(Modifier.width(14.dp))

                    Column {
                        Text(
                            text = shopName.ifBlank { "Fair Price Shop Profile" },
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = androidx.compose.ui.graphics.Color.White
                        )
                        Text(
                            text = "FPS Code: ${fpsCode.ifBlank { "Unset" }} • Lic: ${licenceNo.ifBlank { "Unset" }}",
                            style = MaterialTheme.typography.bodySmall,
                            color = androidx.compose.ui.graphics.Color(0xFFE1F5FE)
                        )
                    }
                }
            }

            // Shop Information Section
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
                    Text(
                        "Shop & Dealer Information",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    HorizontalDivider()

                    // Shop Name
                    OutlinedTextField(
                        value = shopName,
                        onValueChange = { shopName = it },
                        label = { Text("Shop Name *") },
                        placeholder = { Text("e.g. M/S Maa Durga PDS Centre") },
                        leadingIcon = { Icon(Icons.Default.Store, contentDescription = null) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            autoCorrectEnabled = false,
                            keyboardType = KeyboardType.Text
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("profile_shop_name_input")
                    )

                    // Proprietor/Secretary Name
                    OutlinedTextField(
                        value = proprietorName,
                        onValueChange = { proprietorName = it },
                        label = { Text("Proprietor / Secretary Name *") },
                        placeholder = { Text("e.g. Ramesh Kumar Sharma") },
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            autoCorrectEnabled = false,
                            keyboardType = KeyboardType.Text
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("profile_proprietor_name_input")
                    )

                    // FPS Code
                    OutlinedTextField(
                        value = fpsCode,
                        onValueChange = { fpsCode = it },
                        label = { Text("FPS Code *") },
                        placeholder = { Text("e.g. FPS-WB-743201") },
                        leadingIcon = { Icon(Icons.Default.Numbers, contentDescription = null) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            autoCorrectEnabled = false,
                            keyboardType = KeyboardType.Ascii
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("profile_fps_code_input")
                    )

                    // Licence No
                    OutlinedTextField(
                        value = licenceNo,
                        onValueChange = { licenceNo = it },
                        label = { Text("Licence No *") },
                        placeholder = { Text("e.g. LIC/PDS/2024/0981") },
                        leadingIcon = { Icon(Icons.Default.VerifiedUser, contentDescription = null) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            autoCorrectEnabled = false,
                            keyboardType = KeyboardType.Ascii
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("profile_licence_no_input")
                    )

                    // Area
                    OutlinedTextField(
                        value = area,
                        onValueChange = { area = it },
                        label = { Text("Area *") },
                        placeholder = { Text("e.g. Ward No 12, Rural Sector") },
                        leadingIcon = { Icon(Icons.Default.Map, contentDescription = null) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            autoCorrectEnabled = false,
                            keyboardType = KeyboardType.Text
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("profile_area_input")
                    )

                    // Shop Address
                    OutlinedTextField(
                        value = shopAddress,
                        onValueChange = { shopAddress = it },
                        label = { Text("Shop Address *") },
                        placeholder = { Text("e.g. Station Road, Near Post Office") },
                        leadingIcon = { Icon(Icons.Default.Home, contentDescription = null) },
                        minLines = 2,
                        maxLines = 3,
                        keyboardOptions = KeyboardOptions(
                            autoCorrectEnabled = false,
                            keyboardType = KeyboardType.Text
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("profile_address_input")
                    )
                }
            }

            // Security Options (PIN, Biometric)
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
                    Text(
                        "Security Options",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    HorizontalDivider()

                    // PIN Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("App Lock", fontWeight = FontWeight.SemiBold)
                            Text(
                                "Require App Lock to open register",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = isPinEnabled,
                            onCheckedChange = { 
                                TouchSoundKit.playPop(context)
                                isPinEnabled = it 
                            },
                            modifier = Modifier.testTag("profile_pin_switch")
                        )
                    }

                    if (isPinEnabled) {
                        OutlinedTextField(
                            value = securityPin,
                            onValueChange = { input ->
                                if (input.length <= 6 && input.all { char -> char.isDigit() }) {
                                    securityPin = input
                                }
                            },
                            label = { Text("Set 4 to 6 Digit PIN *") },
                            placeholder = { Text("e.g. 1234") },
                            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                            keyboardOptions = KeyboardOptions(
                                autoCorrectEnabled = false,
                                keyboardType = KeyboardType.NumberPassword
                            ),
                            visualTransformation = PasswordVisualTransformation(),
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("profile_pin_input")
                        )
                    }

                    Spacer(Modifier.height(4.dp))

                    // Biometric Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Biometric Authentication", fontWeight = FontWeight.SemiBold)
                            Text(
                                "Allow fingerprint or face unlock",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = isBiometricEnabled,
                            onCheckedChange = { 
                                TouchSoundKit.playPop(context)
                                isBiometricEnabled = it 
                            },
                            modifier = Modifier.testTag("profile_biometric_switch")
                        )
                    }
                }
            }

            // Save Button
            Button(
                onClick = {
                    TouchSoundKit.playSuccess(context)
                    onSaveProfile(
                        shopName.trim(),
                        proprietorName.trim(),
                        fpsCode.trim().uppercase(),
                        licenceNo.trim().uppercase(),
                        area.trim(),
                        shopAddress.trim(),
                        securityPin.trim(),
                        isPinEnabled,
                        isBiometricEnabled
                    ) {
                        scope.launch {
                            snackbarHostState.showSnackbar("Shop Profile & Security settings saved successfully!")
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("profile_save_button"),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Save, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Save Profile", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}
