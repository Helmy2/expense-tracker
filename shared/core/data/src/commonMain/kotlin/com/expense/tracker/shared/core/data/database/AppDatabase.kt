package com.expense.tracker.shared.core.data.database

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import com.expense.tracker.shared.core.data.dao.BudgetDao
import com.expense.tracker.shared.core.data.dao.RecurringTemplateDao
import com.expense.tracker.shared.core.data.dao.TransactionDao
import com.expense.tracker.shared.core.data.entity.BudgetEntity
import com.expense.tracker.shared.core.data.entity.RecurringTemplateEntity
import com.expense.tracker.shared.core.data.entity.TransactionEntity

@Database(
    entities = [TransactionEntity::class, BudgetEntity::class, RecurringTemplateEntity::class],
    version = 2,
    exportSchema = true,
)
@ConstructedBy(AppDatabaseConstructor::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun transactionDao(): TransactionDao
    abstract fun budgetDao(): BudgetDao
    abstract fun recurringTemplateDao(): RecurringTemplateDao
}
