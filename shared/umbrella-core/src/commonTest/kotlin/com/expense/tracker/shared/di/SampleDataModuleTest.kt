package com.expense.tracker.shared.di

import androidx.room.RoomDatabase
import com.expense.tracker.shared.core.data.local.SampleDatabase
import com.expense.tracker.shared.core.data.local.SampleDatabaseFactory
import com.expense.tracker.shared.core.domain.TimeProvider
import org.koin.dsl.koinApplication
import kotlin.test.Test
import kotlin.test.assertNotNull

class SampleDataModuleTest {
    @Test
    fun resolvesTimeProviderAndSampleRepository() {
        val testFactory = object : SampleDatabaseFactory {
            override fun createBuilder(): RoomDatabase.Builder<SampleDatabase> {
                error("The DI graph test should not eagerly create the Room database")
            }
        }

        val koin = koinApplication {
            modules(sampleDataModule(testFactory))
        }.koin

        assertNotNull(koin.get<TimeProvider>())
        assertNotNull(koin.get<SampleDatabaseFactory>())
    }
}
