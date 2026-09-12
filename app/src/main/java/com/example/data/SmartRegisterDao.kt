package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface SmartRegisterDao {

    // --- Shop Profile ---
    @Query("SELECT * FROM shop_profile WHERE id = 1 LIMIT 1")
    fun getShopProfile(): Flow<ShopProfile?>

    @Query("SELECT * FROM shop_profile WHERE id = 1 LIMIT 1")
    suspend fun getShopProfileOnce(): ShopProfile?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveShopProfile(profile: ShopProfile)

    // --- Ration Cards ---
    @Query("SELECT * FROM ration_cards ORDER BY headOfFamilyName COLLATE NOCASE ASC")
    fun getAllRationCards(): Flow<List<RationCard>>

    @Query("SELECT * FROM ration_cards WHERE cardNo = :cardNo LIMIT 1")
    suspend fun getRationCard(cardNo: String): RationCard?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRationCard(card: RationCard)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRationCards(cards: List<RationCard>)

    @Update
    suspend fun updateRationCard(card: RationCard)

    @Query("UPDATE ration_cards SET mobileNo = :mobileNo WHERE cardNo = :cardNo")
    suspend fun updateCardMobileNo(cardNo: String, mobileNo: String)

    @Query("DELETE FROM ration_cards WHERE cardNo = :cardNo")
    suspend fun deleteRationCard(cardNo: String)

    @Query("DELETE FROM distribution_records WHERE cardNo = :cardNo")
    suspend fun deleteDistributionRecordsForCard(cardNo: String)

    // --- Distribution Records ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun setDistributionRecord(record: DistributionRecord)

    @Query("DELETE FROM distribution_records WHERE cardNo = :cardNo AND monthYear = :monthYear")
    suspend fun clearDistributionRecord(cardNo: String, monthYear: String)

    @Query("""
        SELECT c.cardNo, c.headOfFamilyName, c.category, c.numberOfMembers, c.mobileNo,
               COALESCE(d.isServed, 0) AS isServed,
               COALESCE(d.servedDateTime, '') AS servedDateTime,
               COALESCE(d.servedTimestamp, 0) AS servedTimestamp
        FROM ration_cards c
        LEFT JOIN distribution_records d
          ON c.cardNo = d.cardNo AND d.monthYear = :monthYear
        ORDER BY c.headOfFamilyName COLLATE NOCASE ASC
    """)
    fun getCardsWithDistribution(monthYear: String): Flow<List<CardWithDistribution>>

    @Query("""
        SELECT c.cardNo, c.headOfFamilyName, c.category, c.numberOfMembers, c.mobileNo,
               COALESCE(d.isServed, 0) AS isServed,
               COALESCE(d.servedDateTime, '') AS servedDateTime,
               COALESCE(d.servedTimestamp, 0) AS servedTimestamp
        FROM ration_cards c
        LEFT JOIN distribution_records d
          ON c.cardNo = d.cardNo AND d.monthYear = :monthYear
        WHERE (c.cardNo LIKE '%' || :query || '%' OR c.headOfFamilyName LIKE '%' || :query || '%')
        ORDER BY c.headOfFamilyName COLLATE NOCASE ASC
    """)
    suspend fun searchCardsWithDistribution(monthYear: String, query: String): List<CardWithDistribution>
}
