package com.expense.tracker.shared.core.data.database

import androidx.room.Room
import androidx.room.RoomDatabase


class IosAppDatabaseFactory : AppDatabaseFactory {
    override fun createBuilder(): RoomDatabase.Builder<AppDatabase> {
        val databasePath = "${iosDatabaseDirectory()}/expense_tracker.db"

        return Room.databaseBuilder<AppDatabase>(
            name = databasePath
        )
    }
}
