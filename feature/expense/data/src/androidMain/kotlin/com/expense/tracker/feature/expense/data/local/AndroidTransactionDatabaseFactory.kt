package com.expense.tracker.feature.expense.data.local

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import com.expense.tracker.shared.core.data.database.androidDatabaseDirectory

class AndroidTransactionDatabaseFactory(
    private val context: Context,
) : TransactionDatabaseFactory {
    override fun createBuilder(): RoomDatabase.Builder<TransactionDatabase> {
        val appContext = context.applicationContext
        val databasePath = "${androidDatabaseDirectory(appContext)}/expense_tracker.db"

        return Room.databaseBuilder<TransactionDatabase>(
            context = appContext,
            name = databasePath,
        )
    }
}
