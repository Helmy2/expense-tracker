package com.expense.tracker.shared.core.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.expense.tracker.shared.core.data.entity.TransactionEntity

@Dao
interface TransactionDao {
    @Query("SELECT * FROM transactions ORDER BY createdAtMillis DESC")
    suspend fun getAll(): List<TransactionEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: TransactionEntity)

    @Query("DELETE FROM transactions WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query(
        "SELECT COALESCE(SUM(amount), 0.0) FROM transactions " +
            "WHERE category = :category " +
            "AND type = 'EXPENSE' " +
            "AND createdAtMillis >= :startMillis " +
            "AND createdAtMillis < :endMillis"
    )
    suspend fun sumExpenseForCategory(
        category: String,
        startMillis: Long,
        endMillis: Long,
    ): Double
}
