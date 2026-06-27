package com.expense.tracker.feature.budget.data.local

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import com.expense.tracker.shared.core.data.database.androidDatabaseDirectory

class AndroidBudgetDatabaseFactory(
    private val context: Context,
) : BudgetDatabaseFactory {
    override fun createBuilder(): RoomDatabase.Builder<BudgetDatabase> {
        val appContext = context.applicationContext
        val databasePath = "${androidDatabaseDirectory(appContext)}/budget_tracker.db"

        return Room.databaseBuilder<BudgetDatabase>(
            context = appContext,
            name = databasePath,
        )
    }
}
