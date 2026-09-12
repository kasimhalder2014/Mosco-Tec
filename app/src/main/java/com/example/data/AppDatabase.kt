package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        ShopProfile::class,
        RationCard::class,
        DistributionRecord::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun smartRegisterDao(): SmartRegisterDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "smart_register_db"
                )
                .fallbackToDestructiveMigration()
                .addCallback(DatabaseCallback(context.applicationContext))
                .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(
            private val context: Context
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                CoroutineScope(Dispatchers.IO).launch {
                    val dao = getDatabase(context).smartRegisterDao()
                    // Initial default shop profile
                    dao.saveShopProfile(
                        ShopProfile(
                            id = 1,
                            shopName = "Maa Annapurna PDS Centre",
                            proprietorName = "Subhash Chandra Roy",
                            fpsCode = "FPS-PDS-8092",
                            licenceNo = "PDS/LIC/2024-554",
                            area = "Sub-Division South, Zone 4",
                            shopAddress = "Ward No. 8, Main Bazar Road, Dist. Nadia",
                            securityPin = "",
                            isPinEnabled = false,
                            isBiometricEnabled = false
                        )
                    )

                    // Seed a few initial cards for immediate testing
                    val sampleCards = listOf(
                        RationCard("RC10023481", "Mohan Lal Sharma", "PHH", 4, "9876543210"),
                        RationCard("RC10023482", "Anita Devi", "AAY", 5, "9876543211"),
                        RationCard("RC10023483", "Abdul Karim Sheikh", "SPHH", 6, "9876543212"),
                        RationCard("RC10023484", "Pooja Verma", "RKSY1", 3, "9876543213"),
                        RationCard("RC10023485", "Suresh Chandra Das", "RKSY2", 4, "9876543214"),
                        RationCard("RC10023486", "Gouranga Ghosh", "PHH", 5, "9876543215"),
                        RationCard("RC10023487", "Fatima Bibi", "AAY", 4, "9876543216"),
                        RationCard("RC10023488", "Ratan Mondal", "SPHH", 2, "9876543217")
                    )
                    dao.insertRationCards(sampleCards)

                    // Mark two cards as served for current month
                    dao.setDistributionRecord(
                        DistributionRecord(
                            cardNo = "RC10023481",
                            monthYear = "2026-09",
                            isServed = true,
                            servedDateTime = "05 Sep 2026, 10:15 AM",
                            servedTimestamp = 1788599700000L
                        )
                    )
                    dao.setDistributionRecord(
                        DistributionRecord(
                            cardNo = "RC10023482",
                            monthYear = "2026-09",
                            isServed = true,
                            servedDateTime = "07 Sep 2026, 02:45 PM",
                            servedTimestamp = 1788788700000L
                        )
                    )
                }
            }
        }
    }
}
