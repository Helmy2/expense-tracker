package com.expense.tracker.feature.budget.data.local

import androidx.room.Room
import androidx.room.RoomDatabase
import com.expense.tracker.shared.core.data.database.iosDatabaseDirectory

class IosBudgetDatabaseFactory : BudgetDatabaseFactory {
    override fun createBuilder(): RoomDatabase.Builder<BudgetDatabase> {
        val databasePath = "${iosDatabaseDirectory()}/budget_tracker.db"

        return Room.databaseBuilder<BudgetDatabase>(
            name = databasePath,
        )
    }
}
