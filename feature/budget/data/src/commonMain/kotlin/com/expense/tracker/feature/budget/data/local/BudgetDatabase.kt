package com.expense.tracker.feature.budget.data.local

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import com.expense.tracker.shared.core.data.buildBundledRoomDatabase

@Database(
    entities = [BudgetEntity::class],
    version = 1,
    exportSchema = true,
)
@ConstructedBy(BudgetDatabaseConstructor::class)
abstract class BudgetDatabase : RoomDatabase() {
    abstract fun budgetDao(): BudgetDao
}

@Suppress("KotlinNoActualForExpect")
expect object BudgetDatabaseConstructor : RoomDatabaseConstructor<BudgetDatabase> {
    override fun initialize(): BudgetDatabase
}

interface BudgetDatabaseFactory {
    fun createBuilder(): RoomDatabase.Builder<BudgetDatabase>
}

fun createBudgetDatabase(
    factory: BudgetDatabaseFactory,
): BudgetDatabase = factory
    .createBuilder()
    .buildBundledRoomDatabase()
