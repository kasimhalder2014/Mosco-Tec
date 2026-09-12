package com.example.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "shop_profile")
data class ShopProfile(
    @PrimaryKey val id: Int = 1,
    val shopName: String = "M/S Maa Durga PDS Centre",
    val proprietorName: String = "Ramesh Kumar Sharma",
    val fpsCode: String = "FPS-WB-743201",
    val licenceNo: String = "LIC/PDS/2024/0981",
    val area: String = "Ward No 12, Rural Sector",
    val shopAddress: String = "Station Road, Near Post Office, Pin - 743201",
    val securityPin: String = "",
    val isPinEnabled: Boolean = false,
    val isBiometricEnabled: Boolean = false
)

@Entity(tableName = "ration_cards")
data class RationCard(
    @PrimaryKey val cardNo: String,
    val headOfFamilyName: String,
    val category: String = "PHH",
    val numberOfMembers: Int,
    val mobileNo: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "distribution_records",
    indices = [
        Index(value = ["cardNo", "monthYear"], unique = true)
    ]
)
data class DistributionRecord(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val cardNo: String,
    val monthYear: String, // format "YYYY-MM" (e.g., "2026-09")
    val isServed: Boolean = false,
    val servedDateTime: String = "", // e.g., "11 Sep 2026, 12:06 PM"
    val servedTimestamp: Long = 0L
)

data class CardWithDistribution(
    val cardNo: String,
    val headOfFamilyName: String,
    val category: String = "PHH",
    val numberOfMembers: Int,
    val mobileNo: String,
    val isServed: Boolean = false,
    val servedDateTime: String = "",
    val servedTimestamp: Long = 0L
)
