package com.expense.tracker.feature.expense.data.local

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import com.expense.tracker.shared.core.data.buildBundledRoomDatabase

@Database(
    entities = [TransactionEntity::class],
    version = 1,
    exportSchema = true,
)
@ConstructedBy(TransactionDatabaseConstructor::class)
abstract class TransactionDatabase : RoomDatabase() {
    abstract fun transactionDao(): TransactionDao
}

@Suppress("KotlinNoActualForExpect")
expect object TransactionDatabaseConstructor : RoomDatabaseConstructor<TransactionDatabase> {
    override fun initialize(): TransactionDatabase
}

interface TransactionDatabaseFactory {
    fun createBuilder(): RoomDatabase.Builder<TransactionDatabase>
}

fun createTransactionDatabase(
    factory: TransactionDatabaseFactory,
): TransactionDatabase = factory
    .createBuilder()
    .buildBundledRoomDatabase()
