package com.detex.app.data.local

import android.content.Context
import androidx.room.*
import kotlinx.coroutines.flow.Flow
import net.sqlcipher.database.SupportFactory
import java.io.File
import java.security.SecureRandom

enum class TransactionType { DEBIT, CREDIT, TRANSFER }

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val merchant: String,
    val amount: Double,
    val type: TransactionType,
    val category: String,
    val timestamp: Long = System.currentTimeMillis(),
    val accountInfo: String = "Cash/Manual",
    val source: String = "MANUAL",
    val isConfirmed: Boolean = true
)

@Dao
interface TransactionDao {
    @Query("SELECT * FROM transactions WHERE isConfirmed = 1 ORDER BY timestamp DESC")
    fun getAllTransactions(): Flow<List<TransactionEntity>>

    @Query("SELECT COALESCE(SUM(amount), 0.0) FROM transactions WHERE type = 'DEBIT' AND isConfirmed = 1 AND timestamp >= :startTime")
    fun getTotalSpentSince(startTime: Long): Flow<Double>

    @Query("SELECT COALESCE(SUM(amount), 0.0) FROM transactions WHERE type = 'CREDIT' AND isConfirmed = 1 AND timestamp >= :startTime")
    fun getTotalIncomeSince(startTime: Long): Flow<Double>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(transaction: TransactionEntity): Long

    @Query("DELETE FROM transactions")
    suspend fun clearAll()
}

@Database(entities = [TransactionEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun transactionDao(): TransactionDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                System.loadLibrary("sqlcipher")
                val passphrase = getOrCreateDatabasePassphrase(context.applicationContext)
                val factory = SupportFactory(passphrase)

                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "detex_aes256_ledger.db"
                )
                    .openHelperFactory(factory)
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private fun getOrCreateDatabasePassphrase(context: Context): ByteArray {
            val file = File(context.filesDir, "detex_db_key.bin")
            return if (file.exists()) {
                file.readBytes()
            } else {
                val key = ByteArray(32) // 256-bit AES key
                SecureRandom().nextBytes(key)
                file.writeBytes(key)
                key
            }
        }
    }
}
