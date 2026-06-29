package com.expense.tracker.shared.core.data.database

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase


class AndroidAppDatabaseFactory(
    private val context: Context,
) : AppDatabaseFactory {
    override fun createBuilder(): RoomDatabase.Builder<AppDatabase> {
        val appContext = context.applicationContext
        val databasePath = "${androidDatabaseDirectory(appContext)}/expense_tracker.db"

        return Room.databaseBuilder<AppDatabase>(
            context = appContext,
            name = databasePath,
        )
    }
}
