package com.expense.tracker.shared.core.data.local

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import com.expense.tracker.shared.core.data.buildBundledRoomDatabase

@Database(
    entities = [SampleItemEntity::class],
    version = 1,
    exportSchema = true
)
@ConstructedBy(SampleDatabaseConstructor::class)
abstract class SampleDatabase : RoomDatabase() {
    abstract fun sampleItemDao(): SampleItemDao
}

@Suppress("KotlinNoActualForExpect")
expect object SampleDatabaseConstructor : RoomDatabaseConstructor<SampleDatabase> {
    override fun initialize(): SampleDatabase
}

interface SampleDatabaseFactory {
    fun createBuilder(): RoomDatabase.Builder<SampleDatabase>
}

fun createSampleDatabase(
    factory: SampleDatabaseFactory
): SampleDatabase = factory
    .createBuilder()
    .buildBundledRoomDatabase()