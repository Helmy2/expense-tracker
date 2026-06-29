package com.expense.tracker.shared.core.data.database

import androidx.room.RoomDatabase
import com.expense.tracker.shared.core.data.buildBundledRoomDatabase

interface AppDatabaseFactory {
    fun createBuilder(): RoomDatabase.Builder<AppDatabase>
}

fun createAppDatabase(factory: AppDatabaseFactory): AppDatabase =
    factory.createBuilder().buildBundledRoomDatabase()
