package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.material3.ripple
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.MainNavigationTab
import com.example.data.ShopProfile
import com.example.ui.util.TouchSoundKit

private val SkyBlueBg = Color(0xFF0288D1)
private val SkyBlueIconBg = Color(0xFF0277BD)
private val SkyBlueSubtext = Color(0xFFE1F5FE)

private val LightGreenNavBg = Color(0xFFE8F5E9)
private val LightGreenNavBorder = Color(0xFFC8E6C9)
private val GreenNavActive = Color(0xFF1B5E20)
private val GreenNavActiveContainer = Color(0xFFC8E6C9)
private val GreenNavInactive = Color(0xFF388E3C)

@Composable
fun TopBarWith3Dot(
    profile: ShopProfile,
    currentMonthFormatted: String,
    currentTab: MainNavigationTab,
    onTabSelected: (MainNavigationTab) -> Unit,
    onOpenExportImport: () -> Unit,
    onOpenPrintPreview: () -> Unit,
    onOpenUserGuide: () -> Unit,
    onOpenAbout: () -> Unit,
    onLockApp: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    val context = LocalContext.current

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding(),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp,
        shadowElevation = 3.dp
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            // 1. Profile Details Header at the top (Sky Blue Background Section)
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = SkyBlueBg
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.weight(1f, fill = false)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = SkyBlueIconBg,
                            modifier = Modifier.size(38.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Default.Storefront,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }

                        Column {
                            Text(
                                text = profile.shopName.ifBlank { "Smart Register" },
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            val details = buildList {
                                if (profile.fpsCode.isNotBlank()) add("FPS: ${profile.fpsCode}")
                                if (profile.proprietorName.isNotBlank()) add(profile.proprietorName)
                                add(currentMonthFormatted)
                            }.joinToString(" • ")
                            Text(
                                text = details,
                                style = MaterialTheme.typography.labelSmall,
                                color = SkyBlueSubtext,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    Spacer(Modifier.width(8.dp))

                    // Top-right Logout Button
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color.White.copy(alpha = 0.22f),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.5f)),
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable {
                                TouchSoundKit.playTap(context)
                                onLockApp()
                            }
                            .testTag("top_bar_logout_button")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.Logout,
                                contentDescription = "Logout",
                                tint = Color.White,
                                modifier = Modifier.size(15.dp)
                            )
                            Text(
                                text = "Logout",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            HorizontalDivider(
                color = LightGreenNavBorder,
                thickness = 1.dp
            )

            // 2. Navigation Row below Profile Details: PROFILE | DISTRIBUTION | CREATE | REPORTS | 3-DOT (Light Green Fill)
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = LightGreenNavBg
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    MainNavigationTab.entries.forEach { tab ->
                        val isSelected = currentTab == tab
                        val activeColor = GreenNavActive
                        val inactiveColor = GreenNavInactive.copy(alpha = 0.8f)
                        val iconColor by animateColorAsState(if (isSelected) activeColor else inactiveColor, label = "iconColor")
                        val textColor by animateColorAsState(if (isSelected) activeColor else inactiveColor, label = "textColor")

                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) GreenNavActiveContainer else Color.Transparent,
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 2.dp)
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable(
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = ripple(bounded = true, radius = 28.dp),
                                        onClick = {
                                            TouchSoundKit.playTap(context)
                                            onTabSelected(tab)
                                        }
                                    )
                                    .padding(vertical = 6.dp, horizontal = 2.dp)
                                    .testTag(tab.testTag)
                            ) {
                                Icon(
                                    imageVector = if (isSelected) tab.filledIcon else tab.outlinedIcon,
                                    contentDescription = tab.title,
                                    tint = iconColor,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.height(3.dp))
                                Text(
                                    text = tab.title,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = textColor,
                                    maxLines = 1,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }

                    // 3-Dot Overflow Menu Button on the far right
                    Box(
                        modifier = Modifier
                            .padding(end = 4.dp)
                            .size(44.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        IconButton(
                            onClick = {
                                TouchSoundKit.playPop(context)
                                showMenu = true
                            },
                            modifier = Modifier
                                .size(40.dp)
                                .testTag("three_dot_menu_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "Menu options",
                                tint = GreenNavActive,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = {
                                Column {
                                    Text("Export (Excel / CSV)", fontWeight = FontWeight.SemiBold)
                                    Text("Download spreadsheet register", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            },
                            leadingIcon = { Icon(Icons.Default.FileDownload, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                            onClick = {
                                TouchSoundKit.playTap(context)
                                showMenu = false
                                onOpenExportImport()
                            },
                            modifier = Modifier.testTag("menu_export_option")
                        )

                        DropdownMenuItem(
                            text = {
                                Column {
                                    Text("Import Excel file", fontWeight = FontWeight.SemiBold)
                                    Text("Import cards from CSV/Excel file", fontSize = 11.sp, color = MaterialTheme.colorScheme.secondary)
                                }
                            },
                            leadingIcon = { Icon(Icons.Default.FileUpload, contentDescription = null, tint = MaterialTheme.colorScheme.secondary) },
                            onClick = {
                                TouchSoundKit.playTap(context)
                                showMenu = false
                                onOpenExportImport()
                            },
                            modifier = Modifier.testTag("menu_import_option")
                        )

                        HorizontalDivider()

                        DropdownMenuItem(
                            text = {
                                Column {
                                    Text("Print", fontWeight = FontWeight.SemiBold)
                                    Text("Served / Unserved with 1st page shop details", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            },
                            leadingIcon = { Icon(Icons.Default.Print, contentDescription = null, tint = MaterialTheme.colorScheme.tertiary) },
                            onClick = {
                                TouchSoundKit.playTap(context)
                                showMenu = false
                                onOpenPrintPreview()
                            },
                            modifier = Modifier.testTag("menu_print_option")
                        )

                        HorizontalDivider()

                        DropdownMenuItem(
                            text = {
                                Column {
                                    Text("User guide", fontWeight = FontWeight.SemiBold)
                                    Text("How to use Smart Register", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            },
                            leadingIcon = { Icon(Icons.Default.HelpOutline, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                            onClick = {
                                TouchSoundKit.playTap(context)
                                showMenu = false
                                onOpenUserGuide()
                            },
                            modifier = Modifier.testTag("menu_user_guide_option")
                        )

                        DropdownMenuItem(
                            text = {
                                Column {
                                    Text("About", fontWeight = FontWeight.SemiBold)
                                    Text("App version & info", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            },
                            leadingIcon = { Icon(Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.secondary) },
                            onClick = {
                                TouchSoundKit.playTap(context)
                                showMenu = false
                                onOpenAbout()
                            },
                            modifier = Modifier.testTag("menu_about_option")
                        )

                        if (profile.isPinEnabled) {
                            HorizontalDivider()
                            DropdownMenuItem(
                                text = { Text("Lock Register Now") },
                                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                                onClick = {
                                    TouchSoundKit.playTap(context)
                                    showMenu = false
                                    onLockApp()
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
}
