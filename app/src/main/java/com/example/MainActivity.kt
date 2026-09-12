package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.SmartRegisterViewModel
import com.example.ui.components.A4PrintPreviewDialog
import com.example.ui.components.AboutDialog
import com.example.ui.components.ImportExportDialog
import com.example.ui.components.SecurityLockDialog
import com.example.ui.components.TopBarWith3Dot
import com.example.ui.components.UserGuideDialog
import com.example.ui.screens.CreateScreen
import com.example.ui.screens.DistributionScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.screens.ReportScreen
import com.example.ui.theme.MyApplicationTheme

import com.example.ui.util.TouchSoundKit

enum class MainNavigationTab(
    val title: String,
    val filledIcon: ImageVector,
    val outlinedIcon: ImageVector,
    val testTag: String
) {
    PROFILE("PROFILE", Icons.Filled.Storefront, Icons.Outlined.Storefront, "nav_profile"),
    DISTRIBUTION("DISTRIBUTION", Icons.Filled.Assignment, Icons.Outlined.Assignment, "nav_distribution"),
    CREATE("CREATE", Icons.Filled.Group, Icons.Outlined.Group, "nav_create"),
    REPORT("REPORTS", Icons.Filled.PieChart, Icons.Outlined.PieChart, "nav_report")
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        TouchSoundKit.init(this)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                SmartRegisterApp()
            }
        }
    }
}

@Composable
fun SmartRegisterApp(
    viewModel: SmartRegisterViewModel = viewModel()
) {
    val context = LocalContext.current
    var currentTab by remember { mutableStateOf(MainNavigationTab.DISTRIBUTION) }

    // Dialog Visibility States
    var showPrintPreview by remember { mutableStateOf(false) }
    var showExportImportDialog by remember { mutableStateOf(false) }
    var showUserGuideDialog by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }

    // State Collection
    val isLocked by viewModel.isAppLocked.collectAsStateWithLifecycle()
    val profile by viewModel.shopProfile.collectAsStateWithLifecycle()
    val selectedMonthYear by viewModel.selectedMonthYear.collectAsStateWithLifecycle()
    val searchQuery by viewModel.distributionSearchQuery.collectAsStateWithLifecycle()
    val distributionFilter by viewModel.distributionFilter.collectAsStateWithLifecycle()

    val distributionCards by viewModel.distributionCards.collectAsStateWithLifecycle()
    val allCardsForMonth by viewModel.allCardsForMonth.collectAsStateWithLifecycle()

    val formattedMonth = remember(selectedMonthYear) {
        viewModel.getFormattedMonthDisplay(selectedMonthYear)
    }

    // Security Lock / Login Screen
    if (isLocked) {
        SecurityLockDialog(
            shopName = profile.shopName,
            fpsCode = profile.fpsCode,
            isBiometricAvailable = true,
            onUnlockWithPin = { pin ->
                viewModel.unlockWithPin(pin)
            },
            onUnlockWithBiometric = {
                viewModel.unlockWithBiometric()
            }
        )
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopBarWith3Dot(
                profile = profile,
                currentMonthFormatted = formattedMonth,
                currentTab = currentTab,
                onTabSelected = { currentTab = it },
                onOpenExportImport = { showExportImportDialog = true },
                onOpenPrintPreview = { showPrintPreview = true },
                onOpenUserGuide = { showUserGuideDialog = true },
                onOpenAbout = { showAboutDialog = true },
                onLockApp = { viewModel.lockApp() }
            )
        }
    ) { innerPadding ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            color = MaterialTheme.colorScheme.background
        ) {
            when (currentTab) {
                MainNavigationTab.PROFILE -> {
                    ProfileScreen(
                        currentProfile = profile,
                        onSaveProfile = viewModel::saveProfile
                    )
                }
                MainNavigationTab.DISTRIBUTION -> {
                    DistributionScreen(
                        monthYear = selectedMonthYear,
                        monthFormatted = formattedMonth,
                        cards = distributionCards,
                        allCards = allCardsForMonth,
                        searchQuery = searchQuery,
                        selectedFilter = distributionFilter,
                        onSearchChange = viewModel::onDistributionSearchQueryChange,
                        onFilterChange = viewModel::setDistributionFilter,
                        onPreviousMonth = { viewModel.changeMonth(-1) },
                        onNextMonth = { viewModel.changeMonth(1) },
                        onMonthSelected = viewModel::setMonthYear,
                        onToggleDistribution = viewModel::toggleCardDistribution,
                        onUpdateMobileNo = viewModel::updateCardMobileNo
                    )
                }
                MainNavigationTab.CREATE -> {
                    CreateScreen(
                        onAddCard = viewModel::addRationCard,
                        onSearchCard = viewModel::searchCardForEditOrDelete,
                        onUpdateCard = viewModel::updateRationCard,
                        onDeleteCard = viewModel::deleteRationCard
                    )
                }
                MainNavigationTab.REPORT -> {
                    ReportScreen(
                        profile = profile,
                        monthYear = selectedMonthYear,
                        monthFormatted = formattedMonth,
                        allCards = allCardsForMonth,
                        onToggleDistribution = viewModel::toggleCardDistribution,
                        onOpenPrintPreview = { showPrintPreview = true },
                        onExportCsv = { viewModel.exportToExcelCsv(context) }
                    )
                }
            }
        }
    }

    // A4 Print & PDF Preview Dialog
    if (showPrintPreview) {
        A4PrintPreviewDialog(
            profile = profile,
            monthYear = selectedMonthYear,
            cards = allCardsForMonth,
            onDismiss = { showPrintPreview = false },
            onDirectPrint = { option ->
                viewModel.printA4(context, option)
            }
        )
    }

    // Import / Export Excel Modal
    if (showExportImportDialog) {
        ImportExportDialog(
            onDismiss = { showExportImportDialog = false },
            onExport = {
                viewModel.exportToExcelCsv(context)
            },
            onImportStream = { stream ->
                viewModel.importCsvFile(stream) { count, message ->
                    Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                }
            },
            onLoadSampleTemplate = {
                viewModel.importSampleTemplate { count, message ->
                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                }
            }
        )
    }

    // User Guide Modal
    if (showUserGuideDialog) {
        UserGuideDialog(
            onDismiss = { showUserGuideDialog = false }
        )
    }

    // About Dialog
    if (showAboutDialog) {
        AboutDialog(
            onDismiss = { showAboutDialog = false }
        )
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(text = "smart Register - $name", modifier = modifier)
}
