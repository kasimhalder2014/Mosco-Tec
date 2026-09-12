package com.example.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.util.TouchSoundKit
import com.example.ui.util.sanitizeInputText

@Composable
fun SecurityLockDialog(
    shopName: String = "",
    fpsCode: String = "",
    isBiometricAvailable: Boolean = true,
    onUnlockWithPin: (String) -> Boolean,
    onUnlockWithBiometric: () -> Unit
) {
    var pinText by remember { mutableStateOf("") }
    var isPinVisible by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current
    val context = LocalContext.current

    val skyBlue = Color(0xFF0288D1)

    Dialog(
        onDismissRequest = { /* Cannot dismiss without unlock */ },
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = false
        )
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            color = Color.Transparent
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 440.dp)
                        .testTag("security_login_card"),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .padding(24.dp)
                            .fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Top Badge
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = skyBlue,
                            modifier = Modifier.size(60.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Default.Lock,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }

                        Spacer(Modifier.height(16.dp))

                        Text(
                            text = "Fair Price Shop Login",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )

                        Spacer(Modifier.height(4.dp))

                        // Display Shop info if available
                        if (shopName.isNotBlank() || fpsCode.isNotBlank()) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                                modifier = Modifier.padding(vertical = 4.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Storefront,
                                        contentDescription = null,
                                        tint = skyBlue,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = "${shopName.ifBlank { "FPS Centre" }} ${if (fpsCode.isNotBlank()) "($fpsCode)" else ""}",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        Spacer(Modifier.height(6.dp))

                        Text(
                            text = "Enter your Security PIN or use Biometric to unlock",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )

                        Spacer(Modifier.height(20.dp))

                        // PIN Input Field
                        OutlinedTextField(
                            value = pinText,
                            onValueChange = { input ->
                                if (input.length <= 6 && input.all { char -> char.isDigit() }) {
                                    pinText = input
                                    errorMessage = ""
                                }
                            },
                            label = { Text("Enter 4 to 6-digit PIN") },
                            placeholder = { Text("e.g. 1234") },
                            leadingIcon = {
                                Icon(Icons.Default.Lock, contentDescription = null, tint = skyBlue)
                            },
                            trailingIcon = {
                                IconButton(onClick = { 
                                    TouchSoundKit.playPop(context)
                                    isPinVisible = !isPinVisible 
                                }) {
                                    Icon(
                                        if (isPinVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                        contentDescription = if (isPinVisible) "Hide PIN" else "Show PIN"
                                    )
                                }
                            },
                            singleLine = true,
                            visualTransformation = if (isPinVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(
                                autoCorrectEnabled = false,
                                keyboardType = KeyboardType.NumberPassword,
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = {
                                    focusManager.clearFocus()
                                    if (pinText.isEmpty()) {
                                        TouchSoundKit.playDelete(context)
                                        errorMessage = "Please enter your PIN"
                                    } else if (!onUnlockWithPin(pinText)) {
                                        TouchSoundKit.playDelete(context)
                                        errorMessage = "Incorrect PIN. (Default is 1234)"
                                    } else {
                                        TouchSoundKit.playSuccess(context)
                                    }
                                }
                            ),
                            isError = errorMessage.isNotEmpty(),
                            supportingText = {
                                if (errorMessage.isNotEmpty()) {
                                    Text(errorMessage, color = MaterialTheme.colorScheme.error)
                                } else {
                                    Text(
                                        "Default PIN is 1234. Change anytime in Profile",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("login_pin_input")
                        )

                        Spacer(Modifier.height(20.dp))

                        // Login / Unlock Button
                        Button(
                            onClick = {
                                focusManager.clearFocus()
                                if (pinText.isEmpty()) {
                                    TouchSoundKit.playDelete(context)
                                    errorMessage = "Please enter your PIN (Default is 1234)"
                                } else if (!onUnlockWithPin(pinText)) {
                                    TouchSoundKit.playDelete(context)
                                    errorMessage = "Incorrect PIN. (Default is 1234)"
                                } else {
                                    TouchSoundKit.playSuccess(context)
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("login_submit_button"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = skyBlue)
                        ) {
                            Text("Login to Register", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        }

                        // Biometric Login Button
                        if (isBiometricAvailable) {
                            Spacer(Modifier.height(10.dp))
                            OutlinedButton(
                                onClick = {
                                    TouchSoundKit.playPop(context)
                                    onUnlockWithBiometric()
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("login_biometric_button"),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(
                                    Icons.Default.Fingerprint,
                                    contentDescription = null,
                                    tint = skyBlue,
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    "Verify with Biometric / Fingerprint",
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

