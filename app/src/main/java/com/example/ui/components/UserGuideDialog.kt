package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

@Composable
fun UserGuideDialog(
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.96f)
                .fillMaxHeight(0.94f),
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(18.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.size(42.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Default.MenuBook,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                        Column {
                            Text(
                                text = "Comprehensive User Guide",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "PDS Regulations, Statutory Standards & Manual",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("user_guide_close_button")
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))

                // Scrollable Content
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Quick Start Workflow Banner
                    QuickStartWorkflowCard()

                    // PDS Regulations & Statutory Compliance Standards Card
                    PdsRegulationsComplianceCard()

                    // Excel & CSV File Preparation & Import Guide (Highlighted)
                    ExcelImportGuideCard()

                    // Detailed Topic 1: Profile & Shop Information Setup
                    DetailedGuideCard(
                        icon = Icons.Default.Storefront,
                        badge = "STEP 1",
                        title = "1. Shop Profile & Security Setup",
                        summary = "Configure shop credentials, official dealer details, and authentication security.",
                        bulletPoints = listOf(
                            "Tap the PROFILE tab on the navigation bar.",
                            "Enter Fair Price Shop Name, Proprietor Name, FPS Code, Licence No, Shop Address, and Area.",
                            "These details automatically print on all official A4 reports, monthly summaries, and Excel files.",
                            "Set a 4-to-6 digit Security PIN (default is 1234 if not configured).",
                            "Enable Biometric / Fingerprint authentication for instant one-touch login.",
                            "Tap 'SAVE SHOP PROFILE' to persist settings."
                        ),
                        proTip = "Keep your FPS Code and Licence No updated as they appear on official government inspection printouts."
                    )

                    // Detailed Topic 2: Daily & Monthly Distribution Workflow
                    DetailedGuideCard(
                        icon = Icons.Default.Assignment,
                        badge = "STEP 2",
                        title = "2. Monthly Ration Distribution Workflow",
                        summary = "Fast daily distribution recording with instant search and automatic timestamping.",
                        bulletPoints = listOf(
                            "Select Target Month: Use the month dropdown at the top to record distribution for current or past months.",
                            "Instant Search: Type any part of the Ration Card Number or Head of Family Name in the search box.",
                            "Mark Served (One-Touch): Tap 'SERVED' on any card. The status instantly changes to green SERVED and records the exact device-synced date & time in Dark Blue.",
                            "Mark Unserved with Confirmation: If you need to revert a served card, tap 'UNSERVED'. A safety prompt 'do you want to unserved [OK / NO]' prevents accidental changes. Tapping OK unserves and clears search for the next beneficiary.",
                            "Filter Views: Use the quick filter chips (All, Served, Unserved) to inspect served or remaining pending beneficiaries.",
                            "Add/Edit Mobile: Tap the mobile badge or icon on any card to update the beneficiary's 10-digit phone number."
                        ),
                        proTip = "The search bar automatically returns focus after serving/unserving for rapid uninterrupted barcode/manual entries."
                    )

                    // Detailed Topic 3: Create, Edit, & Delete Ration Cards
                    DetailedGuideCard(
                        icon = Icons.Default.GroupAdd,
                        badge = "STEP 3",
                        title = "3. Create, Edit & Delete Beneficiary Cards",
                        summary = "Manage your shop's master beneficiary list directly within the CREATE tab.",
                        bulletPoints = listOf(
                            "ADD NEW CARD: Go to CREATE > ADD tab. Enter Ration Card Number, Head of Family Name, Number of Members/Units, and optional 10-digit Mobile Number, then tap 'SAVE RATION CARD'.",
                            "EDIT EXISTING CARD: Switch to CREATE > EDIT tab. Search by Card No or Name, click 'EDIT', modify details, and tap 'UPDATE CARD'.",
                            "DELETE CARD: Switch to CREATE > DELETE tab. Search for the card to remove, tap 'DELETE CARD', and confirm the deletion prompt.",
                            "Member Count / Units: Total family member count is used for aggregate distribution calculations."
                        ),
                        proTip = "Adding a new card automatically initializes its monthly distribution records across all months."
                    )

                    // Detailed Topic 4: Monthly Reports & Analytics
                    DetailedGuideCard(
                        icon = Icons.Default.Analytics,
                        badge = "STEP 4",
                        title = "4. Monthly Analytics, Reports & Progress",
                        summary = "Real-time distribution dashboards with breakdowns, progress bars, and filters.",
                        bulletPoints = listOf(
                            "Navigate to the REPORTS tab to inspect real-time metrics for the active month.",
                            "View Total Families, Total Beneficiary Members/Units, Total Served count, and Total Unserved count.",
                            "Track the live percentage progress bar for monthly distribution completion.",
                            "Filter cards by 'Served', 'Unserved', or 'All Card' with instant in-report search.",
                            "Tap 'Export Excel / CSV' to generate spreadsheet summaries.",
                            "Tap 'A4 Print Preview' to view and print official formatted monthly registers."
                        ),
                        proTip = "Use the Served/Unserved tabs to quickly generate targeted pending lists during month-end distribution drives."
                    )

                    // Detailed Topic 5: A4 Official Document Printing & PDF
                    DetailedGuideCard(
                        icon = Icons.Default.Print,
                        badge = "STEP 5",
                        title = "5. Official A4 Print & PDF Document Generation",
                        summary = "Generate government-compliant official A4 registers ready for physical printing or PDF sharing.",
                        bulletPoints = listOf(
                            "Open 3-Dot Menu (⋮) > Tap 'A4 Print Preview' (or tap Print in the Reports tab).",
                            "Choose document scope: 'All (Served & Unserved)', 'Served Beneficiaries Only', or 'Unserved Beneficiaries Only'.",
                            "Official Header: Formatted with Shop Name, FPS Code, Licence No, Month & Year, and Print Generation Date/Time.",
                            "Summary Table: Displays Total Registered, Served Count, Unserved Count, Total Units, and Completion Rate.",
                            "Beneficiary Roster: Multi-column table with Sl No, Card No, Head of Family Name, Units, Served Status, and Served Date & Time.",
                            "Direct Print: Tap 'Direct Print' to send to any WiFi, Bluetooth, USB, or Thermal printer via Android System Print.",
                            "Save as PDF: Select 'Save as PDF' from the printer dropdown to store a PDF copy on your device."
                        ),
                        proTip = "Official signature boxes for 'Dealer / FPS Proprietor' and 'Inspecting Officer' are included at the bottom of the printout."
                    )

                    // Detailed Topic 6: Excel Export, JSON Backup & Restore
                    DetailedGuideCard(
                        icon = Icons.Default.Backup,
                        badge = "STEP 6",
                        title = "6. Backup, Restore & Data Portability",
                        summary = "Protect your shop data with full offline backups and easy Excel/JSON exports.",
                        bulletPoints = listOf(
                            "Tap 3-Dot Menu (⋮) > Select 'Export / Import & Backup'.",
                            "Export CSV: Generates an Excel-friendly CSV with full dealer metadata and card distribution records.",
                            "Export Full Backup (JSON): Creates a complete encrypted snapshot of all cards, months, and profile settings.",
                            "Import CSV: Bulk import new beneficiary cards from your prepared spreadsheet.",
                            "Restore Backup (JSON): Restore your complete database onto a new or reset phone.",
                            "Load Demo Data: Useful for new dealers to load 50 sample beneficiary cards for training."
                        ),
                        proTip = "We recommend exporting a monthly JSON backup to your Google Drive or external memory at the end of each month."
                    )

                    // Detailed Topic 7: Security, Lock & Logout
                    DetailedGuideCard(
                        icon = Icons.Default.Security,
                        badge = "STEP 7",
                        title = "7. Security, App Lock & Offline Privacy",
                        summary = "Keep your register protected with dual PIN & Biometric security.",
                        bulletPoints = listOf(
                            "Instant Logout: Tap the 'Logout' button on the top-right header to lock your session immediately.",
                            "App Lock on Startup: If PIN or Biometric is enabled in Profile, the app automatically presents the Login screen on opening.",
                            "PIN Login: Enter your 4-digit PIN (default 1234 if unset) and tap 'Unlock Register'.",
                            "Biometric Login: Tap 'Verify with Biometric / Fingerprint' for instant sensor authentication.",
                            "100% Offline & Private: No internet required. Your beneficiary records never leave your local phone storage."
                        ),
                        proTip = "You can change your 4-digit PIN anytime in the PROFILE tab."
                    )

                    // FAQ Section
                    FaqSectionCard()
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("user_guide_got_it_button")
                ) {
                    Text("Close & Return to Register", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun PdsRegulationsComplianceCard() {
    var expanded by remember { mutableStateOf(true) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable { expanded = !expanded },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.35f)
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(38.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Gavel,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSecondary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                    Column {
                        Text(
                            text = "PDS Dealer Regulations & Statutory Standards",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Statutory compliance guidelines for Fair Price Shops",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                IconButton(
                    onClick = { expanded = !expanded },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = if (expanded) "Collapse" else "Expand",
                        tint = MaterialTheme.colorScheme.secondary
                    )
                }
            }

            AnimatedVisibility(
                visible = expanded,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "This PDS (Public Distribution System) Smart Register is built in full compliance with Fair Price Shop (FPS) dealer regulations and statutory distribution standards:",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        lineHeight = 20.sp
                    )

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                    // 1. Fair Price Shop & Dealer Identification
                    RegulationSectionItem(
                        number = "1",
                        title = "Fair Price Shop & Dealer Identification",
                        items = listOf(
                            "Dealer Identity" to "Stores official dealer information in the Profile tab, including Shop Name, Proprietor/Secretary Name, FPS Code, Licence Number, and Shop Address.",
                            "Receipt & Register Headers" to "Automatically includes official FPS credentials on all generated monthly registers and printed reports."
                        )
                    )

                    // 2. Beneficiary & Ration Card Governance
                    RegulationSectionItem(
                        number = "2",
                        title = "Beneficiary & Ration Card Governance",
                        items = listOf(
                            "Transparent Family Units" to "Records Head of Family name, unique Ration Card number, total family member count, and contact number.",
                            "Entitlement Distribution" to "Calculates entitled grain quotas based on family unit sizes to ensure 100% fair entitlement without diversion.",
                            "Accurate Timestamping" to "Automatically records the exact date and time of physical ration distribution to maintain a tamper-evident audit trail."
                        )
                    )

                    // 3. Record Keeping & Audit Readiness
                    RegulationSectionItem(
                        number = "3",
                        title = "Record Keeping & Audit Readiness",
                        items = listOf(
                            "Official A4 Print Engine" to "Generates formatted monthly distribution registers formatted for standard A4 printing with statutory columns (Serial No, Card No, Head of Family, Entitled Qty, Distributed Qty, Date & Timestamp, and Signature line).",
                            "Excel & CSV Export" to "Allows full database backups and electronic register exports to maintain required 36-month audit records for Food & Civil Supplies departmental inspections.",
                            "Non-Drawn Tracking" to "Identifies beneficiaries who have not yet collected their monthly quota for transparent stock reconciliation."
                        )
                    )

                    // 4. Privacy & Access Control
                    RegulationSectionItem(
                        number = "4",
                        title = "Privacy & Access Control",
                        items = listOf(
                            "Security PIN & Biometric Lock" to "Protects beneficiary records and distribution registries from unauthorized access or modification.",
                            "Local Offline Storage" to "Operates entirely on-device via a secure local database, ensuring zero data leakage and full operational independence."
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun RegulationSectionItem(
    number: String,
    title: String,
    items: List<Pair<String, String>>
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    modifier = Modifier.size(24.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = number,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
                Text(
                    text = "$number. $title",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            items.forEach { (subLabel, description) ->
                Row(
                    modifier = Modifier.padding(start = 4.dp),
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier
                            .padding(top = 2.dp)
                            .size(14.dp)
                    )
                    Column {
                        Text(
                            text = "$subLabel:",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.secondary
                        )
                        Text(
                            text = description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 17.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun QuickStartWorkflowCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f))
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    Icons.Default.ElectricBolt,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(22.dp)
                )
                Text(
                    text = "Quick Start: 4-Step Routine",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                QuickStepBadge("1. Setup", "Profile Details")
                QuickStepBadge("2. Cards", "Add / Import")
                QuickStepBadge("3. Serve", "Mark Served")
                QuickStepBadge("4. Report", "Print & Export")
            }
        }
    }
}

@Composable
private fun QuickStepBadge(step: String, label: String) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier.padding(2.dp)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(step, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            Text(label, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun ExcelImportGuideCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFF15803D),
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.TableChart,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
                Column {
                    Text(
                        text = "How to Prepare & Import Excel / CSV",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Bulk import hundreds of beneficiary cards in seconds",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            // Step A: How to Ready the File
            Text(
                text = "Step 1: Prepare Your Excel Spreadsheet",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Text(
                text = "In Microsoft Excel, Google Sheets, or WPS Office, create 5 columns in row 1:",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface
            )

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text("• Column A: Card No (e.g. RC90031121)", style = MaterialTheme.typography.bodySmall, fontFamily = FontFamily.Monospace)
                    Text("• Column B: Head of Family Name (e.g. Ramesh Roy)", style = MaterialTheme.typography.bodySmall, fontFamily = FontFamily.Monospace)
                    Text("• Column C: Category (e.g. PHH, SPHH, AAY)", style = MaterialTheme.typography.bodySmall, fontFamily = FontFamily.Monospace)
                    Text("• Column D: Number of Members (e.g. 4)", style = MaterialTheme.typography.bodySmall, fontFamily = FontFamily.Monospace)
                    Text("• Column E: Mobile No (e.g. 9830012345 or blank)", style = MaterialTheme.typography.bodySmall, fontFamily = FontFamily.Monospace)
                }
            }

            Text(
                text = "Saving File: In Excel, click File > Save As > Select 'CSV (Comma delimited) (*.csv)'.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(2.dp))

            // Step B: How to Import
            Text(
                text = "Step 2: Import File into Smart Register",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Column(
                verticalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                ImportStepItem(number = "1", text = "Tap the 3-dot menu (⋮) at top right > Select 'Import / Export & Backup'.")
                ImportStepItem(number = "2", text = "Tap 'Select CSV / Excel File' to choose your .csv file from your phone storage.")
                ImportStepItem(number = "3", text = "Or tap 'Load Demo 50 Cards' to test with sample data immediately.")
                ImportStepItem(number = "4", text = "The app imports all cards into your local Room database and gives a success report.")
            }
        }
    }
}

@Composable
private fun DetailedGuideCard(
    icon: ImageVector,
    badge: String,
    title: String,
    summary: String,
    bulletPoints: List<String>,
    proTip: String? = null
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable { expanded = !expanded },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (expanded) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f) else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (expanded) 2.dp else 1.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = badge,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                            Text(
                                text = title,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Text(
                            text = summary,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                IconButton(
                    onClick = { expanded = !expanded },
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = if (expanded) "Collapse" else "Expand",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            AnimatedVisibility(
                visible = expanded,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    Spacer(Modifier.height(4.dp))

                    bulletPoints.forEach { point ->
                        Row(
                            verticalAlignment = Alignment.Top,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                Icons.Default.CheckCircleOutline,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier
                                    .padding(top = 2.dp)
                                    .size(15.dp)
                            )
                            Text(
                                text = point,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface,
                                lineHeight = 18.sp
                            )
                        }
                    }

                    if (proTip != null) {
                        Spacer(Modifier.height(4.dp))
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.Top,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    Icons.Default.Lightbulb,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "PRO TIP: $proTip",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    lineHeight = 16.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FaqSectionCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    Icons.Default.Quiz,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "Frequently Asked Questions (FAQ)",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            FaqItem(
                question = "Do I need internet or Wi-Fi to use this app?",
                answer = "No. Smart Register operates 100% offline. All beneficiary records, distributions, timestamps, and reports work completely without any internet connection."
            )

            FaqItem(
                question = "What happens when a new month begins?",
                answer = "Smart Register automatically rolls over into the new month. Your master card roster remains intact, and all cards reset to pending (unserved) ready for the new month's distribution."
            )

            FaqItem(
                question = "Can I unserve a card if I marked it by mistake?",
                answer = "Yes! Tap the green 'SERVED' button on the card. The app asks 'do you want to unserved [OK / NO]'. Tapping OK safely reverts the card to unserved."
            )

            FaqItem(
                question = "How do I print on a physical printer?",
                answer = "Tap 3-Dot Menu > 'A4 Print Preview' > tap 'Direct Print'. Android's built-in Print Manager will detect any connected WiFi, USB, or Bluetooth printer."
            )
        }
    }
}

@Composable
private fun FaqItem(question: String, answer: String) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(
            text = "Q: $question",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = answer,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            lineHeight = 17.sp
        )
    }
}

@Composable
private fun ImportStepItem(number: String, text: String) {
    Row(
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Surface(
            shape = RoundedCornerShape(4.dp),
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(18.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = number,
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface,
            lineHeight = 18.sp
        )
    }
}
