package com.example.ui

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.CardWithDistribution
import com.example.data.RationCard
import com.example.data.ShopProfile
import com.example.data.SmartRegisterRepository
import com.example.export.ExcelCsvHelper
import com.example.print.PrintManagerHelper
import com.example.print.PrintOption
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class SmartRegisterViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: SmartRegisterRepository

    init {
        val database = AppDatabase.getDatabase(application)
        repository = SmartRegisterRepository(database.smartRegisterDao())
    }

    // --- Current Month/Year State (e.g. "2026-09") ---
    private val _selectedMonthYear = MutableStateFlow(getCurrentMonthYear())
    val selectedMonthYear: StateFlow<String> = _selectedMonthYear.asStateFlow()

    // --- Shop Profile ---
    val shopProfile: StateFlow<ShopProfile> = repository.shopProfile
        .combine(_selectedMonthYear) { profile, _ -> profile ?: ShopProfile() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ShopProfile()
        )

    // --- Search Query in Distribution ---
    private val _distributionSearchQuery = MutableStateFlow("")
    val distributionSearchQuery: StateFlow<String> = _distributionSearchQuery.asStateFlow()

    // --- Filter tab in Distribution (All, Served, Unserved) ---
    private val _distributionFilter = MutableStateFlow("ALL")
    val distributionFilter: StateFlow<String> = _distributionFilter.asStateFlow()

    // --- Cards for Selected Month with Distribution Status ---
    private val _rawCardsWithDistribution = MutableStateFlow<List<CardWithDistribution>>(emptyList())

    init {
        viewModelScope.launch {
            _selectedMonthYear.collectLatest { monthYear ->
                repository.getCardsWithDistribution(monthYear).collect { list ->
                    _rawCardsWithDistribution.value = list
                }
            }
        }
    }

    // Filtered Cards for Distribution Screen
    val distributionCards: StateFlow<List<CardWithDistribution>> = combine(
        _rawCardsWithDistribution,
        _distributionSearchQuery,
        _distributionFilter
    ) { cards, query, filter ->
        val trimmed = query.trim()
        val searched = if (trimmed.isEmpty()) {
            cards
        } else {
            cards.filter {
                it.cardNo.contains(trimmed, ignoreCase = true) ||
                        it.headOfFamilyName.contains(trimmed, ignoreCase = true) ||
                        it.mobileNo.contains(trimmed, ignoreCase = true)
            }
        }

        when (filter) {
            "SERVED" -> searched.filter { it.isServed }
            "UNSERVED" -> searched.filter { !it.isServed }
            else -> searched
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // All Cards for selected month (used for Report & Print)
    val allCardsForMonth: StateFlow<List<CardWithDistribution>> = _rawCardsWithDistribution.asStateFlow()

    // --- App Security PIN State ---
    private val _isAppLocked = MutableStateFlow(false)
    val isAppLocked: StateFlow<Boolean> = _isAppLocked.asStateFlow()

    init {
        viewModelScope.launch {
            repository.shopProfile.collectLatest { profile ->
                if (profile != null && (profile.isPinEnabled || profile.isBiometricEnabled)) {
                    _isAppLocked.value = true
                }
            }
        }
    }

    fun unlockWithPin(pin: String): Boolean {
        val current = shopProfile.value
        val expectedPin = if (current.securityPin.isNotBlank()) current.securityPin else "1234"
        return if (pin == expectedPin || (current.securityPin.isBlank() && (pin == "1234" || pin.isEmpty()))) {
            _isAppLocked.value = false
            true
        } else {
            false
        }
    }

    fun unlockWithBiometric(): Boolean {
        _isAppLocked.value = false
        return true
    }

    fun lockApp() {
        _isAppLocked.value = true
    }

    // --- Month Navigation ---
    fun setMonthYear(monthYear: String) {
        _selectedMonthYear.value = monthYear
    }

    fun changeMonth(offsetMonths: Int) {
        val current = _selectedMonthYear.value
        val sdf = SimpleDateFormat("yyyy-MM", Locale.getDefault())
        try {
            val date = sdf.parse(current) ?: Date()
            val cal = Calendar.getInstance().apply { time = date }
            cal.add(Calendar.MONTH, offsetMonths)
            _selectedMonthYear.value = sdf.format(cal.time)
        } catch (_: Exception) {
            _selectedMonthYear.value = getCurrentMonthYear()
        }
    }

    fun getFormattedMonthDisplay(monthYear: String): String {
        return try {
            val sdfInput = SimpleDateFormat("yyyy-MM", Locale.getDefault())
            val sdfOutput = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
            val date = sdfInput.parse(monthYear)
            if (date != null) sdfOutput.format(date) else monthYear
        } catch (_: Exception) {
            monthYear
        }
    }

    // --- Search & Filter ---
    fun onDistributionSearchQueryChange(query: String) {
        _distributionSearchQuery.value = query
    }

    fun setDistributionFilter(filter: String) {
        _distributionFilter.value = filter
    }

    // --- Served / Unserved Quick Actions ---
    fun toggleCardDistribution(cardNo: String, currentServed: Boolean) {
        viewModelScope.launch {
            val monthYear = _selectedMonthYear.value
            if (currentServed) {
                // Mark Unserved (Nondrawal)
                repository.markCardUnserved(cardNo, monthYear)
            } else {
                // Mark Served with current Device Date and Time sync
                val now = System.currentTimeMillis()
                val formattedDateTime = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).apply { timeZone = TimeZone.getDefault() }.format(Date(now))
                repository.markCardServed(cardNo, monthYear, formattedDateTime, now)
            }
        }
    }

    fun markCardServed(cardNo: String) {
        viewModelScope.launch {
            val monthYear = _selectedMonthYear.value
            val now = System.currentTimeMillis()
            val formattedDateTime = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).apply { timeZone = TimeZone.getDefault() }.format(Date(now))
            repository.markCardServed(cardNo, monthYear, formattedDateTime, now)
        }
    }

    // --- Quick Mobile Update ---
    fun updateCardMobileNo(cardNo: String, mobileNo: String) {
        viewModelScope.launch {
            repository.updateCardMobileNo(cardNo, mobileNo.trim())
        }
    }

    // --- Profile Updates ---
    fun saveProfile(
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
    ) {
        viewModelScope.launch {
            val updated = ShopProfile(
                id = 1,
                shopName = shopName.trim(),
                proprietorName = proprietorName.trim(),
                fpsCode = fpsCode.trim(),
                licenceNo = licenceNo.trim(),
                area = area.trim(),
                shopAddress = shopAddress.trim(),
                securityPin = securityPin.trim(),
                isPinEnabled = isPinEnabled,
                isBiometricEnabled = isBiometricEnabled
            )
            repository.saveShopProfile(updated)
            onSuccess()
        }
    }

    // --- Create Section Actions (Add, Edit, Delete) ---
    fun addRationCard(
        cardNo: String,
        headOfFamilyName: String,
        category: String,
        numberOfMembers: Int,
        mobileNo: String,
        onResult: (Boolean, String) -> Unit
    ) {
        viewModelScope.launch {
            val trimmedCardNo = cardNo.trim().uppercase()
            val trimmedName = headOfFamilyName.trim()
            if (trimmedCardNo.isEmpty()) {
                onResult(false, "Card number is required")
                return@launch
            }
            if (trimmedName.isEmpty()) {
                onResult(false, "Head of family name is required")
                return@launch
            }
            val existing = repository.getRationCard(trimmedCardNo)
            if (existing != null) {
                onResult(false, "Card $trimmedCardNo already exists in register!")
                return@launch
            }

            val card = RationCard(
                cardNo = trimmedCardNo,
                headOfFamilyName = trimmedName,
                category = category,
                numberOfMembers = numberOfMembers.coerceAtLeast(1),
                mobileNo = mobileNo.trim()
            )
            repository.insertRationCard(card)
            onResult(true, "Ration card $trimmedCardNo saved successfully")
        }
    }

    suspend fun searchCardForEditOrDelete(query: String): RationCard? {
        val trimmed = query.trim()
        if (trimmed.isEmpty()) return null
        val direct = repository.getRationCard(trimmed.uppercase())
        if (direct != null) return direct

        // Search by name match in all cards
        val all = _rawCardsWithDistribution.value
        val found = all.firstOrNull {
            it.cardNo.equals(trimmed, ignoreCase = true) ||
                    it.headOfFamilyName.contains(trimmed, ignoreCase = true)
        }
        return if (found != null) {
            RationCard(
                cardNo = found.cardNo,
                headOfFamilyName = found.headOfFamilyName,
                category = found.category,
                numberOfMembers = found.numberOfMembers,
                mobileNo = found.mobileNo
            )
        } else null
    }

    fun updateRationCard(
        cardNo: String,
        headOfFamilyName: String,
        category: String,
        numberOfMembers: Int,
        mobileNo: String,
        onResult: (Boolean, String) -> Unit
    ) {
        viewModelScope.launch {
            val trimmedName = headOfFamilyName.trim()
            if (trimmedName.isEmpty()) {
                onResult(false, "Head of family name is required")
                return@launch
            }
            val updated = RationCard(
                cardNo = cardNo,
                headOfFamilyName = trimmedName,
                category = category,
                numberOfMembers = numberOfMembers.coerceAtLeast(1),
                mobileNo = mobileNo.trim()
            )
            repository.updateRationCard(updated)
            onResult(true, "Card $cardNo updated successfully")
        }
    }

    fun deleteRationCard(cardNo: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            repository.deleteRationCard(cardNo)
            onResult(true, "Card $cardNo deleted from register")
        }
    }

    // --- Import / Export Actions ---
    fun exportToExcelCsv(context: Context) {
        val profile = shopProfile.value
        val monthYear = _selectedMonthYear.value
        val cards = _rawCardsWithDistribution.value
        val file = ExcelCsvHelper.exportCardsToCsv(context, profile, monthYear, cards)
        ExcelCsvHelper.shareExportedFile(context, file)
    }

    fun importCsvFile(inputStream: InputStream, onResult: (Int, String) -> Unit) {
        viewModelScope.launch {
            try {
                val cards = ExcelCsvHelper.parseCsvCards(inputStream)
                if (cards.isEmpty()) {
                    onResult(0, "No valid card records found in file")
                    return@launch
                }
                repository.insertRationCards(cards)
                onResult(cards.size, "Successfully imported and saved ${cards.size} ration cards!")
            } catch (e: Exception) {
                onResult(0, "Failed to import file: ${e.localizedMessage}")
            }
        }
    }

    fun importSampleTemplate(onResult: (Int, String) -> Unit) {
        viewModelScope.launch {
            val cards = ExcelCsvHelper.parseCsvCards(ExcelCsvHelper.sampleCsvTemplate.byteInputStream())
            repository.insertRationCards(cards)
            onResult(cards.size, "Loaded ${cards.size} sample cards into register!")
        }
    }

    // --- Printing ---
    fun printA4(context: Context, option: PrintOption) {
        val profile = shopProfile.value
        val monthYear = _selectedMonthYear.value
        val cards = _rawCardsWithDistribution.value
        PrintManagerHelper.printDocument(context, profile, monthYear, option, cards)
    }

    companion object {
        fun getCurrentMonthYear(): String {
            return SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(Date())
        }
    }
}
