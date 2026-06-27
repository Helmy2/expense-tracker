package com.expense.tracker.shared.core.data.local

import androidx.room.Room
import androidx.room.RoomDatabase
import com.expense.tracker.shared.core.data.database.iosDatabaseDirectory

class IosSampleDatabaseFactory : SampleDatabaseFactory {
    override fun createBuilder(): RoomDatabase.Builder<SampleDatabase> {
        val databasePath = "${iosDatabaseDirectory()}/dream_sample.db"

        return Room.databaseBuilder<SampleDatabase>(
            name = databasePath
        )
    }
}