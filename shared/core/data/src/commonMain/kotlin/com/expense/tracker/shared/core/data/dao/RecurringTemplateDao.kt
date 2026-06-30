package com.expense.tracker.shared.core.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.expense.tracker.shared.core.data.entity.RecurringTemplateEntity

@Dao
interface RecurringTemplateDao {
    @Query("SELECT * FROM recurring_templates ORDER BY startDateMillis DESC")
    suspend fun getAll(): List<RecurringTemplateEntity>

    @Query("SELECT * FROM recurring_templates WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): RecurringTemplateEntity?

    @Query("SELECT * FROM recurring_templates WHERE isPaused = 0 ORDER BY startDateMillis DESC")
    suspend fun getUnpaused(): List<RecurringTemplateEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: RecurringTemplateEntity)

    @Query("UPDATE recurring_templates SET isPaused = :isPaused WHERE id = :id")
    suspend fun updatePauseStatus(id: String, isPaused: Boolean)

    @Query("UPDATE recurring_templates SET lastGeneratedDateMillis = :lastGeneratedDateMillis WHERE id = :id")
    suspend fun updateLastGenerated(id: String, lastGeneratedDateMillis: Long)

    @Query("DELETE FROM recurring_templates WHERE id = :id")
    suspend fun deleteById(id: String)
}
