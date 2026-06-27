package com.expense.tracker.shared.core.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface SampleItemDao {
    @Query("SELECT * FROM sample_items ORDER BY position ASC")
    suspend fun getAll(): List<SampleItemEntity>

    @Query("SELECT * FROM sample_items WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): SampleItemEntity?

    @Query("SELECT COUNT(*) FROM sample_items")
    suspend fun count(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<SampleItemEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: SampleItemEntity)

    @Update
    suspend fun update(item: SampleItemEntity)
}
