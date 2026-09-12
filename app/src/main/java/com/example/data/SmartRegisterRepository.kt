package com.example.data

import kotlinx.coroutines.flow.Flow

class SmartRegisterRepository(private val dao: SmartRegisterDao) {

    val shopProfile: Flow<ShopProfile?> = dao.getShopProfile()

    suspend fun getShopProfileOnce(): ShopProfile {
        return dao.getShopProfileOnce() ?: ShopProfile()
    }

    suspend fun saveShopProfile(profile: ShopProfile) {
        dao.saveShopProfile(profile)
    }

    val allCards: Flow<List<RationCard>> = dao.getAllRationCards()

    fun getCardsWithDistribution(monthYear: String): Flow<List<CardWithDistribution>> {
        return dao.getCardsWithDistribution(monthYear)
    }

    suspend fun getRationCard(cardNo: String): RationCard? {
        return dao.getRationCard(cardNo)
    }

    suspend fun insertRationCard(card: RationCard) {
        dao.insertRationCard(card)
    }

    suspend fun insertRationCards(cards: List<RationCard>) {
        dao.insertRationCards(cards)
    }

    suspend fun updateRationCard(card: RationCard) {
        dao.updateRationCard(card)
    }

    suspend fun updateCardMobileNo(cardNo: String, mobileNo: String) {
        dao.updateCardMobileNo(cardNo, mobileNo)
    }

    suspend fun deleteRationCard(cardNo: String) {
        dao.deleteRationCard(cardNo)
        dao.deleteDistributionRecordsForCard(cardNo)
    }

    suspend fun markCardServed(
        cardNo: String,
        monthYear: String,
        formattedDateTime: String,
        timestamp: Long
    ) {
        dao.setDistributionRecord(
            DistributionRecord(
                cardNo = cardNo,
                monthYear = monthYear,
                isServed = true,
                servedDateTime = formattedDateTime,
                servedTimestamp = timestamp
            )
        )
    }

    suspend fun markCardUnserved(cardNo: String, monthYear: String) {
        dao.clearDistributionRecord(cardNo, monthYear)
    }
}
