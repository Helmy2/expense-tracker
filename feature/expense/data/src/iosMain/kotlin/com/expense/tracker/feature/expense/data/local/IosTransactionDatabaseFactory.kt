package com.expense.tracker.feature.expense.data.local

import androidx.room.Room
import androidx.room.RoomDatabase
import com.expense.tracker.shared.core.data.database.iosDatabaseDirectory

class IosTransactionDatabaseFactory : TransactionDatabaseFactory {
    override fun createBuilder(): RoomDatabase.Builder<TransactionDatabase> {
        val databasePath = "${iosDatabaseDirectory()}/expense_tracker.db"

        return Room.databaseBuilder<TransactionDatabase>(
            name = databasePath
        )
    }
}
