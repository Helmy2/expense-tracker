package com.expense.tracker.shared.core.data

import androidx.room.RoomDatabase
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import kotlinx.coroutines.Dispatchers

fun <T : RoomDatabase> RoomDatabase.Builder<T>.buildBundledRoomDatabase(): T = setDriver(BundledSQLiteDriver())
    .setQueryCoroutineContext(Dispatchers.Default)
    .fallbackToDestructiveMigration(dropAllTables = true)
    .build()
