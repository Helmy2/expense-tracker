package com.expense.tracker.feature.expense.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey val id: String,
    val amount: Double,
    val type: String,
    val category: String,
    val note: String,
    val createdAtMillis: Long,
)
