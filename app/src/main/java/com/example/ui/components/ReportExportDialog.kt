package com.example.ui.components

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.CardWithDistribution
import com.example.data.ShopProfile
import com.example.export.ExcelCsvHelper
import com.example.export.PdfHelper
import com.example.print.PrintOption

@Composable
fun PdfExportDialog(
    profile: ShopProfile,
    monthYear: String,
    cards: List<CardWithDistribution>,
    onDismiss: () -> Unit
) {
    var selectedOption by remember { mutableStateOf(PrintOption.ALL_SERVED_AND_UNSERVED) }
    val context = LocalContext.current

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

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
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
                            color = Color(0xFFFEE2E2),
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Default.PictureAsPdf,
                                    contentDescription = null,
                                    tint = Color(0xFFB91C1C),
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                        Column {
                            Text(
                                text = "Save & Export PDF",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Device Storage & Google Drive",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                // Showing Details Card
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "REPORT DETAILS",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFFB91C1C),
                            letterSpacing = 0.5.sp
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Month / Year:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(monthYear, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Fair Price Shop:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(profile.shopName.ifBlank { "FPS Shop" }, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }

                        if (profile.fpsCode.isNotBlank() || profile.licenceNo.isNotBlank()) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("FPS Code / Licence:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("${profile.fpsCode} • ${profile.licenceNo}", fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                            }
                        }

                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                            modifier = Modifier.padding(vertical = 2.dp)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Selected Cards:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            Text(
                                "$totalFamilies Families ($totalMembers Members)",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                // Filter Option Selector
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "Choose Records to Include in PDF:",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        PrintOption.entries.forEach { option ->
                            val isSelected = selectedOption == option
                            val count = when (option) {
                                PrintOption.ALL_SERVED -> cards.count { it.isServed }
                                PrintOption.ALL_UNSERVED -> cards.count { !it.isServed }
                                PrintOption.ALL_SERVED_AND_UNSERVED -> cards.size
                            }

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSelected) Color(0xFFFEE2E2) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                                border = if (isSelected) BorderStroke(1.5.dp, Color(0xFFB91C1C)) else BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { selectedOption = option }
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        RadioButton(
                                            selected = isSelected,
                                            onClick = { selectedOption = option },
                                            colors = RadioButtonDefaults.colors(
                                                selectedColor = Color(0xFFB91C1C)
                                            ),
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Text(
                                            text = option.label,
                                            fontSize = 13.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isSelected) Color(0xFFB91C1C) else MaterialTheme.colorScheme.onSurface
                                        )
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = if (isSelected) Color(0xFFB91C1C) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f)
                                    ) {
                                        Text(
                                            text = "$count",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Action Buttons
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = {
                            try {
                                val file = PdfHelper.saveAndSharePdf(
                                    context = context,
                                    profile = profile,
                                    monthYear = monthYear,
                                    option = selectedOption,
                                    cards = cards
                                )
                                Toast.makeText(
                                    context,
                                    "PDF saved successfully: ${file.name}\n(Available in Downloads & Google Drive)",
                                    Toast.LENGTH_LONG
                                ).show()
                                onDismiss()
                            } catch (e: Exception) {
                                Toast.makeText(context, "Error generating PDF: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB91C1C)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("save_pdf_device_driver_button")
                    ) {
                        Icon(Icons.Default.SaveAlt, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "Save PDF to Device / Drive",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }

                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                    ) {
                        Text("Close", fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

@Composable
fun ExcelExportDialog(
    profile: ShopProfile,
    monthYear: String,
    cards: List<CardWithDistribution>,
    onDismiss: () -> Unit
) {
    var selectedOption by remember { mutableStateOf(PrintOption.ALL_SERVED_AND_UNSERVED) }
    val context = LocalContext.current

    val filteredCards = remember(selectedOption, cards) {
        when (selectedOption) {
            PrintOption.ALL_SERVED -> cards.filter { it.isServed }
            PrintOption.ALL_UNSERVED -> cards.filter { !it.isServed }
            PrintOption.ALL_SERVED_AND_UNSERVED -> cards
        }
    }

    val totalFamilies = filteredCards.size
    val totalMembers = filteredCards.sumOf { it.numberOfMembers }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
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
                            color = Color(0xFFDCFCE7),
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Default.TableChart,
                                    contentDescription = null,
                                    tint = Color(0xFF15803D),
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                        Column {
                            Text(
                                text = "Save & Export Excel",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Excel & CSV (.csv / .xlsx format)",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                // Showing Details Card
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "EXCEL EXPORT DETAILS",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF15803D),
                            letterSpacing = 0.5.sp
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Month / Year:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(monthYear, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Fair Price Shop:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(profile.shopName.ifBlank { "FPS Shop" }, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Spreadsheet Format:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("Microsoft Excel Compatible (.csv)", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        }

                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                            modifier = Modifier.padding(vertical = 2.dp)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Selected Cards:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            Text(
                                "$totalFamilies Families ($totalMembers Members)",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF15803D)
                            )
                        }
                    }
                }

                // Filter Option Selector
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "Choose Records to Include in Excel:",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        PrintOption.entries.forEach { option ->
                            val isSelected = selectedOption == option
                            val count = when (option) {
                                PrintOption.ALL_SERVED -> cards.count { it.isServed }
                                PrintOption.ALL_UNSERVED -> cards.count { !it.isServed }
                                PrintOption.ALL_SERVED_AND_UNSERVED -> cards.size
                            }

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSelected) Color(0xFFDCFCE7) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                                border = if (isSelected) BorderStroke(1.5.dp, Color(0xFF15803D)) else BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { selectedOption = option }
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        RadioButton(
                                            selected = isSelected,
                                            onClick = { selectedOption = option },
                                            colors = RadioButtonDefaults.colors(
                                                selectedColor = Color(0xFF15803D)
                                            ),
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Text(
                                            text = option.label,
                                            fontSize = 13.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isSelected) Color(0xFF15803D) else MaterialTheme.colorScheme.onSurface
                                        )
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = if (isSelected) Color(0xFF15803D) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f)
                                    ) {
                                        Text(
                                            text = "$count",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Action Buttons
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = {
                            try {
                                val file = ExcelCsvHelper.saveAndShareCsv(
                                    context = context,
                                    profile = profile,
                                    monthYear = monthYear,
                                    cards = cards,
                                    option = selectedOption
                                )
                                Toast.makeText(
                                    context,
                                    "Excel saved successfully: ${file.name}\n(Available in Downloads & Google Drive)",
                                    Toast.LENGTH_LONG
                                ).show()
                                onDismiss()
                            } catch (e: Exception) {
                                Toast.makeText(context, "Error saving Excel: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF15803D)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("save_excel_device_driver_button")
                    ) {
                        Icon(Icons.Default.SaveAlt, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "Save Excel to Device / Drive",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }

                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                    ) {
                        Text("Close", fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}
