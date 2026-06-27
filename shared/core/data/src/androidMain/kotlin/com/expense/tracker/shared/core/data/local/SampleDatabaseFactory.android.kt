package com.expense.tracker.shared.core.data.local

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import com.expense.tracker.shared.core.data.database.androidDatabaseDirectory

class AndroidSampleDatabaseFactory(
    private val context: Context
) : SampleDatabaseFactory {
    override fun createBuilder(): RoomDatabase.Builder<SampleDatabase> {
        val appContext = context.applicationContext
        val databasePath = "${androidDatabaseDirectory(appContext)}/dream_sample.db"

        return Room.databaseBuilder<SampleDatabase>(
            context = appContext,
            name = databasePath
        )
    }
}